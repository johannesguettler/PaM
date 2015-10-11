/**
 * Class to handle the database handling for the scenarios.
 *
 * @author Johannes Scherle
 * November 2014
 * johannes.scherle@googlemail.com
 * University of Freiburg
 */
package Scenario;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ScenarioHelper extends SQLiteOpenHelper {

  private ScenarioDatabaseHelper scenarioDatabaseHelper;

  // If you change the database schema, you must increment the database version.
  private static final int DATABASE_VERSION = 1;

  // The columns.
  private static final String KEY_TIME = "TIME";
  private static final String KEY_HRTO = "HRTO";
  private static final String KEY_HEARTPATTERN = "HEARTPATTERN";

  private static final String KEY_BPSYS = "BPSYS";
  private static final String KEY_BPDIAS = "BPDIAS";
  private static final String KEY_BPPATTERN = "BPPATTERN";

  private static final String KEY_OXYTO = "OXYTO";
  private static final String KEY_OXYPATTERN = "OXYPATTERN";

  private static final String KEY_RESPTO = "RESPTO";
  private static final String KEY_RESPPATTERN = "RESPPATTERN";

  private static final String KEY_CARBTO = "CARBTO";
  private static final String KEY_CARBPATTERN = "CARBPATTERN";

  private static final String KEY_HEARTON = "HEARTON";
  private static final String KEY_BPON = "BPON";
  private static final String KEY_OXYON = "OXYON";
  private static final String KEY_CARBON = "CARBON";
  private static final String KEY_CUFFON = "CUFFON";
  private static final String KEY_RESPON = "RESPON";

  private static final String KEY_TIMESTAMP = "TIMESTAMP";
  private static final String KEY_FLAG = "FLAG";
  private static final String KEY_SYNCTIMER = "SYNCTIMER";
  private static final String KEY_FLAG_TYPE = "FLAGTYPE";
  private static final String KEY_FLAG_COMMENT = "FLAGCOMMENT";
  private static final String KEY_TIMERSTATE = "TIMERSTATE";

  private static final String INTEGER = " INTEGER,";
  private static final String STRING = " STRING,";

  private static String DATABASE_NAME = "ScenarioDatabase";
  private static final String KEY_INDEX = "_id";

  /**
   * Constructor
   * @param context
   */
  public ScenarioHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    scenarioDatabaseHelper = new ScenarioDatabaseHelper(context);
  }

  /**
   * Add a scenario to the database
   * @param theScenario
   * @return 1 if successful -1 if not.
   */
  public int addScenario(Scenario theScenario) {
    // Add the Scenario to the Scenario Database.
    if (this.scenarioDatabaseHelper.addScenario(theScenario) != -1) {
      // Create a new table.
      String CREATE_EVENTS_TABLE = "CREATE TABLE IF NOT EXISTS "
          + theScenario.getName() + " ( " +
          KEY_INDEX + " INTEGER PRIMARY KEY , " +
          KEY_TIME + INTEGER +
          KEY_HRTO + INTEGER +
          KEY_HEARTPATTERN + INTEGER +
          KEY_BPSYS + INTEGER +
          KEY_BPDIAS + INTEGER +
          KEY_BPPATTERN + INTEGER +
          KEY_OXYTO + INTEGER +
          KEY_OXYPATTERN + INTEGER +
          KEY_RESPTO + INTEGER +
          KEY_RESPPATTERN + INTEGER +
          KEY_CARBTO + INTEGER +
          KEY_CARBPATTERN + INTEGER +
          KEY_TIMESTAMP + INTEGER +
          KEY_HEARTON + INTEGER +
          KEY_BPON + INTEGER +
          KEY_CUFFON + INTEGER +
          KEY_OXYON + INTEGER +
          KEY_CARBON + INTEGER +
          KEY_RESPON + INTEGER +
          KEY_SYNCTIMER + INTEGER +
          KEY_FLAG + INTEGER +
          KEY_FLAG_TYPE + INTEGER +
          KEY_FLAG_COMMENT + STRING +
          KEY_TIMERSTATE + " INTEGER" + ");";
      SQLiteDatabase db = this.getWritableDatabase();
      db.execSQL(CREATE_EVENTS_TABLE);
      // And fill it.
      ListIterator<ProtocolEvent> it = theScenario.getEventList().listIterator();
      while (it.hasNext())
        addEvent(it.next(), db, theScenario.getName());
      db.close(); // Closing database connection
      return 1;
    } else
      return -1;
  }

  /**
   * On create method.
   */
  public void onCreate(SQLiteDatabase db) {
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
    onCreate(db);
  }

  /**
   * On downgrade method.
   */
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  /**
   * Load a scenario from the database
   * @param scenarioName
   * @return the loaded scenario if successful, null if not.
   */
  public Scenario loadScenario(String scenarioName) {
    Scenario theScenario = null;
    List<ProtocolEvent> eventList = new ArrayList<>();
    boolean badQuery = false;
    // Select All Query, to get all events.
    String selectQuery = "SELECT * FROM " + scenarioName;
    SQLiteDatabase db = this.getWritableDatabase();
    try {
      Cursor cursor = db.rawQuery(selectQuery, null);
      // looping through all rows and adding the events to the list.
      if (cursor.moveToFirst()) {
        do {
          ProtocolEvent event = new ProtocolEvent(cursor.getInt(1),        //TIME
              cursor.getInt(2),                // HRTO
              Scenario.intToHeartPattern(cursor.getInt(3)),  // HPATTERN
              cursor.getInt(4),                // BPSYS
              cursor.getInt(5),                // BPDIAS
              Scenario.intToBpPattern(cursor.getInt(6)),    // BPPATTERN
              cursor.getInt(7),                // OXYTO
              Scenario.intToO2Pattern(cursor.getInt(8)),    // OXYPATTERN
              cursor.getInt(9),                // RESPTO
              Scenario.intToRespPattern(cursor.getInt(10)),  // RESPPATTERN
              cursor.getInt(11),                // CARBTO
              Scenario.intToCarbPattern(cursor.getInt(12)),  // CARBPATTERN
              cursor.getInt(13),                // TIMESTAMP
              ((cursor.getInt(14) == 1) ? true : false),    // HEARTON
              ((cursor.getInt(15) == 1) ? true : false),    // BPON
              ((cursor.getInt(16) == 1) ? true : false),    // CUFFON
              ((cursor.getInt(17) == 1) ? true : false),    // OXYON
              ((cursor.getInt(18) == 1) ? true : false),    // CARBON
              ((cursor.getInt(19) == 1) ? true : false),    // RESPON
              ((cursor.getInt(20) == 1) ? true : false),    // SYNCTIMER
              ((cursor.getInt(21) == 1) ? true : false),    // FLAG
              Scenario.intToFlagType(cursor.getInt(22)),
              ((cursor.getString(23) == null || (cursor.getString(23).equals
                  ("null")))? null: cursor.getString(23)),
              Scenario.intToTimerState(cursor.getInt(24)));  // TIMER STATE
          eventList.add(event);
        } while (cursor.moveToNext());
      }
      // Build a new scenario.
      theScenario = new Scenario(scenarioName, true, eventList);
      // Get the runnable for the scenario.
      boolean runnable = scenarioDatabaseHelper.getRunnable(theScenario);
      theScenario.setRunnable(runnable);
      badQuery = false;
    } catch (SQLException e) {
      badQuery = true;
    }
    if (badQuery)
      return null;
    else
      return theScenario;
  }

  /**
   * Add an event to a scenario
   * @param e The event to add to a scenario
   * @param db The SQLite database
   * @param tableName The name of the scenario.
   */
  public static void addEvent(ProtocolEvent e, SQLiteDatabase db, String
      tableName) {
    // Put all the values of the event to the database.
    ContentValues values = new ContentValues();
    values.put(KEY_TIME, e.time);
    values.put(KEY_HRTO, e.heartRateTo);
    values.put(KEY_HEARTPATTERN, e.heartPattern.ordinal());
    values.put(KEY_BPSYS, e.bloodPressureSys);
    values.put(KEY_BPDIAS, e.bloodPressureDias);
    values.put(KEY_BPPATTERN, e.bloodPressurePattern.ordinal());
    values.put(KEY_OXYTO, e.oxygenTo);
    values.put(KEY_OXYPATTERN, e.oxyPattern.ordinal());
    values.put(KEY_RESPTO, e.respRate);
    values.put(KEY_RESPPATTERN, e.respPattern.ordinal());
    values.put(KEY_CARBTO, e.carbTo);
    values.put(KEY_CARBPATTERN, e.carbPattern.ordinal());
    values.put(KEY_TIMESTAMP, e.timeStamp);
    values.put(KEY_HEARTON, (e.heartOn) ? 1 : 0);
    values.put(KEY_BPON, (e.bpOn) ? 1 : 0);
    values.put(KEY_CUFFON, (e.cuffOn) ? 1 : 0);
    values.put(KEY_OXYON, (e.oxyOn) ? 1 : 0);
    values.put(KEY_CARBON, (e.carbOn) ? 1 : 0);
    values.put(KEY_RESPON, (e.respOn) ? 1 : 0);
    values.put(KEY_SYNCTIMER, (e.syncTimer) ? 1 : 0);
    values.put(KEY_FLAG, (e.flag) ? 1 : 0);
    values.put(KEY_FLAG_TYPE, (e.flagType == null)? 100: e.flagType.ordinal());
    values.put(KEY_FLAG_COMMENT, e.flagComment);
    values.put(KEY_TIMERSTATE, e.timerState.ordinal());
    db.insert(tableName, null, values);
  }

  /**
   * Returns all available scenarios.
   * The true in the call determines the runnable value (true for scenario).
   * @return A List with all scenarios.
   */
  public List<Scenario> getAllScenarios() {
    return this.scenarioDatabaseHelper.getAllScenarios(this, true);
  }

  /**
   * Returns all available protocols.
   * The true in the call determines the runnable value (false for protocol).
   * @return A List with all scenarios.
   */
  public List<Scenario> getAllProtocols() {
    return this.scenarioDatabaseHelper.getAllScenarios(this, false);
  }

  /**
   * Deletes a scenario.
   * Drops the table here and deletes the entry in the ScenarioDatabase.
   */
  public void deleteScenario(Scenario scenario) {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("DROP TABLE IF EXISTS " + scenario.getName());
    scenarioDatabaseHelper.deleteScenario(scenario);

  }

  /**
   * Delete all scenarios, meaning drop all tables in database.
   */
  public void deleteAllScenarios() {
    SQLiteDatabase db = this.getWritableDatabase();
    List<Scenario> scenarios = getAllScenarios();
    Iterator<Scenario> it = scenarios.iterator();
    while (it.hasNext())
      db.execSQL("DROP TABLE IF EXISTS " + it.next().getName());
    // Clear the ScenarioDatabase.
    scenarioDatabaseHelper.deleteAllScenarios();
  }

  /**
   * Change the runnable value. Calls the function in the scenario database.
   * @param scenario
   */
  public void setRunnableInDatabase(Scenario scenario) {
    this.scenarioDatabaseHelper.setRunnableInDatabase(scenario);
  }

}
