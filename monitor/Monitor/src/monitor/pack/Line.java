package monitor.pack;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Line {
  //Shaders
  private final String vertexShaderCode =
      // This matrix member variable provides a hook to manipulate
      // the coordinates of the objects that use this vertex shader
      "uniform mat4 uMVPMatrix;" +
          "attribute vec4 vPosition;" +
          "void main() {" +
          // the matrix must be included as a modifier of gl_Position
          // Note that the uMVPMatrix factor *must be first* in order
          // for the matrix multiplication product to be correct.
          "  gl_Position = uMVPMatrix * vPosition;" +
          "}";

  private final String fragmentShaderCode =
      "precision mediump float;" +
          "uniform vec4 vColor;" +
          "void main() {" +
          "  gl_FragColor = vColor;" +
          "}";
  private FloatBuffer vertexBuffer;
  private final int program;
  private int positionHandle;
  private int colorHandle;
  private int mvPMatrixHandle;
  private int pos = 0;
  private boolean gap;
  private boolean fill = false;
  private boolean draw = true;

  // number of coordinates per vertex in this array
  private static final int COORDS_PER_VERTEX = 3;
  private int resolution;
  private float lineCoords[];
  private int vertexCount;
  private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

  // Set color with red, green, blue and alpha (opacity) values
  private float color[] = {1F, 0.0F, 0.0f, 1.0f};


  //Constructor
  public Line(int res, boolean gap) {
    this.gap = gap;
    resolution = res;
    lineCoords = new float[resolution * 3];
    vertexCount = lineCoords.length / COORDS_PER_VERTEX;
    for (int i = 0; i < resolution; i++) {
      lineCoords[i * 3] = -1F + (2f * i / (resolution - 1));
      lineCoords[i * 3 + 1] = 0F;
      lineCoords[i * 3 + 2] = 0F;
    }
    // initialize vertex byte buffer for shape coordinates
    ByteBuffer bb = ByteBuffer.allocateDirect(
        // (number of coordinate values * 4 bytes per float)
        (lineCoords.length + 3) * 4);
    // use the device hardware's native byte order
    bb.order(ByteOrder.nativeOrder());

    // create a floating point buffer from the ByteBuffer
    vertexBuffer = bb.asFloatBuffer();
    // add the coordinates to the FloatBuffer
    vertexBuffer.put(lineCoords);
    // set the buffer to read the first coordinate
    vertexBuffer.position(0);
    // prepare shaders and OpenGL program
    int vertexShader = GLRenderer.loadShader(
        GLES20.GL_VERTEX_SHADER, vertexShaderCode);
    int fragmentShader = GLRenderer.loadShader(
        GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
    program = GLES20.glCreateProgram();             // create empty OpenGL Program
    GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
    GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
    GLES20.glLinkProgram(program);                  // create OpenGL program executables
  }

  //Set Color of Line
  public void setColor(float r, float g, float b) {
    color[0] = r;
    color[1] = g;
    color[2] = b;
  }

  //Set new Point received from the SignalServer
  public void setValue(float val) {
    vertexBuffer.clear();

    lineCoords[pos * 3 + 1] = val;
    vertexBuffer.put(lineCoords);
    // set the buffer to read the first coordinate
    vertexBuffer.position(0);
    pos++;
    if (pos >= resolution) {
      pos = 0;
    }
  }

  /**
   * Encapsulates the OpenGL ES instructions for drawing this shape.
   *
   * @param mvpMatrix - The Model View Project matrix in which to draw
   *                  this shape.
   */
  public void draw(float[] mvpMatrix) {
    if (!draw) {
      return;
    }
    // Add program to OpenGL environment
    GLES20.glUseProgram(program);

    // get handle to vertex shader's vPosition member
    positionHandle = GLES20.glGetAttribLocation(program, "vPosition");

    // Enable a handle to the triangle vertices
    GLES20.glEnableVertexAttribArray(positionHandle);

    // Prepare the triangle coordinate data
    GLES20.glVertexAttribPointer(
        positionHandle, COORDS_PER_VERTEX,
        GLES20.GL_FLOAT, false,
        vertexStride, vertexBuffer);

    // get handle to fragment shader's vColor member
    colorHandle = GLES20.glGetUniformLocation(program, "vColor");

    // Set color for drawing the triangle
    GLES20.glUniform4fv(colorHandle, 1, color, 0);

    // get handle to shape's transformation matrix
    mvPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

    // Apply the projection and view transformation
    GLES20.glUniformMatrix4fv(mvPMatrixHandle, 1, false, mvpMatrix, 0);
    //Check if we need a small gap
    if (gap) {
      if (fill) {
        int i = 0;
        while (i < resolution - 1) {

          vertexBuffer.clear();
          int start = i;
          //Nothing to Fill
          if (lineCoords[i * 3 + 1] == 0) {
            //Loop through all points which equals 0
            while (i < resolution - 1 && lineCoords[i * 3 + 1] == 0 && !(i >= pos && i < pos + resolution / 100)) {
              i++;
            }

            vertexBuffer.put(lineCoords, start * 3, (i - start) * 3);
            vertexBuffer.position(0);
            if (pos >= resolution) {
              pos = 0;
            }
            GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, (i - start));
          } else {
            //We got something to fill
            //Add Vertex as central Point of all Triangles
            vertexBuffer.put(new float[]{-1F + (2f * i / (resolution -1)),0,0});
            while(i < resolution -1 && lineCoords[i*3+1] != 0 && !(i >=
                pos && i < pos + resolution/100))
              i++;
            /*vertexBuffer.put(new float[]{-1F + (2f * i / (resolution -1)),0,
                0});*/
            vertexBuffer.put(lineCoords,start*3,(i-start)*3);
            //Add HelperVertex to fill whole Curve
            vertexBuffer.put(new float[]{-1F + (2f * i / (resolution -1)),0,
                0});
            vertexBuffer.position(0);
            GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, i);
            //We need at least 3 Vertexes for a triangle
            if(i - start > 2)
              GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, i);
          }
          while (i >= pos && i < pos + resolution / 100) {
            i++;
          }
        }
      } else {
        int startPos = pos + resolution / 100;
        int tempPos = startPos;
        //Check if the gap is at the beginning of the line
        startPos = startPos >= resolution ? startPos - resolution : 0;
        //Draw Line before Gap
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, startPos, pos - startPos);
        //Check if we need a line after gap
        if (tempPos < resolution)
        //Draw Line after Gap
        {
          GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, tempPos, vertexCount - tempPos);
        }
      }
    } else //No GAP
    {
      GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertexCount);
    }
    // Disable vertex array
    GLES20.glDisableVertexAttribArray(positionHandle);

  }

  //Draw/Hide Line
  public void setDrawAble(boolean draw) {
    this.draw = draw;
  }

  //Draw getter
  public boolean getDrawAble() {
    return draw;
  }

  //Enable/Disable Linefilling
  public void setFill(boolean fill) {
    this.fill = fill;
  }
}
