package monitor.pack;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

/**
 * Created by Jo on 04.07.2015.
 */
public class NumberPickerPreference extends DialogPreference {

  private int maxValue = 100;
  private int minValue = 0;

  private NumberPicker picker;
  private int value;

  public NumberPickerPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);

  }

  private void init(AttributeSet attrs) {
    TypedArray a=getContext().obtainStyledAttributes(
        attrs,
        R.styleable.NumberPickerPreferenceAttributes);

    // get minimum 7 maximum value
    minValue = a.getInteger(R.styleable.NumberPickerPreferenceAttributes_numberPickerMinValue, 0);
    String s = a.getString(R.styleable.NumberPickerPreferenceAttributes_numberPickerMinValue);
    Log.d("debug", (s == null)? "String is null!!!!!": s);
    maxValue = a.getInt(R.styleable.NumberPickerPreferenceAttributes_numberPickerMaxValue,
        200);

    setDefaultValue(a.getInteger(R.styleable
        .NumberPickerPreferenceAttributes_numberPickerDefaultValue, (minValue
        + maxValue) / 2));
    // set preference-summary to default value
    setSummary(Integer.toString(value));

    a.recycle();
  }
  public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected View onCreateDialogView() {
    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.gravity = Gravity.CENTER;

    picker = new NumberPicker(getContext());
    picker.setLayoutParams(layoutParams);

    FrameLayout dialogView = new FrameLayout(getContext());
    dialogView.addView(picker);

    return dialogView;
  }

  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);
    picker.setMinValue(minValue);
    picker.setMaxValue(maxValue);
    picker.setValue(getValue());
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      int newValue = picker.getValue();
      if (callChangeListener(newValue)) {
        setValue(newValue);
      }
    }
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return value;
  }

  @Override
  protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    setValue(restorePersistedValue ? getPersistedInt(minValue) : (Integer) defaultValue);
  }

  public void setValue(int value) {
    this.value = value;
    persistInt(this.value);
    // set new summary
    setSummary(Integer.toString(value));
  }

  public int getValue() {
    return this.value;
  }
}

