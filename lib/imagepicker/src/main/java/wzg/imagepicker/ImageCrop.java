package wzg.imagepicker;

import android.content.Context;
import android.content.Intent;
import java.io.Serializable;
import wzg.imagepicker.ui.ImageCropActivity;

/**
 Created by wuzhengu on 2020/02/01 <br/>
 图片裁剪
 */
public class ImageCrop extends Intent
{
	public static final String KEY_IMAGE="crop_image";
	public static final String KEY_WIDTH="crop_width";
	public static final String KEY_HEIGHT="crop_height";
	public static final String KEY_ROUND="crop_round";
	public static final String KEY_CROP="crop_crop";
	public static final String KEY_DIR="crop_dir";
	
	/**
	 @param width 裁剪后图片宽度
	 @param height 裁剪后图片高度
	 */
	public ImageCrop setCropSize(int width, int height){
		putExtra(KEY_WIDTH, width);
		putExtra(KEY_HEIGHT, height);
		return this;
	}
	
	/**
	 @param path 裁剪后图片保存路径
	 */
	public ImageCrop setCropDir(String path){
		putExtra(KEY_DIR, path);
		return this;
	}
	
	/**
	 @param yes 是否裁剪为圆形
	 */
	public ImageCrop setCropRound(boolean yes){
		putExtra(KEY_ROUND, yes);
		return this;
	}
	
	/**
	 @param yes 是否需要裁剪
	 */
	public ImageCrop setCrop(boolean yes){
		putExtra(KEY_CROP, yes);
		return this;
	}
	/**
	 @param image 需要裁剪的图片，一般为String类型，即图片路径
	 */
	public ImageCrop setImage(Serializable image){
		putExtra(KEY_IMAGE, image);
		return this;
	}
	
	/**
	 裁剪后图片宽度
	 */
	public int getCropWidth(){
		return getIntExtra(KEY_WIDTH, 0);
	}
	
	/**
	 裁剪后图片高度
	 */
	public int getCropHeight(){
		return getIntExtra(KEY_HEIGHT, 0);
	}
	
	/**
	 裁剪后保存路径
	 */
	public String getCropDir(){
		return getStringExtra(KEY_DIR);
	}
	
	/**
	 是否裁剪为圆形
	 */
	public boolean getCropRound(){
		return getBooleanExtra(KEY_ROUND, false);
	}
	
	/**
	 需要裁剪的图片，一般为String类型，即图片路径
	 */
	public <T extends Serializable> T getImage(){
		return (T)getSerializableExtra(KEY_IMAGE);
	}
	
	/**
	 是否需要裁剪
	 */
	public boolean needCrop(){
		return getBooleanExtra(KEY_CROP, false) && getCropWidth()>0 && getCropHeight()>0;
	}
	
	/**
	 设置 Activity，生成 intent
	 */
	public Intent intent(Context ctxt){
		setClass(ctxt, ImageCropActivity.class);
		return this;
	}
	
	/**
	 新建 ImageCrop 对象，并导入 intent 参数
	 */
	public static ImageCrop from(Intent intent){
		ImageCrop intent2=new ImageCrop();
		intent2.putExtras(intent);
		return intent2;
	}
}
