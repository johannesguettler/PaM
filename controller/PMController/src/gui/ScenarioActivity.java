/**
 * @package Patient Monitor - Controller
 * @copyright (c) 2014 University of Freiburg
 * @author Johannes Scherle, Benjamin Voelker
 * @email {scherlej, voelkerb}@informatik.uni-freiburg.de
 * @date 17.11.2014
 * @summary Scenario view with all UI elements for setting playing back scenarios.
 */


package gui;

// Import stuff goes here

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.pmcontroller1.R;

import java.util.List;

import Scenario.Event;
import Scenario.Event.TimerState;
import Scenario.EventAdapter;
import Scenario.Scenario;
import Scenario.ScenarioHelper;
//import Scenario.ScenarioHelper;

public class ScenarioActivity extends Activity {

  //@SuppressWarnings("unused")
  // Debug mode shows stickies
  private static final boolean DEBUG = true;
  // Auto hide the Action bar after this time
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
  // Auto hide active or not
  private static final boolean AUTO_HIDE_ACTIVE = false;
  // If hiding was initialized don't hide again
  private boolean WILL_HIDE = false;
  public static ScenarioHelper scenarioHelper;
  public static EventAdapter eventAdapter;
  public static List<Scenario> currentScenarios;
  public static List<Event> currentEvents;
  public static Scenario currentScenario;
  public static Event currentEvent;
  public static Integer currentPositionEvent = 0;
  public static ListView listViewScenario;
  public static ListView listViewEvents;
  private static boolean scenarioRunning = false;
  private static boolean scenarioPaused = false;
  private Handler scenarioHandler;
  private int timeInSec = 0;

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onCreate(android.os.Bundle)
   * Constructor of the Scenario class
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    // Overlay action bar and content, so that the content does not get shrinked if the action bar is visible
    this.getWindow();
    requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

    // Set up the actionbar correct
    setupActionBar();


    setContentView(R.layout.activity_scenario);

    // Make the statusbar disappear at init
    if (AUTO_HIDE_ACTIVE) delayedHide(AUTO_HIDE_DELAY_MILLIS);

