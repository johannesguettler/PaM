package monitor.pack;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Jo on 09.09.2015.
 */
public class DefiFragment extends Fragment {
  View fragmentView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    fragmentView = inflater.inflate(R.layout.defi_fragment_layout, container,
        false);

    return fragmentView;
  }
  public View returnView() {
    return fragmentView;
  }
}