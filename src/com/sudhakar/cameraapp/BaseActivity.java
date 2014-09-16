package com.sudhakar.cameraapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public abstract class BaseActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
	
	}
	
	protected void startInit(){
		initView();
		initData();
		initEvent();
	}
	@Override
	protected void onResume() {
		super.onResume();
		
//		 if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
//		  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		 }
	}

	public abstract void initView();
	public abstract void initData();
	public abstract void initEvent();

}


