package com.fromberg.facedetectiontest;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup.LayoutParams; 

public class MainActivity extends Activity {

	public int mFrontCameraId = 0;
	public int mBackCameraId = 0;
	public Camera camera;
	private CameraPreview mPreview;
	private GLSurfaceView mView;
	public MyRenderer mRenderer;
	public MyFaceDetectionListener mFaceDetectionListener;
	public Face [] mFaces;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
        	Toast.makeText(this, "No Camera on this Device", Toast.LENGTH_LONG).show();
        	return;
        }

        mFrontCameraId = findFrontFacingCamera();
        mBackCameraId = findBackFacingCamera();
        
        int cameraId = mFrontCameraId; // use the front camera for now
        //int cameraId = mBackCameraId; // use the front camera for now

		try {
			camera = Camera.open(cameraId);
		} catch (Exception e) {
			Toast.makeText(this, "Camera could not be opened", Toast.LENGTH_LONG).show();
		}
		
		if (cameraId < 0) {
			Toast.makeText(this, "No Front Facing camera found",
					Toast.LENGTH_LONG).show();
			return;
		} 
		
		mFaceDetectionListener = new MyFaceDetectionListener (this);
		mPreview = new CameraPreview(this, camera, mFaceDetectionListener);
		setContentView(mPreview);
		
		mView = new GLSurfaceView(this);
		mView.setEGLContextClientVersion(2);
		mView.setZOrderMediaOverlay(true);
		mView.setEGLConfigChooser(8,8,8,8,16,0);
		mRenderer = new MyRenderer(this);
		mView.setRenderer(mRenderer);
		mView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

		addContentView(mView, new LayoutParams 
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 

		int maxNumFaces = camera.getParameters().getMaxNumDetectedFaces();
		Log.d("FaceDetectionTest", "max num detected faces = " + maxNumFaces);
    }
    
    protected void onPause() {
    	if (camera != null) {
    		camera.release();
    		camera = null;
    	}
    	super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private int findFrontFacingCamera() {
    	int cameraId = -1;
    	// search for the front facing camera
    	int numberOfCameras = Camera.getNumberOfCameras();
    	for (int i = 0; i < numberOfCameras; i++)
    	{
    		CameraInfo info = new CameraInfo();
    		Camera.getCameraInfo(i, info);
    		if (info.facing == CameraInfo.CAMERA_FACING_FRONT)
    		{
    			Log.d("FaceDetectionTest", "Camera found");
    			cameraId = i;
    			break;
    		}
    	}
    	return cameraId;
    }

    private int findBackFacingCamera() {
    	int cameraId = -1;
    	// search for the back facing camera
    	int numberOfCameras = Camera.getNumberOfCameras();
    	for (int i = 0; i < numberOfCameras; i++)
    	{
    		CameraInfo info = new CameraInfo();
    		Camera.getCameraInfo(i, info);
    		if (info.facing == CameraInfo.CAMERA_FACING_BACK)
    		{
    			Log.d("FaceDetectionTest", "Camera found");
    			cameraId = i;
    			break;
    		}
    	}
    	return cameraId;
    }
}
