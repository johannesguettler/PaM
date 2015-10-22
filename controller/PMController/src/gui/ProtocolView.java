package gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.example.pmcontroller1.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import Scenario.ProtocolEvent;
import Scenario.ProtocolEvent.Flag;

/**
 * Created by Jo on 05.10.2015.
 */
public class ProtocolView extends View {
  private final List<ProtocolEvent> entries;
  private final int entryDistanceInSeconds;
  private int xDimension;
  private int yDimensionTable;
  private final int topCaptionHeight;
  private final int leftCaptionWidth;
  private final int flagBandHeight;
  private final int valueBandHeight;
  private int columnDistance;
  private final int maxValue;
  private final int protocolEntries;
  private double pixelsPerValueUnit;
  private int lastEventXcenter = -100;
  private boolean lastFlagInSecondline = false;


  public ProtocolView(Context context, int entryDistanceInSeconds,
                      List<ProtocolEvent>
                          entries) {
    super(context);
    this.entryDistanceInSeconds = entryDistanceInSeconds;
    this.entries = entries;

    flagBandHeight = 80;
    valueBandHeight = 60;
    leftCaptionWidth = 60;
    topCaptionHeight = 30;
    columnDistance = 25;
    maxValue = 250;
    pixelsPerValueUnit = 0;

    // count protocol entries
    int counter = 0;
    for (ProtocolEvent event : entries) {
      if (!event.flag) {
        counter++;
      }
    }
    protocolEntries = counter;
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

    yDimensionTable = yDimension - topCaptionHeight - flagBandHeight - valueBandHeight;
    int maxXDimensionTable = xDimension - leftCaptionWidth;
    int tempColumnDistance = maxXDimensionTable / entries.size();
    if (tempColumnDistance > columnDistance) {
      columnDistance = tempColumnDistance;
    }
    pixelsPerValueUnit = (float) yDimensionTable / maxValue;
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
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setColor(Color.BLACK);
    paint.setStrokeWidth(paint.getStrokeWidth() * 4);
    int x1 = leftCaptionWidth;
    int y1 = topCaptionHeight;
    int x2 = leftCaptionWidth;
    int y2 = yDimensionTable + topCaptionHeight;
    for (int i = 0; i < entries.size(); i++) {
      ProtocolEvent event = entries.get(i);
      if (event.flag) {
        drawFlag(event, canvas);
        addFlagToList(event);
        String flagTypeString = (event.flagType == null ? "null" : event
            .flagType.toString());
        Log.e("ProtocolView.drawEvent", "draw flag. flag?: " + event.flag + " " +
            "flagType" + flagTypeString + "; flag Comment: " + event.flagComment);
      } else {

        Log.e("ProtocolView.drawEvent", "draw event. heartrate: " + event
            .heartRateTo);
        drawBloodPressure(x1, event.bloodPressureSys, event.bloodPressureDias,
            canvas, paint);
        drawHeartRate(x1, event.heartRateTo, canvas, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        y2 += valueBandHeight / 3;
        canvas.drawText(Integer.toString(event.oxygenTo), x1, y2 - 5, paint);
        y2 += valueBandHeight / 3;
        canvas.drawText(Integer.toString(event.carbTo), x1, y2 - 5, paint);
        y2 += valueBandHeight / 3;
        canvas.drawText(Integer.toString(event.respRate), x1, y2 - 5, paint);
        x1 += columnDistance;
        y2 = yDimensionTable + topCaptionHeight;
      }
    }
  }

  private void drawBreathingFrequency(int x, int y2, Integer carbTo, Canvas canvas, Paint paint) {

  }

  private void drawEtCo2(int x, int y, int co2Value, Canvas canvas, Paint paint) {

  }

  private void drawTable(Canvas canvas, double pixelsPerValueUnit) {
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.STROKE);
    paint.setColor(Color.BLACK);
    canvas.drawLine(leftCaptionWidth, topCaptionHeight, leftCaptionWidth,
        yDimensionTable + topCaptionHeight, paint);

    int x1 = leftCaptionWidth;
    int y1 = topCaptionHeight;
    int x2 = leftCaptionWidth;
    int y2 = yDimensionTable + topCaptionHeight;
    int lineOffsetLeft = 7;
    Date time = new Date(0);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    for (int i = 0; i <= protocolEntries; i++) {
      canvas.drawLine(x1, y1, x1, y2, paint);
      //Integer.toString(i * entryDistanceInSeconds)
      if (i % 2 == 0) {
        time.setTime((i * entryDistanceInSeconds) * 1000);
        canvas.drawText(simpleDateFormat.format(time), x1 - 15,
            topCaptionHeight - 3, paint);
      }
      x1 += columnDistance;
    }
    x2 = x1;
    x1 = leftCaptionWidth;
    for (int i = 0; i <= (maxValue / 50); i++) {
      canvas.drawLine(x1 - lineOffsetLeft, y1, x2, y1, paint);
      int currentValue = maxValue - (i * 50);
      canvas.drawText(Integer.toString(currentValue), leftCaptionWidth -
              (lineOffsetLeft + 22),
          y1 + 4, paint);
      y1 += pixelsPerValueUnit * 50;
    }
    Context context = getContext();
    String valueCaptions[] = {
        context.getString(R.string.protocol_spo2_line_caption),
        context.getString(R.string.protocol_etco2_line_caption),
        context.getString(R.string.protocol_breathing_frequency_line_caption)};

    y1 = yDimensionTable + topCaptionHeight;
    for (int i = 0; i < valueCaptions.length; i++) {
      y1 += valueBandHeight / valueCaptions.length;
      canvas.drawLine(x1 - lineOffsetLeft, y1, x2, y1, paint);
      int currentValue = maxValue - (i * 50);
      canvas.drawText(valueCaptions[i], leftCaptionWidth - (lineOffsetLeft +
              40),
          y1 - 5, paint);
    }
  }


