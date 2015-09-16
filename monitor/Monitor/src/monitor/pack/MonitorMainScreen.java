/*
 * Copyright: Universit�t Freiburg, 2015
 * Authors: Marc Pfeifer <pfeiferm@tf.uni-freiburg.de> Everthing, except Defibirllator
 * 			Johannes Scherle <johannes.scherle@googlemail.com> Defibrillator
 */

package monitor.pack;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A activity containing the main view of patient-monitor-simulation-app.
 */
public class MonitorMainScreen extends Activity {

  /**
   * active clients send back usage-data to the conroller (AED-usage ...)
   */
  public static boolean ACTIVE_CLIENT;

  // PRIVATE:

  private enum ControllerInfoType {
    CHANGED_TO_DEFI_SCREEN, DEFI_FIRED
  }
  //FINAL MEMBERS:
  // private final boolean DEBUG = false;  // De(Activate) the debug-messages.
  private final int hideSystemBarDelay = 2000;  // Delay-time for hiding the system-bar.
  private final int heartBlinkDelay = 300;  // Blink time of the little heart that indicates the heart-beat (in ms).
  private final int paramBlinkDelay = 300;  // Blink time of the parameters while ventfibri, ventflutter or asystole (in ms).
  private final int paramBlinkRepeatTime = 1000; // Time between each blink of the parameters while ventfibri, ventflutter or asystole (in ms).
  private final int nibpDelay = 12000;  // Time until the result of a NIBP-measurement is displayed after the start of it.
  private final int incDecStepTime = 100;  // Delay time between each auto increase/decrease step of the alarm thresholds, when the buttons are pressed continuously.
  private final int incDecAutoStartDelay = 500;  // Delay before the auto increase/decrease starts, when the buttons are pressed continuously.
  private final int alarmPauseTime = 60; // Time in sec for which the alarm is paused if the alarm pause button is pressed.


  // The threshold above a random must be to trigger a variation of a parameter.
  private final float randThreshold = 0.7f;
  // The factor by which the current EKG value is divided to get the minimum number of
  // heart-beats with constant parameter values -> Means without a new random-variation.
  private final int ekgDivFactor = 10;
  // The factor by which the current respiration value is divided to get the minimum number of
  // breaths with a constant parameter values -> Means without a new random-variation.
  private final int respDivFactor = 10;
  // The multiplicators with which a random is multiplied to get a random offset for
  // the parameters -> Standard deviation.
  private final double ekgRandMult = 1;
  private final double rrRandMult = 1;
  private final double o2RandMult = 1;
  private final double co2RandMult = 1;
  private final double respRandMult = 1;

  // MEMBERS:
  // Indicators if curves/values for each parameter are active.
  private boolean ekgActive;
  private boolean rrActive;
  private boolean o2Active;
  private boolean co2Active;
  private boolean nibpActive;
  private boolean respActive;
  // The current alarm threshold values for each parameter.
  private int ekgAlarmUpValue;  // EKG upper value.
  private int ekgAlarmLowValue;  // EKG lower value.
  private int rrDiaAlarmUpValue;  // Diastolic blood pressure upper value.
  private int rrDiaAlarmLowValue;  // Diastolic blood pressure lower value.
  private int rrSysAlarmUpValue;  // Systolic blood pressure upper value.
  private int rrSysAlarmLowValue;  // Sysstolic blood pressure lower value.
  private int o2AlarmLowValue;  // O2 saturation lower value.
  private int co2AlarmUpValue;  // CO2 upper value.
  private int co2AlarmLowValue;  // CO2 lower value.
  // Indicator if the alarm in general is on or off.
  private boolean alarmActive;
  // Indicators if the alarm for a parameter is active.
  private boolean ekgAlarmOn=true;
  private boolean rrAlarmOn=true;
  private boolean o2AlarmOn=true;
  private boolean co2AlarmOn=true;
  // Indicator if the alarm is currently paused.
  private boolean alarmPaused;

  // Indicators if the settings-/defi-layouts are hidden.
  private boolean defiHidden;
  // Settings- and defi-layouts.

  private RelativeLayout defiLayout;
  private DefiFragment defiFragment;

  // Non inversive blood pressure measurement.
  private int nibpAutoTime;  // The current auto-repeat-time.
  private boolean nibpRunning;  // Indicates if a measurement is running.
  private boolean autoNIBPRunning;  // Indicates if a auto-measurement is running.

  // The current vital-parameter-values.
  private int diaBloodPressure;
  private int sysBloodPressure;
  private int ekgValue;
  private int o2Value;
  private int co2Value;
  private int respValue;
  private Event.HeartPattern heartPattern;
  // The current vital-parameter-values including some random variation.
  private int ekgRandValue;
  private int diaRandValue;
  private int sysRandValue;
  private int o2RandValue;
  private int co2RandValue;
  private int respRandValue;
  // Counters for the number of curve peaks of the different parameters.
  private int ekgPeakCounter;
  private int rrPeakCounter;
  private int o2PeakCounter;
  private int co2PeakCounter;
  private int respPeakCounter;

  // Indicates if the EKG blinking while ventfibri or ventflutter is active.
  private boolean ekgBlinkActive;
  // Indicates if the EKG, RR and O2 are blinking while asystole.
  private boolean paramBlinkActive;

  // States which menu is currently open: 0 - EKG, 1 - RR, 2 - O2, 3 - CO2.
  private int menuSelection;

  // Indicator if the EKG-sound is active.
  private boolean ekgSoundOn;

  // Indicator if the settings are opened for the first time.
  private boolean firstSettingsOpen;

  // Contains the time since the start of the timer.
  private int timeInSec;
  // Indicates if the timer is running.
  private boolean timerActive;

  // Some needed instances.
  private UpdateHandler updateHandler;
  private Signalserver signalServer;
  private GLActivity glActivity;
  private SharedPreferences defaultSharedPreferences;
  private SoundHandler soundHandler;
  private Timer autoNibpTimer;
  private Timer incDecTimer;
  private Timer ekgBlinkTimer;
  private Timer paramBlinkTimer;
  private Timer alarmPauseTimer;
  private Timer stopWatchTimer;
  private Random randomGenerator;


  // Variables for the defi-mode
  private int defiEnergy = 100;
  private int defiEnergyUpperBound = 360;
  private int defiEnergyLowerBound = 40;
  private int defiEnergyThr = 150;
  private boolean defiCharged = false;
  private boolean defiCharging = false;
  Handler handler;
  private static MonitorMainScreen instance;
  /**
   * Variables for the generation of the defi sound,
   * not needed at the moment.
   private Timer _defiTimer;
   private int _defiTimerInterval = 500;
   private int _defiSoundFrequency = 100;
   private int _defiFrequencySteps = 40;
   private int _defi1kTime = 3000;
   private int _defi1kFrequency = 7000;
   private int _defiSteps = 0;
   private int _defiSoundIncrement = 0;
   */


  /**
   * @return
   */
  public static MonitorMainScreen getInstance() {
    return instance;
  }

  /**
   * Do all the initializations and add Listeners.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // initialize instance
    // Android Activities are inherently singletons and the launch mode of
    // this Activity is "singleTop" so we can do this safely
    instance = this;


    // Hide the title-bar.
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    // Minimize the System-Bar and hide the Action-Bar if necessary.
    final View decorView = getWindow().getDecorView();
    if (Build.VERSION.SDK_INT >= 19) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          | View.SYSTEM_UI_FLAG_LOW_PROFILE);
    } else if (Build.VERSION.SDK_INT >= 16) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LOW_PROFILE);
    } else {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    // Minimize the System-Bar (and the Action-Bar) again after hideSystemBarDelay ms after a click on it.
    decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
      @Override
      public void onSystemUiVisibilityChange(int visibility) {
        if (visibility != View.VISIBLE) {
          return;
        }
        new Handler().postDelayed(new Runnable() {
          public void run() {
            if (Build.VERSION.SDK_INT >= 19) {
              decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                  | View.SYSTEM_UI_FLAG_LOW_PROFILE);
            } else if (Build.VERSION.SDK_INT >= 16) {
              decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LOW_PROFILE);
            } else {
              decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
          }
        }, hideSystemBarDelay);
      }
    });

    setContentView(R.layout.activity_monitor_main_screen);

    // Initialize some members.
    defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    // Deactivate all curves/values as default.
    ekgActive = false;
    rrActive = false;
    o2Active = false;
    co2Active = false;
    nibpActive = false;
    respActive = false;


    // Deactivate the EKG and parameter blinking as default.
    ekgBlinkActive = false;
    paramBlinkActive = false;

    defiHidden = true;

    // Set the NIBP and the auto NIBP to "not running" as default.
    nibpRunning = false;
    autoNIBPRunning = false;

    // Initialize the number of curve peaks.
    ekgPeakCounter = 0;
    rrPeakCounter = 0;
    o2PeakCounter = 0;
    co2PeakCounter = 0;

    // Set the default menu selection to EKG.
    menuSelection = 0;

    // Deactivate the EKG-sound as default.
    ekgSoundOn = false;

    // Indicate the settings are opened for the first time.
    firstSettingsOpen = true;

    // Set the time to 0 at start.
    timeInSec = 0;
    // And deactivate the timer at start.
    timerActive = false;

    // Create the instances.


    defiFragment = new DefiFragment();
/*    defiLayout = (RelativeLayout) defiFragment.returnView().findViewById(R.id
        .defiLayout);*/
    updateHandler = new UpdateHandler(this);
    signalServer = new Signalserver(this);
    soundHandler = new SoundHandler(this);
    randomGenerator = new Random();
    handler = new Handler();

    // Set the MainScreen in Client.

    EnterScreen.client.setMonitorMainScreen(this);

    int[] lineColors = loadLineColors();
    glActivity = new GLActivity(this, lineColors[0], lineColors[1],
        lineColors[2], lineColors[3]);

