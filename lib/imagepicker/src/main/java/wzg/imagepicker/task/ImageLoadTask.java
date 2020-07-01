package wzg.imagepicker.task;

import android.content.Context;
import java.util.ArrayList;
import wzg.imagepicker.data.MediaFile;
import wzg.imagepicker.loader.ImageScanner;
import wzg.imagepicker.loader.MediaHandler;

public class ImageLoadTask implements Runnable
{
	private Context mContext;
	private ImageScanner mImageScanner;
	private MediaLoadCallback mCallback;
	
	public ImageLoadTask(Context context, MediaLoadCallback cb){
		this.mContext=context;
		this.mCallback =cb;
		mImageScanner=new ImageScanner(context);
	}
	
	public ImageLoadTask showGif(boolean show){
		mImageScanner.setShowGif(show);
		return this;
	}
	
	@Override
	public void run(){
		//存放所有照片
		ArrayList<MediaFile> list=new ArrayList<>();
		if(mImageScanner!=null){
			list=mImageScanner.queryMedia();
		}
		if(mCallback!=null){
			mCallback.onMediaLoad(MediaHandler.getImageFolder(mContext, list));
		}
	}
}
