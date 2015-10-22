package Scenario;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.example.pmcontroller1.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

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
    CRM_OTHER_NEG, SHOCK;

    // Mapping to id
    private static final HashMap<Integer, Flag> map = new HashMap<>();
    static
    {
      for (Flag flag : Flag.values())
        map.put(flag.ordinal(), flag);
    }

    /**
     * get Flag associated with de inv value (ordinal)
     * @param value
     * @return
     */
    public static Flag from(int value)
    {
      return map.get(value);
    }
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

  public static String getEventText(ProtocolEvent event, Context context) {
    int id;
    switch (event.flagType){
      case SHOCK:
        return "";
      case A_B_POS:
      case A_B_NEG:
        id = R.string.flag_button_AB_text;
        break;
      case C_NEG:
      case C_POS:
        id = R.string.flag_button_C_text;
        break;
      case D_E_NEG:
      case D_E_POS:
        id = R.string.flag_button_DE_text;
        break;
      default:
        id = R.string.flag_button_CRM_text;
    }

    return context.getString(id);
  }
  public static String getFullFlagDescription(Flag flagType, Context context) {
    int id;
    switch (flagType){
      case SHOCK:
        id = R.string.flag_SHOCK_description;
        break;
      case A_B_POS:
      case A_B_NEG:
        id = R.string.flag_AB_description;
        break;
      case C_NEG:
      case C_POS:
        id = R.string.flag_C_description;
        break;
      case D_E_NEG:
      case D_E_POS:
        id = R.string.flag_DE_description;
        break;
      case CRM_COMM_POS:
      case CRM_COMM_NEG:
        id = R.string.flag_CRM_communication_description;
        break;
      case CRM_ORG_NEG:
      case CRM_ORG_POS:
      id = R.string.flag_CRM_organisation_description;
      break;
      case CRM_TEAM_NEG:
      case CRM_TEAM_POS:
      id = R.string.flag_CRM_team_description;
      break;
      default:
        id = R.string.flag_CRM_other_description;
    }

    return context.getString(id);
  }
  public static int getEventColor(ProtocolEvent event) {
    Flag flagType = event.flagType;
    if (flagType == Flag.A_B_POS || flagType == Flag.C_POS || flagType ==
        Flag.D_E_POS || flagType == Flag.CRM_COMM_POS || flagType == Flag
        .CRM_ORG_POS || flagType == Flag.CRM_TEAM_POS || flagType == Flag.CRM_OTHER_POS){
      return Color.GREEN;
    } else if (flagType == Flag.SHOCK){
      return Color.YELLOW;
    }
    return Color.RED;
  }
}
