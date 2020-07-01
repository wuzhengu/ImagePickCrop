package wzg.imagepicker.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import wzg.imagepicker.data.MediaFolder;
import wzg.imagepicker.pkg.R;
import wzg.imagepicker.ui.ImagePickActivity;

public class ImageFoldersAdapter extends RecyclerView.Adapter<ImageFoldersAdapter.ViewHolder>
{
	private List<MediaFolder> mMediaFolderList;
	private int mCurrentImageFolderIndex;
	
	public ImageFoldersAdapter(List<MediaFolder> list, int position){
		this.mMediaFolderList=list;
		this.mCurrentImageFolderIndex=position;
	}
	
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup vg, int viewType){
		LayoutInflater inflater = LayoutInflater.from(vg.getContext());
		return new ViewHolder(inflater.inflate(R.layout.item_recyclerview_folder, null));
	}
	
	@Override
	public void onBindViewHolder(ViewHolder h, final int position){
		Context ctxt = h.itemView.getContext();
		final MediaFolder mediaFolder=mMediaFolderList.get(position);
		String folderCover=mediaFolder.getFolderCover();
		String folderName=mediaFolder.getFolderName();
		int imageSize=mediaFolder.getMediaFileList().size();
		if(!TextUtils.isEmpty(folderName)){
			h.mFolderName.setText(folderName);
		}
		h.mImageSize.setText(String.format(ctxt.getString(R.string.image_num), imageSize));
		if(mCurrentImageFolderIndex==position){
			h.mImageFolderCheck.setVisibility(View.VISIBLE);
		}else{
			h.mImageFolderCheck.setVisibility(View.GONE);
		}
		//加载图片
		try{
			ImagePickActivity.getImageLoader(null).loadImage(h.mImageCover, folderCover);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(mImageFolderChangeListener!=null){
			h.itemView.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View view){
					mCurrentImageFolderIndex=position;
					notifyDataSetChanged();
					mImageFolderChangeListener.onImageFolderChange(view, position);
				}
			});
		}
	}
	
	@Override
	public int getItemCount(){
		return mMediaFolderList==null? 0: mMediaFolderList.size();
	}
	
	class ViewHolder extends RecyclerView.ViewHolder
	{
		private ImageView mImageCover;
		private TextView mFolderName;
		private TextView mImageSize;
		private ImageView mImageFolderCheck;
		
		public ViewHolder(View itemView){
			super(itemView);
			mImageCover=itemView.findViewById(R.id.iv_item_imageCover);
			mFolderName=itemView.findViewById(R.id.tv_item_folderName);
			mImageSize=itemView.findViewById(R.id.tv_item_imageSize);
			mImageFolderCheck=itemView.findViewById(R.id.image_item_check);
		}
	}
	
	/**
	 接口回调，Item点击事件
	 */
	private ImageFoldersAdapter.OnImageFolderChangeListener mImageFolderChangeListener;
	
	public void setOnImageFolderChangeListener(ImageFoldersAdapter.OnImageFolderChangeListener onItemClickListener){
		this.mImageFolderChangeListener=onItemClickListener;
	}
	
	public interface OnImageFolderChangeListener
	{
		void onImageFolderChange(View view, int position);
	}
}
