
package monitor.pack;

/**
 * A signalserver, which provides patterns for heartrhythm,
 * blood pressure, O2 saturation and respiration.
 *
 * @author Jonas Reinmuth
 * @version 1.0
 */

public class Signalserver {
  // The size of the arrays in which the values of all curves are stored.
  private final int arraysize;
  // Event heartpattern which stores the current heartpattern.
  private Event.HeartPattern heartrhythm;
  // If the heart pattern gets changed by the controller, it is first stored in nextrhythm.
  // The pattern is finally changed if the end of the array with the previous pattern is reached.
  private Event.HeartPattern nextrhythm;
  // The array in which the values of the sine heart rhythm are stored.
  private double[] sinerhythm;
  // The array in which the values of the sine heart rhythm with pacemaker are stored.
  private double[] pacerhythm;
  // The array in which the values of the left bundle branch block rhythm are stored.
  private double[] lbbbrhythm;
  // The array in which the values of the stemi rhythm are stored.
  private double[] stemirhythm;
  // The array in which the values of the ventricular flutter rhythm are stored.
  private double[] ventflutterrhythm;
  // The array in which the values of the ventricular fibrillation rhythm are stored.
  private double[] ventfibrirhythm;
  // The array in which the values of the cpr scheme are stored.
  private double[] cprrhythm;
  // The array in which the values of the asystolic line are stored.
  private double[] asystolicrhythm;
  // The array in which the values the basic invasive bloodpressure curve are stored.
  // The values lie between 0 and 1 and are scaled in the getBloodPressure() method
  // to get the actual value.
  private double[] bloodpressure;
  // The array in which the values of the basic SPO2 curve are stored. To get the actual
  // value it is multiplied with the O2 max value in the getO2Value method.
  private double[] SPO2;
  // The array in which the values of the basic CO2 curve are stored. To get the actual
  // value it is multiplied with the CO2 max value in the getCO2Value method.
  private double[] CO2;

  //heartrhythm peaks and first peak-index. Needed to correct interpolation
  // errors
  private double peakSinerhythm;
  private int peakIndexSinerhythm;
  private double peakPacerhythm;
  private int peakIndexPacerhythm;
  private double peakLbbbrhythm;
  private int peakIndexLbbbrhythm;
  private double peakStemirhythm;
  private int peakIndexStemirhythm;
  private double peakVentflutterrhythm;
  private int peakIndexVentflutterrhythm;
  private double peakVentfibrirhythm;
  private int peakIndexVentfibrirhythm;
  private double peakCprrhythm;
  private int peakIndexCprrhythm;
  private double peakAsystolicrhythm;
  private int peakIndexAsystolicrhythm;
  //heartrhythm peaks and first peak-index. Needed to correct interpolation
  // errors
  private double negativPeakSinerhythm;
  private int negativPeakIndexSinerhythm;
  private double negativPeakPacerhythm;
  private int negativPeakIndexPacerhythm;
  private double negativPeakLbbbrhythm;
  private int negativPeakIndexLbbbrhythm;
  private double negativPeakStemirhythm;
  private int negativPeakIndexStemirhythm;
  private double negativPeakVentflutterrhythm;
  private int negativPeakIndexVentflutterrhythm;
  private double negativPeakVentfibrirhythm;
  private int negativPeakIndexVentfibrirhythm;
  private double negativPeakCprrhythm;
  private int negativPeakIndexCprrhythm;
  private double negativPeakAsystolicrhythm;
  private int negativPeakIndexAsystolicrhythm;
  // The counter in which the current position of the array is stored. To get the next
  // position, use the increment() method.
  private double index;
  // Flag which is set if the patient has coldfingers.
  private boolean coldfinger;
  // Will be used to store the previous position in the array to see if the array has been looped.
  private double oldindex;
  // The CO2 curve gets its own index counter, because it implicates the breathing of the patient
  // and breathing isn't connected to the heartrate.
  private double CO2index;
  // Pi
  private final double pi;
  // The basicheartrate is the heart rate which is achieved when the index counter is
  // incremented by 1. It is used to calculate how the value "index" has to be incremented
  // to achieve the actual heart rate.
  private final int basicheartrate;
  // The current heart rate.
  private int heartrate;
  // The current breathing rate.
  private int respirationrate;
  // Systolic blood pressure.
  private int maxbloodpressure;
  // Diastolic blood pressure.
  private int minbloodpressure;
  // Current O2 value.
  private int O2MaxValue;
  // Current CO2 value.
  private int CO2MaxValue;
  // Flag which is set when acoustic signal and heart blinking in the monitor object was triggered, so it won't
  // trigger in the same peak multiple times.
  private boolean acousticsignaltriggered;
  // Flag which is set when a O2 threshold value is reached to indicate a peak in the O2 curve.
  private boolean O2peak;
  // Flag which is set when pulse of the pacemaker was triggered. Used in the pacemaker rhythm.
  private boolean pacepulse;
  // A random number which is changed in every cycle of the absolute arrythmia rhythm. Inserts randomly QRS Peaks
  private double aapeak;
  // Stores the previous randomnumber of the absolute arrhytmia rhythm.
  private double aapreviouspeak;
  // Flag which is set to true if aapeak has been calculated.
  private boolean aapeakcalc;
  // A counter used in the avblock rhythm, counts every peak and gets reseted if a peak is skipped.
  private int avblockpeak;
  // The main screen.
  private MonitorMainScreen mms;