    RelativeLayout curveViewLayout = (RelativeLayout) this.findViewById(R.id.curveViewLayout);
    curveViewLayout.addView(glActivity);
    glActivity.setSignalserver(signalServer);


    // Initialize the slider for the repeating-time of the autom. NIBP.
    SeekBar nibpAutoTimeSeekBar = (SeekBar) this.findViewById(R.id.nibpAutoTimeSeekBar);
    nibpAutoTime = 180;  // Default value: 3 min.
    // Adjust the slider to this value.
    nibpAutoTimeSeekBar.setProgress(42);
    // Add a Listener for a change on the slider.
    nibpAutoTimeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        nibpAutoTimeChange(progress);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // Hide the little bells in the parameter sections, which indicates that the alarm is active.
    ImageView ekgAlarmImageView = (ImageView) this.findViewById(R.id.ekgAlarmImageView);
    ekgAlarmImageView.setVisibility(View.INVISIBLE);
    ImageView ibpAlarmImageView = (ImageView) this.findViewById(R.id.ibpAlarmImageView);
    ibpAlarmImageView.setVisibility(View.INVISIBLE);
    ImageView nibpAlarmImageView = (ImageView) this.findViewById(R.id.nibpAlarmImageView);
    nibpAlarmImageView.setVisibility(View.INVISIBLE);
    ImageView o2AlarmImageView = (ImageView) this.findViewById(R.id.o2AlarmImageView);
    o2AlarmImageView.setVisibility(View.INVISIBLE);
    ImageView co2AlarmImageView = (ImageView) this.findViewById(R.id.co2AlarmImageView);
    co2AlarmImageView.setVisibility(View.INVISIBLE);
    // Hide the little speaker in the EKG parameter sections, which indicates that the EKG sound is active.
    ImageView ekgSoundImageView = (ImageView) this.findViewById(R.id.ekgSoundImageView);
    ekgSoundImageView.setVisibility(View.INVISIBLE);

    // Hide defi UI-elements.
    showHideDefiUiElements(false);



    // Write the default time (= 0) to the status-bar.
    updateTime();

    // Create a timer for a stop watch in the status bar which shows the time since the
    // start of the monitor. Not needed at the moment.
    /*Timer stopTimer = new Timer();
    stopTimer.schedule(new TimerTask() {
        	public void run(){
        		runOnUiThread(new Runnable() {
        			public void run() {
        				timeInSec++;  // Increase the counter every second.
        				updateTime();  // Update the GUI.
        			}
        		});
    		}
    	}, 1000, 1000);*/

    // set parameter textfields to black
// initialize preference values, third parameter: override safed values
    PreferenceManager.setDefaultValues(this, R.xml.settings_alarm, false);
    PreferenceManager.setDefaultValues(this, R.xml.settings_colors, false);
    // load saved/default values

    loadSavedPreferences();

