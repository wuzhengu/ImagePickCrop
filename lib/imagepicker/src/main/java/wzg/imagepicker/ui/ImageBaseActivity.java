package wzg.imagepicker.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

public abstract class ImageBaseActivity extends AppCompatActivity
{
	public ImageBaseActivity getActivity(){
		return this;
	}
	
	public ImageBaseActivity getContext(){
		return this;
	}
	
	@Override
	protected void onCreate(Bundle state){
		super.onCreate(state);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
		int layoutId=getLayoutId();
		if(layoutId!=0) setContentView(layoutId);
	}
	
	protected int getLayoutId(){
		return 0;
	}
	
}
