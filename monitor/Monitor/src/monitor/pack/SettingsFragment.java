package monitor.pack;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Jo on 29.06.2015.
 */
public class SettingsFragment extends PreferenceFragment implements
    SharedPreferences.OnSharedPreferenceChangeListener{

  @Override
  public void onCreate(Bundle savedInstaceState) {
    super.onCreate(savedInstaceState);

    String settings = getArguments().getString("settings");
    if ("alarms".equals(settings)) {
      addPreferencesFromResource(R.xml.settings_alarm);
    } else if ("colors".equals(settings)) {
      addPreferencesFromResource(R.xml.settings_colors);
    }
  }
  @Override
  public void onResume(){
    super.onResume();
    getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
  }
  public void onPause(){
    super.onPause();
    getPreferenceScreen().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Toast.makeText(getActivity(), key, Toast.LENGTH_LONG);
    Log.v("PREFERENCE CHANGE", "[" + key + "]");
    System.out.println("ÄNDERUNG mit Key: " + key);
  }
}
