package wzg.imagepicker.manager;

import java.util.ArrayList;
import java.util.List;
import wzg.imagepicker.utils.MediaFileUtil;

public class SelectionManager
{
	private static volatile SelectionManager mSelectionManager;
	private ArrayList<String> mSelectImages=new ArrayList<>();
	private int mMaxCount=1;

	private SelectionManager(){
	}

	public static SelectionManager getInstance(){
		if(mSelectionManager==null){
			synchronized(SelectionManager.class){
				if(mSelectionManager==null){
					mSelectionManager=new SelectionManager();
				}
			}
		}
		return mSelectionManager;
	}

	/**
	 设置最大选择数
	 */
	public void setMaxCount(int maxCount){
		this.mMaxCount=maxCount;
	}

	/**
	 获取当前设置最大选择数
	 */
	public int getMaxCount(){
		return this.mMaxCount;
	}

	/**
	 获取当前所选图片集合path
	 */
	public List<String> getSelectList(){
		return mSelectImages;
	}

	/**
	 添加/移除图片到选择集合
	 */
	public boolean select(String imagePath){
		if(mSelectImages.contains(imagePath)){
			return mSelectImages.remove(imagePath);
		}else{
			if(mMaxCount==1) mSelectImages.clear();
			if(mSelectImages.size()<mMaxCount){
				return mSelectImages.add(imagePath);
			}else{
				return false;
			}
		}
	}

	/**
	 添加图片到选择集合
	 */
	public void select(List<String> images){
		if(images==null) return;
		for(String image : images){
			if(!mSelectImages.contains(image) && mSelectImages.size()<mMaxCount){
				mSelectImages.add(image);
			}
		}
	}

	/**
	 判断当前图片是否被选择
	 */
	public boolean isImageSelect(String imagePath){
		if(mSelectImages.contains(imagePath)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 是否还可以继续选择图片
	 */
	public boolean isCanChoose(){
		if(getSelectList().size()<mMaxCount){
			return true;
		}
		return false;
	}

	/**
	 是否可以添加到选择集合（在singleType模式下，图片视频不能一起选）
	 */
	public static boolean isCanAddSelectionPaths(String currentPath, String filePath){
		if((MediaFileUtil.isVideoFileType(currentPath) && !MediaFileUtil.isVideoFileType(filePath)) ||
		   (!MediaFileUtil.isVideoFileType(currentPath) && MediaFileUtil.isVideoFileType(filePath))){
			return false;
		}
		return true;
	}

	/**
	 清除已选图片
	 */
	public void removeAll(){
		mSelectImages.clear();
	}
}
