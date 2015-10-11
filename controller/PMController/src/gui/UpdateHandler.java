package gui;

import java.util.Date;
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
  private static final long PROTOCOL_STORE_INTERVAL_SECONDS = 30;
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

  private Timer protocolTimer;
  private TimerTask protocolTimerTask;
  private long lastProtocolStoreTime;

  private float heartRateInc;
  private float diaBloodPressureInc;
  private float sysBloodPressureInc;
  private float o2Inc;
  private float co2Inc;
  private float respInc;
  private long protocolDelayMilliseconds;

  public UpdateHandler(ControllerActivity controllerActivity) {
    this.controllerActivity = controllerActivity;
    protocolDelayMilliseconds = 0;
    timer = new Timer();
    protocolTimer = new Timer();
    initializeTimerTask();
  }

  private void inititializeProtocolTimerTask() {
    protocolTimerTask = new TimerTask() {
      @Override
      public void run() {
        storeProtocolEvent();
      }
    };
  }

  private void initializeTimerTask() {
    timerTask = new TimerTask() {

      @Override
      public void run() {
        calculateValuesAndSendEvent();
      }
    };
  }

  private void startTimerTask() {
    timer.schedule(timerTask, (int) (updateInterval * 1000), (int) (updateInterval * 1000));
  }

  private void calculateValuesAndSendEvent() {
    if (calculationNecessary) {
      updateCount++;  // Count the steps.
      calculateAndStoreCurrentValues();
    }

    MainActivity.server.out(lastEvent.toJson().toString());
    controllerActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        controllerActivity.setCurrentValuesTextFields(lastEvent);
      }
    });

  }

  private void calculateAndStoreCurrentValues() {
    lastEvent.heartRateTo = (lastEKGValue + (int) (heartRateInc * (float)
        updateCount));
    lastEvent.bloodPressureDias = lastDiaBPValue + (int)
        (diaBloodPressureInc * (float) updateCount);
    lastEvent.bloodPressureSys = lastSysBPValue + (int)
        (sysBloodPressureInc * (float) updateCount);
    lastEvent.oxygenTo = (lasto2Value + (int) (o2Inc * (float) updateCount));
    lastEvent.carbTo = lastco2Value + (int) (co2Inc * (float) updateCount);
    lastEvent.respRate = lastRespValue + (int) (respInc * (float) updateCount);
    if (updateCount >= divFactor) {
      calculationNecessary = false;

    }
  }


  public void handleEvent(Event event, boolean valueWriteEvent) {
    boolean startTimerTask = false;
    if (lastEvent == null) {
      startTimerTask = true;
      lastEvent = event;
    }
    setLastValues();

    //synchronized (lastEvent) {
    if (valueWriteEvent) {
      if (event.time > 0) {
        calculationNecessary = true;
        updateCount = 0;
        divFactor = Math.round(((float) event.time) / updateInterval);

        heartRateInc = (float) (event.heartRateTo - lastEvent.heartRateTo) / divFactor;
        diaBloodPressureInc = (float) (event.bloodPressureDias - lastEvent
            .bloodPressureDias) / divFactor;
        sysBloodPressureInc = (float) (event.bloodPressureSys - lastEvent
            .bloodPressureSys) / divFactor;
        o2Inc = (float) (event.oxygenTo - lastEvent.oxygenTo) / divFactor;
        co2Inc = (float) (event.carbTo - lastEvent.carbTo) / divFactor;
        respInc = (float) (event.respRate - lastEvent.respRate) / divFactor;
        //calculateAndStoreCurrentValues();
      } else {
        calculationNecessary = false;
      }

    }
    lastEvent = event;
    lastEvent.time = 0;
    if (startTimerTask) {
      startTimerTask();
    }

    //}
  }

  private void setLastValues() {
    lastEKGValue = lastEvent.heartRateTo;
    lastDiaBPValue = lastEvent.bloodPressureDias;
    lastSysBPValue = lastEvent.bloodPressureSys;
    lasto2Value = lastEvent.oxygenTo;
    lastco2Value = lastEvent.carbTo;
    lastRespValue = lastEvent.respRate;
  }

  public void onDestroy() {
    if (timerTask != null) {
      timerTask.cancel();
    }
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
    if (protocolTimerTask != null) {
      protocolTimerTask.cancel();
    }
    if (protocolTimer != null) {
      protocolTimer.cancel();
      protocolTimer.purge();
    }
  }

  public void startProtocol() {
    inititializeProtocolTimerTask();
    protocolTimer.scheduleAtFixedRate(protocolTimerTask,
        protocolDelayMilliseconds, (PROTOCOL_STORE_INTERVAL_SECONDS * 1000));
  }

  public void pauseProtocol() {
    long timerTaskSheduledExecutionTime = protocolTimerTask
        .scheduledExecutionTime();
    protocolDelayMilliseconds = timerTaskSheduledExecutionTime -
        lastProtocolStoreTime;

  }

  public void endProtocol() {
    protocolTimerTask.cancel();
    protocolDelayMilliseconds = 0;
  }

  private void storeProtocolEvent() {
    //synchronized (lastEvent) {
    controllerActivity.addProtocolEvent(lastEvent, false, null, null);
    //}
    lastProtocolStoreTime = new Date().getTime();
  }
}