  // Constructor
  // Takes the monitor object which has openend the signalserver, so the signalserver can trigger blinking and sound in
  // the monitor.
  Signalserver(MonitorMainScreen m) {

    // Set monitor object.
    mms = m;
    // Initialize class variables.
    arraysize = 100;
    sinerhythm = new double[arraysize];
    pacerhythm = new double[arraysize];
    lbbbrhythm = new double[arraysize];
    stemirhythm = new double[arraysize];
    cprrhythm = new double[arraysize];
    asystolicrhythm = new double[arraysize];
    ventflutterrhythm = new double[arraysize];
    ventfibrirhythm = new double[arraysize];
    bloodpressure = new double[arraysize];
    SPO2 = new double[arraysize];
    CO2 = new double[arraysize];
    index = 0;
    oldindex = 0;
    CO2index = 0;
    pi = 3.14159265359;
    basicheartrate = 60;
    heartrate = 50;
    respirationrate = 20;
    maxbloodpressure = 160;
    minbloodpressure = 80;
    O2MaxValue = 99;
    CO2MaxValue = 40;
    acousticsignaltriggered = false;
    O2peak = false;
    pacepulse = false;
    aapeak = 1;
    aapreviouspeak = 1;
    aapeakcalc = false;
    avblockpeak = 0;
    heartrhythm = Event.HeartPattern.SINE;
    nextrhythm = Event.HeartPattern.SINE;
    coldfinger = false;
    // -------------------
    // CREATE SINE PATTERN
    // -------------------
    for (int i = 0; i <= 99; ++i) {
      sinerhythm[i] = 0;
    }
    for (int i = 1; i <= 10; ++i) {
      sinerhythm[i - 1 + 27] = 10 * (Math.sin(i * pi / 10));
    }
    for (int i = 1; i <= 2; ++i) {
      sinerhythm[i - 1 + 49] = -9 * (Math.sin(i * pi / 4));
    }
    for (int i = 1; i <= 12; ++i) {
      sinerhythm[i - 1 + 52] = sinerhythm[i - 1 + 51] + 9.363636;
    }
    sinerhythm[61] = 93;
    for (int i = 1; i <= 4; ++i) {
      sinerhythm[i - 1 + 62] = sinerhythm[i - 1 + 61] - 28.5;
    }
    for (int i = 1; i <= 2; ++i) {
      sinerhythm[i - 1 + 66] = -20 * (Math.sin((3 + i) * pi / 5));
    }
    sinerhythm[67] = -1.2;
    sinerhythm[68] = -1;
    for (int i = 1; i <= 18; ++i) {
      sinerhythm[i - 1 + 81] = 15 * (Math.sin(i * pi / 18));
    }
    // ------------------------
    // CREATE PACEMAKER PATTERN
    // ------------------------
    for (int i = 0; i <= 99; ++i) {
      pacerhythm[i] = 0;
    }
    for (int i = 1; i <= 10; ++i) {
      pacerhythm[i - 1 + 27] = 10 * (Math.sin(i * pi / 10));
    }
    for (int i = 1; i <= 2; ++i) {
      pacerhythm[i - 1 + 49] = -9 * (Math.sin(i * pi / 4));
    }
    for (int i = 1; i <= 12; ++i) {
      pacerhythm[i - 1 + 52] = pacerhythm[i - 1 + 51] + 9.363636;
    }
    pacerhythm[61] = 93;
    for (int i = 1; i <= 4; ++i) {
      pacerhythm[i - 1 + 62] = pacerhythm[i - 1 + 61] - 28.5;
    }
    for (int i = 1; i <= 2; ++i) {
      pacerhythm[i - 1 + 66] = -20 * (Math.sin((3 + i) * pi / 5));
    }
    pacerhythm[67] = -1.2;
    pacerhythm[68] = -1;
    for (int i = 1; i <= 18; ++i) {
      pacerhythm[i - 1 + 81] = 15 * (Math.sin(i * pi / 18));
    }
    pacerhythm[40] = 80;
    // ---------------------------------------
    // CREATE LEFT BUNDLE BRANCH BLOCK PATTERN
    // ---------------------------------------
    for (int i = 0; i <= 99; ++i) {
      lbbbrhythm[i] = 0;
    }
    for (int i = 1; i <= 10; ++i) {
      lbbbrhythm[i - 1 + 27] = 10 * (Math.sin(i * pi / 10));
    }
    for (int i = 1; i <= 3; ++i) {
      lbbbrhythm[i - 1 + 50] = lbbbrhythm[i - 1 + 49] + 26.67;
    }
    for (int i = 1; i <= 4; ++i) {
      lbbbrhythm[i - 1 + 53] = lbbbrhythm[i - 1 + 52] - 2.5;
    }
    for (int i = 1; i <= 6; ++i) {
      lbbbrhythm[i - 1 + 57] = lbbbrhythm[i - 1 + 56] + 3.83;
    }
    for (int i = 1; i <= 5; ++i) {
      lbbbrhythm[i - 1 + 63] = lbbbrhythm[i - 1 + 62] - 18.6;
    }
    lbbbrhythm[67] = -1.2;
    lbbbrhythm[68] = -1;
    for (int i = 1; i <= 21; ++i) {
      lbbbrhythm[i - 1 + 72] = -15 * (Math.sin(i * pi / 21));
    }
    for (int i = 1; i <= 6; ++i) {
      lbbbrhythm[i - 1 + 93] = 2 * (Math.sin(i * pi / 6));
    }
    // --------------------
    // CREATE STEMI PATTERN
    // --------------------
    for (int i = 0; i <= 99; ++i) {
      stemirhythm[i] = 0;
    }
    for (int i = 1; i <= 10; ++i) {
      stemirhythm[i - 1 + 27] = 10 * (Math.sin(i * pi / 10));
    }
    for (int i = 1; i <= 2; ++i) {
      stemirhythm[i - 1 + 49] = -9 * (Math.sin(i * pi / 4));
    }
    for (int i = 1; i <= 12; ++i) {
      stemirhythm[i - 1 + 52] = stemirhythm[i - 1 + 51] + 9.363636;
    }
    stemirhythm[61] = 93;
    for (int i = 1; i <= 2; ++i) {
      stemirhythm[i - 1 + 62] = stemirhythm[i - 1 + 61] - 28.5;
    }
    for (int i = 1; i <= 9; ++i) {
      stemirhythm[i - 1 + 64] = 36;
    }
    for (int i = 1; i <= 21; ++i) {
      stemirhythm[i - 1 + 72] = 15 * (Math.sin(i * pi / 21)) + 36;
    }
    for (int i = 1; i < 6; ++i) {
      stemirhythm[i - 1 + 93] = stemirhythm[i - 1 + 92] - 6;
    }
    // ----------------------------------
    // CREATE VENTRICULAR FLUTTER PATTERN
    // ----------------------------------
    for (int i = 0; i <= 99; ++i) {
      ventflutterrhythm[i] = Math.sin(2 * pi * i / 12.35) * 50 + 40;
    }
    // ---------------------------------------
    // CREATE VENTRICULAR FIBRILLATION PATTERN
    // ---------------------------------------
    for (int i = 0; i <= 99; ++i) {
      ventfibrirhythm[i] = Math.sin(2 * pi * i / 9.1) * 10 + 5;
    }
    // ------------------
    // CREATE CPR PATTERN
    // ------------------
    for (int i = 0; i <= 99; ++i) {
      cprrhythm[i] = Math.sin(2 * pi * i / 33) * 40 + 30;
    }
    // ---------------------
    // CREATE ASYSTOLIC LINE
    // ---------------------
    for (int i = 0; i <= 99; ++i) {
      asystolicrhythm[i] = Math.sin(2 * pi * i / 99) * 2;
    }
    // ----------------------------------
    // CREATE BASIC BLOODPRESSURE PATTERN
    // ----------------------------------
    for (int i = 1; i <= 100; ++i) {
      bloodpressure[i - 1] = 0;
    }
    for (int i = 1; i <= 48; ++i) {
      bloodpressure[i - 1 + 52] = Math.sin(i * pi / 48);
    }
    for (int i = 1; i <= 50; ++i) {
      if (i - 1 + 95 > 99) {
        bloodpressure[i - 1 + 95 - 100] = 0.5 * Math.sin((39 + i) * pi / 90);
      } else {
        bloodpressure[i - 1 + 95] = 0.5 * Math.sin((39 + i) * pi / 90);
      }
    }
    bloodpressure[95] = 0.38;
    bloodpressure[96] = 0.41;
    bloodpressure[97] = 0.43;
    bloodpressure[98] = 0.45;
    bloodpressure[99] = 0.47;
    // -------------------
    // CREATE SPO2 PATTERN
    // -------------------
    for (int i = 1; i <= 100; ++i) {
      SPO2[i - 1] = 0;
    }
    for (int i = 1; i <= 30; ++i) {
      SPO2[i - 1] = Math.sin(i * pi / 48);
    }
    for (int i = 1; i <= 70; ++i) {
      if (i >= 25 && i <= 33) {
        SPO2[i - 1 + 30] = 0.9 * (1 + 0.005 * i) * Math.exp(-0.4 * (i - 1) * 2 * pi / 72);
      } else {
        SPO2[i - 1 + 30] = 0.9 * Math.exp(-0.4 * (i - 1) * 2 * pi / 72);
      }
    }
    // set heartrhythm peaks and index of first peak
    setHeartRhythmPeaks();
    // ------------------
    // CREATE CO2 PATTERN
    // ------------------
    double maxCo2 = 0;
    double minCo2 = 0;
    for (int i = 1; i <= 100; ++i) {
      CO2[i - 1] = 0;
    }
    for (int i = 1; i <= 19; ++i) {
      double temp = i * 0.048;
      CO2[i - 1 + 44] = i * 0.048;
      if (temp > maxCo2) {
        maxCo2 = temp;
      }
      if (temp < minCo2) {
        minCo2 = temp;
      }

    }
    for (int i = 1; i <= 28; ++i) {
      double temp = 0.9 + (i * 0.003);
      CO2[i - 1 + 63] = 0.9 + (i * 0.003);
      if (temp > maxCo2) {
        maxCo2 = temp;
      }
      if (temp < minCo2) {
        minCo2 = temp;
      }
    }
    for (int i = 1; i <= 9; ++i) {
      double temp = 1 - (i * 1.0 / 9.0);
      CO2[i - 1 + 91] = 1 - (i * 1.0 / 9.0);
      if (temp > maxCo2) {
        maxCo2 = temp;
      }
      if (temp < minCo2) {
        minCo2 = temp;
      }
    }
    System.out.println("MinCO2 value: " + minCo2
        + "\nMaxCo2 value: " + maxCo2);


    CO2 = new double[]{0.000000, 0.000039, 0.000024, 0.006194, 0.023405, 0.053559, 0.098557, 0.160301, 0.240693, 0.339068, 0.446680, 0.553197, 0.648286, 0.721720, 0.769993, 0.800164, 0.820212, 0.836167, 0.849183, 0.859668, 0.868027, 0.874666, 0.879994, 0.884415, 0.888335, 0.892040, 0.895573, 0.898948, 0.902180, 0.905284, 0.908274, 0.911166, 0.913975, 0.916714, 0.919399, 0.922045, 0.924666, 0.927277, 0.929893, 0.932528, 0.935198, 0.937910, 0.940663, 0.943455, 0.946285, 0.949151, 0.952051, 0.954985, 0.957950, 0.960944, 0.963967, 0.967016, 0.970090, 0.973187, 0.976306, 0.979444, 0.982601, 0.985752, 0.988859, 0.991884, 0.994785, 0.997525, 1.000000, 0.992076, 0.944846, 0.854502, 0.736439, 0.606433, 0.480263, 0.371633, 0.282611, 0.211211, 0.155441, 0.113309, 0.082825, 0.061998, 0.048835, 0.041346, 0.037540, 0.035425, 0.033260, 0.030985, 0.028724, 0.026451, 0.024137, 0.021754, 0.019274, 0.016669, 0.013929, 0.011138, 0.008415, 0.005880, 0.003652, 0.001852, 0.000597, 0.000008, 0.000007, -0.000020, -0.000068, -0.000000};

  }

