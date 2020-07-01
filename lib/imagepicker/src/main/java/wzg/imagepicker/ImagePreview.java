package wzg.imagepicker;

import android.content.Context;
import wzg.imagepicker.ui.ImagePreviewActivity;

/**
 Created by wuzhengu on 2020/02/02 <br/>
 图片浏览
 */
public class ImagePreview extends EasyIntent
{
	public interface K3
	{
		EasyKey.TextList ITEMS = new EasyKey.TextList("PREVIEW_ITEMS");
		EasyKey.Int INDEX = new EasyKey.Int("PREVIEW_INDEX");
	}
	
	public static ImagePreview with(Context ctxt){
		return new ImagePreview(ctxt);
	}
	
	public ImagePreview(Context ctxt){
		super(ctxt, ImagePreviewActivity.class);
	}
}
