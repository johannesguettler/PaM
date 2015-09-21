package monitor.pack;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Jo on 09.09.2015.
 */
public class DefiFragment extends Fragment {
  View fragmentView;
  int energy = 0;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    fragmentView = inflater.inflate(R.layout.defi_fragment_layout, container,
        false);
    TextView energyTextView = (TextView) returnView().findViewById(R.id
        .defi_energy_textView);
    energyTextView.setText(String.valueOf(energy) + " J");
    return fragmentView;
  }
  public View returnView() {
    return fragmentView;
  }
  public void setEnergy(int newEnergy){
    energy = newEnergy;
  }
}