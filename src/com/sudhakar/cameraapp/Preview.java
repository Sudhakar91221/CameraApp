package com.sudhakar.cameraapp;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class Preview extends SurfaceView implements SurfaceHolder.Callback { 

	private SurfaceHolder holder; 
	private Camera camera; 
	private static Context mContext;
	private CameraCallback callback=null;
	private int picW=0,picH=0;
	private int currW,currH;

	int camera_id = 1;
	@SuppressWarnings("deprecation")
	public Preview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mContext=context;

		getDisplayMetrix();

		holder = getHolder(); 
		holder.setFixedSize(currW, currH);

		holder.addCallback(this); 

		holder.setKeepScreenOn(true);

		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	public void autoFocus(){
		camera.autoFocus(new AutoFocusCallback(){
			@Override
			public void onAutoFocus(boolean arg0, Camera arg1) {
				//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
			}
		});
	}
	@SuppressWarnings("deprecation")
	public Preview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		getDisplayMetrix();
		holder = getHolder(); 
		holder.setFixedSize(currW, currH);
		holder.addCallback(this); 
		holder.setKeepScreenOn(true);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@SuppressWarnings("deprecation")
	public Preview(Context context) { 
		super(context); 
		mContext = context;
		getDisplayMetrix();
		holder = getHolder(); 
		holder.setFixedSize(currW, currH);
		holder.addCallback(this); 
		holder.setKeepScreenOn(true);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
	} 

	private PictureCallback pictureCallback = new PictureCallback() { 
		@Override 
		public void onPictureTaken(byte[] data, Camera camera) { 
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			log("data[]="+data+" bmp="+bmp+" camera="+camera);

			if(null!=callback)  
				callback.onCameraEnd(bmp);
		} 
	}; 

	public void surfaceCreated(SurfaceHolder holder) { 
		try { 
			camera = Camera.open(); 
			camera.setPreviewDisplay(holder); 
			camera.setDisplayOrientation(getPreviewDegree());
			camera.startPreview();
			if(null!=callback) callback.onCameraStart();
		} catch (IOException exception) { 
			camera.release(); 
			camera = null; 
		} 
	} 

	public void surfaceDestroyed(SurfaceHolder holder) { 
		if(camera != null)
			camera.stopPreview();
		camera.release();
	} 

	@SuppressWarnings("deprecation")
	public void surfaceChanged(final SurfaceHolder holder, int format, int w, int h) { 
		try { 
			Camera.Parameters parameters = camera.getParameters(); 
			
			parameters.set("camera_id", 1);
			parameters.setPictureFormat(PixelFormat.JPEG); 
			if (((Activity)mContext).getWindowManager().getDefaultDisplay().getOrientation() == 0) 
				parameters.setPreviewSize(w, h);
			else parameters.setPreviewSize(h, w); 
			parameters.setPreviewFrameRate(3);
			parameters.setJpegQuality(90);
			
			Log.d("tag", "w="+w+"h="+h);
			if(picH==0||picW==0){
				parameters.setPictureSize(currW, currH);
			}
			else{
				parameters.setPictureSize(picW, picH);	
			}

			parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
			if(null != callback) callback.onCameraChanged();

			camera.autoFocus(new AutoFocusCallback() { 
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					if(null!=callback)  callback.onCameraFocus(success);
				} 
			}); 
		} catch (Exception e) { 
		} 
	} 

	public int getPreviewDegree() {
		int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();

		Log.v(getClass().getName(), "Rotation = "+rotation);
		int degree = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 90;
			break;
		case Surface.ROTATION_90:
			degree = 0;
			break;
		case Surface.ROTATION_180:
			degree = 270;
			break;
		case Surface.ROTATION_270:
			degree = 180;
			break;
		}

		return degree;
	}

	public void takePicture() { 
		if (camera != null) {
			camera.takePicture(null, null, pictureCallback);
		}

	} 

	public void pausePreview(){
		if(camera != null){
			camera.stopPreview();
		}
	}

	public void resumePreview(){
		if(camera != null) camera.startPreview();
	}

	public void setOnCameraCallback(CameraCallback callback){
		this.callback=callback;
	}
	public void setPictureSize(int width,int height){
		this.picW=width;
		this.picH=height;
	}

	public void getDisplayMetrix(){
		DisplayMetrics dm=new DisplayMetrics();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
		this.currH = dm.heightPixels;
		this.currW = dm.widthPixels;
	}

	public interface CameraCallback{
		void onCameraStart();

		void onCameraChanged();

		void onCameraEnd(Bitmap bitmap);

		void onCameraFocus(boolean bool);
	}

	void log(String msg){
		Log.d(getClass().getSimpleName(), msg);
	}

	@SuppressLint("NewApi")
	public void frontCamera(boolean flag){

		if (Camera.getNumberOfCameras() >= 2) {

			try{
				if (flag == true) {
					//if you want to open front facing camera use this line   
					camera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
				} else if (flag == false) {
					//if you want to use the back facing camera
					camera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
				}
				holder = getHolder(); 
				surfaceCreated(holder);
			} catch(Exception exception){
				Toast.makeText(mContext, ""+exception.getMessage(), Toast.LENGTH_SHORT).show();
			}			
//			return true; 

		}else {
			Toast.makeText(mContext, "No Front Camera available.", Toast.LENGTH_SHORT).show();
//			return true; 
		}
	}
} 