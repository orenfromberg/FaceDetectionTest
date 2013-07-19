package com.fromberg.facedetectiontest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.hardware.Camera.Face;

class MyRenderer implements GLSurfaceView.Renderer {

    public MyRenderer(Context context) {
        mContext = context;
    }
    
    public void onDrawFrame(GL10 glUnused) {

        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        
		Face[] faces = ((MainActivity) mContext).mFaces;
		if (faces != null) {
			for (int i = 0; i < faces.length; i++) {
				drawBoxAroundFace(faces[i]);
			}
		}		        
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
    	// no viewport ! we are not going from view to normalized device coordinates.
    	// we are already in normalized device coords!
    	//GLES20.glViewport(0, 0, width, height);
   	}

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

		// prepare shaders and OpenGL program
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader
														// to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment
															// shader to program
		GLES20.glLinkProgram(mProgram); // create OpenGL program executables
		
		bb = ByteBuffer.allocateDirect(
		// (number of coordinate values * 4 bytes per float)
				quadCoords.length * 4);
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());

		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(quadCoords);        

    	GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        // here create the transform used to go from camera device coordinates to OpenGL normalized device coordinates.
        
        // 1st do a mirror over x since we are using the front camera
        mCamDeviceToOpenGLMatrix.setScale(-1, 1);
        
        // 2nd do a scale and flip over y since we are going from camera device to OpenGL normalized
        mCamDeviceToOpenGLMatrix.postScale(.001f, -.001f);

    }

    public int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }
	
    private void drawBoxAroundFace(Face face)
    {
    	myRect.bottom = face.rect.bottom;
    	myRect.top = face.rect.top;
    	myRect.left = face.rect.left;
    	myRect.right = face.rect.right;

    	mCamDeviceToOpenGLMatrix.mapRect(myRect);
    	
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		quadCoords[0] = myRect.right;
		quadCoords[1] = myRect.top;
		quadCoords[2] = 0.0f;
		quadCoords[3] = myRect.left;
		quadCoords[4] = myRect.top;
		quadCoords[5] = 0.0f;
		quadCoords[6] = myRect.left;
		quadCoords[7] = myRect.bottom;
		quadCoords[8] = 0.0f;
		quadCoords[9] = myRect.right;
		quadCoords[10] = myRect.bottom;
		quadCoords[11] = 0.0f;

		vertexBuffer.position(0);
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(quadCoords);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

		// get handle to fragment shader's vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

		// Set color for drawing the triangle
		
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);

		GLES20.glLineWidth(10.0f);

		// Draw the triangle
		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, vertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);   	
    }

    private Context mContext;
    private static String TAG = "MyRenderer";
    
	private final String vertexShaderCode = "attribute vec4 vPosition;"
			+ "void main() {" + "  gl_Position = vPosition;" + "}";

	private final String fragmentShaderCode = "precision mediump float;"
			+ "uniform vec4 vColor;" + "void main() {"
			+ "  gl_FragColor = vColor;" + "}";

	private int mProgram;
	private int mPositionHandle;
	private int mColorHandle;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;

	float [] quadCoords = new float[12];
	
	private final int vertexCount = quadCoords.length / COORDS_PER_VERTEX;
	private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex

	// Set color with red, green, blue and alpha (opacity) values
	float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };
	
	private RectF myRect = new RectF();

	private ByteBuffer bb;
	private FloatBuffer vertexBuffer;

	private android.graphics.Matrix mCamDeviceToOpenGLMatrix = new android.graphics.Matrix();
}
