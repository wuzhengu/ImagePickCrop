package app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 Created by wuzhengu on 2020/02/02 <br/>
 */
public class BaseActivity extends AppCompatActivity
{
	
	public BaseActivity getActivity(){
		return this;
	}
	
	public BaseActivity getContext(){
		return this;
	}
	
	@Override
	public void onCreate(Bundle state){
		super.onCreate(state);
	}
	
	@Override
	public void onActivityResult(int reqCode, int resCode, Intent intent){
		super.onActivityResult(reqCode, resCode, intent);
	}
}
