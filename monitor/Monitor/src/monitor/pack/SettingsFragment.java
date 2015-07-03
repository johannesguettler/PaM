package monitor.pack;


import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Jo on 29.06.2015.
 */
public class SettingsFragment extends PreferenceFragment {

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

}
