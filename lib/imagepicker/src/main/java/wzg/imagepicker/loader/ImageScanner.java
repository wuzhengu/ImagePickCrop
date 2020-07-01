package wzg.imagepicker.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import wzg.imagepicker.data.MediaFile;

public class ImageScanner extends AbsMediaScanner<MediaFile>
{
	private boolean mShowGif;
	
	public ImageScanner(Context context){
		super(context);
	}
	
	public ImageScanner setShowGif(boolean show){
		mShowGif = show;
		return this;
	}
	
	@Override
	protected Uri getScanUri(){
		return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	}
	
	@Override
	protected String[] getProjection(){
		return new String[]{
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.MIME_TYPE,
			MediaStore.Images.Media.BUCKET_ID,
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
			MediaStore.Images.Media.DATE_TAKEN
		};
	}
	
	@Override
	protected String getSelection(){
		String s = Images.Media.MIME_TYPE+"=? or "+Images.Media.MIME_TYPE+"=?";
		if(mShowGif) s += " or "+Media.MIME_TYPE+"=?";
		return s;
	}
	
	@Override
	protected String[] getSelectionArgs(){
		if(!mShowGif) return new String[]{"image/jpeg", "image/png"};
		return new String[]{"image/jpeg", "image/png", "image/gif"};
	}
	
	@Override
	protected String getOrder(){
		return MediaStore.Images.Media.DATE_TAKEN+" desc";
	}
	
	/**
	 * 构建媒体对象
	 */
	@Override
	protected MediaFile parse(Cursor cursor){
		String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
		String mime = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
		Integer folderId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
		String folderName =
			cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
		long dateToken = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
		MediaFile mediaFile = new MediaFile();
		mediaFile.setPath(path);
		mediaFile.setMime(mime);
		mediaFile.setFolderId(folderId);
		mediaFile.setFolderName(folderName);
		mediaFile.setDateToken(dateToken);
		return mediaFile;
	}
}
