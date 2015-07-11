package monitor.pack;


import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

/**
 * Created by Jo on 29.06.2015.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
  private SharedPreferences defaultSharedPreferences;
  private GLActivity mainGlActivity;
  private MonitorMainScreen monitorMainScreen;
  private Resources resources;
  @Override
  public void onCreate(Bundle savedInstaceState) {
    super.onCreate(savedInstaceState);
    monitorMainScreen = MonitorMainScreen.getInstance();
    mainGlActivity = monitorMainScreen.getGlActivity();
    defaultSharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(monitorMainScreen);
    resources = getResources();

    String settings = getArguments().getString("settings");
    if ("alarms".equals(settings)) {
      addPreferencesFromResource(R.xml.settings_alarm);
    } else if ("colors".equals(settings)) {
      addPreferencesFromResource(R.xml.settings_colors);
    }

  }

    @Override
  public void onResume() {
    super.onResume();
    defaultSharedPreferences
        .registerOnSharedPreferenceChangeListener(this);
  }

  public void onPause() {
    super.onPause();
    defaultSharedPreferences
        .unregisterOnSharedPreferenceChangeListener(this);
  }

/*  *//**
   * set all summaries of thresholds and colors to saved values
   *//*
  private void setSummaries() {
    SharedPreferences sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(MonitorMainScreen.getInstance());
    Map<String, ?> preferenceMap = sharedPreferences.getAll();

    for (Map.Entry<String, ?> entry: preferenceMap.entrySet()) {
      String entryKey = entry.getKey();
      if (entryKey.endsWith("_threshold") || entryKey.endsWith("_color")) {
        setSummary(entryKey, sharedPreferences);
      }
    }
  }

  *//**
   * set new summary for preference field to the coresponding value
   * @param key
   * @param sharedPreferences
   *//*
  private void setSummary(String key, SharedPreferences sharedPreferences) {
    Preference preference = findPreference(key);
    if (preference == null) {
      Log.e("error", "preference with key " + key + " == null");
      return;
    }

    if(preference instanceof NumberPickerPreference) {
      preference.setSummary(Integer.toString(sharedPreferences.getInt(key,
          0)));
      Log.e("debug", "summary of " + key+"changed to "+Integer.toString(sharedPreferences.getInt(key,
          0)));
    } else if (preference instanceof ListPreference) {
      preference.setSummary(((ListPreference) preference).getEntry());
      Log.e("debug", "summary of " + key+"changed to "+((ListPreference) preference).getEntry());
    }
  }*/

