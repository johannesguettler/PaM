package Scenario;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Jo on 06.10.2015.
 */
public class ProtocolEvent extends Event {

  public String flagComment;
  public Flag flagType;

  //enum for different protocol flags
  public enum Flag {
    A_B_POS, A_B_NEG, C_POS, C_NEG, D_E_POS, D_E_NEG, CRM_COMM_POS, CRM_COMM_NEG,
    CRM_TEAM_POS, CRM_TEAM_NEG, CRM_ORG_POS, CRM_ORG_NEG, CRM_OTHER_POS,
    CRM_OTHER_NEG
  }
  /**
   * Constructor
   *
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
   * @param flagType
   * @param timerState
   */
  public ProtocolEvent(Integer time, Integer heartRateTo, Event.HeartPattern
      heartPattern, Integer bloodPressureSys, Integer bloodPressureDias,
                       Event.BloodPressPattern bloodPressurePattern, Integer
                           oxygenTo, Event.O2Pattern oxyPattern, Integer
                           respRate, Event.RespPattern respPattern, Integer
                           carbTo, Event.CarbPattern carbPattern, Integer
                           timeStamp, boolean heartOn, boolean bpOn, boolean
                           cuffOn, boolean oxyOn, boolean carbOn, boolean
                           respOn, boolean syncTimer, boolean isFlagEvent,
                       Flag flagType, String flagComment,
                       TimerState timerState) {
    super(time, heartRateTo, heartPattern, bloodPressureSys,
        bloodPressureDias, bloodPressurePattern, oxygenTo, oxyPattern,
        respRate, respPattern, carbTo, carbPattern, timeStamp, heartOn, bpOn,
        cuffOn, oxyOn, carbOn, respOn, syncTimer, isFlagEvent, timerState);
    this.flagType = flagType;
    this.flagComment = flagComment;
  }
  public ProtocolEvent(Event event, Flag flagType, String flagComment) {
    super(event.time, event.heartRateTo, event.heartPattern, event.bloodPressureSys,
        event.bloodPressureDias, event.bloodPressurePattern, event.oxygenTo,
        event.oxyPattern, event.respRate, event.respPattern, event.carbTo,
        event.carbPattern, event.timeStamp, event.heartOn, event.bpOn, event
            .cuffOn, event.oxyOn, event.carbOn, event.respOn, event.syncTimer, event.flag, event
            .timerState);
    this.flagType = flagType;
    this.flagComment = flagComment;
  }
  /**
   * Typical toString method.
   */
  @Override
  public String toString() {
    String string = "Scheduler: " + this.time.toString() + "\n" +
        "BPSys: " + this.bloodPressureSys.toString() + ",  " +
        "BPDias: " + this.bloodPressureDias.toString() + ",  " +
        "Pattern: " + this.bloodPressurePattern.toString() + ",  " +
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
        "Flag: " + ((this.flag) ? "Yes" : "No") + "\n" +
        "FlagType: " + this.flagType.name() + "\n" +
        "FlagComment: " + this.flagComment + "\n" +
        "Timer State: " + this.timerState.toString();
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
    //System.out.println(gson.toJson(this));
    return gson.toJson(this);
  }

  /**
   * Convert a JSon String to an event.
   * @param g JSon String to parse
   * @return The parsed Event.
   */
  static public Event fromJsonEvent(String g) {
    Gson gson = new Gson();
    return gson.fromJson(g, ProtocolEvent.class);
  }
}
