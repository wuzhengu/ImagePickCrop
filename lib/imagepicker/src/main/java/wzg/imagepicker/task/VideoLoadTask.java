package wzg.imagepicker.task;

import android.content.Context;
import java.util.ArrayList;
import wzg.imagepicker.data.MediaFile;
import wzg.imagepicker.listener.MediaLoadCallback;
import wzg.imagepicker.loader.MediaHandler;
import wzg.imagepicker.loader.VideoScanner;

public class VideoLoadTask implements Runnable
{
	private Context mContext;
	private VideoScanner mVideoScanner;
	private MediaLoadCallback mMediaLoadCallback;

	public VideoLoadTask(Context context, MediaLoadCallback mediaLoadCallback){
		this.mContext=context;
		this.mMediaLoadCallback=mediaLoadCallback;
		mVideoScanner=new VideoScanner(context);
	}

	@Override
	public void run(){
		//存放所有视频
		ArrayList<MediaFile> videoFileList=new ArrayList<>();
		if(mVideoScanner!=null){
			videoFileList=mVideoScanner.queryMedia();
		}
		if(mMediaLoadCallback!=null){
			mMediaLoadCallback.loadMediaSuccess(MediaHandler.getVideoFolder(mContext, videoFileList));
		}
	}
}
