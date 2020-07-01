package wzg.imagepicker;

import android.content.Context;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import wzg.imagepicker.ui.ImagePreviewActivity;

/**
 Created by wuzhengu on 2020/02/02 <br/>
 图片浏览
 */
public class ImagePreview extends Intent
{
	public static final String KEY_POSITION="image-position";
	public static final String KEY_LIST="image-list";
	
	/**
	 当前浏览位置
	 */
	public int getPosition(){
		return getIntExtra(KEY_POSITION, -1);
	}
	
	/**
	 浏览图片文件路径
	 */
	public List<String> getImages(){
		return getStringArrayListExtra(KEY_LIST);
	}
	
	/**
	 当前浏览位置
	 */
	public ImagePreview setPosition(int list){
		putExtra(KEY_POSITION, list);
		return this;
	}
	
	/**
	 浏览图片文件路径
	 */
	public ImagePreview setImages(List<String> images){
		ArrayList<String> list2=null;
		if(images instanceof ArrayList) list2=(ArrayList<String>)images;
		else list2=new ArrayList<>(images);
		putExtra(KEY_LIST, list2);
		return this;
	}
	
	/**
	 设置 Activity，生成 intent
	 */
	public Intent intent(Context ctxt){
		setClass(ctxt, ImagePreviewActivity.class);
		return this;
	}
	/**
	 新建 ImagePreview 对象，并导入 intent 参数
	 */
	public static ImagePreview from(Intent intent){
		ImagePreview intent2=new ImagePreview();
		intent2.putExtras(intent);
		return intent2;
	}
}
