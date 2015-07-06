package monitor.pack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Jo on 02.07.2015.
 */
public class SettingsActivity extends PreferenceActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
/*    SharedPreferences sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(this);
    sharedPreferences.registerOnSharedPreferenceChangeListener(this);*/

   // getActionBar().setDisplayHomeAsUpEnabled(true);

/*

    Toast.makeText(this, "Einstellungen-Activity gestartet.", Toast.LENGTH_SHORT).show();
    Toast.makeText(this, "Zur�ck mit Back-Button.", Toast.LENGTH_SHORT).show();
*/
  }


  @Override
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.preference_headers, target);
  }

  protected boolean isValidFragment(String fragmentName) {
    return SettingsFragment.class.getName().equals(fragmentName);
  }

/*  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Toast.makeText(this, key, Toast.LENGTH_LONG);
    Log.v("PREFERENCE CHANGE", "[" + key + "]");
    *//*if (key.equals(R.strin)) {
      Preference connectionPref = findPreference(key);
      // Set summary to be the user-description for the selected value
      connectionPref.setSummary(sharedPreferences.getString(key, ""));
    }*//*

  }*/
}