  private void addFlagToList(ProtocolEvent event) {

  }

  private void drawSpO2(int x, int y, int spO2Value, Canvas canvas, Paint
      paint) {
    /*Paint paint = new Paint();
    paint.setColor(Color.BLACK);
    paint.setStyle(Paint.Style.STROKE);
    int y = (int) (topCaptionHeight + yDimensionTable - (spO2Value *
        pixelsPerValueUnit));
    canvas.drawCircle(x, y, 7f, paint);*/

  }

  private void drawHeartRate(int x, int heartRate, Canvas canvas, Paint
      paint) {
    int y = (int) (topCaptionHeight + yDimensionTable - (heartRate *
        pixelsPerValueUnit));
    canvas.drawCircle(x, y, 7f, paint);
  }

  private void drawBloodPressure(int x, int bloodPressureSys, int
      bloodPressureDias, Canvas canvas, Paint paint) {
    int arrowHeight = 11;
    int arrowWidth = 11;
    int ySys = (int) (topCaptionHeight + yDimensionTable - (bloodPressureSys *
        pixelsPerValueUnit));
    int yDia = (int) (topCaptionHeight + yDimensionTable - (bloodPressureDias *
        pixelsPerValueUnit));
    int halfRectThickness = 2;
    canvas.drawRect(x - halfRectThickness, ySys, x + halfRectThickness, yDia,
        paint);
    canvas.drawLine(x - arrowWidth, ySys - arrowHeight, x, ySys, paint);
    canvas.drawLine(x + arrowWidth, ySys - arrowHeight, x, ySys, paint);

    canvas.drawLine(x - arrowWidth, yDia + arrowHeight, x, yDia, paint);
    canvas.drawLine(x + arrowWidth, yDia + arrowHeight, x, yDia, paint);
  }

  private void drawFlag(ProtocolEvent event, Canvas canvas) {
    //TODO shockflag!
    int eventTime = event.timeStamp;
    int halfFlagSize = columnDistance / 4;
    if ((halfFlagSize * 4) > flagBandHeight) {
      halfFlagSize = flagBandHeight / 4;
    }
    int eventXcenter = leftCaptionWidth + (int) (((float) columnDistance /
        entryDistanceInSeconds) * eventTime);
    int eventYTop = topCaptionHeight + yDimensionTable + valueBandHeight + 8;

    int x1 = eventXcenter - halfFlagSize;
    int x2 = eventXcenter + halfFlagSize;
    int y1 = eventYTop;
    int y2 = eventYTop + 2 * halfFlagSize;
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    if (event.flagType != Flag.SHOCK) {
      if (((eventXcenter - lastEventXcenter) < (2 * halfFlagSize)) && !lastFlagInSecondline) {
        lastFlagInSecondline = true;
        y1 = topCaptionHeight + yDimensionTable + valueBandHeight +
            (flagBandHeight / 2);
        y2 = y1 + 2 * halfFlagSize;
      } else {
        lastFlagInSecondline = false;
      }
      lastEventXcenter = eventXcenter;
      paint.setColor(ProtocolEvent.getEventColor(event));
      paint.setTextAlign(Paint.Align.CENTER);
      String text = ProtocolEvent.getEventText(event, getContext());
      canvas.drawRoundRect(new RectF(x1, y1, x2, y2), (halfFlagSize / 4),
          (halfFlagSize / 4), paint);
      paint.setColor(Color.WHITE);
      canvas.drawText(text, eventXcenter, y1 + halfFlagSize + (halfFlagSize / 4), paint);
    } else {
      // draw shockflag
      y2 = topCaptionHeight + yDimensionTable;
      y1 = y2 - 2*halfFlagSize;
      Drawable shockImage = getContext().getResources().getDrawable(R
          .drawable.flag_shock);
      shockImage.setBounds(x1,y1,x2,y2);
      shockImage.draw(canvas);
    }
  }
}
