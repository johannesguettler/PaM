package gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pmcontroller1.R;

/**
 * Created by Jo on 21.09.2015.
 */
public class ProtocolFlagCrmDialogFragment extends DialogFragment {

  View localView;
  // Use this instance of the interface to deliver action events
  ProtocolFlagCrmDialogListener dialogListener;
  private boolean positiveRating;

  public boolean isPositiveRating() {
    return positiveRating;
  }

  public interface ProtocolFlagCrmDialogListener{
    public void onProtocolFlagCrmDialogPositiveClick(ProtocolFlagCrmDialogFragment dialog);
    public void onProtocolFlagCrmDialogNegativeClick(ProtocolFlagCrmDialogFragment dialog);
  }

  // Override the Fragment.onAttach() method to instantiate the
  // ProtocolFlagCrmDialogListener
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the DefiReactionDialogListener so we can send events to the host
      dialogListener = (ProtocolFlagCrmDialogListener) activity;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(activity.toString()
          + " must implement DefiReactionDialogListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    positiveRating = getArguments().getBoolean("positiveRating");
    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    localView = inflater.inflate(R.layout.protocol_flag_crm_dialog_layout,
        null);
    builder.setView(localView);

    TextView title = (TextView) localView.findViewById(R.id
        .protocol_flag_crm_dialog_title);
    String posNeg = (positiveRating)? "Positive": "Negative";
    title.setText(posNeg + " CRM-flag");

    // set radio group
    RadioGroup radioGroup = (RadioGroup) localView.findViewById(R.id
        .protocol_flag_crm_dialog_radio_group);
    RadioButton communicationRadioButton = (RadioButton)localView
        .findViewById(R.id.radio_button_communication);
    RadioButton teamRadioButton = (RadioButton)localView
        .findViewById(R.id.radio_button_team);
    RadioButton organisationRadioButton = (RadioButton)localView
        .findViewById(R.id.radio_button_organisation);
    RadioButton otherRadioButton = (RadioButton)localView
        .findViewById(R.id.radio_button_other);
    otherRadioButton.setChecked(true);

    // set apply and cancel buttons
    ProtocolFlagCrmDialogFragment instance = this;
    Button cancelButton = (Button)localView.findViewById(R.id
        .protocol_flag_crm_negative_button);
    Button applyButton = (Button) localView.findViewById(R.id
        .protocol_flag_crm_positive_button);
    applyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dialogListener.onProtocolFlagCrmDialogNegativeClick(instance);
        instance.dismiss();
      }
    });

    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        instance.dismiss();
      }
    });
    // Create the AlertDialog object and return it
    return builder.create();
  }
  public int getRadioGroupSelectetButtonId() {
    if(localView == null) {
      return -1;
    }
    RadioGroup radioGroup = (RadioGroup) localView.findViewById(R.id
        .protocol_flag_crm_dialog_radio_group);
    return radioGroup.getCheckedRadioButtonId();
  }
  public String getTextfieldText(){
    if(localView == null) {
      return "";
    }
    EditText editText = (EditText) localView.findViewById(R.id
        .protocol_flag_crm_edit_text);
    return editText.getText().toString();
  }
}