  private void setHeartRhythmPeaks() {
    double rhythms[][] = {
        sinerhythm,
        pacerhythm,
        lbbbrhythm,
        stemirhythm,
        cprrhythm,
        asystolicrhythm,
        ventflutterrhythm,
        ventfibrirhythm

    };
    double peaks[] = new double[rhythms.length];
    double negPeaks[] = new double[rhythms.length];
    int peakIndeces[] = new int[rhythms.length];
    int negPeakIndeces[] = new int[rhythms.length];
    // search for peak
    for (int i = 0; i < peaks.length; i++) {
      peaks[i] = 0.0;
      negPeaks[i] = 0.0;
      peakIndeces[i] = 0;
      negPeakIndeces[i] = 0;
      for (int j = 0; j < rhythms[i].length; j++) {
        double tempRhythmValue = rhythms[i][j];
        if (peaks[i] < tempRhythmValue) {
          peaks[i] = tempRhythmValue;
          peakIndeces[i] = j;
        }
        if (negPeaks[i] > tempRhythmValue) {
          negPeaks[i] = tempRhythmValue;
          negPeakIndeces[i] = j;
        }
      }
    }
    // set positive peaks
    int it = 0;
    peakSinerhythm = peaks[it++];
    peakPacerhythm = peaks[it++];
    peakLbbbrhythm = peaks[it++];
    peakStemirhythm = peaks[it++];
    peakVentflutterrhythm = peaks[it++];
    peakVentfibrirhythm = peaks[it++];
    peakCprrhythm = peaks[it++];
    peakAsystolicrhythm = peaks[it++];
    it = 0;
    peakIndexSinerhythm = peakIndeces[it++];
    peakIndexPacerhythm = peakIndeces[it++];
    peakIndexLbbbrhythm = peakIndeces[it++];
    peakIndexStemirhythm = peakIndeces[it++];
    peakIndexVentflutterrhythm = peakIndeces[it++];
    peakIndexVentfibrirhythm = peakIndeces[it++];
    peakIndexCprrhythm = peakIndeces[it++];
    peakIndexAsystolicrhythm = peakIndeces[it++];

    // set negative peaks
    it = 0;
    negativPeakSinerhythm = negPeaks[it++];
    negativPeakPacerhythm = negPeaks[it++];
    negativPeakLbbbrhythm = negPeaks[it++];
    negativPeakStemirhythm = negPeaks[it++];
    negativPeakVentflutterrhythm = negPeaks[it++];
    negativPeakVentfibrirhythm = negPeaks[it++];
    negativPeakCprrhythm = negPeaks[it++];
    negativPeakAsystolicrhythm = negPeaks[it++];
    it = 0;
    negativPeakIndexSinerhythm = negPeakIndeces[it++];
    negativPeakIndexPacerhythm = negPeakIndeces[it++];
    negativPeakIndexLbbbrhythm = negPeakIndeces[it++];
    negativPeakIndexStemirhythm = negPeakIndeces[it++];
    negativPeakIndexVentflutterrhythm = negPeakIndeces[it++];
    negativPeakIndexVentfibrirhythm = negPeakIndeces[it++];
    negativPeakIndexCprrhythm = negPeakIndeces[it++];
    negativPeakIndexAsystolicrhythm = negPeakIndeces[it++];
  }

