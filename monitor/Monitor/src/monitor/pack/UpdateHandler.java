/*
 * Copyright: Universität Freiburg, 2015
 * Author: Marc Pfeifer <pfeiferm@tf.uni-freiburg.de>
 */

package monitor.pack;

import java.util.Timer;
import java.util.TimerTask;


/**
 * A class which handles the updating of the parameters on the patient monitor
 * when a new event from the controller arrives.
 */
public class UpdateHandler {
  // FINAL MEMBERS:
  // De(Activate) the debug-messages.
  private final boolean DEBUG = false;
  // The update-interval-time in seconds. For REALLY exact timing use values 0.1, 0.2, 0.5, 1.
  // Otherwise the number of increment-steps will maybe rounded.
  private final float updateInterval = 0.5f;

  // MEMBERS:
  private MonitorMainScreen mms;  // The main GUI-Thread.
  private Timer timer;  // The schedule-timer.
  // The scheduled time divided by the updateInterval -> Number of increment-steps.
  private float divFactor;
  // The parameter values before the begin of an scheduled update.
  private int startEKGValue;
  private int startDiaBPValue;
  private int startSysBPValue;
  private int starto2Value;
  private int startco2Value;
  private int startRespValue;
  // Counter for the update-steps.
  private int updateCount;

  /**
   * Constructor which sets the MonitorMainInstance.
   */
  public UpdateHandler(MonitorMainScreen m) {
    this.mms = m;
  }

  /**
   * Updates the GUI with the new values from the event either immediately or
   * step by step if a schedule time is set.
   *
   * @param jsonEvent - The new event as JSON string.
   */
  public void updateGui(String jsonEvent) {
    // Cancel an old scheduled task if necessary.
    if (timer != null) timer.cancel();
    // Try to parse the given JSON string into an Event-object.
    Event e;
    try {
      e = Event.fromJsonEvent(jsonEvent);
    } catch (Exception ex) {
      System.err.println(ex);
      return;
    }
    // Synchronize the timer.
    if (e.timerState == Event.TimerState.START) {
      mms.startStopTimer(true);
    } else if (e.timerState == Event.TimerState.STOP || e.timerState == Event.TimerState.PAUSE) {
      mms.startStopTimer(false);
    } else if (e.timerState == Event.TimerState.RESET) {
      mms.resetTimer();
    }
    if (e.syncTimer) {
      mms.setTimerValue(e.timeStamp);
    }
    // Update the EKG- and the O2-curve-pattern.
    mms.changeEKGPattern(e.heartPattern);
    mms.changeO2Pattern(e.oxyPattern);
    // Update the active-states.
    mms.setEKGActive(e.heartOn);
    mms.setRRActive(e.bpOn);
    mms.setO2Active(e.oxyOn);
    mms.setCO2Active(e.carbOn);
    mms.setNIBPActive(e.cuffOn);
    mms.setRespActive(e.respOn);
    // When there is a schedule-time:
    if (e.time > 0) {
      // Get the current values for EKG, blood pressure, O2 and CO2.
      updateValues();
      // Calculate the division-factor.
      divFactor = Math.round(((float) e.time) / updateInterval);
      // Calculate the increment/decrement steps-size for each parameter.
      final float heartRateInc = (float) (e.heartRateTo - startEKGValue) / divFactor;
      final float diaBloodPressureInc = (float) (e.bloodPressureDias - startDiaBPValue) / divFactor;
      final float sysBloodPressureInc = (float) (e.bloodPressureSys - startSysBPValue) / divFactor;
      final float o2Inc = (float) (e.oxygenTo - starto2Value) / divFactor;
      final float co2Inc = (float) (e.carbTo - startco2Value) / divFactor;
      final float respInc = (float) (e.respRate - startRespValue) / divFactor;
      if (DEBUG) {
        System.out.println("Start values:");
        System.out.println("EKG: " + startEKGValue
            + ", DiaBP: " + startDiaBPValue
            + ", SysBP: " + startSysBPValue
            + ", O2: " + starto2Value
            + ", CO2: " + startco2Value
            + ", Resp: " + startRespValue);
        System.out.println("Number of increment steps: " + divFactor);
        System.out.println("Increment-Step-Sizes:");
        System.out.println("EKGInc: " + heartRateInc
            + ", DiaBPInc: " + diaBloodPressureInc
            + ", SysBPInc: " + sysBloodPressureInc
            + ", O2Inc: " + o2Inc
            + ", CO2Inc: " + co2Inc
            + ", RespInc: " + respInc);
      }
      updateCount = 0;
      // Create a scheduled task which increases/decreases the parameters
      // each time step until the given final value is reached.
      timer = new Timer();
      timer.scheduleAtFixedRate(new TimerTask() {
        public void run() {
          updateCount++;  // Count the steps.
          mms.setEKG(startEKGValue + (int) (heartRateInc * (float) updateCount));
          mms.setIBP(startDiaBPValue + (int) (diaBloodPressureInc * (float) updateCount),
              startSysBPValue + (int) (sysBloodPressureInc * (float) updateCount));
          mms.setO2(starto2Value + (int) (o2Inc * (float) updateCount));
          mms.setCO2(startco2Value + (int) (co2Inc * (float) updateCount));
          mms.setResp(startRespValue + (int) (respInc * (float) updateCount));
          if (updateCount >= divFactor) {
            this.cancel();
          }
        }
      }, (int) (updateInterval * 1000), (int) (updateInterval * 1000));
      // When there is no schedule-time, just set the values directly.
    } else {
      mms.setEKG(e.heartRateTo);
      mms.setIBP(e.bloodPressureDias, e.bloodPressureSys);
      mms.setO2(e.oxygenTo);
      mms.setCO2(e.carbTo);
      mms.setResp(e.respRate);
    }
  }