    // Set up the back view for onclicklistener
    final View backView = findViewById(R.id.scenario_background_view);
    // If the background is clicked, the status bar should appear
    if (AUTO_HIDE_ACTIVE) {
      backView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          // Show the action bar
          getActionBar().show();
          // And hide it again after delay if not yet scheduled
          if (!WILL_HIDE) delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
      });
    }


    TimingLogger timings = new TimingLogger("TopicLogTag", "read scenarios");
    // Set the list adapter and read the scenario list.
    readScenariosToList();
    timings.dumpToLog();


    // Set the on item click listener for the scenario list.
    listViewScenario = (ListView) findViewById(R.id.listViewScenario);
    listViewScenario.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapter, View v,
                              int position, long arg3) {
        //v.setSelected(true);
        Scenario chosenScenario = (Scenario) adapter.getItemAtPosition(position);
        currentScenario = chosenScenario;
        currentEvents = chosenScenario.getEventList();
        readEventsToList(chosenScenario);
      }
    });

    // Set the on item click listener for the event list.
    listViewEvents = (ListView) findViewById(R.id.listViewEvents);
    listViewEvents.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapter, View v,
                              int position, long arg3) {
        listViewEvents.setItemChecked(position, true);
        Event chosenEvent = (Event) adapter.getItemAtPosition(position);
        currentPositionEvent = position;
        currentEvent = chosenEvent;
        timeInSec = currentEvent.timeStamp;
        // Sync the timer.
        currentEvent.syncTimer = true;
        MainActivity.server.out(currentEvent.toJson());
      }
    });
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onWindowFocusChanged(boolean)
   * If dialog boxes or other notification appear, the navigation and status bar will get visible -> hide it again if done
   */
  @SuppressLint("InlinedApi")
  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    // make the activity fulscreen without navigation bar
    if (hasFocus) {
      // make the activity fulscreen without navigation bar
      if (Build.VERSION.SDK_INT >= 16) { //Jelly Bean
        this.findViewById(android.R.id.content).setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
      } else if (Build.VERSION.SDK_INT >= 19) { //KITKAT
        this.findViewById(android.R.id.content).setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onResume()
   * If the application resumes from other applications, make it full screen again
   */
  @SuppressLint("InlinedApi")
  @Override
  protected void onResume() {
    super.onResume();
    // make the activity fulscreen without navigation bar
    if (Build.VERSION.SDK_INT >= 16) { //Jelly Bean
      this.findViewById(android.R.id.content).setSystemUiVisibility(
          View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    } else if (Build.VERSION.SDK_INT >= 19) { //KITKAT
      this.findViewById(android.R.id.content).setSystemUiVisibility(
          View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.scenario, menu);
    if (ControllerActivity.PREFERENCE_WINDOW_ACTIVE) {
      menu.add(Menu.NONE, R.id.action_settings, 100, "")
          .setIcon(R.drawable.ic_action_settings)
          .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }
    getActionBar().setDisplayShowHomeEnabled(false);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    if (scenarioRunning)
      stopScenario();
    int id = item.getItemId();
    if (id == android.R.id.home) {
      NavUtils.navigateUpFromSameTask(this);
      return true;
    } else if (id == R.id.action_settings) {
      startActivity(new Intent(this, PreferenceActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Read all the stored scenarios and visualise them in the listview.
   * @author Johannes
   */
  public void readScenariosToList() {
    currentScenarios = scenarioHelper.getAllScenarios();
    try {
      ListAdapter adapter2 = new ArrayAdapter<Scenario>(getApplicationContext(),
          R.layout.my_simple_list_item_1, currentScenarios);
      ListView listViewScenario = (ListView) findViewById(R.id.listViewScenario);
      listViewScenario.setAdapter(adapter2);
    } finally {
    }
  }

  /**
   * Read all events from the current scenario to a listview
   * @param chosenScenario : the Scenario from which the events should be
   * displayed.
   * @author Johannes
   */
  public void readEventsToList(Scenario chosenScenario) {
    // Get the events from the chosen scenario.
    Event[] eventList = (Event[]) chosenScenario.getEventList().toArray(new Event[0]);
    eventAdapter = new EventAdapter(this, R.layout.event_item, eventList);
    ListView listViewEvents = (ListView) findViewById(R.id.listViewEvents);
    listViewEvents.setAdapter(eventAdapter);
  }

  /**
   * Clear the events in the listview. Used when a scenario is deleted.
   * @author Johannes
   */
  public void clearEventList() {
    ListView listViewEvents = (ListView) findViewById(R.id.listViewEvents);
    listViewEvents.setAdapter(null);
  }

  /**
   * Delete a scenario from the database. This is the callback function
   * for the delete button.
   * @param view
   * @author Johannes
   */
  public void deleteScenario(View view) {
    if (scenarioRunning)
      stopScenario();
    scenarioHelper.deleteScenario(currentScenario);
    readScenariosToList();
    clearEventList();
  }

  /**
   * Callback function for the apply button, not used in the current
   * implementation
   * @param view
   * @author Johannes
   */
  public void applyPressedScenario(View view) {
    // Send to Server
    MainActivity.server.out(currentEvent.toJson().toString());
  }

  /**
   * Select the next event from the event list. Only used in the protocoll-
   * mode at the moment.
   * @param view
   * @author Johannes
   */
  public void nextEvent(View view) {
    if (currentEvents != null && currentPositionEvent != null) {
      if (currentPositionEvent >= currentEvents.size() - 1)
        currentPositionEvent = 0;
      else
        currentPositionEvent++;
      listViewEvents.setItemChecked(currentPositionEvent, true);
      currentEvent = (Event) listViewEvents
          .getItemAtPosition(currentPositionEvent);
    }
  }

  /**
   * Start the automatic playback of the scenario. Callback function for
   * the play button.
   * @param view
   * @author Johannes
   */
  public void startScenario(View view) {
    if (currentEvents != null && currentPositionEvent != null
        && !scenarioRunning) {
      currentEvent = (Event) listViewEvents
          .getItemAtPosition(currentPositionEvent);
      /*
			 * Get the first events from the current scenario and send them to
			 * the monitor.
			 * Used a while loop for the case that the scenario starts with
			* several timestamp == 0 Events in a row.
			*/
      int firstTimestamp = currentEvent.timeStamp;
      while (currentEvent.timeStamp == firstTimestamp) {
        // Start the timer on the Monitor again
        if (scenarioPaused) {
          currentEvent.timerState = TimerState.START;
          scenarioPaused = false;
        }
        // Send the event only if it is not a flag.
        if (!currentEvent.flag)
          MainActivity.server.out(currentEvent.toJson().toString());
        listViewEvents.setItemChecked(currentPositionEvent, true);
        currentPositionEvent++;
        if (currentPositionEvent < currentEvents.size() - 1) {
          currentEvent = (Event) listViewEvents
              .getItemAtPosition(currentPositionEvent);
        } else {
          listViewEvents.setItemChecked(currentPositionEvent, true);
          currentPositionEvent = 0;
          return;
        }
      }
      scenarioRunning = true;
      // Set up a timer which controls the stopwatch and the sending
      // of events.
      scenarioHandler = new Handler();
      scenarioHandler.postDelayed(new Runnable() {
        public void run() {
          timeInSec++; // Increase the counter every second.
          if (scenarioRunning)
            updateTime(); // Update the GUI.
          if (currentEvents != null
              && currentPositionEvent != null) {
            // If the timer equals the timestamp of the next event
            // and the event is not a flag, send it to the monitor.
            if (timeInSec == currentEvent.timeStamp) {
              // If we reached the last event.
              if (currentPositionEvent >= currentEvents
                  .size() - 1) {
                if (!currentEvent.flag)
                  listViewEvents.setItemChecked(
                      currentPositionEvent, true);
                MainActivity.server.out(currentEvent
                    .toJson().toString());
                if (DEBUG)
                  System.out.println("Sent Event");
                // Stop the timer and reset the variables.
                stopScenario();
                if (DEBUG)
                  System.out.println("Stopped Scenario");
              } else {
                // For the case that several events with the
                // same time stamp occur.
                int currentTimestamp = currentEvent.timeStamp;
                while (currentTimestamp == currentEvent.timeStamp) {
                  MainActivity.server.out(currentEvent
                      .toJson().toString());
                  if (DEBUG)
                    System.out.println("Sent Event");
                  if (!currentEvent.flag)
                    listViewEvents.
                        setItemChecked(currentPositionEvent, true);
                  currentTimestamp = currentEvent.timeStamp;
                  currentPositionEvent++;
                  currentEvent = (Event) listViewEvents
                      .getItemAtPosition(currentPositionEvent);
                }
              }
            }
          }
          // If the scenario is running set the next callback,
          // else stop the timer.
          if (scenarioRunning)
            scenarioHandler.postDelayed(this, 1000);
          else if (!scenarioRunning)
            scenarioHandler.removeCallbacks(this);
        }
      }, 1000);

    }
  }

  /**
   * The callback function for the stopScenario-Button.
   * @param view
   * @author Johannes
   */
  public void stopScenarioCallback(View view) {
    stopScenario();
  }

  /**
   * Stops the scenario. The timer will be stopeped, the next time
   * the timer is called.
   * @author Johannes
   */
  public void stopScenario() {
    currentPositionEvent = 0;
    timeInSec = 0;
    updateTime();
    scenarioRunning = false;
  }

  /**
   * Pause the scenario
   */
  public void pauseScenario(View view) {
    scenarioRunning = false;
    currentPositionEvent--;
    // Send last Event to pause the timer on the monitor.
    currentEvent = (Event) listViewEvents
        .getItemAtPosition(currentPositionEvent);
    currentEvent.timerState = TimerState.PAUSE;
    MainActivity.server.out(currentEvent.toJson());
    scenarioPaused = true;
  }

  /**
   * Writes the current time since the start of the scenario in the status-bar. The format
   * is mm:ss.
   * Taken from Marc Pfeifer
   */
  private void updateTime() {
    // Schedule the update of the timer in main GUI-thread.
    runOnUiThread(new Runnable() {
      public void run() {
        TextView statusBarTitle = (TextView) findViewById(R.id.scenarioTimer);
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
   *  Select the previous event from the event list. Only used in the
   *  protocoll mode in the current implementation.
   * @param view
   * @author Johannes
   */
  public void previousEvent(View view) {
    if (currentEvents != null && currentPositionEvent != null) {
      if (currentPositionEvent == 0)
        currentPositionEvent = currentEvents.size() - 1;
      else
        currentPositionEvent--;
      listViewEvents.setItemChecked(currentPositionEvent, true);
      currentEvent = (Event) listViewEvents
          .getItemAtPosition(currentPositionEvent);
    }

  }

  /**
   * Make the scenario a protocol, which is only change the runnable value.
   * @param view
   * @author Johannes
   */
  public void changeToProtocol(View view) {
    if (currentScenario != null) {
      currentScenario.setRunnable(!currentScenario.isRunnable());
      scenarioHelper.setRunnableInDatabase(currentScenario);
      readScenariosToList();
      clearEventList();
    }
  }

  /*
   * Set up the action bar, if it is available
   */
  private void setupActionBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      // Show the Up button in the action bar.
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }
  }

  /*
   * Schedules a call to hide() the action bar in milliseconds,
   */
  private void delayedHide(int delayMillis) {
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        // Hide the action bar
        getActionBar().hide();
        // Reset flag
        WILL_HIDE = false;
      }
    }, delayMillis);
    // Flag that hiding is in progress
    WILL_HIDE = true;
  }
}