  // Changes the heart rhythm curve to a new pattern.
  // If the current heart rhythm is Asystole the heart rhythm gets changed immediately.
  public void changeHeartRhythm(Event.HeartPattern rhythm) {
    nextrhythm = rhythm;
    if (heartrhythm == Event.HeartPattern.ASYSTOLE) {
      heartrhythm = rhythm;
      index = 0;
      CO2index = 0;
    }
  }

  // Sets the heart rate to a new value.
  public void changeHeartRate(int newValue) {
    heartrate = newValue;
  }

  // Sets new values for systolic and diastolic blood pressure.
  public void changeBloodPressure(int systolicBloodPressure, int diastolicBloodPressure) {
    maxbloodpressure = systolicBloodPressure;
    minbloodpressure = diastolicBloodPressure;
  }

  // Sets the O2pattern to normal oder coldfinger.
  public void changeO2pattern(Event.O2Pattern pattern) {
    if (pattern == Event.O2Pattern.COLDFINGER) {
      coldfinger = true;
    } else {
      coldfinger = false;
    }
  }

  // Sets the oxygen saturation to a new value.
  public void changeO2Value(int newValue) {
    O2MaxValue = newValue;
  }

  // Sets the respirationrate to a new value.
  public void changeRespirationRate(int newValue) {
    respirationrate = newValue;
  }

  // Sets the CO2 ejection to a new value
  public void changeCO2Value(int newValue) {
    CO2MaxValue = newValue;
  }

  // Increments the index counter depending on the current heart rate.
  // Closes the loop if the end of the array is reached.
  public void increment() {
    // Save the last used position in "oldindex"
    oldindex = index;
    double inc;
    // The heartrate determines how fast it is gone through the array. There also some rhythms/patterns in which the heartrate setting
    // has to be ignored because the heartrate is fixed. This is the case in CPR, Asystole and ventricular flutter or fibrillation.
    if (heartrhythm == Event.HeartPattern.CPR || heartrhythm == Event.HeartPattern.VENTFLUTTER || heartrhythm == Event.HeartPattern.VENTFIBRI) {
      inc = 1;
    } else if (heartrhythm == Event.HeartPattern.ASYSTOLE) {
      inc = 0.3;
    } else
    // Calculate how the counter has to be incremented to achieve the current heartrate.
    {
      inc = ((double) heartrate) / basicheartrate;
    }
    // Increment the index counter.
    index += inc;
    // Calculate how the counter has to be incremented to achieve the current respiration rate.
    inc = ((double) respirationrate) / basicheartrate;
    // Increment the index counter for CO2 ejection.
    CO2index += inc;
    // If the index counter reached the end of the array, set it back to the beginning of the array.
    // Also apply the next pattern.
    if (index > (arraysize - 1)) {
      index = index - (arraysize - 1);
      heartrhythm = nextrhythm;
      // In the AV Block rhythm every fourth peak is skipped. The counter therefore is counted up here if the end
      // of the array is reached.
      ++avblockpeak;
      if (avblockpeak > 3) {
        avblockpeak = 0;
      }
      // Call randomvariationfunctions in monitor for heartrate, blood pressure, O2 and CO2 curve.
      // Skip if pattern is asystole, ventflutter or fibrillation because the value in the monitor is 0 for these patterns.
      if (heartrhythm != Event.HeartPattern.ASYSTOLE && heartrhythm != Event.HeartPattern.VENTFLUTTER && heartrhythm != Event.HeartPattern.VENTFIBRI) {
        mms.rrVariation();
        mms.co2RespVariation();
        mms.o2Variation();
        mms.ekgVariation();
      }
    }
    // If the CO2 index counter reached the end of the array, set it back to the beginning of the array.
    if (CO2index > (arraysize - 1)) {
      CO2index = CO2index - (arraysize - 1);
    }
  }

