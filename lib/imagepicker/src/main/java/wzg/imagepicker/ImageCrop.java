package wzg.imagepicker;

import android.content.Context;
import android.content.Intent;
import wzg.imagepicker.ui.ImageCropActivity;

/**
 * Created by wuzhengu on 2020/02/01 <br/>
 * 图片裁剪
 */
public class ImageCrop extends EasyIntent
{
	public interface K2
	{
		EasyKey.Text IMAGE = new EasyKey.Text("CROP_IMAGE");
		EasyKey.Int WIDTH = new EasyKey.Int("CROP_WIDTH");
		EasyKey.Int HEIGHT = new EasyKey.Int("CROP_HEIGHT");
		EasyKey.Bool ROUND = new EasyKey.Bool("CROP_ROUND");
		EasyKey.Text DIR = new EasyKey.Text("CROP_DIR");
	}
	
	public static ImageCrop with(Context ctxt){
		return new ImageCrop(ctxt);
	}
	
	public ImageCrop(Context ctxt){
		super(ctxt, ImageCropActivity.class);
	}
	
	public static boolean checkIntent(Intent intent){
		if(K2.WIDTH.get(intent, 0)<=0) return false;
		if(K2.HEIGHT.get(intent, 0)<=0) return false;
		if(K2.DIR.get(intent, null)==null) return false;
		return true;
	}
}
