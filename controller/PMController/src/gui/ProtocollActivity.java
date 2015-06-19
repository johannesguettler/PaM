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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.pmcontroller1.R;

import java.util.List;

import Scenario.Event;
import Scenario.EventAdapter;
import Scenario.Scenario;
import Scenario.ScenarioHelper;

public class ProtocollActivity extends Activity {

  @SuppressWarnings("unused")
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
  public static Integer currentPositionEvent;
  public static ListView listViewScenario;
  public static ListView listViewEvents;

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

    setContentView(R.layout.activity_protocoll);

    // Make the statusbar disappear at init
    if (AUTO_HIDE_ACTIVE) delayedHide(AUTO_HIDE_DELAY_MILLIS);


    if (AUTO_HIDE_ACTIVE) {
      // Set up the back view for onclicklistener
      final View backView = findViewById(R.id.protocoll_background_view);
      // If the background is clicked, the status bar should appear
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

    // Set the list adapter and read the scenario list.
    readScenariosToList();

    // Set the on item click listener for the scenario list.
    listViewScenario = (ListView) findViewById(R.id.listViewScenario);
    listViewScenario.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapter, View v,
                              int position, long arg3) {
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
        Event chosenEvent = (Event) adapter.getItemAtPosition(position);
        currentPositionEvent = position;
        currentEvent = chosenEvent;
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
   * Read all Protocols to the listview.
   * @author Johannes
   */
  public void readScenariosToList() {
    currentScenarios = scenarioHelper.getAllProtocols();
    try {
      ListAdapter adapter2 = new ArrayAdapter<Scenario>(getApplicationContext(),
          R.layout.my_simple_list_item_1, currentScenarios);
      ListView listViewScenario = (ListView) findViewById(R.id.listViewScenario);
      listViewScenario.setAdapter(adapter2);
    } finally {
    }
  }

  /**
   * Read the events of a protocol to the listview.
   * @author Johannes
   */
  public void readEventsToList(Scenario chosenScenario) {
    // Get the events from the chosen scenario.
    Event[] eventList = (Event[]) chosenScenario.getEventList().toArray(new Event[0]);
    eventAdapter = new EventAdapter(this, R.layout.event_item, eventList);
    ListView listViewEvents = (ListView) findViewById(R.id.listViewEvents);
    listViewEvents.setAdapter(eventAdapter);
    currentPositionEvent = 0;
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
   * Select the previous event from the event list. Only used in the protocoll-
   * mode at the moment.
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
   * Make the Protocol a Scenario. Not used in the current implementation.
   * @param view
   * @author Johannes
   */
  public void changeToScenario(View view) {
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