  // Calculates the output value of a heart pattern. Uses a heart pattern and a position of the array as input.
  // Interpolates the array if the position is a decimal.
  private double calcHeartRateValue(double[] pattern, double position, int
      peakIndex, double peakValue, int negPeakIndex, double negPeakValue) {
    double value = 0;
    // A scaling factor to scale the patterns to the optimal height.
    double scale = 1.4;
    // Interpolate if position is a decimal.
    if (Math.floor(position) != Math.ceil(position)) {
      // check if there is a peak between the two interpolation values.
      // Dependent on the sampling rate use the nearest peak value (or not)
      double samplingStepSize = ((double) heartrate) / basicheartrate;
      double distanceToLowerPeakIndex = position - (double)peakIndex;
      double distanceToHigherPeakIndex = (double)peakIndex - position;
      double distanceToLowerNegPeakIndex = position - (double)negPeakIndex;
      double distanceToHigherNegPeakIndex = (double)negPeakIndex - position;
      if((peakIndex != 0) && ((distanceToLowerPeakIndex <= samplingStepSize &&
          distanceToLowerPeakIndex > 0)
          || (distanceToHigherPeakIndex < samplingStepSize &&
          distanceToHigherPeakIndex > 0))){

        value = peakValue;
      } else if((negPeakIndex != 0) && ((distanceToLowerNegPeakIndex <=
          samplingStepSize &&
          distanceToLowerNegPeakIndex > 0)
          || (distanceToHigherNegPeakIndex < samplingStepSize &&
          distanceToHigherNegPeakIndex > 0))) {
        value =negPeakValue;
      } else {
        value = pattern[(int) Math.floor(position)] + (pattern[(int) Math.ceil(position)] - pattern[(int) Math.floor(position)]) / (Math.ceil(position) - Math.floor(position)) * (position - Math.floor(position)) * scale;
      }
    }
    // Otherwise just read out the value of the heart rate array if index is a natural number.
    else {
      value = pattern[(int) position] * scale;
    }
    //Log.e("DEBUG SignalServer", "used heartvalue: "+value);
    return value;
  }

  // Calculates the output value for the bloodpressure curve. Uses a position as input. Interpolates the array if
  // the position is a decimal.
  private double calcBloodPressureValue(double position) {
    double value = 0;
    // Interpolate if index is a double.
    if (Math.floor(position) != Math.ceil(position)) {
      value = (bloodpressure[(int) Math.floor(position)] + (bloodpressure[(int) Math.ceil(position)] - bloodpressure[(int) Math.floor(position)]) / (Math.ceil(position) - Math.floor(position)) * (position - Math.floor(position))) * 0.55 * (maxbloodpressure - minbloodpressure);
    }
    // Otherwise just read out the value of the blood pressure array if index is a natural number.
    else {
      value = bloodpressure[(int) position] * 0.55 * (maxbloodpressure - minbloodpressure);
    }
    return value;
  }

  // Calculates the output value for the oxygen saturation curve. Uses a position as input. Interpolates the array if
  // the position is a decimal.
  private double calcSPO2Value(double position) {
    double value = 0;
    // Interpolate if index is a double.
    if (Math.floor(position) != Math.ceil(position)) {
      value = (SPO2[(int) Math.floor(position)] + (SPO2[(int) Math.ceil(position)] - SPO2[(int) Math.floor(position)]) / (Math.ceil(position) - Math.floor(position)) * (position - Math.floor(position))) * O2MaxValue;
    }
    // Otherwise just read out the value of the SPO2 array if index is a natural number.
    else {
      value = SPO2[(int) position] * O2MaxValue;
    }
    return value;
  }

  // Triggers acoustic signal and heart blinking in monitor object if a peak value is exceeded. Uses the current heartratevalue
  // and a threshold value which triggers the sound and blinking once exceeded.
  private void triggerBeepBlink(double heartratevalue, int threshold) {
    if (acousticsignaltriggered == false) {
      if (heartratevalue >= threshold) {
        // blink and beep -> call in the monitor object
        mms.heartPeak();
        // Indicate it has been blinked and beeped.
        acousticsignaltriggered = true;
      }
    } else {
      // Indicate the peak has been passed and it can be blinked and beeped again.
      if (heartratevalue <= threshold) {
        acousticsignaltriggered = false;
      }
    }
  }

  // Calls the O2peak function in the monitor object which triggers a blink and a sound if there is no
  // EKG curve. Uses the current O2saturation value and a threshold value that has to be exceeded for the call.
  private void triggerO2peak(double O2Value, int threshold) {
    // Call function in monitor if it hasn't been already triggered in this peak and a threshold is exceeded.
    if (O2peak == false) {
      if (O2Value >= threshold) {
        mms.o2Peak();
        // Indicate the function has been called.
        O2peak = true;
      }
    } else {
      // Reset flag for the next peak.
      if (O2Value <= threshold) {
        O2peak = false;
      }
    }
  }

