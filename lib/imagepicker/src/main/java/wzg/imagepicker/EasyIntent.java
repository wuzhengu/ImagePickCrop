package wzg.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by wuzhengu on 2020-07-01 <br/>
 */
public class EasyIntent extends Intent
{
	protected final Context mContext;
	
	EasyIntent(Context ctxt, Class<? extends Activity> cls){
		mContext = ctxt;
		if(ctxt!=null) setClass(ctxt, cls);
	}
	
	public boolean start(int reqCode){
		if(reqCode==-1){
			if(mContext!=null ){
				mContext.startActivity(this);
				return true;
			}
		}else{
			if(mContext instanceof Activity){
				((Activity)mContext).startActivityForResult(this, reqCode);
				return true;
			}
		}
		return false;
	}
	
	public <Value> Value get(EasyKey<Value> key, Value value){
		return key.get(this, value);
	}
	
	public <Value> EasyIntent set(EasyKey<Value> key, Value value){
		key.set(this, value);
		return this;
	}
	
	public EasyIntent set(Intent intent){
		putExtras(intent);
		return this;
	}
}
