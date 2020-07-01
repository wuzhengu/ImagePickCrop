package wzg.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import wzg.imagepicker.loader.ImageLoader;
import wzg.imagepicker.manager.ConfigManager;
import wzg.imagepicker.manager.SelectionManager;
import wzg.imagepicker.ui.ImagePickActivity;

/**
 Created by wuzhengu on 2020/02/01 <br/>
 图片选择
 */
public class ImagePicker
{
	public static final String EXTRA_SELECT_IMAGES="selectItems";
	private static volatile ImagePicker mImagePicker;
	
	/**
	 创建对象
	 */
	public static ImagePicker getInstance(){
		if(mImagePicker==null){
			synchronized(ImagePicker.class){
				if(mImagePicker==null){
					mImagePicker=new ImagePicker();
				}
			}
		}
		return mImagePicker;
	}
	
	/**
	 选择图片后返回
	 */
	public static void commitSelection(Activity act){
		ArrayList<String> list=new ArrayList<>(SelectionManager.getInstance().getSelectList());
		Intent intent=new Intent();
		intent.putStringArrayListExtra(EXTRA_SELECT_IMAGES, list);
		act.setResult(Activity.RESULT_OK, intent);
		act.finish();
	}
	
	/**
	 设置标题
	 */
	public ImagePicker setTitle(String title){
		ConfigManager.getInstance().setTitle(title);
		return this;
	}
	
	/**
	 是否支持相机
	 */
	public ImagePicker showCamera(boolean yes){
		ConfigManager.getInstance().setShowCamera(yes);
		return this;
	}
	
	/**
	 是否展示图片
	 */
	public ImagePicker showImage(boolean yes){
		ConfigManager.getInstance().setShowImage(yes);
		return this;
	}
	
	/**
	 是否展示视频
	 */
	public ImagePicker showVideo(boolean yes){
		ConfigManager.getInstance().setShowVideo(yes);
		return this;
	}
	
	/**
	 是否过滤GIF图片(默认不过滤)
	 */
	public ImagePicker filterGif(boolean yes){
		ConfigManager.getInstance().setFilterGif(yes);
		return this;
	}
	
	/**
	 图片最大选择数
	 */
	public ImagePicker setMaxCount(int maxCount){
		ConfigManager.getInstance().setMaxCount(maxCount);
		return this;
	}
	
	/**
	 设置单类型选择（只能选图片或者视频）
	 */
	public ImagePicker setSingleType(boolean yes){
		ConfigManager.getInstance().setSingleType(yes);
		return this;
	}
	
	/**
	 设置图片加载器
	 */
	public ImagePicker setImageLoader(ImageLoader loader){
		ConfigManager.getInstance().setImageLoader(loader);
		return this;
	}
	
	/**
	 设置图片选择历史记录
	 */
	public ImagePicker setImagePaths(List<String> list){
		ConfigManager.getInstance().setImagePaths(list);
		return this;
	}
	
	/**
	 设置裁剪后图片宽高
	 */
	public ImagePicker setCropSize(int width, int height){
		getCrop().setCropSize(width, height);
		return this;
	}
	
	/**
	 设置裁剪后图片保存路径
	 */
	public ImagePicker setCropDir(String path){
		getCrop().setCropDir(path);
		return this;
	}
	
	/**
	 设置是否裁剪为圆形
	 */
	public ImagePicker setCropRound(boolean yes){
		getCrop().setCropRound(yes);
		return this;
	}
	
	/**
	 设置是否需要裁剪
	 */
	public ImagePicker setCrop(boolean yes){
		getCrop().setCrop(yes);
		return this;
	}
	
	ImageCrop getCrop(){
		if(mCrop==null) mCrop=new ImageCrop();
		return mCrop;
	}
	
	ImageCrop mCrop;
	
	/**
	 生成 intent
	 */
	public Intent intent(Context ctxt){
		Intent intent=new Intent(ctxt, ImagePickActivity.class);
		intent.putExtras(getCrop());
		return intent;
	}
	
	/**
	 启动 Activity
	 */
	public void start(Activity act, int reqCode){
		act.startActivityForResult(intent(act), reqCode);
	}
}