  // Returns the current heart rate value in dependence to the index counter. Considers the current rhythm and
  // calls a method in the Monitor class to generate the relevant acoustic signal.
  public double getHeartRateValue() {
    // The Value in the current position of the array. Gets calculated in the method and returned.
    double heartratevalue;
    // Threshold value at which acoustic signal and heart blinking is triggered when exceeded.
    int threshold = 50;
    // ------ CALCULATION FOR HEARTRATEVALUE WITH sinerhythm ------
    if (heartrhythm == Event.HeartPattern.SINE) {
      heartratevalue = calcHeartRateValue(sinerhythm, index,
          peakIndexSinerhythm, peakSinerhythm, negativPeakIndexSinerhythm,
          negativPeakSinerhythm);
      // Trigger acoustic signal and heart blinking in monitor object.
      triggerBeepBlink(heartratevalue, threshold);
    }
    // ------ CALCULATION FOR HEARTRATEVALUE WITH ABSOLUTE ARRYTHMIA (also LBBBAA)------
    else if (heartrhythm == Event.HeartPattern.ARRYTHMIC || heartrhythm == Event.HeartPattern.LEFTBLOCKAA) {
      // If the array has been looped set aapeakcalc to false so aapeak gets recalculated.
      if (index < oldindex) {
        aapeakcalc = false;
      }
      // Get the random threshold for skipping or inserting a QRS peak if not already calculated.
      if (aapeakcalc == false) {
        // Save previous random number.
        aapreviouspeak = aapeak;
        // Get random number.
        aapeak = Math.random();
        // Indicate random number has been calculated.
        aapeakcalc = true;
      }
      // Between 48 and 66 the QRS Peak is stored in the "sinerhythm" array which has the same shape in the
      // absolute arrhythmia rhythm. This peak will be randomly inserted in the AA rhythm.
      if (index <= 20) {
        // Randomly create QRS peak in the beginning of the array.
        if (aapeak < 0.5) {
          double newindex = index + 48;
          // Differentiate between AA and LBBBAA.
          if (heartrhythm == Event.HeartPattern.ARRYTHMIC)
          // Absolute Arrythmia with sine pattern.
          {
            heartratevalue = calcHeartRateValue(sinerhythm, newindex,
                peakIndexSinerhythm, peakSinerhythm,
                negativPeakIndexSinerhythm, negativPeakSinerhythm);
          } else
          // Absolute Arrythmia with left bundle branch block pattern.
          {
            heartratevalue = calcHeartRateValue(lbbbrhythm, newindex,
                peakIndexLbbbrhythm, peakLbbbrhythm,
                negativPeakIndexLbbbrhythm, negativPeakLbbbrhythm);
          }
        }
        // Or simulate atrial fibrillation with random values.
        else {
          heartratevalue = (Math.random() * 7 - 3.5);
        }
      } else if (index >= 48 && index <= 68) {
        // Create atrial fibrillation in the middle of the array.
        if (aapeak < 0.5) {
          heartratevalue = (Math.random() * 7 - 3.5);
        }
        // Or create QRS peak in the middle of the array.
        else {
          // Differentiate between AA and LBBBAA.
          if (heartrhythm == Event.HeartPattern.ARRYTHMIC)
          // Absolute Arrythmia with sine pattern.
          {
            heartratevalue = calcHeartRateValue(sinerhythm, index,
                peakIndexSinerhythm, peakSinerhythm,
                negativPeakIndexSinerhythm, negativPeakSinerhythm);
          } else
          // Absolute Arrythmia with left bundle branch block pattern.
          {
            heartratevalue = calcHeartRateValue(lbbbrhythm, index,
                peakIndexLbbbrhythm, peakLbbbrhythm,
                negativPeakIndexLbbbrhythm, negativPeakLbbbrhythm);
          }
        }
      }
      // Between the peaks always fibrillate with random values.
      else {
        heartratevalue = (Math.random() * 7 - 3.5);
      }
      // Trigger acoustic signal and heart blinking in monitor object.
      triggerBeepBlink(heartratevalue, threshold);
    }
    // ------ CALCULATION FOR HEARTRATEVALUE WITH AVBLOCK RHYTHM ------
    else if (heartrhythm == Event.HeartPattern.AVBLOCK) {
      // The fourth peak gets skipped
      if (avblockpeak == 3) {
        // QRS Peak starts at index 49 in the array. After this point we just insert zeros.
        if (index > 48) {
          heartratevalue = 0;
        }
        // We have to make sure the T zone of the pattern doesn't get cut if we shift the pattern to the right,
        // so we insert the end of the last pattern in the beginning of the current one.
        else if (index < ((avblockpeak - 1) * 6)) {
          heartratevalue = calcHeartRateValue(sinerhythm, index + 99 - (
              (avblockpeak - 1) * 6),peakIndexSinerhythm, peakSinerhythm,
              negativPeakIndexSinerhythm, negativPeakSinerhythm);
        } else {
          heartratevalue = calcHeartRateValue(sinerhythm, index,
              peakIndexSinerhythm, peakSinerhythm, negativPeakIndexSinerhythm, negativPeakSinerhythm);
        }
      }
      // avblockpeak counts up from zero to three. Every time the array is looped, the QRS Peak gets delayed more.
      else {
        if (index > 48) {
          heartratevalue = calcHeartRateValue(sinerhythm, index -
              (avblockpeak * 6),peakIndexSinerhythm, peakSinerhythm, negativPeakIndexSinerhythm, negativPeakSinerhythm);
        }
        // We have to make sure the T zone of the pattern doesn't get cut if we shift the pattern to the right,
        // so we insert the end of the last pattern in the beginning of the current one.
        else if (index < ((avblockpeak - 1) * 6)) {
          heartratevalue = calcHeartRateValue(sinerhythm, index + 99 - (
              (avblockpeak - 1) * 6),peakIndexSinerhythm, peakSinerhythm, negativPeakIndexSinerhythm, negativPeakSinerhythm);
        } else {
          heartratevalue = calcHeartRateValue(sinerhythm, index,
              peakIndexSinerhythm, peakSinerhythm, negativPeakIndexSinerhythm, negativPeakSinerhythm);
        }
      }
      // Trigger acoustic signal and heart blinking in monitor object.
      triggerBeepBlink(heartratevalue, threshold);
    }
    // ------ CALCULATION FOR HEARTRATEVALUE WITH LEFT BUNDLE BRANCH BLOCK RHYTHM ------
    else if (heartrhythm == Event.HeartPattern.LEFTBLOCK) {
      heartratevalue = calcHeartRateValue(lbbbrhythm, index,
          peakIndexLbbbrhythm, peakLbbbrhythm, negativPeakIndexLbbbrhythm,
          negativPeakLbbbrhythm);
      // Trigger acoustic signal and heart blinking in monitor object.
      triggerBeepBlink(heartratevalue, threshold);
    }
    // ------ CALCULATION FOR HEARTRATEVALUE WITH STEMI RHYTHM ------
    else if (heartrhythm == Event.HeartPattern.STEMI) {
      heartratevalue = calcHeartRateValue(stemirhythm, index,
          peakIndexStemirhythm, peakStemirhythm, negativPeakIndexStemirhythm,
          negativPeakStemirhythm);
      // Trigger acoustic signal and heart blinking in monitor object.
      triggerBeepBlink(heartratevalue, threshold + 20);
    }
    // ------ CALCULATION FOR HEARTRATEVALUE WITH PACEMAKER RHYTHM ------
    else if (heartrhythm == Event.HeartPattern.PACE) {
      // A scaling factor to scale the patterns to the optimal height.
      heartratevalue = calcHeartRateValue(pacerhythm, index,
          peakIndexPacerhythm, peakPacerhythm, negativPeakIndexPacerhythm,
          negativPeakPacerhythm);
      // Adjust the peak of the pacemaker, so it looks more similar in every loop.
      if (pacepulse == false) {
        if (heartratevalue >= 40) {
          heartratevalue = 98;
          pacepulse = true;
        }
      } else {
        if (index <= 10) {
          pacepulse = false;
        }
      }
      // Trigger acoustic signal and heart blinking in monitor object.
      triggerBeepBlink(heartratevalue, threshold);
    }
    // ------ CALCULATION FOR HEARTRATEVALUE WITH VENTRICULAR FLUTTER RHYTHM ------
    else if (heartrhythm == Event.HeartPattern.VENTFLUTTER) {
      heartratevalue = ventflutterrhythm[(int) index];
    }
    // ------ CALCULATION FOR HEARTRATEVALUE WITH VENTRICULAR FIBRILLATION RHYTHM ------
    else if (heartrhythm == Event.HeartPattern.VENTFIBRI) {
      heartratevalue = ventfibrirhythm[(int) index] + (Math.random() * 6 - 3);
    }
    // ------ CALCULATION FOR HEARTRATEVALUE WITH CPR ------
    else if (heartrhythm == Event.HeartPattern.CPR) {
      heartratevalue = cprrhythm[(int) index];
    }
    // ------ CALCULATION FOR HEARTRATEVALUE WITH ASYSTOLE ------
    else if (heartrhythm == Event.HeartPattern.ASYSTOLE) {
      heartratevalue = asystolicrhythm[(int) index];
    } else {
      heartratevalue = 0;
    }
    return heartratevalue;
  }

