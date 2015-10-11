package Scenario;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.pmcontroller1.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jo on 09.10.2015.
 */
public class FlagListAdapter extends ArrayAdapter<ProtocolEvent>{
  Context context;
  ProtocolEvent[] data = null;
  int layoutResourceId;

  public FlagListAdapter(Context context, int layoutResourceId,
                         ProtocolEvent[] data) {
    // TODO Auto-generated constructor stub
    super(context, layoutResourceId, data);
    this.context = context;
    this.layoutResourceId = layoutResourceId;
    this.data = data;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // TODO Auto-generated method stub
 /*   // 1. Create inflater
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    // 2. Get rowView from inflater
    View rowView = inflater.inflate(R.layout.flag_list_item, parent, false);

    // 3. Get the two text view from the rowView
    TextView titleTextView = (TextView) rowView.findViewById(R.id.flagListItemTitle);
    TextView commentTextView = (TextView) rowView.findViewById(R.id.flagListItemTextView);

    // 4. Set the text for textView
    String title = ProtocolEvent.getFullFlagDescription(data[position]
        .flagType, context);
    titleTextView.setText(title);
    titleTextView.setHeight(20);
    commentTextView.setText("hier könnte ihr kommentar stehen");

    // 5. retrn rowView
    return rowView;*/
    View convView = convertView;

    FlagHolder  holder= null;

    if(convView == null )
    {
      LayoutInflater inflater = ((Activity)context).getLayoutInflater();
      convView = inflater.inflate(layoutResourceId, parent, false);

      holder = new FlagHolder();
      holder.textViewTitle = (TextView) convView.findViewById(R.id
          .flagListItemTitle);
      Log.e("DEBUG FlagListAdapter", (holder.textViewTitle == null)?
          "textViewTitle == null!": ("textViewTitle == "+holder.textViewTitle
          .getText()));
      holder.textViewComment = (TextView) convView.findViewById(R.id
          .flagListItemTextView);
      holder.flagColorView = convView.findViewById(R.id
          .flagListItemColorLine);

      convView.setTag(holder);
    }
    else
    {
      holder = (FlagHolder)convView.getTag();
    }

    String title = ProtocolEvent.getFullFlagDescription(data[position]
        .flagType, context);
    Date time = new Date(0);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    time.setTime(data[position].timeStamp * 1000);
    title = "(" + simpleDateFormat.format(time)+") " + title;
    holder.textViewTitle.setTextSize(13);
    //holder.textViewTitle.setTextColor(Color.RED);
    holder.textViewTitle.setText(title);
    holder.textViewComment.setText(data[position].flagComment);
    holder.flagColorView.setBackgroundColor(ProtocolEvent.getEventColor
        (data[position]));
    return convView;
  }
  static class FlagHolder {
    View flagColorView;
    TextView textViewTitle;
    TextView textViewComment;
  }
}
