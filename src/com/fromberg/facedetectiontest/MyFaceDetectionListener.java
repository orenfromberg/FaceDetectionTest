package com.fromberg.facedetectiontest;

import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.util.Log;

public class MyFaceDetectionListener implements FaceDetectionListener {
	
	public MainActivity mActivity;
	
	public MyFaceDetectionListener(MainActivity activity) {
		mActivity = activity;
	}
	
	@Override
	public void onFaceDetection(Face[] faces, Camera camera) {
		// TODO Auto-generated method stub
		//Log.d("FaceDetectionTest", "found " + faces.length + " faces out of " + camera.getParameters().getMaxNumDetectedFaces());
				
		// tell the renderer about the faces we found
		mActivity.mFaces = faces.clone();
		
	}

}