  // Returns the current blood pressure value in dependence to the index counter.
  public double getBloodPressureValue() {
    double value;
    if (heartrhythm == Event.HeartPattern.ARRYTHMIC || heartrhythm == Event.HeartPattern.LEFTBLOCKAA) {
      // For the bloodpressure curve in aa rhythm we have to consider if the previous heart beat has been taken or skipped.
      // With the previous and the actual random number ("aapreviouspeak, aapeak") there are four possibilities.
      if (aapeak < 0.5) {
        if (aapreviouspeak >= 0.5 && index < 6) {
          value = calcBloodPressureValue(index);
        } else {
          double newindex = index + 52;
          if (newindex > (arraysize - 1)) {
            newindex = newindex - (arraysize - 1);
          }
          value = calcBloodPressureValue(newindex);
        }
      } else if (aapeak >= 0.5 && aapreviouspeak < 0.5 && index < 50) {
        value = 0;
      } else {
        value = calcBloodPressureValue(index);
      }
    }
    // For AVBlock rhythm, every fourth amplitude has to be skipped.
    else if (heartrhythm == Event.HeartPattern.AVBLOCK) {
      // The amplitude starts at index 52 so this value has to be zero.
      if (avblockpeak == 3 && index > 51) {
        value = 0;
      }
      // The end of the amplitude is in the beginning of the array, this also has to be zero.
      else if (avblockpeak == 0 && index < 50) {
        value = 0;
      }
      // For all the other amplitudes the standard calculation we used in the sine rhythm works just fine.
      else {
        value = calcBloodPressureValue(index);
      }
    }
    // If the heartrhythm is ventflutter nearly no blood gets pumped at all. The shrinked
    // ventflutter heartrate pattern itself can be used to simulate the bloodemission.
    else if (heartrhythm == Event.HeartPattern.VENTFLUTTER) {
      value = Math.abs((calcHeartRateValue(ventflutterrhythm, index,
          peakIndexVentflutterrhythm, peakVentflutterrhythm,
          negativPeakIndexVentflutterrhythm, negativPeakIndexVentflutterrhythm) - 40) /
          35);
    }
    // If the heartrhythm is ventfibri nearly no blood gets pumped at all. The shrinked
    // ventfibri heartrate pattern itself can be used to simulate the bloodemission.
    else if (heartrhythm == Event.HeartPattern.VENTFIBRI) {
      value = Math.abs((calcHeartRateValue(ventfibrirhythm, index,
          peakIndexVentfibrirhythm, peakVentfibrirhythm,
          negativPeakIndexVentfibrirhythm, negativPeakVentfibrirhythm) -
          25) / 25);
    }
    // In case of a reanimation the blood gets pumped with the pushing frequenzy of the reanimator.
    else if (heartrhythm == Event.HeartPattern.CPR) {
      value = Math.abs((calcHeartRateValue(cprrhythm, index,
          peakIndexCprrhythm, peakCprrhythm, negativPeakIndexCprrhythm, negativPeakCprrhythm)) /
          10);
    }
    // If there is no heart contraction no blood gets pumped through the body and no BP curve is visible.
    else if (heartrhythm == Event.HeartPattern.ASYSTOLE) {
      value = 0;
    }
    // Else show the normal bloodpressure curve.
    else {
      value = calcBloodPressureValue(index);
    }
    // If maxbloodpressure is under 50 make curve even flatter.
    if ((maxbloodpressure) <= 50) {
      value = value / 2;
    }
    // Return the value of the curve shifted by diastolic bloodpressure.
    return value + minbloodpressure * 0.55;
  }

