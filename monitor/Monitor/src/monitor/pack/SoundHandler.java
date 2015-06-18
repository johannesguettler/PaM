/*
 * Copyright: Universität Freiburg, 2015
 * Authors: Marc Pfeifer <pfeiferm@tf.uni-freiburg.de> Everything except Defibrillator-Sounds
 * 			Johannes Scherle <johannes.scherle@googlemail.com> Defibrillator-Sounds
 */

package monitor.pack;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import java.util.Timer;
import java.util.TimerTask;


/**
 * A class which handles the complete sound-output of a patient-monitor. This consists of the
 * playback of given sound-files and the generation and playback of sounds with a given frequency.
 */
public class SoundHandler {
  // FINAL MEMBERS:
  // De(Activate) the debug-messages.
  private final boolean DEBUG = false;
  // The sampling-rate of the generated sounds.
  private final int sampleRate = 16000;
  // The standard-sound-length.
  private final int soundLength = 150;
  // The volume of the sound.
  private final float soundVolume = 0.1f;
  // The interval of the alarm-sounds.
  private final int alarmInterval = 1000;
  // The length of one alarm sound.
  private final int alarmLength = 500;
  // The frequency of the alarm sound.
  private final int alarmFreq = 660;
  // Maximum volume of the alarm sound.
  private final float maxAlarmVol = 0.5f;
  // The number by which the length of the alarm sound is divided.
  // With this one can control how long the fade in, the hold, and the
  // fade out time are. The first fraction is the fade in time, the
  // last the fade out time. Must be >= 2.0f.
  private final float fadeFraction = 3.0f;
  // The length of one asystole alarm sound.
  private final int asysAlarmLength = 500;
  // The frequency of the second asystole alarm sound.
  private final int asysAlarmFreq = 587;
  // The interval of the asystole alarm-sounds.
  private final int asysAlarmInterval = 1000;

  // MEMBERS:
  private SoundPool sp;  // The soundPool/sound-player.
  private Timer timer;  // The alarm-sound-timer.
  private Timer asysTimer;  // The asystole alarm-sound-timer.
  private int bpSound;  // The pump-sound for non invasive blood pressure measurement
  // Flags which indicates if the alarm for a parameter should be active.
  private boolean ekgAlarm;
  private boolean rrAlarm;
  private boolean o2Alarm;
  private boolean co2Alarm;
  // A flags which indicates if the alarm-sound is active.
  private boolean alarmOn;
  // A flags which indicates if the alarm-sound is active.
  private boolean asysAlarmOn;
  // A flags which indicates which asystole alarm sound should be played next.
  private boolean asysNormalSound;
  // The AudioTracks.
  AudioTrack soundAudioTrack;
  AudioTrack alarmAudioTrack;

  // Members for the defi-mode.
  MediaPlayer defiMp;

  // PUBLIC:


  /**
   * Constructor which initializes a player, the sound-files and some members.
   *
   * @param m - The main GUI-thread.
   */
  public SoundHandler(MonitorMainScreen m) {
    // Create a sound-player according to the API-level.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      buildSoundHandler();
    } else {
      createSoundHandler();
    }
    // Load the pump-sound for non invasive blood pressure measurement.
    bpSound = sp.load(m, R.raw.bpmeasuremntsound, 1);

