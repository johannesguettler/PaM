package gui;

import java.util.Timer;
import java.util.TimerTask;

import Scenario.Event;

/**
 * Created by Jo on 28.09.2015.
 * handles new events from the controllerActivity and sends current
 * values in a given interval to the monitor
 * if a timer is set the current values are calculated
 * !!! starts running on first call of handleEvent !!!
 */
public class UpdateHandler {
  private final ControllerActivity controllerActivity;
  private final float updateInterval = 0.5f;
  private Event lastEvent = null;

  private Timer timer;  // The schedule-timer.
  // The scheduled time divided by the updateInterval -> Number of increment-steps.
  private float divFactor;
  // The parameter values before the begin of an scheduled update.
  private int lastEKGValue;
  private int lastDiaBPValue;
  private int lastSysBPValue;
  private int lasto2Value;
  private int lastco2Value;
  private int lastRespValue;
  // Counter for the update-steps.
  private static int updateCount;
  private boolean calculationNecessary;


  public Event.TimerState timerState;
  private TimerTask timerTask;
  private float heartRateInc;
  private float diaBloodPressureInc;
  private float sysBloodPressureInc;
  private float o2Inc;
  private float co2Inc;
  private float respInc;

  public UpdateHandler(ControllerActivity controllerActivity){
    this.controllerActivity = controllerActivity;
    timer = new Timer();
    initializeTimerTask();
  }

  private void initializeTimerTask() {
    timerTask = new TimerTask(){

      @Override
      public void run() {
        calculateValuesAndSendEvent();
      }
    };
  }
  private void startTimerTask() {
    timer.schedule(timerTask, (int)(updateInterval * 1000), (int)(updateInterval * 1000));
  }

  private void calculateValuesAndSendEvent() {
    if(calculationNecessary) {
      updateCount++;  // Count the steps.

      lastEvent.heartRateTo = (lastEKGValue + (int) (heartRateInc * (float)
          updateCount));
      lastEvent.bloodPressureDias = lastDiaBPValue + (int)
              (diaBloodPressureInc * (float)updateCount);
      lastEvent.bloodPressureSys = lastSysBPValue + (int)
          (sysBloodPressureInc * (float) updateCount);
      lastEvent.oxygenTo =  (lasto2Value + (int) (o2Inc * (float) updateCount));
      lastEvent.carbTo = lastco2Value + (int) (co2Inc * (float) updateCount);
      lastEvent.respRate = lastRespValue + (int) (respInc * (float) updateCount);
      if (updateCount >= divFactor) {
        calculationNecessary = false;
      }
    }
    MainActivity.server.out(lastEvent.toJson().toString());
  }


  public void handleEvent(Event event){
    boolean startTimerTask = false;
    if (lastEvent == null) {
       startTimerTask = true;
      setInitialValues();
    }
    lastEvent = event;
    if(event.time > 0) {
      calculationNecessary = true;
      updateCount = 0;
      divFactor = Math.round(((float) event.time) / updateInterval);
      lastEvent.time = 0;

      heartRateInc = (float) (event.heartRateTo - lastEKGValue) / divFactor;
      diaBloodPressureInc = (float) (event.bloodPressureDias - lastDiaBPValue) /
          divFactor;
      sysBloodPressureInc = (float) (event.bloodPressureSys - lastSysBPValue) /
          divFactor;
      o2Inc = (float) (event.oxygenTo - lasto2Value) / divFactor;
      co2Inc = (float) (event.carbTo - lastco2Value) / divFactor;
      respInc = (float) (event.respRate - lastRespValue) / divFactor;
    }
    if(startTimerTask){
      startTimerTask();
    }
  }

  private void setInitialValues() {
    lastEKGValue = controllerActivity.getHeartRateValue();
    lastDiaBPValue = controllerActivity.getBloodPressureDiastolicValue();
    lastSysBPValue = controllerActivity.getBloodPressureSystolicValue();
    lasto2Value = controllerActivity.getO2RateValue();
    lastco2Value = controllerActivity.getCo2RateValue();
    lastRespValue = controllerActivity.getRespirationRateValue();
  }

  public void onDestroy() {
    if(timerTask != null){
      timerTask.cancel();
    }
    if(timer!=null){
      timer.cancel();
      timer.purge();
    }
  }

}
