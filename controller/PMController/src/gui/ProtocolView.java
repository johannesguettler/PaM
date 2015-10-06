package gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.HorizontalScrollView;

import java.sql.Time;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Scenario.Event;

/**
 * Created by Jo on 05.10.2015.
 */
public class ProtocolView extends View {
  private final List<Event> entries;
  private final int entryDistanceInSeconds;
  private int xDimension;
  private int yDimensionTable;
  private final int topCaptionHeight;
  private final int leftCaptionWidth;
  private final int flagBandHeight;
  private int columnDistance;
  private final int maxValue;


  public ProtocolView(Context context, int entryDistanceInSeconds, List<Event>
      entries) {
    super(context);
    this.entryDistanceInSeconds = entryDistanceInSeconds;
    this.entries = entries;

    flagBandHeight = 80;
    leftCaptionWidth = 40;
    topCaptionHeight = 30;
    columnDistance = 20;
    maxValue = 250;
  }


  @Override
  protected void onDraw(Canvas canvas) {
    // TODO Auto-generated method stub
    super.onDraw(canvas);

    // draw white background
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.WHITE);
    canvas.drawPaint(paint);

    int xDimension = getWidth();
    int yDimension = getHeight();

    yDimensionTable = yDimension - topCaptionHeight - flagBandHeight;
    int maxXDimensionTable = xDimension - leftCaptionWidth;
    int tempColumnDistance = maxXDimensionTable / 20;//entries.size();
    // TODO:use actual entry size
    if(tempColumnDistance > columnDistance){
      columnDistance = tempColumnDistance;
    }
    double pixelsPerValueUnit = (float)yDimensionTable / maxValue;
    drawTable(canvas, pixelsPerValueUnit);
    drawEvents(canvas, pixelsPerValueUnit);


    /*int x = getWidth();
    int y = getHeight();
    int radius;
    radius = 100;
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.WHITE);
    canvas.drawPaint(paint);
    // Use Color.parseColor to define HTML colors
    paint.setColor(Color.parseColor("#CD5C5C"));
    canvas.drawCircle(x / 2, y / 2, radius, paint);*/
  }

  private void drawEvents(Canvas canvas, double pixelsPerValueUnit) {


  }

  private void drawTable(Canvas canvas, double pixelsPerValueUnit) {
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.STROKE);
    paint.setColor(Color.BLACK);
    canvas.drawLine(leftCaptionWidth, topCaptionHeight, leftCaptionWidth,
        yDimensionTable+topCaptionHeight,paint);

    int x1 = leftCaptionWidth;
    int y1 = topCaptionHeight;
    int x2 = leftCaptionWidth;
    int y2 = yDimensionTable+topCaptionHeight;
    Date time = new Date(0);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    //TODO: to events.size()
    for (int i = 0; i <= 20; i++) {
      canvas.drawLine(x1, y1, x1, y2, paint);
      //Integer.toString(i * entryDistanceInSeconds)
      time.setTime((i * entryDistanceInSeconds) * 1000);
      canvas.drawText(simpleDateFormat.format(time),x1-7,
          topCaptionHeight-3,paint);

      x1 += columnDistance;
    }
    x2 = x1;
    x1 = leftCaptionWidth;
    for (int i = 0; i <= (maxValue / 50); i++) {
      canvas.drawLine(x1, y1, x2, y1, paint);
      int currentValue = maxValue - (i * 50);
      canvas.drawText(Integer.toString(currentValue), 15, y1+4, paint);
      y1 += pixelsPerValueUnit * 50;
    }


  }
}
