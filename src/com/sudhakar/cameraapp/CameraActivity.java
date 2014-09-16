package com.sudhakar.cameraapp;

/**
 * @author Sudhakar Pachava Tirupati
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CameraActivity extends BaseActivity implements Preview.CameraCallback, OnClickListener{

	LinearLayout bottom;
//	View markView = null;  
	ImageButton takepicture = null; 
	Preview preview;
	Bitmap result = null;
	Bitmap mark = null;  

	PictureClickListener listener; 

	private String lock = "lock";

	private int picWidth;

	private boolean flag;
	public static ImageButton frontCam;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_camera);
		super.startInit();
	}

	@Override
	public void onCameraStart() {
		log("onCameraStart");

//		mark = ImageHandler.getViewBitmap(markView);
		computeImageWH();
	}

	@Override
	public void onCameraChanged() {
		log("onCameraChanged");
		synchronized (lock) {
			result = null;	
		}
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCameraEnd(final Bitmap bitmap) {
		log("onCameraEnd");

		preview.pausePreview();
		//		focus.setVisibility(View.GONE);
		synchronized (lock) {
			Bitmap zoomBitmap = ImageHandler.zoomBitmap(bitmap, picWidth, picWidth);
			Log.v(getLocalClassName(), "Bitmap Image"+preview.getPreviewDegree());

			final Bitmap result = rotaingImageView(preview.getPreviewDegree(), zoomBitmap);

			Log.v(getLocalClassName(), "Bitmap Image"+result);
			zoomBitmap = null;

			preview.resumePreview();

			new Thread(){
				@SuppressLint("SimpleDateFormat")
				public void run(){
					synchronized (lock) {
						// Create a media file name
						String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
						String path = "JUZFOOD-"+timeStamp+".jpg";
						saveFile(path, result);

					}
				}
			}.start();
		}
	}


	@Override
	public void onCameraFocus(boolean bool) {
		log("onCameraFocus");
		if(bool){
		}
		else{
			//			Toast.makeText(getApplicationContext(), "", 500).show();
		}
	}

	/**
	 */
	private void computeImageWH(){

		int statusHeight = 0;
		Rect localRect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		Log.d("tag", "statusHeight:"+statusHeight);

		DisplayMetrics dm=new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		picWidth = dm.widthPixels;
//		picHeight = dm.heightPixels - statusHeight - bottom.getHeight();
	}
	/**
	 * @author Sudhakar Pachava Tirupati
	 * 
	 */
	class PictureClickListener implements View.OnClickListener{
		

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.ibCameraTakePicture:
				preview.takePicture();
//				Toast.makeText(getApplicationContext(), "take picture is done!", 500).show();

				//				preview.resumePreview();
				break;

			case R.id.preview:

				preview.autoFocus();

				break;
				
			case R.id.ibCameraFrontCam:
				if (flag == false) {
					flag = true;
					preview.frontCamera(true);
				} else{
					flag = false;
					preview.frontCamera(false);
				}
				
				break;
			}
		}

	}

	/**
	 * @param SAVE IMAGE TO SDCARD
	 * @param name
	 * @param source
	 * @param mark
	 * @return String
	 */
	public void saveFile(String name,Bitmap bm){

		String path = Environment.getExternalStorageDirectory().toString()+"/JuzFood/image";
		File folder = new File(path);
		if(!folder.exists())
			folder.mkdir();
		File file = new File(folder,name);
		if(file.exists()){
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			//			return null;
		}
		try {
			FileOutputStream fos=new FileOutputStream(file);
			bm.compress(CompressFormat.JPEG, 100, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			//			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			//			return null;
		}

		Log.v(getLocalClassName(), "Pathhhh Camera === "+file.getAbsolutePath());

		startActivity(new Intent(this, CameraDetailsActivity.class).putExtra("path", file.getAbsolutePath()));

		//		return file.getAbsolutePath();
	}

	/**
	 * @param name
	 * @param source
	 * @param mark
	 */
	public void saveMarkPicture(String name,Bitmap source,Bitmap mark){
		Bitmap bitmap = ImageHandler.watermark(source, mark, 5, 5);
		saveFile(name, bitmap);
	}

	/**
	 * 
	 * @param angle 
	 * @param bitmap 
	 * @return Bitmap 
	 */  
	public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {  

		Matrix matrix = new Matrix();
		matrix.postRotate(angle);

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,  
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;  
	}

	@Override
	public void initView() {
		log("initView");
		
		takepicture=(ImageButton) findViewById(R.id.ibCameraTakePicture);
		frontCam = (ImageButton) findViewById(R.id.ibCameraFrontCam);
//		save=(ImageButton) findViewById(R.id.bt_camera_save);
		preview = (Preview) findViewById(R.id.preview);
		bottom = (LinearLayout)findViewById(R.id.bottom);

		listener = new PictureClickListener();
		takepicture.setOnClickListener(listener);
		frontCam.setOnClickListener(listener);
//		save.setOnClickListener(listener);
//		markView = findViewById(R.id.txt_mark);

		preview.setOnClickListener(listener);
	}

	@Override
	public void initData() {
		log("initData");
	}

	@Override
	public void initEvent() {
		log("initEvent");
		preview.setOnCameraCallback(this);
	}
	
	@Override
	public void onClick(View v) {

	}
	
	void log(String msg){
		Log.d(getClass().getSimpleName(), msg);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen for landscape and portrait
		if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} 
		initEvent();
	}
}
