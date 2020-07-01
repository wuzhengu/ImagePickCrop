package wzg.lib.imagepicker.loader;

import android.content.Context;
import android.widget.ImageView;

/**
 Created by wuzhengu on 2020/02/01 <br/>
 */
public class EmptyImageLoader implements ImageLoader
{
	@Override
	public void loadImage(ImageView view, Object image){
		
	}
	
	@Override
	public void clearCache(Context ctxt){
		
	}
}