/**
 * catch al changes in preferences
 *
 * @param sharedPreferences
 * @param key
 */ @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      // catch color changes
      // colors: String
      // thresolds: int
      // alarm-buttons: boolean
      if (key.endsWith("_color")) {
        String value = sharedPreferences.getString(key, "");

        changeColor(key, value);
      } else if(key.endsWith("_threshold")) {
//TODO: set thresholds
      } else if (key.endsWith("_alarm")) {
//TODO: set alarm on/off
      }
    }

    /**
     * Changes the color of the parameters/curves or background
     *
     * @param key   preference key
     * @param value preference value
     */
  private void changeColor(String key, String value) {
    int colorId;
    int color;
    int red;
    int green;
    int blue;
    switch (value) {
      case "red": {
        colorId = R.color.red;
        break;
      }
      case "blue": {
        colorId = R.color.blue;
        break;
      }
      case "green": {
        colorId = R.color.green;
        break;
      }
      case "yellow": {
        colorId = R.color.yellow;
        break;
      }
      case "grey": {
        colorId = R.color.grey;
        break;
      }
      case "white": {
        colorId = R.color.white;
        break;
      }
      default: {
        colorId = R.color.black;
        break;
      }

    }
    color = resources.getColor(colorId);
    red = getRedInt(color);
    green = getGreenInt(color);
    blue = getBlueInt(color);
    if (key == getString(R.string.key_background_color)) {
      changeBackColor(key, color, red, green, blue);
    } else {
      changeLineColor(key, color, red, green, blue);
    }
  }

  private void changeLineColor(String key, int color, int red, int green, int blue) {
    // Change the color of the parameters/curve of the currently selected vital-parameter.
    if (key == getString(R.string.key_ecg_color)) {
      // Change the color of the EKG-parameters/curve.
      TextView ekgValueTextView = (TextView) monitorMainScreen.findViewById(R.id
          .ekgValueTextView);
      ekgValueTextView.setTextColor(color);
      mainGlActivity.SetColor(GLRenderer.LineType.Heart, red, green, blue);
    } else if (key == getString(R.string.key_rr_color)) {
      // Change the color of the blood pressure-parameters/curve.
      TextView ibpValueTextView = (TextView) monitorMainScreen.findViewById(R.id
          .ibpValueTextView);
      TextView nibpValueTextView = (TextView) monitorMainScreen.findViewById(R.id
          .nibpValueTextView);
      ibpValueTextView.setTextColor(color);
      nibpValueTextView.setTextColor(color);
      mainGlActivity.SetColor(GLRenderer.LineType.Blood, red, green, blue);
    } else if (key == getString(R.string.key_spo2_color)) {
      // Change the color of the O2-parameters/curve.
      TextView o2ValueTextView = (TextView) monitorMainScreen.findViewById(R.id
          .o2ValueTextView);
      o2ValueTextView.setTextColor(color);
      mainGlActivity.SetColor(GLRenderer.LineType.O2, red, green, blue);
    } else if (key == getString(R.string.key_etco2_color)) {
      // Change the color of the CO2-parameters/curve and respiration-parameter.
      TextView co2ValueTextView = (TextView) monitorMainScreen.findViewById(R.id
          .co2ValueTextView);
      TextView afValueTextView = (TextView) monitorMainScreen.findViewById(R.id
          .afValueTextView);
      co2ValueTextView.setTextColor(color);
      afValueTextView.setTextColor(color);
      mainGlActivity.SetColor(GLRenderer.LineType.CO2, red, green, blue);
    }
  }

  /**
   * Changes the background color of the whole parameters/curve-view according to a
   * pressed button.
   * @param value color-value
   * @param color color as int
   * @param red red-value
   * @param green green-value
   * @param blue blue value
   */
  private void changeBackColor(String value, int color, int red, int green, int blue) {
    // Get all relevant layout-elements.
    RelativeLayout ekgParamLayout = (RelativeLayout) monitorMainScreen.findViewById(R.id.ekgParamLayout);
    RelativeLayout ibpParamLayout = (RelativeLayout) monitorMainScreen.findViewById(R.id.ibpParamLayout);
    RelativeLayout o2ParamLayout = (RelativeLayout) monitorMainScreen.findViewById(R.id.o2ParamLayout);
    RelativeLayout co2ParamLayout = (RelativeLayout) monitorMainScreen.findViewById(R.id.co2ParamLayout);
    RelativeLayout nibpParamLayout = (RelativeLayout) monitorMainScreen.findViewById(R.id.nibpParamLayout);
    RelativeLayout nibpSettingsLayout = (RelativeLayout) monitorMainScreen.findViewById(R.id.nibpSettingsLayout);
    RelativeLayout defiLayout = (RelativeLayout) monitorMainScreen.findViewById(R.id.defiLayout);
    TextView ekgTitleTextView = (TextView) monitorMainScreen.findViewById(R.id.ekgTitleTextView);
    TextView ibpTitleTextView = (TextView) monitorMainScreen.findViewById(R.id.ibpTitleTextView);
    TextView ibpUnitTextView = (TextView) monitorMainScreen.findViewById(R.id.ibpUnitTextView);
    TextView o2TitleTextView = (TextView) monitorMainScreen.findViewById(R.id.o2TitleTextView);
    TextView o2UnitTextView = (TextView) monitorMainScreen.findViewById(R.id.o2UnitTextView);
    TextView co2TitleTextView = (TextView) monitorMainScreen.findViewById(R.id.co2TitleTextView);
    TextView co2UnitTextView = (TextView) monitorMainScreen.findViewById(R.id.co2UnitTextView);
    TextView afTitleTextView = (TextView) monitorMainScreen.findViewById(R.id.afTitleTextView);
    TextView nibpSettingsTitleTextView = (TextView) monitorMainScreen.findViewById(R.id.nibpSettingsTitleTextView);
    TextView nibpAutoTimeTextView = (TextView) monitorMainScreen.findViewById(R.id.nibpAutoTimeTextView);
    TextView nibpParamTitleTextView = (TextView) monitorMainScreen.findViewById(R.id.nibpParamTitleTextView);
    TextView nibpUnitTextView = (TextView) monitorMainScreen.findViewById(R.id.nibpUnitTextView);
    TextView defiTitletextView = (TextView) monitorMainScreen.findViewById(R.id.defiTitletextView);
    TextView defiEnergy = (TextView) monitorMainScreen.findViewById(R.id.defiEnergy);
    // Distinguish which color was selected and save it.

    int defiEnergyColor = 0;
    int drawable = 0;
    if (value == "white") {
      color = Color.BLACK;
      defiEnergyColor = Color.WHITE;
      drawable = R.drawable.border_white_back;
      // Change background color of GL-part.
      mainGlActivity.SetColor(GLRenderer.LineType.Background, 255, 255, 255);
    } else {
      color = Color.WHITE;
      defiEnergyColor = Color.BLACK;
      drawable = R.drawable.border_black_back;
      // Change background color of GL-part.
      mainGlActivity.SetColor(GLRenderer.LineType.Background, 0, 0, 0);
    }
    // Set the new color in all relevant layout-elements.
    ekgParamLayout.setBackgroundResource(drawable);
    ibpParamLayout.setBackgroundResource(drawable);
    o2ParamLayout.setBackgroundResource(drawable);
    co2ParamLayout.setBackgroundResource(drawable);
    nibpParamLayout.setBackgroundResource(drawable);
    nibpSettingsLayout.setBackgroundResource(drawable);
    defiLayout.setBackgroundResource(drawable);
    ekgTitleTextView.setTextColor(color);
    ibpTitleTextView.setTextColor(color);
    ibpUnitTextView.setTextColor(color);
    o2TitleTextView.setTextColor(color);
    o2UnitTextView.setTextColor(color);
    co2TitleTextView.setTextColor(color);
    co2UnitTextView.setTextColor(color);
    afTitleTextView.setTextColor(color);
    nibpSettingsTitleTextView.setTextColor(color);
    nibpAutoTimeTextView.setTextColor(color);
    nibpParamTitleTextView.setTextColor(color);
    nibpUnitTextView.setTextColor(color);
    defiTitletextView.setTextColor(color);
    defiEnergy.setBackgroundColor(color);
    defiEnergy.setTextColor(defiEnergyColor);
  }

  private int getRedInt(int colorHex) {
    return (colorHex >> 16) & 0xFF;
  }

  private int getGreenInt(int colorHex) {
    return (colorHex >> 8) & 0xFF;
  }

  private int getBlueInt(int colorHex) {
    return (colorHex >> 0) & 0xFF;
  }


}
/*  @Override
  public void onResume(){
    super.onResume();
    getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
  }
  public void onPause(){
    super.onPause();
    getPreferenceScreen().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);
  }*/



