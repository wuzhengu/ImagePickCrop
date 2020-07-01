package wzg.imagepicker.task;

import android.content.Context;
import java.util.ArrayList;
import wzg.imagepicker.data.MediaFile;
import wzg.imagepicker.listener.MediaLoadCallback;
import wzg.imagepicker.loader.ImageScanner;
import wzg.imagepicker.loader.MediaHandler;

public class ImageLoadTask implements Runnable
{
	private Context mContext;
	private ImageScanner mImageScanner;
	private MediaLoadCallback mMediaLoadCallback;
	
	public ImageLoadTask(Context context, MediaLoadCallback mediaLoadCallback){
		this.mContext=context;
		this.mMediaLoadCallback=mediaLoadCallback;
		mImageScanner=new ImageScanner(context);
	}
	
	@Override
	public void run(){
		//存放所有照片
		ArrayList<MediaFile> imageFileList=new ArrayList<>();
		if(mImageScanner!=null){
			imageFileList=mImageScanner.queryMedia();
		}
		if(mMediaLoadCallback!=null){
			mMediaLoadCallback.loadMediaSuccess(MediaHandler.getImageFolder(mContext, imageFileList));
		}
	}
}
