package wzg.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import java.util.List;
import wzg.imagepicker.ui.ImagePickActivity;

/**
 * Created by wuzhengu on 2020/02/01 <br/>
 * 图片选择
 */
public class ImagePick extends EasyIntent
{
	
	public interface K1
	{
		EasyKey.TextList ITEMS = new EasyKey.TextList("PICK_ITEMS");
		EasyKey.Text TITLE = new EasyKey.Text("PICK_TITLE");
		EasyKey.Bool SHOW_CAMERA = new EasyKey.Bool("PICK_SHOW_CAMERA");
		EasyKey.Bool SHOW_IMAGE = new EasyKey.Bool("PICK_SHOW_IMAGE");
		EasyKey.Bool SHOW_VIDEO = new EasyKey.Bool("PICK_SHOW_VIDEO");
		EasyKey.Bool SHOW_GIF = new EasyKey.Bool("PICK_SHOW_GIF");
		EasyKey.Bool MULTI_TYPE = new EasyKey.Bool("PICK_MULTI_TYPE");
		EasyKey.Int MAX_COUNT = new EasyKey.Int("PICK_MAX_COUNT");
		EasyKey.Text LOADER = new EasyKey.Text("PICK_LOADER");
	}
	
	public static ImagePick with(Context ctxt){
		return new ImagePick(ctxt);
	}
	
	public ImagePick(Context ctxt){
		super(ctxt, ImagePickActivity.class);
	}
	
	public static void submit(Activity act, List<String> list){
		Intent intent=new Intent();
		K1.ITEMS.set(intent, list);
		act.setResult(Activity.RESULT_OK, intent);
		act.finish();
	}
}
