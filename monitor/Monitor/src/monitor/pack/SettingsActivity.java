package monitor.pack;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Jo on 02.07.2015.
 */
public class SettingsActivity extends PreferenceActivity implements
    Preference.OnPreferenceChangeListener {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

/*

    Toast.makeText(this, "Einstellungen-Activity gestartet.", Toast.LENGTH_SHORT).show();
    Toast.makeText(this, "Zurück mit Back-Button.", Toast.LENGTH_SHORT).show();
*/
  }

  @Override
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.preference_headers, target);
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object value) {
    return false;
  }

  protected boolean isValidFragment(String fragmentName) {
    return SettingsFragment.class.getName().equals(fragmentName);
  }
}
