package gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pmcontroller1.R;

/**
 * Created by Jo on 22.08.2015.
 */
public class DefiReactionDialogFragment extends DialogFragment {



  //
  Spinner heartRhythmSpinner;

  // Use this instance of the interface to deliver action events
  DefiReactionDialogListener dialogListener;
  /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
  public interface DefiReactionDialogListener {
    public void onDefiReactionDialogPositiveClick(DefiReactionDialogFragment dialog);
    public void onDefiReactionDialogNegativeClick(DefiReactionDialogFragment dialog);
  }



  // Override the Fragment.onAttach() method to instantiate the DefiReactionDialogListener
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the DefiReactionDialogListener so we can send events to the host
      dialogListener = (DefiReactionDialogListener) activity;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(activity.toString()
          + " must implement DefiReactionDialogListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    int energy = getArguments().getInt("energy", 0);
    String energyString = (energy == 0)? "???": Integer.toString(energy);

    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View localView = inflater.inflate(R.layout.defibrillation_reaction_popup,
        null);
    builder.setView(localView);

    //initialize Dialog Elements
    TextView dialogTitle = (TextView) localView.findViewById(R.id
        .defi_reaction_dialog_title);
    heartRhythmSpinner = (Spinner) localView.findViewById(R.id
        .defi_reaction_heart_rate_spinner);
    Button cancelButton = (Button)localView.findViewById(R.id
        .defi_reaction_negative_button);
    Button applyButton = (Button) localView.findViewById(R.id
        .defi_reaction_positive_button);

    // set Element functions
    dialogTitle.setText("Defibrillator opened.\nEnergy: " + energyString +
        " Joule.\n" + "Reaction on shock?");
    ImageArrayAdapter heartRateImageArrayAdapter= new ImageArrayAdapter(getActivity(),
        new Integer[]{R.drawable.hr_sine,
            R.drawable.hr_absolute_arrhythmie,
            R.drawable.hr_avblock,
            R.drawable.hr_leftblock,
            R.drawable.hr_leftblock_aa,
            R.drawable.hr_stemi,
            R.drawable.hr_pacer,
            R.drawable.hr_ventriflutter,
            R.drawable.hr_ventifibri,
            R.drawable.hr_cpr,
            R.drawable.hr_asystoly});
    heartRhythmSpinner.setAdapter(heartRateImageArrayAdapter);
    heartRhythmSpinner.setDropDownWidth((int) getResources().getDimension(R.dimen.slider_value_max));

    DefiReactionDialogFragment instance = this;
    applyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dialogListener.onDefiReactionDialogPositiveClick(instance);
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
  public int getHeartRhythmSpinnerItemPosition() {
    return heartRhythmSpinner.getSelectedItemPosition();
  }
}