    initializeTextViews();
  }


  /**
   * Hide the System-Bar (and the Action-Bar) again after reentering the app.
   */
  @Override
  protected void onResume() {
    super.onResume();
    // Minimize the System-Bar and hide the Action-Bar if necessary.
    final View decorView = getWindow().getDecorView();
    if (Build.VERSION.SDK_INT >= 19) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          | View.SYSTEM_UI_FLAG_LOW_PROFILE);
    } else if (Build.VERSION.SDK_INT >= 16) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LOW_PROFILE);
    } else {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }
  }

  /**
   * Destroy the soundHandler object when the app closed to stop the sounds.
   */
  @Override
  protected void onDestroy() {
    if (soundHandler != null) {
      soundHandler.destroy();
    }
    super.onDestroy();
  }

  /**
   * Calculates, displays and stores the new auto-repeating-time for the NIBP. If necessary
   * the current running NIBP is updated with a new repeating time. This method
   * is called each time there is change on the auto-time-slider in the NIBP-section.
   *
   * @param progress of the slider.
   */
  private void nibpAutoTimeChange(int progress) {
    // Old calculation -> Not needed at the moment.
    /*nibpAutoTime = 20 + progress * 2;
    int timeMin = nibpAutoTime / 60;
		int timeSec = nibpAutoTime % 60;
		TextView nibpAutoTimeTextView = (TextView) this.findViewById(R.id.nibpAutoTimeTextView);
		if (timeSec < 10) {
			nibpAutoTimeTextView.setText("" + timeMin + ":0" + timeSec);
		} else {
			nibpAutoTimeTextView.setText("" + timeMin + ":" + timeSec);
		}*/
    // Calculate and display the new auto repeat time.
    int timeMin = 0;
    if (progress <= 16) {
      nibpAutoTime = 60;
      timeMin = 1;
    } else if (progress <= 33) {
      nibpAutoTime = 120;
      timeMin = 2;
    } else if (progress <= 50) {
      nibpAutoTime = 180;
      timeMin = 3;
    } else if (progress <= 67) {
      nibpAutoTime = 300;
      timeMin = 5;
    } else if (progress <= 84) {
      nibpAutoTime = 600;
      timeMin = 10;
    } else {
      nibpAutoTime = 1200;
      timeMin = 20;
    }
    TextView nibpAutoTimeTextView = (TextView) this.findViewById(R.id.nibpAutoTimeTextView);
    nibpAutoTimeTextView.setText("" + timeMin + " min");
    // Update a running NIBP with a new auto repeat time if necessary.
    if (autoNIBPRunning) {
      if (autoNibpTimer != null) {
        autoNibpTimer.cancel();
      }
      autoNibpTimer = new Timer();
      autoNibpTimer.scheduleAtFixedRate(new TimerTask() {
        public void run() {
          runOnUiThread(new Runnable() {
            public void run() {
              runNIBP();
            }
          });
        }
      }, (nibpAutoTime * 1000), (nibpAutoTime * 1000));
    }
  }

  /**
   * Makes the EKG-heart blink once for heartBlinkDelay ms.
   */
  private void blinkHeart() {
    final ImageView ekgHeartImageView = (ImageView) this.findViewById(R.id.ekgHeartImageView);
    ekgHeartImageView.setVisibility(View.INVISIBLE);
    /*new Handler().postDelayed(new Runnable() {
      public void run() {
        ekgHeartImageView.setVisibility(View.VISIBLE);
      }
    }, heartBlinkDelay);*///TODO: remove completely
  }



  /**
   * A functions which activates/deactivates the corresponding alarm if the current vital-parameter-values are
   * above or below the current alarm thresholds. The alarm is only triggered if the parameter itself is active, the
   * alarm of the parameter is active and general alarm is active.
   */
  private void checkAlarm() {
    // EKG:
    if ((ekgRandValue < ekgAlarmLowValue || ekgRandValue > ekgAlarmUpValue) && ekgAlarmOn && ekgActive && alarmActive) {
      soundHandler.setEKGAlarm(true);
    } else {
      soundHandler.setEKGAlarm(false);
    }
    // Blood pressure:
    if ((diaRandValue < rrDiaAlarmLowValue || diaRandValue > rrDiaAlarmUpValue ||
        sysRandValue < rrSysAlarmLowValue || sysRandValue > rrSysAlarmUpValue) && rrAlarmOn && rrActive && alarmActive) {
      soundHandler.setRRAlarm(true);
    } else {
      soundHandler.setRRAlarm(false);
    }
    // O2 Saturation:
    if (o2RandValue < o2AlarmLowValue && o2AlarmOn && o2Active && alarmActive) {
      soundHandler.setO2Alarm(true);
    } else {
      soundHandler.setO2Alarm(false);
    }
    // CO2:
    if ((co2RandValue < co2AlarmLowValue || co2RandValue > co2AlarmUpValue) && co2AlarmOn && co2Active && alarmActive) {
      soundHandler.setCO2Alarm(true);
    } else {
      soundHandler.setCO2Alarm(false);
    }
  }

  /**
   * A function which simulates a non inversive blood pressure measurement by playing a pump
   * sound and showing a delayed measurement result.
   */
  private void runNIBP() {
    nibpRunning = true;
    // Indicate the running measurement with a yellow button.
    Button nibpStartButton = (Button) findViewById(R.id.nibpStartButton);
    nibpStartButton.setBackgroundResource(R.drawable.yellow_button);
    // Play the pump sound and trigger a delayed result-display.
    soundHandler.playBPSound();
    new Handler().postDelayed(new Runnable() {
      public void run() {
        TextView ekgValueTextView = (TextView) findViewById(R.id.nibpValueTextView);
        // Only show the actual value if NIBP is set to active.
        if (nibpActive) {
          ekgValueTextView.setText("" + sysBloodPressure + "/" + diaBloodPressure);
        } else {
          ekgValueTextView.setText("0/0");
        }
        // Change the button back to its normal green color.
        Button nibpStartButton = (Button) findViewById(R.id.nibpStartButton);
        nibpStartButton.setBackgroundResource(R.drawable.green_button);
        nibpRunning = false;
      }
    }, nibpDelay);
  }

  /**
   * Writes the current time science the start of the timer in the status-bar. The format
   * is mm:ss.
   */
  private void updateTime() {
    // Schedule the update of the timer in main GUI-thread.
    runOnUiThread(new Runnable() {
      public void run() {
        TextView statusBarTitle = (TextView) findViewById(R.id.statusBarTitle);
        // Calculate the minute and second value out of the second-counter.
        int sec = timeInSec % 60;
        int min = timeInSec / 60;
        // If necessary add leading zeros to the well that it looks nice.
        if (sec < 10 && min < 10) {
          statusBarTitle.setText("Timer:   0" + min + ":0" + sec);
        } else if (sec < 10) {
          statusBarTitle.setText("Timer:   " + min + ":0" + sec);
        } else if (min < 10) {
          statusBarTitle.setText("Timer:   0" + min + ":" + sec);
        } else {
          statusBarTitle.setText("Timer:   " + min + ":" + sec);
        }
      }
    });
  }

  /**
   * Makes the EKG-parameter blink once for paramBlinkDelay ms.
   */
  private void blinkEKGParam() {
    runOnUiThread(new Runnable() {
      public void run() {
        final TextView ekgValueTextView = (TextView) findViewById(R.id.ekgValueTextView);
        if (ekgActive) {
          ekgValueTextView.setVisibility(View.INVISIBLE);
        }
        new Handler().postDelayed(new Runnable() {
          public void run() {
            runOnUiThread(new Runnable() {
              public void run() {
                ekgValueTextView.setVisibility(View.VISIBLE);
              }
            });
          }
        }, paramBlinkDelay);
      }
    });
  }

  /**
   * Makes the EKG, RR and O2-parameter blink once for paramBlinkDelay ms if they are active.
   */
  private void blinkParam() {
    runOnUiThread(new Runnable() {
      public void run() {
        final TextView ekgValueTextView = (TextView) findViewById(R.id.ekgValueTextView);
        final TextView ibpValueTextView = (TextView) findViewById(R.id.ibpValueTextView);
        final TextView o2ValueTextView = (TextView) findViewById(R.id.o2ValueTextView);
        if (ekgActive) {
          ekgValueTextView.setVisibility(View.INVISIBLE);
        }
        if (rrActive) {
          ibpValueTextView.setVisibility(View.INVISIBLE);
        }
        if (o2Active) {
          o2ValueTextView.setVisibility(View.INVISIBLE);
        }
        new Handler().postDelayed(new Runnable() {
          public void run() {
            runOnUiThread(new Runnable() {
              public void run() {
                ekgValueTextView.setVisibility(View.VISIBLE);
                ibpValueTextView.setVisibility(View.VISIBLE);
                o2ValueTextView.setVisibility(View.VISIBLE);
              }
            });
          }
        }, paramBlinkDelay);
      }
    });
  }

  /**
   * Shows/Hides all Difibrillator GUI elements-
   *
   * @param show - states if the elments should be shown or not.
   */
  private void showHideDefiUiElements(boolean show) {
    FragmentManager fragmentManager = getFragmentManager();
    if(show && (fragmentManager.findFragmentByTag("defiFragment") == null)) {
      fragmentManager.beginTransaction()
          .add(R.id.defi_fragment_container, defiFragment, "defiFragment")
          .commit();
    } else {
      fragmentManager.beginTransaction()
          .remove(defiFragment)
          .commit();
    }
   //TODO: link to new layout
   /* RelativeLayout defiLayout = (RelativeLayout) this.findViewById(R.id.defiLayout);
    if (show) {
      defiLayout.setVisibility(View.VISIBLE);
    } else {
      defiLayout.setVisibility(View.INVISIBLE);
    }*/
  }

  // PUBLIC:

  /**
   * Opens the settings-view via its weight-factor.
   *
   */
  public void openSettings() {
    // new: settingsActivity
    startActivity(new Intent(this, SettingsActivity.class).putExtra
        (PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, true));
  }

  /**
   * Opens/Closes the defi-view via its weight-factor. If the settings-view is already open,
   * close it first.
   *
   * @param view which called the method.
   */
  public void openCloseDefi(View view) {
    /*LinearLayout.LayoutParams loParamsDefi = (LinearLayout.LayoutParams) defiLayout.getLayoutParams();*/
    Button openCloseDefiButton = (Button) this.findViewById(R.id.openCloseDefiButton);
    if (defiHidden) {
      defiHidden = false;
      openCloseDefiButton.setText("Defibrillator >");
      // Show defi UI-elements.
      showHideDefiUiElements(true);
    } else {
      defiHidden = true;
      openCloseDefiButton.setText("< Defibrillator");
      // Hide defi UI-elements.
      showHideDefiUiElements(false);
    }
    //defiLayout.setLayoutParams(loParamsDefi);
  }

  /**
   * Start/Stop an automatically NIBP measurement every nibpAutoTime sec.
   *
   * @param view which called the method.
   */
  public void autoNIBP(View view) {
    Button nibpAutoButton = (Button) this.findViewById(R.id.nibpAutoButton);
    if (autoNIBPRunning) {
      // If the measurement is running, stop it by deleting the scheduled measuring task and reset the button.
      nibpAutoButton.setBackgroundResource(R.drawable.green_button);
      nibpAutoButton.setText("Start A.-NIBP");
      autoNIBPRunning = false;
      if (autoNibpTimer != null) {
        autoNibpTimer.cancel();
      }
    } else {
      if (!nibpRunning) {
        // If no measurement is running, schedule a measurement task every nibpAutoTime and indicate
        // this by changing the button color to red.
        nibpAutoButton.setBackgroundResource(R.drawable.red_button);
        nibpAutoButton.setText("Stop A.-NIBP");
        autoNIBPRunning = true;
        if (autoNibpTimer != null) {
          autoNibpTimer.cancel();
        }
        autoNibpTimer = new Timer();
        autoNibpTimer.scheduleAtFixedRate(new TimerTask() {
          public void run() {
            runOnUiThread(new Runnable() {
              public void run() {
                runNIBP();
              }
            });
          }
        }, 0, (nibpAutoTime * 1000));
      }
    }
  }

  /**
   * Start a single NIBP measurement if no measurement is already running.
   *
   * @param view which called the method.
   */
  public void startNIBP(View view) {
    if (!nibpRunning) {
      runNIBP();
    }
  }

  /**
   * Sets and saves the EKG-value. If the heart pattern is ventfibri, ventflutter or asystole
   * sets the value to 0 and makes the EKG (and other values) blink.
   *
   * @param value - current heart beat per minute value.
   */
  public void setEKG(int value) {
    ekgValue = value;
    ekgRandValue = value;
    // Only do something if the parameter is set active.
    if (ekgActive) {
      // If the current heart pattern is ventfibri or ventflutter set EKG value to 0 and make it blink, if this isn't already the case.
      if (heartPattern == Event.HeartPattern.VENTFIBRI || heartPattern == Event.HeartPattern.VENTFLUTTER) {
        if (!ekgBlinkActive) {
          // Set EKG value to 0.
          ekgValue = 0;
          ekgRandValue = 0;
          runOnUiThread(new Runnable() {
            public void run() {
              TextView ekgValueTextView = (TextView) findViewById(R.id.ekgValueTextView);
              ekgValueTextView.setText("0");
            }
          });
          // Stop the timer if necessary.
          if (ekgBlinkTimer != null) {
            ekgBlinkTimer.cancel();
          }
          // Start blinking.
          ekgBlinkTimer = new Timer();
          ekgBlinkTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
              blinkEKGParam();
            }
          }, 0, paramBlinkRepeatTime);
          ekgBlinkActive = true;
          // Start alarm if necessary.
          checkAlarm();
        }
      } else if (heartPattern == Event.HeartPattern.ASYSTOLE) {
        // If the current heart pattern is asystole set EKG value to 0 and make the EKG, RR and O2 blink, if this isn't already the case.
        ekgValue = 0;
        ekgRandValue = 0;
        runOnUiThread(new Runnable() {
          public void run() {
            TextView ekgValueTextView = (TextView) findViewById(R.id.ekgValueTextView);
            ekgValueTextView.setText("0");
          }
        });
        // Stop the EKG blink timer if necessary.
        if (ekgBlinkTimer != null) {
          ekgBlinkTimer.cancel();
          ekgBlinkActive = false;
        }
        if (!paramBlinkActive) {
          // Stop the timer if necessary.
          if (paramBlinkTimer != null) {
            paramBlinkTimer.cancel();
          }
          // Start blinking.
          paramBlinkTimer = new Timer();
          paramBlinkTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
              blinkParam();
            }
          }, 0, paramBlinkRepeatTime);
          paramBlinkActive = true;
        }
      } else {
        // If there is an other heartpattern schedule the display in main GUI thread and stop the blinking (if necessary).
        if (ekgBlinkTimer != null) {
          ekgBlinkTimer.cancel();
          ekgBlinkActive = false;
        }
        runOnUiThread(new Runnable() {
          public void run() {
            TextView ekgValueTextView = (TextView) findViewById(R.id.ekgValueTextView);
            ekgValueTextView.setText("" + ekgValue);
          }
        });
        // Adjust the EKG curve to the new value.
        signalServer.changeHeartRate(value);
        // Start alarm if necessary.
        checkAlarm();
      }
    }
  }

  /**
   * Sets and saves the invasible blood pressure values. If the heart pattern is asystole
   * sets the value to 0 and makes the RR (and other values) blink. If the heart pattern is
   * ventfibri or ventflutter sets the value to 0.
   *
   * @param diaValue - current low blood pressure value.
   * @param sysValue - current high blood pressure value.
   */
  public void setIBP(int diaValue, int sysValue) {
    // If the current heart pattern is asystole set RR value to 0/0 and make the EKG, RR and O2 blink, if this isn't already the case.
    if (heartPattern == Event.HeartPattern.ASYSTOLE) {
      // Save the values.
      diaBloodPressure = 0;
      diaRandValue = 0;
      sysBloodPressure = 0;
      sysRandValue = 0;
      // Only do something if the parameter is set active.
      if (rrActive) {
        // Schedule the display of the new values in main GUI thread.
        runOnUiThread(new Runnable() {
          public void run() {
            TextView ibpValueTextView = (TextView) findViewById(R.id.ibpValueTextView);
            ibpValueTextView.setText("0/0");
          }
        });
        if (!paramBlinkActive) {
          // Stop the timer if necessary.
          if (paramBlinkTimer != null) {
            paramBlinkTimer.cancel();
          }
          // Start blinking.
          paramBlinkTimer = new Timer();
          paramBlinkTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
              blinkParam();
            }
          }, 0, paramBlinkRepeatTime);
          paramBlinkActive = true;
        }

      }
      // If the current heart pattern is ventfibri or ventflutter set RR value to 0/0.
    } else if (heartPattern == Event.HeartPattern.VENTFIBRI || heartPattern == Event.HeartPattern.VENTFLUTTER) {
      // Save the values.
      diaBloodPressure = 0;
      diaRandValue = 0;
      sysBloodPressure = 0;
      sysRandValue = 0;
      // Only do something if the parameter is set active.
      if (rrActive) {
        // Schedule the display of the new values in main GUI thread.
        runOnUiThread(new Runnable() {
          public void run() {
            TextView ibpValueTextView = (TextView) findViewById(R.id.ibpValueTextView);
            ibpValueTextView.setText("0/0");
          }
        });
        // Start alarm if necessary.
        checkAlarm();
      }
    } else {
      // Save the values.
      diaBloodPressure = diaValue;
      diaRandValue = diaValue;
      sysBloodPressure = sysValue;
      sysRandValue = sysValue;
      // Only do something if the parameter is set active.
      if (rrActive) {
        // Schedule the display of the new values in main GUI thread.
        runOnUiThread(new Runnable() {
          public void run() {
            TextView ibpValueTextView = (TextView) findViewById(R.id.ibpValueTextView);
            ibpValueTextView.setText("" + sysBloodPressure + "/" + diaBloodPressure);
          }
        });
        // Adjust the blood pressure curve to the new value.
        signalServer.changeBloodPressure(sysBloodPressure, diaBloodPressure);
        // Start alarm if necessary.
        checkAlarm();
      }
    }
  }

  /**
   * Sets and saves the oxygen saturation value. If the heart pattern is asystole
   * sets the value to 0 and makes the RR (and other values) blink. If the heart pattern is
   * ventfibri or ventflutter sets the value to 0.
   *
   * @param value - current oxygen saturation value.
   */
  public void setO2(int value) {
    o2Value = value;
    o2RandValue = value;
    // If the current heart pattern is asystole set O2 value to 0 and make the EKG, RR and O2 blink, if this isn't already the case.
    if (heartPattern == Event.HeartPattern.ASYSTOLE) {
      // Only do something if the parameter is set active.
      if (o2Active) {
        // Save the value and schedule the display in main GUI thread.
        o2Value = 0;
        o2RandValue = 0;
        runOnUiThread(new Runnable() {
          public void run() {
            TextView o2ValueTextView = (TextView) findViewById(R.id.o2ValueTextView);
            o2ValueTextView.setText("0");
          }
        });
        if (!paramBlinkActive) {
          // Stop the timer if necessary.
          if (paramBlinkTimer != null) {
            paramBlinkTimer.cancel();
          }
          // Start blinking.
          paramBlinkTimer = new Timer();
          paramBlinkTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
              blinkParam();
            }
          }, 0, paramBlinkRepeatTime);
          paramBlinkActive = true;
        }
      }
      // If the current heart pattern is ventfibri or ventflutter set O2 value to 0.
    } else if (heartPattern == Event.HeartPattern.VENTFIBRI || heartPattern == Event.HeartPattern.VENTFLUTTER) {
      // Only do something if the parameter is set active.
      if (o2Active) {
        // Save the value and schedule the display in main GUI thread.
        o2Value = 0;
        o2RandValue = 0;
        runOnUiThread(new Runnable() {
          public void run() {
            TextView o2ValueTextView = (TextView) findViewById(R.id.o2ValueTextView);
            o2ValueTextView.setText("0");
          }
        });
        // Start alarm if necessary.
        checkAlarm();
      }
    } else {
      // Only do something if the parameter is set active.
      if (o2Active) {
        // Schedule the display in main GUI thread.
        runOnUiThread(new Runnable() {
          public void run() {
            TextView o2ValueTextView = (TextView) findViewById(R.id.o2ValueTextView);
            o2ValueTextView.setText("" + o2Value);
          }
        });
        // Adjust the O2 curve to the new value.
        signalServer.changeO2Value(value);
        // Start alarm if necessary.
        checkAlarm();
      }
    }
  }

  /**
   * Sets and saves the CO2 value.
   *
   * @param value - current CO2 value.
   */
  public void setCO2(int value) {
    co2Value = value;
    co2RandValue = value;
    // Only do something if the parameter is set active.
    if (co2Active) {
      // Schedule the display in main GUI thread.
      runOnUiThread(new Runnable() {
        public void run() {
          TextView co2ValueTextView = (TextView) findViewById(R.id.co2ValueTextView);
          co2ValueTextView.setText("" + co2Value);
        }
      });
      // Adjust the CO2 curve to the new value.
      signalServer.changeCO2Value(value);
      // Start alarm if necessary.
      checkAlarm();
    }
  }

  /**
   * Sets and saves the respiration value.
   *
   * @param value - current respiration value.
   */
  public void setResp(int value) {
    respValue = value;
    respRandValue = value;
    // Only do something if the parameter is set active.
    if (respActive) {
      // Schedule the display in main GUI thread.
      runOnUiThread(new Runnable() {
        public void run() {
          TextView afValueTextView = (TextView) findViewById(R.id.afValueTextView);
          afValueTextView.setText("" + respValue);
        }
      });
    }
    signalServer.changeRespirationRate(value);
  }

  /**
   * Generates a gaussian variation on the ekg value. The value is kept constant until the
   * number of heart-beats since the last change reaches a certain value and a random number
   * is greater than a threshold.
   */
  public void ekgVariation() {
    if (ekgActive) {  // Only do something if EKG is active.
      ekgPeakCounter++;
      // Check if new variation should be done and adjust the curve.
      if (Math.random() > randThreshold && ekgPeakCounter > (ekgValue / ekgDivFactor)) {
        double rand = randomGenerator.nextGaussian();
        Log.e("debug, Main, ekg", "randomvalue: "+rand+", applied value: "+(
            (int) (rand * ekgRandMult)));
        ekgRandValue = ekgValue + (int) (rand * ekgRandMult);
        signalServer.changeHeartRate(ekgRandValue);
        ekgPeakCounter = 0;
      }
      // One heart-beat after a change also update the parameter value and trigger
      // a alarm if necessary.
      if (ekgPeakCounter == 1) {
        runOnUiThread(new Runnable() {
          public void run() {
            TextView ekgValueTextView = (TextView) findViewById(R.id.ekgValueTextView);
            ekgValueTextView.setText("" + ekgRandValue);
          }
        });
        checkAlarm();
      }
    }
  }

  /**
   * Generates a gaussian variation on the blood pressure value. The value is kept constant
   * until the number of heart-beats since the last change reaches a certain value and a random
   * number is greater than a threshold.
   */
  public void rrVariation() {
    if (rrActive) {  // Only do something if RR is active.
      rrPeakCounter++;
      // Check if new variation should be done and adjust the curve.
      if (Math.random() > randThreshold && rrPeakCounter > (ekgValue / ekgDivFactor)) {
        double rand = randomGenerator.nextGaussian();
        diaRandValue = diaBloodPressure + (int) (rand * rrRandMult);
        rand = randomGenerator.nextGaussian();
        sysRandValue = sysBloodPressure + (int) (rand * rrRandMult);
        signalServer.changeBloodPressure(sysRandValue, diaRandValue);
        rrPeakCounter = 0;
      }
      // One heart-beat after a change also update the parameter value and trigger
      // a alarm if necessary.
      if (rrPeakCounter == 1) {
        runOnUiThread(new Runnable() {
          public void run() {
            TextView ibpValueTextView = (TextView) findViewById(R.id.ibpValueTextView);
            ibpValueTextView.setText("" + sysRandValue + "/" + diaRandValue);
          }
        });
        checkAlarm();
      }
    }
  }

  /**
   * Generates a gaussian variation on the o2 value. The value is kept constant until the
   * number of heart-beats since the last change reaches a certain value and a random number
   * is greater than a threshold.
   */
  public void o2Variation() {
    if (o2Active) {  // Only do something if O2 is active.
      o2PeakCounter++;
      // Check if new variation should be done and adjust the curve.
      if (Math.random() > randThreshold && o2PeakCounter > (ekgValue / ekgDivFactor)) {
        double rand = randomGenerator.nextGaussian();
        o2RandValue = o2Value + (int) (rand * o2RandMult);
        if (o2RandValue > 100) {
          o2RandValue = 100;
        }
        signalServer.changeO2Value(o2RandValue);
        o2PeakCounter = 0;
      }
      // One heart-beat after a change also update the parameter value and trigger
      // a alarm if necessary.
      if (o2PeakCounter == 1) {
        runOnUiThread(new Runnable() {
          public void run() {
            TextView o2ValueTextView = (TextView) findViewById(R.id.o2ValueTextView);
            o2ValueTextView.setText("" + o2RandValue);
          }
        });
        checkAlarm();
      }
    }
  }

  /**
   * Generates a gaussian variation on the CO2 and the respiration value. The value is kept constant
   * until the number of heart-beats since the last change reaches a certain value and a random
   * number is greater than a threshold.
   */
  public void co2RespVariation() {
    // CO2:
    if (co2Active) { // Only do something if CO2 is active.
      co2PeakCounter++;
      // Check if new variation should be done and adjust the curve.
      if (Math.random() > randThreshold && co2PeakCounter > (respValue / respDivFactor)) {
        double rand = randomGenerator.nextGaussian();
        co2RandValue = co2Value + (int) (rand * co2RandMult);
        signalServer.changeCO2Value(co2RandValue);
        co2PeakCounter = 0;
      }
      // One heart-beat after a change also update the parameter value and trigger
      // a alarm if necessary.
      if (co2PeakCounter == 1) {
        runOnUiThread(new Runnable() {
          public void run() {
            TextView co2ValueTextView = (TextView) findViewById(R.id.co2ValueTextView);
            co2ValueTextView.setText("" + co2RandValue);
          }
        });
        checkAlarm();
      }
    }
    // Respiration:
    // Only do something if there is a active breathing.
    if (respValue > 0 && respActive) {
      respPeakCounter++;
      // Check if new variation should be done and adjust the curve.
      if (Math.random() > randThreshold && respPeakCounter > (respValue / respDivFactor)) {
        double rand = randomGenerator.nextGaussian();
        respRandValue = respValue + (int) (rand * respRandMult);
        // Catch negative values.
        if (respRandValue < 0) {
          respRandValue = 0;
        }
        signalServer.changeRespirationRate(respRandValue);
        respPeakCounter = 0;
      }
      // One heart-beat after a change also update the parameter value and trigger
      // a alarm if necessary.
      if (respPeakCounter == 1) {
        runOnUiThread(new Runnable() {
          public void run() {
            TextView afValueTextView = (TextView) findViewById(R.id.afValueTextView);
            afValueTextView.setText("" + respRandValue);
          }
        });
        checkAlarm();
      }
    }
  }

  /**
   * Displays the EKG-settings by marking the EKG-selection-button and loading/displaying the
   * current EKG-settings in the settings-layout.
   *
   * @param view which called the method.
   */
  /*public void showEKGSettings(View view) {
    menuSelection = 0;
    // Change the button-text-color of the selected button to black.
    Button ekgSettingsButton = (Button) this.findViewById(R.id.ekgSettingsButton);
    ekgSettingsButton.setTextColor(Color.BLACK);
    // Move the arrow under the button.
    ImageView setPointArrowImageView = (ImageView) this.findViewById(R.id.settingsPointingArrowImageView);
    int arrowPos = ekgSettingsButton.getLeft() + ekgSettingsButton.getWidth() / 2 - setPointArrowImageView.getWidth() / 2;
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) setPointArrowImageView.getLayoutParams();
    params.setMargins(arrowPos, 0, 0, 0); //Parameters for left, top, right and bottom margin.
    setPointArrowImageView.setLayoutParams(params);
    // Change the button-text-colors of the remaining buttons back to white.
    Button ibpSettingsButton = (Button) this.findViewById(R.id.ibpSettingsButton);
    ibpSettingsButton.setTextColor(Color.WHITE);
    Button o2SettingsButton = (Button) this.findViewById(R.id.o2SettingsButton);
    o2SettingsButton.setTextColor(Color.WHITE);
    Button co2SettingsButton = (Button) this.findViewById(R.id.co2SettingsButton);
    co2SettingsButton.setTextColor(Color.WHITE);
    // Display the sound settings. Not needed at the moment.
    // showSoundSetting(true);
    // Display/Hide and setup the alarm settings.
    showAlarmSetting(true, false, false, false, "Frequency", "", "" + ekgAlarmLowValue, "" + ekgAlarmUpValue, "", "");
    // Update the alarm and sound on/off buttons.
    setAlarmButton(ekgAlarmOn);
    setSoundButton(ekgSoundOn);
  }

  *//**
   * Displays the blood pressure-settings by marking RR-selection-button and loading/displaying the
   * current blood pressure-settings in the settings-layout.
   *
   * @param view which called the method.
   *//*
  public void showIBPSettings(View view) {
    menuSelection = 1;
    // Change the button-text-color of the selected button to black.
    Button ibpSettingsButton = (Button) this.findViewById(R.id.ibpSettingsButton);
    ibpSettingsButton.setTextColor(Color.BLACK);
    // Move the arrow under the button.
    ImageView setPointArrowImageView = (ImageView) this.findViewById(R.id.settingsPointingArrowImageView);
    int arrowPos = ibpSettingsButton.getLeft() + ibpSettingsButton.getWidth() / 2 - setPointArrowImageView.getWidth() / 2;
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) setPointArrowImageView.getLayoutParams();
    params.setMargins(arrowPos, 0, 0, 0); //Parameters for left, top, right and bottom margin.
    setPointArrowImageView.setLayoutParams(params);
    // Change the button-text-colors of the remaining buttons back to white.
    Button ekgSettingsButton = (Button) this.findViewById(R.id.ekgSettingsButton);
    ekgSettingsButton.setTextColor(Color.WHITE);
    Button o2SettingsButton = (Button) this.findViewById(R.id.o2SettingsButton);
    o2SettingsButton.setTextColor(Color.WHITE);
    Button co2SettingsButton = (Button) this.findViewById(R.id.co2SettingsButton);
    co2SettingsButton.setTextColor(Color.WHITE);
    // Hide the sound settings. Not needed at the moment.
    // showSoundSetting(false);
    // Display/Hide and setup the alarm settings.
    showAlarmSetting(true, false, false, true, "Systolic BP", "Diastolic BP", "" + rrSysAlarmLowValue, "" + rrSysAlarmUpValue, "" + rrDiaAlarmLowValue, "" + rrDiaAlarmUpValue);
    // Update the alarm on/off button.
    setAlarmButton(rrAlarmOn);
  }

  *//**
   * Displays the O2-settings by marking O2-selection-button and loading/displaying the
   * current O2-settings in the settings-layout.
   *
   * @param view which called the method.
   *//*
  public void showO2Settings(View view) {
    menuSelection = 2;
    // Change the button-text-color of the selected button to black.
    Button o2SettingsButton = (Button) this.findViewById(R.id.o2SettingsButton);
    o2SettingsButton.setTextColor(Color.BLACK);
    // Move the arrow under the button.
    ImageView setPointArrowImageView = (ImageView) this.findViewById(R.id.settingsPointingArrowImageView);
    int arrowPos = o2SettingsButton.getLeft() + o2SettingsButton.getWidth() / 2 - setPointArrowImageView.getWidth() / 2;
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) setPointArrowImageView.getLayoutParams();
    params.setMargins(arrowPos, 0, 0, 0); //Parameters for left, top, right and bottom margin.
    setPointArrowImageView.setLayoutParams(params);
    // Change the button-text-colors of the remaining buttons back to white.
    Button ekgSettingsButton = (Button) this.findViewById(R.id.ekgSettingsButton);
    ekgSettingsButton.setTextColor(Color.WHITE);
    Button ibpSettingsButton = (Button) this.findViewById(R.id.ibpSettingsButton);
    ibpSettingsButton.setTextColor(Color.WHITE);
    Button co2SettingsButton = (Button) this.findViewById(R.id.co2SettingsButton);
    co2SettingsButton.setTextColor(Color.WHITE);
    // Hide the sound settings. Not needed at the moment.
    // showSoundSetting(false);
    showAlarmSetting(true, true, false, false, "O2 Saturation", "", "" + o2AlarmLowValue, "", "", "");
    // Update the alarm on/off button.
    setAlarmButton(o2AlarmOn);
  }

  *//**
   * Displays the CO2-settings by marking CO2-selection-button and loading/displaying the
   * current CO2-settings in the settings-layout.
   *
   * @param view which called the method.
   *//*
  public void showCO2Settings(View view) {
    menuSelection = 3;
    // Change the button-text-color of the selected button to black.
    Button co2SettingsButton = (Button) this.findViewById(R.id.co2SettingsButton);
    co2SettingsButton.setTextColor(Color.BLACK);
    // Move the arrow under the button.
    ImageView setPointArrowImageView = (ImageView) this.findViewById(R.id.settingsPointingArrowImageView);
    int arrowPos = co2SettingsButton.getLeft() + co2SettingsButton.getWidth() / 2 - setPointArrowImageView.getWidth() / 2;
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) setPointArrowImageView.getLayoutParams();
    params.setMargins(arrowPos, 0, 0, 0); //Parameters for left, top, right and bottom margin.
    setPointArrowImageView.setLayoutParams(params);
    // Change the button-text-colors of the remaining buttons back to white.
    Button ekgSettingsButton = (Button) this.findViewById(R.id.ekgSettingsButton);
    ekgSettingsButton.setTextColor(Color.WHITE);
    Button ibpSettingsButton = (Button) this.findViewById(R.id.ibpSettingsButton);
    ibpSettingsButton.setTextColor(Color.WHITE);
    Button o2SettingsButton = (Button) this.findViewById(R.id.o2SettingsButton);
    o2SettingsButton.setTextColor(Color.WHITE);
    // Hide the sound settings. Not needed at the moment.
    // showSoundSetting(false);
    // Hide and setup the alarm settings.
    showAlarmSetting(true, false, false, false, "CO2-Level", "", "" + co2AlarmLowValue, "" + co2AlarmUpValue, "", "");
    // Update the alarm and sound on/off buttons.
    setAlarmButton(co2AlarmOn);
  }
*///TODO: remove

  /**
   * Turns the alarm of the currently in the settings selected parameter on/off.
   *
   * @param view which called the method.
   */
  public void triggerAlarm(View view) {
    if (menuSelection == 0) {  // EKG-settings selected.
      // Trigger the alarm(-button).
      ekgAlarmOn = !ekgAlarmOn;
      //setAlarmButton(ekgAlarmOn);//TODO:remove
      // Show/Hide the little bell in the EKG-parameter-section.
      ImageView ekgAlarmImageView = (ImageView) this.findViewById(R.id.ekgAlarmImageView);
      if (ekgAlarmOn && alarmActive) {
        ekgAlarmImageView.setVisibility(View.VISIBLE);
        checkAlarm();
      } else {
        ekgAlarmImageView.setVisibility(View.INVISIBLE);
        soundHandler.setEKGAlarm(false);
      }
    } else if (menuSelection == 1) {  // RR-settings selected.
      // Trigger the alarm(-button).
      rrAlarmOn = !rrAlarmOn;
      //setAlarmButton(rrAlarmOn);//TODO:remove
      // Show/Hide the little bells in the IBP- and the NIBP-parameter-section.
      ImageView ibpAlarmImageView = (ImageView) this.findViewById(R.id.ibpAlarmImageView);
      ImageView nibpAlarmImageView = (ImageView) this.findViewById(R.id.nibpAlarmImageView);
      if (rrAlarmOn && alarmActive) {
        ibpAlarmImageView.setVisibility(View.VISIBLE);
        nibpAlarmImageView.setVisibility(View.VISIBLE);
        checkAlarm();
      } else {
        ibpAlarmImageView.setVisibility(View.INVISIBLE);
        nibpAlarmImageView.setVisibility(View.INVISIBLE);
        soundHandler.setRRAlarm(false);
      }
    } else if (menuSelection == 2) {  // O2-settings selected.
      // Trigger the alarm(-button).
      o2AlarmOn = !o2AlarmOn;
      //setAlarmButton(o2AlarmOn);TODO:remove
      // Show/Hide the little bell in the O2-parameter-section.
      ImageView o2AlarmImageView = (ImageView) this.findViewById(R.id.o2AlarmImageView);
      if (o2AlarmOn && alarmActive) {
        o2AlarmImageView.setVisibility(View.VISIBLE);
        checkAlarm();
      } else {
        o2AlarmImageView.setVisibility(View.INVISIBLE);
        soundHandler.setO2Alarm(false);
      }
    } else if (menuSelection == 3) {  // CO2-settings selected.
      // Trigger the alarm(-button).
      co2AlarmOn = !co2AlarmOn;
      //setAlarmButton(co2AlarmOn);//TODO:remove
      // Show/Hide the little bell in the O2-parameter-section.
      ImageView co2AlarmImageView = (ImageView) this.findViewById(R.id.co2AlarmImageView);
      if (co2AlarmOn && alarmActive) {
        co2AlarmImageView.setVisibility(View.VISIBLE);
        checkAlarm();
      } else {
        co2AlarmImageView.setVisibility(View.INVISIBLE);
        soundHandler.setCO2Alarm(false);
      }
    }
  }

  /**
   * Turns the QRS sound on/off.
   *
   * @param view which called the method.
   */
  public void triggerSound(View view){
    triggerSound();
  }
  public void triggerSound() {
    // Trigger the sound(-button).
    ekgSoundOn = !ekgSoundOn;
    //setSoundButton(ekgSoundOn);TODO:remove
    // Show/Hide the little speaker in the EKG-parameter-section.
    ImageView ekgSoundImageView = (ImageView) this.findViewById(R.id.ekgSoundImageView);

    if (ekgSoundOn) {
      ekgSoundImageView.setVisibility(View.VISIBLE);
    } else {
      ekgSoundImageView.setVisibility(View.INVISIBLE);
    }
    // Show/Hide the little speaker on the main sound button in the status bar and change the text color.
    Button mainSoundOnOffButton = (Button) this.findViewById(R.id.mainSoundOnOffButton);
    if (ekgSoundOn) {
      Drawable newIcon = getResources().getDrawable(R.drawable.speaker2icon);
      mainSoundOnOffButton.setCompoundDrawablesWithIntrinsicBounds(null, null, newIcon, null);
      mainSoundOnOffButton.setTextColor(Color.BLACK);
    } else {
      Drawable newIcon = getResources().getDrawable(R.drawable.nspeaker2icon);
      mainSoundOnOffButton.setCompoundDrawablesWithIntrinsicBounds(null, null, newIcon, null);
      mainSoundOnOffButton.setTextColor(Color.WHITE);
    }
  }

  /**
   * Activates/Deactivates the alarm in general.
   * called from general alarm on/off button
   * @param view which called the method.
   */
  public void alarmOnOff(View view) {
    // If the alarm is paused, cancel this.
    if (alarmPaused) {
      if (alarmPauseTimer != null) {
        alarmPauseTimer.cancel();
      }
      alarmPaused = false;
      //alarmActive = true;
      Button alarmPauseButton = (Button) this.findViewById(R.id.alarmPauseButton);
      alarmPauseButton.setTextColor(Color.WHITE);
    }
   /* if (alarmActive) {
      alarmActive = false;
    } else {
      alarmActive = true;
    }*/
    alarmActive = !alarmActive;


    //triggerAlarm(view);
    setAlarmIcons();
    checkAlarm();
  }

  /**
   * set icon of general alarm button and show/hide alarm bell for the
   * parameters
   */
  private void setAlarmIcons(){
    Button mainAlarmOnOffButton = (Button) this.findViewById(R.id.mainAlarmOnOffButton);
    ImageView ekgAlarmImageView = (ImageView) this.findViewById(R.id.ekgAlarmImageView);
    ImageView ibpAlarmImageView = (ImageView) this.findViewById(R.id.ibpAlarmImageView);
    ImageView nibpAlarmImageView = (ImageView) this.findViewById(R.id.nibpAlarmImageView);
    ImageView o2AlarmImageView = (ImageView) this.findViewById(R.id.o2AlarmImageView);
    ImageView co2AlarmImageView = (ImageView) this.findViewById(R.id.co2AlarmImageView);
    if (!alarmActive) {
      // Change the button icon and text color.
      Drawable newIcon = getResources().getDrawable(R.drawable.nbell2icon);
      mainAlarmOnOffButton.setCompoundDrawablesWithIntrinsicBounds(null, null, newIcon, null);
      mainAlarmOnOffButton.setTextColor(Color.WHITE);
      // Hide the little bells at the parameters.
      ekgAlarmImageView.setVisibility(View.INVISIBLE);
      ibpAlarmImageView.setVisibility(View.INVISIBLE);
      nibpAlarmImageView.setVisibility(View.INVISIBLE);
      o2AlarmImageView.setVisibility(View.INVISIBLE);
      co2AlarmImageView.setVisibility(View.INVISIBLE);
    } else {
      // Change the button icon and text color.
      Drawable newIcon = getResources().getDrawable(R.drawable.bell2icon);
      mainAlarmOnOffButton.setCompoundDrawablesWithIntrinsicBounds(null, null, newIcon, null);
      mainAlarmOnOffButton.setTextColor(Color.BLACK);
      // Show little bells at the parameters for which there is an alarm active now.
      Log.e("debug, setAlarmIcons", "alarmActive==true, egkAlarmActive? " +
          ""+Boolean.toString(ekgAlarmOn));
      if (ekgAlarmOn) {
        ekgAlarmImageView.setVisibility(View.VISIBLE);
      } else {
        ekgAlarmImageView.setVisibility(View.INVISIBLE);
      }
      if (rrAlarmOn) {
        ibpAlarmImageView.setVisibility(View.VISIBLE);
        nibpAlarmImageView.setVisibility(View.VISIBLE);
      }else{
        ibpAlarmImageView.setVisibility(View.INVISIBLE);
        nibpAlarmImageView.setVisibility(View.INVISIBLE);
      }
      if (o2AlarmOn) {
        o2AlarmImageView.setVisibility(View.VISIBLE);
      }else{
        o2AlarmImageView.setVisibility(View.INVISIBLE);
      }
      if (co2AlarmOn) {
        co2AlarmImageView.setVisibility(View.VISIBLE);
      }else{
        co2AlarmImageView.setVisibility(View.INVISIBLE);
      }
    }
  }

  /**
   * Pauses the alarm for one minute, if it is active.
   *
   * @param view which called the method.
   */
  public void pauseAlarm(View view) {
    // If the alarm is already paused, cancel the pause and reactivate the alarm.
    if (alarmPaused) {
      if (alarmPauseTimer != null) {
        alarmPauseTimer.cancel();
      }
      alarmPaused = false;
      Button alarmPauseButton = (Button) this.findViewById(R.id.alarmPauseButton);
      alarmPauseButton.setTextColor(Color.WHITE);
      alarmActive = true;
      checkAlarm();
    } else {
      // If the alarm is not already paused and alarm is active, stop it an schedule a
      // reactivation after alarmPauseTime s.
      if (alarmActive) {
        final Button alarmPauseButton = (Button) this.findViewById(R.id.alarmPauseButton);
        alarmPauseButton.setTextColor(Color.BLACK);
        alarmActive = false;
        checkAlarm();
        alarmPaused = true;
        alarmPauseTimer = new Timer();
        alarmPauseTimer.schedule(new TimerTask() {
          public void run() {
            runOnUiThread(new Runnable() {
              public void run() {
                alarmActive = true;
                checkAlarm();
                alarmPaused = false;
                alarmPauseButton.setTextColor(Color.WHITE);
              }
            });
          }
        }, (alarmPauseTime * 1000));
      }
    }
  }

  /**
   * Should be called if the highest point of one period of the heart-curve is reached to
   * trigger the sound and blinking heart.
   */
  public void heartPeak() {
    // Schedule a blink of the little EKG-heart and, if activated, the heart-sound in the
    // main GUI thread.
    runOnUiThread(new Runnable() {
      public void run() {
        blinkHeart();
        if (ekgSoundOn) {
          soundHandler.playHeartSound(o2Active, o2Value);
        }
      }
    });
  }

  /**
   * Should be called if the highest point of one period of the o2-curve is reached to
   * trigger the sound and blinking heart, while EKG isn't available.
   */
  public void o2Peak() {
    // If there is no EKG-signal, schedule a blink of the little EKG-heart and, if
    // activated, the heart-sound in the main GUI thread.
    if (!ekgActive) {
      runOnUiThread(new Runnable() {
        public void run() {
          blinkHeart();
          if (ekgSoundOn) {
            soundHandler.playHeartSound(true, o2Value);
          }
        }
      });
    }
  }

  /**
   * Starts/Stops the timer in the status bar.
   *
   * @param run - states if the timer should run or not.
   */
  public void startStopTimer(boolean run) {
    // If the timer should run, create a scheduled task, that every increases a value
    // every second and displays it.
    if (run) {
      if (!timerActive) {
        stopWatchTimer = new Timer();
        stopWatchTimer.schedule(new TimerTask() {
          public void run() {
            runOnUiThread(new Runnable() {
              public void run() {
                timeInSec++;  // Increase the counter every second.
                updateTime();  // Update the GUI.
              }
            });
          }
        }, 1000, 1000);
        timerActive = true;
      }
    } else {
      // If the timer should be stopped, cancel the scheduled task
      if (stopWatchTimer != null) {
        stopWatchTimer.cancel();
      }
      timerActive = false;
    }
  }

  /**
   * Resets the timer in status bar.
   */
  public void resetTimer() {
    timeInSec = 0;
    runOnUiThread(new Runnable() {
      public void run() {
        TextView statusBarTitle = (TextView) findViewById(R.id.statusBarTitle);
        statusBarTitle.setText("Timer:   00:00");
      }
    });
  }

 /* *//**
   * Increases the first upper alarm threshold by one and schedules an automatic increase if the
   * button is pressed continuously.
   *
   * @param view which called the method.
   *//*
  public void startIncFirstAlarmUpTH(View view) {
    // Increase the first upper threshold value by one.
    incFirstAlarmUpTH();
    // Schedule an automatic increase after incDecAutoStartDelay ms every incDecStepTime
    // until the button is released.
    firstAlarmUpperTHUpButtonPressed = true;
    if (incDecTimer != null) {
      incDecTimer.cancel();
    }
    incDecTimer = new Timer();
    incDecTimer.scheduleAtFixedRate(new TimerTask() {
      public void run() {
        if (!firstAlarmUpperTHUpButtonPressed) {
          this.cancel();
        } else {
          incFirstAlarmUpTH();
        }
      }
    }, incDecAutoStartDelay, incDecStepTime);
  }

  *//**
   * Performs an increase of the currently selected first upper alarm threshold,
   * displays the new value and activates the alarm if necessary.
   *//*
  public void incFirstAlarmUpTH() {
    // Schedule the increase in the main GUI-thread.
    runOnUiThread(new Runnable() {
      public void run() {
        final TextView firstAlarmUpperTHValueTextView = (TextView) findViewById(R.id.firstAlarmUpperTHValueTextView);
        if (menuSelection == 0) {  // EKG-settings selected.
          if ((ekgAlarmUpValue + ekgAlarmIncDecStep) <= ekgAlarmUpValueMax) {
            // When the new value doesn't conflict with the borders, save and display it.
            ekgAlarmUpValue = ekgAlarmUpValue + ekgAlarmIncDecStep;
            firstAlarmUpperTHValueTextView.setText("" + ekgAlarmUpValue);
          }
        } else if (menuSelection == 1) {  // RR-settings selected.
          if ((rrSysAlarmUpValue + rrAlarmIncDecStep) <= rrSysAlarmUpValueMax) {
            // When the new value doesn't conflict with the borders, save and display it.
            rrSysAlarmUpValue = rrSysAlarmUpValue + rrAlarmIncDecStep;
            firstAlarmUpperTHValueTextView.setText("" + rrSysAlarmUpValue);
          }
        } else if (menuSelection == 3) {  // CO2-settings selected.
          if ((co2AlarmUpValue + co2AlarmIncDecStep) <= co2AlarmUpValueMax) {
            // When the new value doesn't conflict with the borders, save and display it.
            co2AlarmUpValue = co2AlarmUpValue + co2AlarmIncDecStep;
            firstAlarmUpperTHValueTextView.setText("" + co2AlarmUpValue);
          }
        }
        // Activate the alarm if necessary.
        checkAlarm();
      }
    });
  }*///TODO: remove


  /**
   * Returns the current EKG-value.
   *
   * @return current EKG value.
   */
  public int getEKGValue() {
    return ekgValue;
  }

  /**
   * Returns the current diastolic blood pressure-value.
   *
   * @return current diastolic blood pressure value.
   */
  public int getDiaBPValue() {
    return diaBloodPressure;
  }

  /**
   * Returns the current systolic blood pressure-value.
   *
   * @return current systolic blood pressure value.
   */
  public int getSysBPValue() {
    return sysBloodPressure;
  }

  /**
   * Returns the current O2-value.
   *
   * @return current O2 value.
   */
  public int getO2Value() {
    return o2Value;
  }

  /**
   * Returns the current CO2-value.
   *
   * @return current CO2 value.
   */
  public int getCO2Value() {
    return co2Value;
  }

  /**
   * Returns the current respiration-value.
   *
   * @return current respiration value.
   */
  public int getRespValue() {
    return respValue;
  }

  /**
   * Triggers a update of the GUI with a new event.
   *
   * @param jsonEvent - The new event as JSON string.
   */
  public void newEvent(String jsonEvent) {
    updateHandler.updateGui(jsonEvent);
  }

  /**
   * Triggers a change of the general EKG-curve-pattern and saves it. Start the
   * asystole alarm and stops the normal alarm, if necessary.
   *
   * @param p - New curve pattern.
   */
  public void changeEKGPattern(Event.HeartPattern p) {
    signalServer.changeHeartRhythm(p);
    heartPattern = p;
    if (p == Event.HeartPattern.ASYSTOLE) {
      soundHandler.playAsystoleAlarm(true);
      soundHandler.setEKGAlarm(false);
      soundHandler.setRRAlarm(false);
      soundHandler.setO2Alarm(false);
    } else {
      soundHandler.playAsystoleAlarm(false);
      checkAlarm();
      // Stop the parameter blinking if necessary.
      if (paramBlinkTimer != null) {
        paramBlinkTimer.cancel();
        paramBlinkActive = false;
      }
    }
  }

  /**
   * Triggers a change of the general O2-curve-pattern.
   *
   * @param p - New curve pattern.
   */
  public void changeO2Pattern(Event.O2Pattern p) {
    signalServer.changeO2pattern(p);
  }

  /**
   * Sets the time of the timer.
   *
   * @param timeStamp - New time of the timer.
   */
  public void setTimerValue(int timeStamp) {
    timeInSec = timeStamp;
  }

  /**
   * Sets if EKG-curve/parameter is active.
   *
   * @param active - Indicates if EKG is active.
   */
  public void setEKGActive(final boolean active) {
    Log.e("debug", "call of setEKGActive, set value to: " + Boolean.toString
        (active));
    ekgActive = active;
    // Schedule show/hide  of the curve and values.
    runOnUiThread(new Runnable() {
      public void run() {
        TextView ekgValueTextView = (TextView) findViewById(R.id.ekgValueTextView);
        if (active) {
          glActivity.ToogleLine(GLRenderer.LineType.Heart, true);
          ekgValueTextView.setText("" + ekgValue);
        } else {
          glActivity.ToogleLine(GLRenderer.LineType.Heart, false);
          ekgValueTextView.setText("- - -");
        }
      }
    });
  }

  /**
   * Sets if RR-curve/parameter is active.
   *
   * @param active - Indicates if RR is active.
   */
  public void setRRActive(final boolean active) {
    rrActive = active;
    // Schedule show/hide  of the curve and values.
    runOnUiThread(new Runnable() {
      public void run() {
        TextView ibpValueTextView = (TextView) findViewById(R.id.ibpValueTextView);
        if (active) {
          glActivity.ToogleLine(GLRenderer.LineType.Blood, true);
          ibpValueTextView.setText("" + sysBloodPressure + "/" + diaBloodPressure);
        } else {
          glActivity.ToogleLine(GLRenderer.LineType.Blood, false);
          ibpValueTextView.setText("- - / - -");
        }
      }
    });

  }

  /**
   * Sets if O2-curve/parameter is active.
   *
   * @param active - Indicates if O2 is active.
   */
  public void setO2Active(final boolean active) {
    o2Active = active;
    // Schedule show/hide  of the curve and values.
    runOnUiThread(new Runnable() {
      public void run() {
        TextView o2ValueTextView = (TextView) findViewById(R.id.o2ValueTextView);
        if (active) {
          glActivity.ToogleLine(GLRenderer.LineType.O2, true);
          o2ValueTextView.setText("" + o2Value);
        } else {
          glActivity.ToogleLine(GLRenderer.LineType.O2, false);
          o2ValueTextView.setText("- -");
        }
      }
    });
  }

  /**
   * Sets if CO2-curve/parameter is active.
   *
   * @param active - Indicates if CO2 is active.
   */
  public void setCO2Active(final boolean active) {
    co2Active = active;
    // Schedule show/hide  of the curve and values.
    runOnUiThread(new Runnable() {
      public void run() {
        TextView co2ValueTextView = (TextView) findViewById(R.id.co2ValueTextView);
        if (active) {
          glActivity.ToogleLine(GLRenderer.LineType.CO2, true);
          co2ValueTextView.setText("" + co2Value);
        } else {
          glActivity.ToogleLine(GLRenderer.LineType.CO2, false);
          co2ValueTextView.setText("- -");
        }
      }
    });
  }

  /**
   * Sets if Respiration-parameter is active.
   *
   * @param active - Indicates if CO2 is active.
   */
  public void setRespActive(final boolean active) {
    respActive = active;
    // Schedule show/hide  of the curve and values.
    runOnUiThread(new Runnable() {
      public void run() {
        TextView afValueTextView = (TextView) findViewById(R.id.afValueTextView);
        if (active) {
          afValueTextView.setText("" + respValue);
        } else {
          afValueTextView.setText("- -");
        }
      }
    });
  }

  /**
   * Sets if NIBP is active.
   *
   * @param active - Indicates if NIBP is active.
   */
  public void setNIBPActive(boolean active) {
    nibpActive = active;
  }

  /**
   * Increase the energy of the defibrilator.
   */
  public void defiEnergyUp(View view) {
    if (defiEnergy + 10 <= defiEnergyThr) {
      defiEnergy = ((defiEnergy + 10 == defiEnergyUpperBound)
          ? defiEnergyUpperBound : defiEnergy + 10);
    } else {
      defiEnergy = ((defiEnergy + 50 >= defiEnergyUpperBound)
          ? defiEnergyUpperBound : defiEnergy + 50);
    }
    TextView energy = (TextView) defiFragment.returnView().findViewById(R.id
        .defi_energy_textView);
    energy.setText(String.valueOf(defiEnergy) + " J");
  }

  /**
   * Decrease the energy of the defibrilator.
   */
  public void defiEnergyDown(View view) {
    if (defiEnergy - 10 <= defiEnergyThr) {
      defiEnergy = ((defiEnergy - 10 <= defiEnergyLowerBound)
          ? defiEnergyLowerBound : defiEnergy - 10);
    } else {
      defiEnergy = ((defiEnergy - 50 <= defiEnergyLowerBound)
          ? defiEnergyLowerBound : defiEnergy - 50);
    }
    TextView energy = (TextView) defiFragment.returnView().findViewById(R.id
        .defi_energy_textView);
    energy.setText(String.valueOf(defiEnergy) + " J");
  }

  /**
   * Charge the defibrilator
   */
  public void defiCharge(View view) {
    if (!defiCharged && !defiCharging) {
      soundHandler.playDefiCharge(this.getBaseContext());
      handler = new Handler();
      defiCharging = true;
      handler.postDelayed(new Runnable() {

        @Override
        public void run() {
          handler.removeCallbacks(this);
          if (defiCharging) {
            defiCharged = true;
            changeShockButton();
            soundHandler.playDefiReady(getBaseContext());
          }
          defiCharging = false;
        }
      }, 4000);
    }

		/*
      _defiTimerInterval = (int) (((double) _defi1kTime * ((double)defiEnergy / 1000)) / (double)_defiFrequencySteps);
	    _defiSoundIncrement = (int) (((double)_defi1kFrequency * ((double)(defiEnergy) / 1000)) / (double)_defiFrequencySteps);
		_defiTimer = new Timer();
		_defiTimer.scheduleAtFixedRate(new TimerTask() {
	        	public void run(){
	        		if (_defiSteps >= _defiFrequencySteps) {     			
	        			defiCharged = true;
	        			_defiSteps = 0;
	        			_defiSoundFrequency = 100;
	        			MonitorMainScreen.this.runOnUiThread(new Runnable() {
	        				public void run() {
	        					changeShockButton(true);
	        				}
	        			});
	        			this.cancel();
	        			
	        		} else {
	        			soundHandler.playFreqSound(_defiSoundFrequency,
	        					_defiTimerInterval);
	        			_defiSoundFrequency += _defiSoundIncrement;
	        			_defiSteps++;
	        		}        		
	    		}
        	}, 0, _defiTimerInterval);
        	*/
  }

  /**
   * Discharge the defibrilator
   */
  public void defiDischarge(View view) {
    defiCharged = false;
    defiCharging = false;
    changeShockButton();
    soundHandler.stopDefiReady(getBaseContext());
  }

  /**
   * Change the background of the shock button.
   */
  private void changeShockButton() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // TODO Auto-generated method stub
        ImageButton shockButton = (ImageButton)
            defiFragment.getView().findViewById(R.id.defiShockButton);
        if (defiCharged) {
          shockButton.setBackgroundResource(R.drawable.roundedbutton_green);
        } else {
          shockButton.setBackgroundResource(R.drawable.roundedbutton);
        }
      }
    });
  }

  /**
   * Make a shock.
   */
  public void shock(View view) {
    if (defiCharged) {
      signalServer.getShocked();
      defiCharged = false;
      changeShockButton();
      soundHandler.stopDefiReady(getBaseContext());
      sendControllerInfo(ControllerInfoType.DEFI_FIRED);
    }
  }

  public GLActivity getGlActivity() {
    return glActivity;
  }

  /**
   * load saved/default values and set fields
   */
  private void loadSavedPreferences() {
    // load thresolds and alarm-settings
    // dynamic set general alarm on/off value
    alarmActive = defaultSharedPreferences.getBoolean
        ("key_general_alarm_on", true);
    SharedPreferences.Editor editor = defaultSharedPreferences.edit();
    editor.putBoolean("key_general_alarm_on", alarmActive);
    Log.e("debug, Mian, loadAlarms", "load kgeneral alarmOn. "+Boolean
        .toString(alarmActive));
    Map<String, ?> preferenceMap = defaultSharedPreferences.getAll();

    for (Map.Entry<String, ?> entry : preferenceMap.entrySet()) {
      String entryKey = entry.getKey();
      if (entryKey.endsWith("_threshold")) {
        updateAlarmThreshold(defaultSharedPreferences.getInt(entryKey, 0), entryKey);
      } else if (entryKey.endsWith("_alarm")) {
        Log.e("debug, Mian, loadAlarms", "load key: "+entryKey+", value(def" +
            ".true): " +
            ""+Boolean.toString(defaultSharedPreferences.getBoolean(entryKey,
            true))+", value(def.false): "+Boolean.toString(defaultSharedPreferences.getBoolean(entryKey,
            false)));
        updateAlarmOnOff(defaultSharedPreferences.getBoolean(entryKey, true),
            entryKey);
      }
    }


   /* // initialize Textviews
    TextView ekgValueTextView =
        (TextView) this.findViewById(R.id.ekgValueTextView);
    TextView ibpValueTextView = (TextView) this.findViewById(R.id.ibpValueTextView);
    TextView nibpValueTextView = (TextView) this.findViewById(R.id.nibpValueTextView);
    TextView o2ValueTextView = (TextView) this.findViewById(R.id.o2ValueTextView);
    TextView co2ValueTextView = (TextView) this.findViewById(R.id.co2ValueTextView);
    TextView afValueTextView = (TextView) this.findViewById(R.id.afValueTextView);
*/
//TODO: load colors from shared preferences

   /* ekgValueTextView.setTextColor(Color.BLACK);
    ibpValueTextView.setTextColor(Color.BLACK);
    nibpValueTextView.setTextColor(Color.BLACK);
    o2ValueTextView.setTextColor(Color.BLACK);
    co2ValueTextView.setTextColor(Color.BLACK);
    afValueTextView.setTextColor(Color.BLACK);*/

    //TODO: set linecolors with values from sharedPreferences


    //initializeLineColors();
    // set backgroundcolor
    SettingsFragment.changeBackColor(defaultSharedPreferences.getString
        (getString(R.string.key_background_color), "white"), this);

  }

  private int[] loadLineColors() {
    int[] lineColorKeyIds = new int[]{
        R.string.key_ecg_color,
        R.string.key_rr_color,
        R.string.key_spo2_color,
        R.string.key_etco2_color
  };
    int[] colors = new int[4];
    for (int i = 0; i< colors.length; i++) {
      String key = getString(lineColorKeyIds[i]);
      String value = defaultSharedPreferences.getString
          (key, "black");
      colors[i] = getResources().getColor(SettingsFragment
          .getColorIdFromValue(value));
      SettingsFragment.changeLineColor(this,null,getResources(),key,value);
    }

    return colors;
  }

  private void initializeTextViews() {

  }

  public void updateAlarmThreshold(int value, String key) {
    if (key.equals(getString(R.string.key_ecg_lower_threshold))) {
      ekgAlarmLowValue = value;
    } else if (key.equals(getString(R.string.key_ecg_upper_threshold))) {
      ekgAlarmUpValue = value;
      Log.e("debug", "ecg upper thershold changed to " + value);
    } else if (key.equals(getString(R.string
        .key_rr_diastolic_lower_threshold))) {
      rrDiaAlarmLowValue = value;
    } else if (key.equals(getString(R.string
        .key_rr_diastolic_upper_threshold))) {
      rrDiaAlarmUpValue = value;
    } else if (key.equals(getString(R.string.key_rr_systolic_lower_threshold)
    )) {
      rrSysAlarmLowValue = value;
    } else if (key.equals(getString(R.string.key_rr_systolic_upper_threshold)
    )) {
      rrSysAlarmUpValue = value;
    } else if (key.equals(getString(R.string.key_spo2_threshold))) {
      o2AlarmLowValue = value;
    } else if (key.equals(getString(R.string.key_etco2_lower_threshold))) {
      co2AlarmLowValue = value;
    } else if (key.equals(getString(R.string.key_etco2_upper_threshold))) {
      co2AlarmUpValue = value;
    }
  }

  public void updateAlarmOnOff(boolean alarmOn, String key) {
    if (key.equals(getString(R.string.key_ecg_alarm))) {
      ekgAlarmOn = alarmOn;
      //findViewById(R.id.ekgAlarmImageView).setVisibility();
    } else if (key.equals(getString(R.string.key_rr_alarm))) {
      rrAlarmOn = alarmOn;
    } else if (key.equals(getString(R.string.key_etco2_alarm))) {
      co2AlarmOn = alarmOn;
    } else if (key.equals(getString(R.string.key_spo2_alarm))) {
      o2AlarmOn = alarmOn;
    }
    //triggerSound();
    setAlarmIcons();
  }

  /**
   * send JSon-typed info to the controller screen
   * @param infoType
   */
  private void sendControllerInfo(ControllerInfoType infoType) {
    String key = "";
    JSONObject infoObject = new JSONObject();
    try {switch(infoType) {
      case DEFI_FIRED:
        key = "defi_shock";
        infoObject.put(key, defiEnergy);
        break;
      case CHANGED_TO_DEFI_SCREEN:
        key = "defi_open";

infoObject.put(key, "opened");
        break;
      default:
        return;
    }
      EnterScreen.client.out(infoObject.toString());
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }


}
