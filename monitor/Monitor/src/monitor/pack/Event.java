/**
 * Container-Class for the events
 * @author Johannes
 * February 2015
 * johannes.scherle@gmail.com
 * University Freiburg
 */
package monitor.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Event {
	
  // An index to identify the event.
  public Integer _index;

  // Variables for the heart rate and pattern.
  public Integer heartRateTo;
  public Integer time;
  public enum HeartPattern {
    SINE, ARRYTHMIC, AVBLOCK, LEFTBLOCK, LEFTBLOCKAA, STEMI, PACE, 
    VENTFLUTTER, VENTFIBRI, CPR, ASYSTOLE
  }  
  public HeartPattern heartPattern;
  public boolean heartOn;
  
  // Variables for the blood pressure.
  public Integer bloodPressureSys;
  public Integer bloodPressureDias;
  public enum BloodPressPattern {
	  NORMAL, BP2
  }
  public BloodPressPattern bpPattern;
  public boolean bpOn;
  public boolean cuffOn;
  
  // Variables for the oxygen
  public Integer oxygenTo;
  public enum O2Pattern {
	    NORMAL, COLDFINGER
  }	  
  public O2Pattern oxyPattern;
  
  // Variables for Respiration  
  public boolean oxyOn;
  public Integer respRate;
  public enum RespPattern{
	  NORMAL, RESP1
  }
  public RespPattern respPattern;
  public boolean respOn;
  
  // Variables for the CO2
  public Integer carbTo;
  public boolean carbOn;
  public enum CarbPattern{
	  NORMAL, CARB1
  }
  public CarbPattern carbPattern;
  
  // Variables for the timing.
  public Integer timeStamp;
  public boolean syncTimer;
  public boolean flag;
  public enum TimerState {
	  RUN, START, PAUSE, STOP, RESET
  }
  public TimerState timerState;
  
  /**
   * Constructor
   * @param time
   * @param heartRateTo
   * @param heartPattern
   * @param bloodPressureSys
   * @param bloodPressureDias
   * @param bloodPressurePattern
   * @param oxygenTo
   * @param oxyPattern
   * @param respRate
   * @param respPattern
   * @param carbTo
   * @param carbPattern
   * @param timeStamp
   * @param heartOn
   * @param bpOn
   * @param cuffOn
   * @param oxyOn
   * @param carbOn
   * @param respOn
   * @param syncTimer
   * @param flag
   * @param timerState
   */
  public Event(Integer time, Integer heartRateTo, 
        HeartPattern heartPattern, Integer bloodPressureSys,
        Integer bloodPressureDias, BloodPressPattern bloodPressurePattern,
        Integer oxygenTo, O2Pattern oxyPattern, Integer respRate, 
        RespPattern respPattern, Integer carbTo, CarbPattern carbPattern,
        Integer timeStamp, boolean heartOn, boolean bpOn, boolean cuffOn,
        boolean oxyOn, boolean carbOn, boolean respOn, boolean syncTimer,
        boolean flag, TimerState timerState) {
	    	  
    this.time = time;
    this.heartRateTo = heartRateTo;
    this.heartPattern = heartPattern;
    this.bloodPressureSys = bloodPressureSys;
    this.bloodPressureDias = bloodPressureDias;
    this.bpPattern = bloodPressurePattern;
    this.oxygenTo = oxygenTo;
    this.oxyPattern = oxyPattern;
    this.respRate = respRate;
    this.respPattern = respPattern;
    this.carbTo = carbTo;
    this.carbPattern = carbPattern;
    this.timeStamp = timeStamp;
    this.heartOn = heartOn;
    this.bpOn = bpOn;
    this.oxyOn = oxyOn;
    this.carbOn = carbOn;
    this.cuffOn = cuffOn;
    this.respOn = respOn;
    this.syncTimer = syncTimer;
    this.flag = flag;
    this.timerState = timerState;
  };
  
  /**
   * Typical toString method.
   */
  @Override
  public String toString() {
    String string = "Scheduler: " + this.time.toString() + "\n" +
    		        "BPSys: " + this.bloodPressureSys.toString() + ",  " +
    		        "BPDias: " + this.bloodPressureDias.toString() + ",  " +
    		        "Pattern: " + this.bpPattern.toString() + ",  " +
    		        "Curve BP: " + ((this.bpOn) ? "On" : "Off") + "\n" +
    		        "HR: " + this.heartRateTo.toString() + ",  " +
    		        "Pattern: " + this.heartPattern.toString() + ",  " +
                    "Curve HR: " + ((this.heartOn) ? "On" : "Off") + "\n" +
    		        "Oxy: " + this.oxygenTo.toString() + ",  " +
                    "Pattern: " + this.oxyPattern.toString() + ",  " +
                    "Curve O2: " + ((this.oxyOn) ? "On" : "Off") + "\n" +
                    "Resp: " + this.respRate.toString() + ",  " +
                    "Pattern: " + this.respPattern.toString() + "\n" +
                    "Curve Resp: " + ((this.respOn) ? "On" : "Off") + "\n" +
                    "Carb: " + this.carbTo.toString() + ",  " +
                    "Curve Carb: " + ((this.carbOn) ? "On" : "Off") + ",  " +
                    "Pattern: " + this.carbPattern.toString() + "\n" +
    		        "CuffCorrect: " + ((this.cuffOn) ? "Yes" : "No") + "\n" +
                    "TimeStamp: " + this.timeStamp.toString() + "\n" +
                    "SyncTimer: " + ((this.syncTimer) ? "Yes" : "No") + "\n" +
                    "Flag: " + ((this.flag) ? "Yes" : "No" + "\n" +
                    "Timer State: " + this.timerState.toString());
	return string;    
  }
  
  /**
   * Convert an Event to JSon String.
   * @return JSon String
   */
  public String toJson() {
	  GsonBuilder builder = new GsonBuilder();
      builder.setPrettyPrinting().serializeNulls();
      Gson gson = builder.create();      
      System.out.println(gson.toJson(this));	  
	  return gson.toJson(this);
  }
  
  /**
   * Convert a JSon String to an event.
   * @param g JSon String to parse
   * @return The parsed Event.
   */
  static public Event fromJsonEvent(String g) {
	  Gson gson = new Gson();
	  return gson.fromJson(g, Event.class);	  
  }
}
