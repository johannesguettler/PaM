package monitor.pack;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements Renderer {
  public enum LineType {Background, Heart, Blood, O2, CO2, AF, Trenner}

  private Line lineHeart;
  private Line lineBlood;
  private Line lineO2;
  private Line lineCO2;
  private Line lineAF;
  private Line lineTrenner;
  private int width;
  private int height;
  private float bgColor[] = {0.0F, 0.0F, 0.0f, 1.0f};
  private double lastUpdate = -1;
  private double deltaTime = 0;
  private Signalserver signalServer;
  // mvPMatrix is an abbreviation for "Model View Projection Matrix"
  private final float[] mvPMatrix = new float[16];
  private final float[] projectionMatrix = new float[16];
  private final float[] viewMatrix = new float[16];

  public GLRenderer() {
    // initialize Lines do this before onSurfaceCreated() is called, therfore
    // it's in the constructor
    //Init HeartLine
    if (lineHeart == null) {
      lineHeart = new Line(500, true);
      lineHeart.setColor(0f, 1f, 0f);
    }
    //Init BloodLine
    if (lineBlood == null) {
      lineBlood = new Line(500, true);
      lineBlood.setColor(1f, 0f, 0f);
    }
    //Init O2Line
    if (lineO2 == null) {
      lineO2 = new Line(500, true);
      lineO2.setColor(1f, 1f, 0f);
    }
    //Init CO2Line
    if (lineCO2 == null) {
      lineCO2 = new Line(500, true);
      lineCO2.setColor(0.5f, 0.5f, 0.5f);
      lineCO2.setFill(true);
    }
    //Init AFLine
    if (lineAF == null) {
      lineAF = new Line(500, true);
      lineAF.setColor(1f, 1f, 0f);
    }
    //Init Line between curves
    if (lineTrenner == null) {
      lineTrenner = new Line(2, false);
      lineTrenner.setColor(0.1f, 0.1f, 0.1f);
      lineTrenner.setValue(0);
      lineTrenner.setValue(0);
      lineTrenner.setDrawAble(true);
    }

  }

  //Loads OpenGL Shaders
  static int loadShader(int type, String shaderCode) {

    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
    int shader = GLES20.glCreateShader(type);

    // add the source code to the shader and compile it
    GLES20.glShaderSource(shader, shaderCode);
    GLES20.glCompileShader(shader);

    return shader;
  }

  //Computes elapsed Time since last frame
  private double getDeltaTime() {
    if (lastUpdate == -1) {
      lastUpdate = System.nanoTime();
      return 0;
    }
    double currentTime = System.nanoTime();
    double delta = currentTime - lastUpdate;
    lastUpdate = currentTime;
    return delta;
  }


  //Draw all Curves
  @Override
  public void onDrawFrame(GL10 gl) {
    deltaTime += getDeltaTime() / 1000000;
    // Draw background color
    GLES20.glClearColor(bgColor[0], bgColor[1], bgColor[2], 1.0f);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    gl.glLineWidth(3f);
    // Set the camera position (View matrix)
    Matrix.setLookAtM(viewMatrix, 0, 0, 0, 1, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    float[] drawMatrix = new float[16];
    // Calculate the projection and view transformation
    Matrix.multiplyMM(mvPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    if (signalServer == null) return;
    //Get Data from SignalServer
    while (deltaTime > 1000f / 60f) {
      deltaTime -= 1000f / 60f;
      //EKG
      if (lineHeart.getDrawAble())
        lineHeart.setValue((float) signalServer.getHeartRateValue() / 250);
      else
        lineHeart.setValue(0);
      //Bloodpressure
      if (lineBlood.getDrawAble())
        lineBlood.setValue((float) signalServer.getBloodPressureValue() / 170);
      else
        lineBlood.setValue(0);
      //ETCO2
      if (lineCO2.getDrawAble())
        //lineCO2.setValue(50);
        lineCO2.setValue((float) signalServer.getCO2Value() / 250);
      else
        lineCO2.setValue(0);
      //SpO2
      if (lineO2.getDrawAble())
        lineO2.setValue((float) signalServer.getO2Value() / 250);
      else
        lineO2.setValue(0);
      signalServer.increment();
    }

    //Scissor Test for Curves
    gl.glEnable(GL10.GL_SCISSOR_TEST);
    // Draw EKG Line
    gl.glScissor(0, (int) (height * 0.71F), width, (int) (height * (0.29F)));
    drawMatrix = mvPMatrix.clone();
    Matrix.translateM(drawMatrix, 0, 0F, 0.55F, 0F);
    lineHeart.draw(drawMatrix);


    //Draw Bloodpressure Line
    gl.glScissor(0, (int) (height * 0.48F), width, (int) (height * (0.23F)));
    drawMatrix = mvPMatrix.clone();
    Matrix.translateM(drawMatrix, 0, 0F, -0.11F, 0F);
    lineBlood.draw(drawMatrix); //Translation = 0

    //Draw  O2 Line
    gl.glScissor(0, (int) (height * 0.24F), width, (int) (height * (0.23F)));
    drawMatrix = mvPMatrix.clone();
    Matrix.translateM(drawMatrix, 0, 0F, -0.52F, 0F);
    lineO2.draw(drawMatrix);

    gl.glScissor(0, 0, width, (int) (height * (0.23F)));
    //Draw CO2 Line
    drawMatrix = mvPMatrix.clone();
    Matrix.translateM(drawMatrix, 0, 0F, -0.99F, 0F);
    lineCO2.draw(drawMatrix);

    gl.glDisable(GL10.GL_SCISSOR_TEST);
    //Trennlinien zwischen den einzelnen Kurven
    gl.glLineWidth(2f);
    drawMatrix = mvPMatrix.clone();
    Matrix.translateM(drawMatrix, 0, 0F, 0.415F, 0F);
    lineTrenner.draw(drawMatrix);
    drawMatrix = mvPMatrix.clone();
    Matrix.translateM(drawMatrix, 0, 0F, -0.056F, 0F);
    lineTrenner.draw(drawMatrix);
    drawMatrix = mvPMatrix.clone();
    Matrix.translateM(drawMatrix, 0, 0F, -0.528F, 0F);
    lineTrenner.draw(drawMatrix);
  }

  //Called if Size of RenderWindow changes
  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    GLES20.glViewport(0, 0, width, height);
    this.width = width;
    this.height = height;
    // this projection matrix is applied to object coordinates
    // in the onDrawFrame() method

    Matrix.orthoM(projectionMatrix, 0, -1, +1, -1, 1, 1, 7);


  }

  //Change Color of given Curve in r,g,b(0-1)
  public void SetColor(LineType line, float r, float g, float b) {
    if (line == LineType.AF)
      lineAF.setColor(r, g, b);
    if (line == LineType.Blood)
      lineBlood.setColor(r, g, b);
    if (line == LineType.CO2)
      lineCO2.setColor(r, g, b);
    if (line == LineType.Heart)
      lineHeart.setColor(r, g, b);
    if (line == LineType.O2)
      lineO2.setColor(r, g, b);
    if (line == LineType.Trenner)
      lineTrenner.setColor(r, g, b);
    if (line == LineType.Background) {
      bgColor[0] = r;
      bgColor[1] = g;
      bgColor[2] = b;
    }

  }

  //Enable/Disable Line
  public void ToogleLine(LineType line, boolean draw) {
    if (line == LineType.AF)
      lineAF.setDrawAble(draw);
    if (line == LineType.Blood)
      lineBlood.setDrawAble(draw);
    if (line == LineType.CO2)
      lineCO2.setDrawAble(draw);
    if (line == LineType.Heart)
      lineHeart.setDrawAble(draw);
    if (line == LineType.O2)
      lineO2.setDrawAble(draw);
  }

  //Initialze RenderWindow and Curves
  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    //Clear Framebuffer
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    //Enable Linesmoothing
    gl.glEnable(GL10.GL_LINE_SMOOTH);
    gl.glEnable(GL10.GL_BLEND);
    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
  }

  //Setter SignalServer
  public void setSignalserver(Signalserver s) {
    signalServer = s;
  }

}
