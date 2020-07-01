package wzg.lib.imagepicker.task;

import android.content.Context;
import java.util.ArrayList;
import wzg.lib.imagepicker.data.MediaFile;
import wzg.lib.imagepicker.listener.MediaLoadCallback;
import wzg.lib.imagepicker.loader.ImageScanner;
import wzg.lib.imagepicker.loader.MediaHandler;

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
