package wzg.imagepicker.manager;

import android.content.Context;
import java.util.List;
import wzg.imagepicker.loader.DefaultImageLoader;
import wzg.imagepicker.loader.ImageLoader;

public class ConfigManager
{
	private static volatile ConfigManager mConfigManager;
	
	public static ConfigManager getInstance(){
		if(mConfigManager==null){
			synchronized(SelectionManager.class){
				if(mConfigManager==null){
					mConfigManager=new ConfigManager();
				}
			}
		}
		return mConfigManager;
	}
	
	public static final int SELECT_MODE_SINGLE=0;
	public static final int SELECT_MODE_MULTI=1;
	private String title;
	private boolean showCamera=true;
	private boolean showImage=true;
	private boolean showVideo=false;
	private boolean filterGif=false;
	private int selectionMode=SELECT_MODE_SINGLE;
	private int maxCount=1;
	private boolean singleType;
	private List<String> imagePaths;
	private ImageLoader imageLoader;
	
	private ConfigManager(){
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setTitle(String title){
		this.title=title;
	}
	
	public boolean isShowCamera(){
		return showCamera;
	}
	
	public void setShowCamera(boolean showCamera){
		this.showCamera=showCamera;
	}
	
	public boolean isShowImage(){
		return showImage;
	}
	
	public void setShowImage(boolean showImage){
		this.showImage=showImage;
	}
	
	public boolean isShowVideo(){
		return showVideo;
	}
	
	public void setShowVideo(boolean showVideo){
		this.showVideo=showVideo;
	}
	
	public int getMaxCount(){
		return maxCount;
	}
	
	public void setMaxCount(int maxCount){
		if(maxCount>1){
			setSelectionMode(SELECT_MODE_MULTI);
		}
		this.maxCount=maxCount;
	}
	
	public boolean isFilterGif(){
		return filterGif;
	}
	
	public void setFilterGif(boolean filterGif){
		this.filterGif=filterGif;
	}
	
	public int getSelectionMode(){
		return selectionMode;
	}
	
	public void setSelectionMode(int mSelectionMode){
		this.selectionMode=mSelectionMode;
	}
	
	public List<String> getImagePaths(){
		return imagePaths;
	}
	
	public void setImagePaths(List<String> imagePaths){
		this.imagePaths=imagePaths;
	}
	
	public ImageLoader getImageLoader() {
		if(imageLoader==null) imageLoader=new DefaultImageLoader();
		return imageLoader;
	}
	
	public boolean isSingleType(){
		return singleType;
	}
	
	public void setSingleType(boolean singleType){
		this.singleType=singleType;
	}
	
	public void setImageLoader(ImageLoader imageLoader){
		this.imageLoader=imageLoader;
	}
	
	public void clear(Context ctxt){
		if(imagePaths!=null) imagePaths.clear();
		try{
			if(imageLoader!=null) imageLoader.clearCache(ctxt);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
