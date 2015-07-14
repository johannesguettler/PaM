package monitor.pack;

import android.content.Context;


import android.opengl.GLSurfaceView;

public class GLActivity extends GLSurfaceView {

  private GLRenderer renderer;

  public GLActivity(Context context, int lineHeartDefaultColor, int lineBloodDefaultColor, int
      lineO2DefaultColor, int lineCo2DefaultColor) {
    super(context);
    //OpenGL ES2
    setEGLContextClientVersion(2);
    setEGLConfigChooser(8, 8, 8, 8, 16, 0);
    //Intialize renderer
    renderer = new GLRenderer(lineHeartDefaultColor, lineBloodDefaultColor,
    lineO2DefaultColor, lineCo2DefaultColor);
    setRenderer(renderer);
   
  }


  //Public Setter for Lines and Background
  public void SetColor(GLRenderer.LineType line, int r, int g, int b) {
    if (renderer != null) {
      renderer.SetColor(line, r / 255f, g / 255f, b / 255f);
    }
  }

  //Enable/Disable given Line
  public void ToogleLine(GLRenderer.LineType line, boolean draw) {
    if (renderer != null) {
      renderer.ToogleLine(line, draw);
    }
  }

  //Set SignalServer
  public void setSignalserver(Signalserver s) {
    //Render shouldn't be equal null
    if (renderer != null) {
      renderer.setSignalserver(s);
    }
  }

}
