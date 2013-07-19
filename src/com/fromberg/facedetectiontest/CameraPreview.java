package com.fromberg.facedetectiontest;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.hardware.Camera.FaceDetectionListener;

public class CameraPreview extends SurfaceView implements Callback {

	private android.view.SurfaceHolder mHolder;
	private Camera mCamera;
	private FaceDetectionListener faceDetectionListener;
	
	public CameraPreview(Context context, Camera camera, MyFaceDetectionListener listener){
		super(context);
		mCamera = camera;
		
		//install a SurfaceHolder.Callback so we get notified when the surface is created and destroyed
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated but required on Android versions prior to 3.0
		//mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		faceDetectionListener = listener;
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay (holder);
			mCamera.startPreview();
			mCamera.setFaceDetectionListener(faceDetectionListener);
			mCamera.startFaceDetection();
		}
		catch (Exception e) {
			Log.d ("FaceDetectionTest", "Error setting camera preview: " + e.getMessage());
		}
	}
	
	public void surfaceDestroyed (SurfaceHolder holder) {
		// empty. take care of releasing camera preview in activity
		
	}
	
	public void surfaceChanged (SurfaceHolder holder, int format, int w, int h) {
		// if preview can change or rotate,  take care of those events here.
		// make sure to stop preview  before resizing or reformatting it.
		if (mHolder.getSurface() == null)
		{
			// preview surface does not exist
			return;
		}
		
		// stop preview before making changes
		try {
			mCamera.stopFaceDetection();
			mCamera.stopPreview();
		}
		catch (Exception e) {
			//ignore, tried to stop a non-existent preview
		}
		
		// set preview size and make any resize, rotate, or reformatting changes here
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
			mCamera.startFaceDetection();
		}
		catch (Exception e) {
			Log.d ("FaceDetectionTest", "Error starting camera preview: " + e.getMessage());
		}
	}
}