  /**
   * Updates the GUI with the new values from the event either immediately or
   * step by step if a schedule time is set.
   *
   * @param e - The new event.
   */
  public void updateGui(final Event e) {
    // Cancel an old scheduled task if necessary.
    if (timer != null) timer.cancel();
    // Synchronize the timer.
    if (e.timerState == Event.TimerState.START) {
      mms.startStopTimer(true);
    } else if (e.timerState == Event.TimerState.STOP || e.timerState == Event.TimerState.PAUSE) {
      mms.startStopTimer(false);
    } else if (e.timerState == Event.TimerState.RESET) {
      mms.resetTimer();
    }
    if (e.syncTimer) {
      mms.setTimerValue(e.timeStamp);
    }
    // Update the EKG- and the O2-curve-pattern.
    mms.changeEKGPattern(e.heartPattern);
    mms.changeO2Pattern(e.oxyPattern);
    // Update the active-states.
    mms.setEKGActive(e.heartOn);
    mms.setRRActive(e.bpOn);
    mms.setO2Active(e.oxyOn);
    mms.setCO2Active(e.carbOn);
    mms.setNIBPActive(e.cuffOn);
    mms.setRespActive(e.respOn);
    // When there is a schedule-time:
    if (e.time > 0) {
      // Get the current values for EKG, blood pressure, O2 and CO2.
      updateValues();
      // Calculate the division-factor.
      divFactor = Math.round(((float) e.time) / updateInterval);
      // Calculate the increment/decrement steps-size for each parameter.
      final float heartRateInc = (float) (e.heartRateTo - startEKGValue) / divFactor;
      final float diaBloodPressureInc = (float) (e.bloodPressureDias - startDiaBPValue) / divFactor;
      final float sysBloodPressureInc = (float) (e.bloodPressureSys - startSysBPValue) / divFactor;
      final float o2Inc = (float) (e.oxygenTo - starto2Value) / divFactor;
      final float co2Inc = (float) (e.carbTo - startco2Value) / divFactor;
      final float respInc = (float) (e.respRate - startRespValue) / divFactor;
      if (DEBUG) {
        System.out.println("Start values:");
        System.out.println("EKG: " + startEKGValue
            + ", DiaBP: " + startDiaBPValue
            + ", SysBP: " + startSysBPValue
            + ", O2: " + starto2Value
            + ", CO2: " + startco2Value
            + ", Resp: " + startRespValue);
        System.out.println("Number of increment steps: " + divFactor);
        System.out.println("Increment-Step-Sizes:");
        System.out.println("EKGInc: " + heartRateInc
            + ", DiaBPInc: " + diaBloodPressureInc
            + ", SysBPInc: " + sysBloodPressureInc
            + ", O2Inc: " + o2Inc
            + ", CO2Inc: " + co2Inc
            + ", RespInc: " + respInc);
      }
      updateCount = 0;
      // Create a scheduled task which increases/decreases the parameters
      // each time step until the given final value is reached.
      timer = new Timer();
      timer.scheduleAtFixedRate(new TimerTask() {
        public void run() {
          updateCount++;  // Count the steps.
          mms.setEKG(startEKGValue + (int) (heartRateInc * (float) updateCount));
          mms.setIBP(startDiaBPValue + (int) (diaBloodPressureInc * (float) updateCount),
              startSysBPValue + (int) (sysBloodPressureInc * (float) updateCount));
          mms.setO2(starto2Value + (int) (o2Inc * (float) updateCount));
          mms.setCO2(startco2Value + (int) (co2Inc * (float) updateCount));
          mms.setResp(startRespValue + (int) (respInc * (float) updateCount));
          if (updateCount >= divFactor) {
            this.cancel();
          }
        }
      }, (int) (updateInterval * 1000), (int) (updateInterval * 1000));
      // When there is no schedule-time, just set the values directly.
    } else {
      mms.setEKG(e.heartRateTo);
      mms.setIBP(e.bloodPressureDias, e.bloodPressureSys);
      mms.setO2(e.oxygenTo);
      mms.setCO2(e.carbTo);
      mms.setResp(e.respRate);
    }
  }

  // PRIVATE:

  /**
   * Gets the current EKG, blood pressure, o2, co2 and respiration values from the monitor
   * as start-values.
   */
  private void updateValues() {
    startEKGValue = mms.getEKGValue();
    startDiaBPValue = mms.getDiaBPValue();
    startSysBPValue = mms.getSysBPValue();
    starto2Value = mms.getO2Value();
    startco2Value = mms.getCO2Value();
    startRespValue = mms.getRespValue();
  }


}