    // Initialize some members.
    ekgAlarm = false;
    rrAlarm = false;
    o2Alarm = false;
    alarmOn = false;
    asysNormalSound = true;
    asysAlarmOn = false;
  }

  /**
   * Plays the pump-sound for non invasive blood pressure measurement.
   */
  public void playBPSound() {
    if (bpSound != 0)
      sp.play(bpSound, 1, 1, 0, 0, 1);
  }

  /**
   * A function which plays a sine sound with a given frequency for a given time.
   * (Based on code form Singhak (http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android).)
   *
   * @param freq   - Frequency of the sound.
   * @param length - Length of the sound in ms.
   */
  public void playFreqSound(float freq, int length) {
    // Calculate the needed number of samples to get the given play-length.
    int bufSize = length * (sampleRate / 1000);
    // Get the minimum number of samples to get a proper sound.
    int minBufSize = AudioTrack.getMinBufferSize(sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT);
    // Correct the number samples if necessary.
    if (bufSize < minBufSize) {
      bufSize = minBufSize;
    }
    // Create a buffer with the calculated length and fill it with samples of sine wave with the given frequency.
    short[] buffer = new short[bufSize];
    float angle = 0;
    float angular_frequency = (float) (2 * Math.PI) * freq / sampleRate;
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = (short) (Short.MAX_VALUE * ((float) Math.sin(angle)) * soundVolume);
      angle += angular_frequency;
    }
    // Close the (old) AudioTrack if necessary.
    if (soundAudioTrack != null) {
      soundAudioTrack.release();
    }
    // Create a new audio-stream and start it.
    soundAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufSize,
        AudioTrack.MODE_STREAM);
    soundAudioTrack.play();
    // Output the buffer via the stream and stop it.
    int b = soundAudioTrack.write(buffer, 0, buffer.length);
    soundAudioTrack.stop();
    if (DEBUG)
      System.out.println("Number of bytes written to sound audioTrack: " + b);
  }

  /**
   * A function which plays a sine alarm-sound with a given frequency for a given time. The sound fades
   * in at the begin and fades out at the end.
   * (Based on code form Singhak (http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android).)
   *
   * @param freq   - Frequency of the sound.
   * @param length - Length of the sound in ms.
   */
  public void playFreqAlarm(float freq, int length) {
    // Calculate the needed number of samples to get the given play-length.
    int bufSize = length * (sampleRate / 1000);
    // Get the minimum number of samples to get a proper sound.
    int minBufSize = AudioTrack.getMinBufferSize(sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT);
    // Correct the number samples if necessary.
    if (bufSize < minBufSize) {
      bufSize = minBufSize;
    }
    // Calculate the number of periods in the whole sound.
    float periodNumber = freq * ((float) length / 1000.0f);
    // Calculate the number of samples in one period.
    float samplesPerPeriod = ((float) bufSize) / periodNumber;
    // Calculate how much the volume of each period must be increased/decreased to fade in/out the sound.
    float incDecFactor = maxAlarmVol / (periodNumber / fadeFraction);
    // Some running variables.
    int periodCount = 1;
    float multFactor = 0;
    if (DEBUG) {
      System.out.println("periodNumber: " + periodNumber
          + ", samplesPerPeriod: " + samplesPerPeriod
          + ", incDecFactor: " + incDecFactor);
    }
    // Create a buffer with the calculated length and fill it with samples of sine wave with the given frequency.
    short[] buffer = new short[bufSize];
    float angle = 0;
    float angular_frequency = (float) (2 * Math.PI) * freq / sampleRate;
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = (short) (Short.MAX_VALUE * ((float) Math.sin(angle)) * multFactor);
      angle += angular_frequency;
      // Check if one hole period is completed.
      if (i >= (samplesPerPeriod * periodCount)) {
        periodCount++;
        // When we are in last fraction of the sound, decrease the multiplication factor
        // which represents the volume of a period.
        if (periodCount > ((fadeFraction - 1.0f) * (periodNumber / fadeFraction))) {
          multFactor = maxAlarmVol - incDecFactor * ((float) (periodCount - (int) ((fadeFraction - 1.0f) * (periodNumber / fadeFraction))));
          if (DEBUG) System.out.println("2. Half - multfactor: " + multFactor);
        }
        // When we are in first fraction of the sound, increase the multiplication factor
        // which represents the volume of a period.
        if (periodCount < (periodNumber / fadeFraction)) {
          multFactor = incDecFactor * (float) periodCount;
          if (DEBUG) System.out.println("1. Half - multfactor: " + multFactor);
        }
      }
    }
    // Close the (old) AudioTrack if necessary.
    if (alarmAudioTrack != null) {
      alarmAudioTrack.release();
    }
    // Create a new audio-stream and start it.
    alarmAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        buffer.length,
        AudioTrack.MODE_STREAM);
    alarmAudioTrack.play();
    // Output the buffer via the stream and stop it.
    int b = alarmAudioTrack.write(buffer, 0, buffer.length);
    alarmAudioTrack.stop();
    if (DEBUG)
      System.out.println("Number of bytes written to alarm audioTrack: " + b);
  }

  /**
   * Plays the heart-beat-sound once corresponding to current o2 saturation.
   *
   * @param o2SatAvailable - Indicates if a o2 saturation value is available.
   * @param o2Sat          - The current o2 saturation.
   */
  public void playHeartSound(boolean o2SatAvailable, int o2Sat) {
    if (o2SatAvailable) {
      // If the o2 saturation vary the frequency of the sound according to it.
      switch (o2Sat) {
        case 100:
          playFreqSound(2200, soundLength);  // CIS
          break;
        case 99:
          playFreqSound(2093, soundLength);  // C
          break;
        case 98:
          playFreqSound(1975, soundLength);  // H
          break;
        case 97:
          playFreqSound(1864, soundLength);  // AIS
          break;
        case 96:
          playFreqSound(1760, soundLength);  // A
          break;
        case 95:
          playFreqSound(1661, soundLength);  // GIS
          break;
        case 94:
          playFreqSound(1567, soundLength);  // G
          break;
        case 93:
          playFreqSound(1479, soundLength);  // FIS
          break;
        case 92:
          playFreqSound(1369, soundLength);  // F
          break;
        case 91:
          playFreqSound(1318, soundLength);  // E
          break;
        case 90:
          playFreqSound(1244, soundLength);  // DIS
          break;
        case 89:
          playFreqSound(1174, soundLength);  // D
          break;
        case 88:
          playFreqSound(1108, soundLength);  // CIS
          break;
        case 87:
          playFreqSound(1046, soundLength);  // C
          break;
        case 86:
          playFreqSound(987, soundLength);  // H
          break;
        case 85:
          playFreqSound(932, soundLength);  // AIS
          break;
        default:
          playFreqSound(932, soundLength);  // AIS
          break;
      }
    } else {
      // If no o2 saturation is available play the standard 932Hz-Sound.
      playFreqSound(932, soundLength);  // AIS
    }
  }

  /**
   * Activates/Deactivates the EKG alarm.
   *
   * @param on - Indicates if the alarm should be on.
   */
  public void setEKGAlarm(boolean on) {
    ekgAlarm = on;
    startStopAlarm();
  }

  /**
   * Activates/Deactivates the blood pressure alarm.
   *
   * @param on - Indicates if the alarm should be on.
   */
  public void setRRAlarm(boolean on) {
    rrAlarm = on;
    startStopAlarm();
  }

  /**
   * Activates/Deactivates the O2 alarm.
   *
   * @param on - Indicates if the alarm should be on.
   */
  public void setO2Alarm(boolean on) {
    o2Alarm = on;
    startStopAlarm();
  }

  /**
   * Activates/Deactivates the CO2 alarm.
   *
   * @param on - Indicates if the alarm should be on.
   */
  public void setCO2Alarm(boolean on) {
    co2Alarm = on;
    startStopAlarm();
  }

  /**
   * Activates/Deactivates the asystole alarm.
   *
   * @param on - Indicates if the alarm should be on.
   */
  public void playAsystoleAlarm(boolean on) {
    if (on) {
      // If the alarm isn't already active, start it.
      if (!asysAlarmOn) {
        // Start a sound which plays the normal alarm sound followed by lower sound and
        // repeat it until it's stopped.
        asysAlarmOn = true;
        asysTimer = new Timer();
        asysTimer.scheduleAtFixedRate(new TimerTask() {
          public void run() {
            if (asysNormalSound) {
              playFreqAlarm(alarmFreq, asysAlarmLength);
              asysNormalSound = false;
            } else {
              playFreqAlarm(asysAlarmFreq, asysAlarmLength);
              asysNormalSound = true;
            }
          }
        }, asysAlarmInterval, asysAlarmInterval);
      }
    } else {
      // Stop the sound.
      if (asysTimer != null) {
        asysTimer.cancel();
      }
      asysAlarmOn = false;
    }
  }

  /**
   * Destroys all used instances if necessary.
   */
  public void destroy() {
    if (soundAudioTrack != null) soundAudioTrack.release();
    if (alarmAudioTrack != null) alarmAudioTrack.release();
    if (timer != null) timer.cancel();
    if (asysTimer != null) asysTimer.cancel();
    if (sp != null) sp.release();
  }

  /**
   * Plays the defi charge sound. (Created by Johannes)
   *
   * @param c - main context
   */
  public void playDefiCharge(Context c) {
    defiMp = MediaPlayer.create(c, R.raw.defiwavcharge);
    defiMp.start();
  }

  /**
   * Plays the defi ready sound in loop. (Created by Johannes)
   *
   * @param c - main context
   */
  public void playDefiReady(Context c) {
    defiMp = MediaPlayer.create(c, R.raw.defiwavready);
    defiMp.setLooping(true);
    defiMp.start();
  }

  /**
   * Stops the defi ready sound. (Created by Johannes)
   *
   * @param c - main context
   */
  public void stopDefiReady(Context c) {
    defiMp.setLooping(false);
    defiMp.stop();
  }

  // PRIVATE:

  /**
   * Create the soundPool acording to API-levels lower 21.
   */
  @SuppressWarnings("deprecation")
  private void createSoundHandler() {
    sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
  }

  /**
   * Create the soundPool acording to API-level 21.
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private void buildSoundHandler() {
    AudioAttributes audioAttributes = new AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build();
    sp = new SoundPool.Builder()
        .setAudioAttributes(audioAttributes)
        .build();
  }

  /**
   * Starts the alarm, if at least one parameter is active and stops it if no one is actove any more.
   */
  private void startStopAlarm() {
    if ((ekgAlarm || rrAlarm || o2Alarm || co2Alarm) && !asysAlarmOn) {
      if (!alarmOn) {
        // If at least one of the three alarms should be active and the
        // alarm-sound isn't already activated start the alarm by scheduling a
        // sound every alarmInterval.
        if (DEBUG) System.out.println("Start Alarm");
        alarmOn = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
          public void run() {
            playFreqAlarm(alarmFreq, alarmLength);
          }
        }, alarmInterval, alarmInterval);
      }
    } else if (alarmOn) {
      // If no of the three alarms should be active, deactivate the alarm-sound.
      if (DEBUG) System.out.println("Stop Alarm");
      if (timer != null) {
        timer.cancel();
      }
      alarmOn = false;
    }
  }


}