  // Returns the current O2 saturation value in dependence of the index counter.
  // Also calls the O2 peak function.
  public double getO2Value() {
    double value;
    int threshold = O2MaxValue - O2MaxValue / 10;
    // In case of ventricular flutter or fibrillation and also in asystole the SPO2 sensor
    // doesn't messure a oxygen saturation so the value stays on zero.
    if (heartrhythm == Event.HeartPattern.VENTFLUTTER || heartrhythm == Event.HeartPattern.VENTFIBRI || heartrhythm == Event.HeartPattern.ASYSTOLE) {
      value = 0;
    }
    // In case of a reanimation the blood gets pumped with the pushing frequenzy of the reanimator. The
    // oxygen saturation is low.
    else if (heartrhythm == Event.HeartPattern.CPR) {
      value = Math.abs((calcHeartRateValue(cprrhythm, index,
          peakIndexCprrhythm, peakCprrhythm, negativPeakIndexCprrhythm, negativPeakCprrhythm)
      ) / 25);
      // Trigger blink and random variation.
      triggerO2peak(value, threshold);
    }
    // For the oxygen saturation curve in aa rhythm we have to consider if the previous heart beat has been taken or skipped.
    // With the previous and the actual random number ("aapreviouspeak, aapeak") there are four possibilities.
    else if (heartrhythm == Event.HeartPattern.ARRYTHMIC || heartrhythm == Event.HeartPattern.LEFTBLOCKAA) {
      if (aapreviouspeak >= 0.5) {
        // Case 1: Two pressure peaks in succession. One usual for index <= 66 and one for index > 66
        if (aapeak < 0.5 && index > 66) {
          // Newindex shifts the subsiding part of the pattern to the beginning of the array.
          double newindex = index - 63;
          value = calcSPO2Value(newindex);
        }
        // Case 2: One standard peak -> return usual oxygen value as used in sine pattern.
        else {
          value = calcSPO2Value(index);
        }
      } else {
        // Case 3: Skip a standard peak. Return one value for index > 63.
        if (aapeak >= 0.5 && index > 63) {
          value = 8;
        }
        // Case 4: A shifted peak before the usual peak in the standard pattern.
        else {
          // Newindex shifts the subsiding part of the pattern to the beginning of the array.
          double newindex = index - 63;
          if (newindex < 0) {
            newindex = newindex + (arraysize - 1);
          }
          value = calcSPO2Value(newindex);
        }
      }
      // Trigger Blink and randomvariation.
      triggerO2peak(value, threshold);
    }
    // If the heartrhythm is avblock, every fourth heart beat is skipped so there is also no oxygen saturated
    // blood that gets pumped through the body which can be measured by the sensor.
    else if (heartrhythm == Event.HeartPattern.AVBLOCK) {
      // The fourth heart peak which is skipped is at avblock == 3, the oxygen sensor measures this one array
      // later so it is at avblock == 0.
      if (avblockpeak == 0) {
        value = 8;
      }
      // All other oxygen saturation peaks are plotted as normal.
      else {
        value = calcSPO2Value(index);
      }
      // Trigger Blink and randomvariation.
      triggerO2peak(value, threshold);
    }
    // In case of other heartrhythms the patient has normal O2 saturation and we can read out the array normally.
    else {
      value = calcSPO2Value(index);
      // Trigger Blink and randomvariation.
      triggerO2peak(value, threshold);
    }
    // If the patient has a low bloodpressure the O2 saturation gets shrinked.
    if (maxbloodpressure < 70 || coldfinger == true) {
      value = value / 20;
    }
    // Return the value.
    return value;
  }

  // Returns the current CO2 value in dependence of the CO2index counter.
  public double getCO2Value() {
    double value;
    // If there's no respiration the sensor measures no CO2 value
    if (respirationrate == 0) {
      value = 0;
    } else {
      // Interpolate if index is a double.
      if (Math.floor(CO2index) != Math.ceil(CO2index)) {
        value = (CO2[(int) Math.floor(CO2index)] + (CO2[(int) Math.ceil(CO2index)] - CO2[(int) Math.floor(CO2index)]) / (Math.ceil(CO2index) - Math.floor(CO2index)) * (CO2index - Math.floor(CO2index))) * CO2MaxValue * 1.1;
        return value;
      }
      // Otherwise just read out the value of the CO2 array if index is a natural number.
      else {
        value = CO2[(int) CO2index] * CO2MaxValue * 1.1;
      }
    }
    return value;
  }

  // Gets called when the defibrillator is used. Sets the pattern to asystole for a short period of time.
  // If the asystole pattern is passed through the heartrhythm before the shock is applied.
  public void getShocked() {
    index = 0;
    CO2index = 0;
    nextrhythm = heartrhythm;
    heartrhythm = Event.HeartPattern.ASYSTOLE;
  }
}
