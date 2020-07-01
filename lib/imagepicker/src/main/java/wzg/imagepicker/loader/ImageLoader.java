package wzg.imagepicker.loader;

import android.content.Context;
import android.widget.ImageView;

/**
 Created by wuzhengu on 2020/02/01 <br/>
 开放图片加载方案
 */
public interface ImageLoader
{
	/**
	 图片加载
	 */
	void loadImage(ImageView view, Object image);
	
	/**
	 缓存清理
	 */
	void clearCache(Context ctxt);
}
