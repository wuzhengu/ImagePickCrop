package wzg.imagepicker.task;

import android.content.Context;
import java.util.ArrayList;
import wzg.imagepicker.data.MediaFile;
import wzg.imagepicker.loader.ImageScanner;
import wzg.imagepicker.loader.MediaHandler;
import wzg.imagepicker.loader.VideoScanner;

public class MediaLoadTask implements Runnable
{
	private Context mContext;
	private ImageScanner mImageScanner;
	private VideoScanner mVideoScanner;
	private MediaLoadCallback mMediaLoadCallback;

	public MediaLoadTask(Context context, MediaLoadCallback mediaLoadCallback){
		this.mContext=context;
		this.mMediaLoadCallback=mediaLoadCallback;
		mImageScanner=new ImageScanner(context);
		mVideoScanner=new VideoScanner(context);
	}
	
	public MediaLoadTask showGif(boolean show){
		mImageScanner.setShowGif(show);
		return this;
	}

	@Override
	public void run(){
		//存放所有照片
		ArrayList<MediaFile> imageFileList=new ArrayList<>();
		//存放所有视频
		ArrayList<MediaFile> videoFileList=new ArrayList<>();
		if(mImageScanner!=null){
			imageFileList=mImageScanner.queryMedia();
		}
		if(mVideoScanner!=null){
			videoFileList=mVideoScanner.queryMedia();
		}
		if(mMediaLoadCallback!=null){
			mMediaLoadCallback.onMediaLoad(MediaHandler.getMediaFolder(mContext, imageFileList, videoFileList));
		}
	}
}
