package wzg.imagepicker.ui;

import android.Manifest;
import android.Manifest.permission;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import wzg.imagepicker.ImageCrop;
import wzg.imagepicker.ImageCrop.K2;
import wzg.imagepicker.ImagePick;
import wzg.imagepicker.ImagePick.K1;
import wzg.imagepicker.adapter.ImageFoldersAdapter;
import wzg.imagepicker.content.ImagePickFileProvider;
import wzg.imagepicker.data.ItemType;
import wzg.imagepicker.data.MediaFile;
import wzg.imagepicker.data.MediaFolder;
import wzg.imagepicker.executors.CommonExecutor;
import wzg.imagepicker.loader.DefaultImageLoader;
import wzg.imagepicker.loader.ImageLoader;
import wzg.imagepicker.manager.SelectionManager;
import wzg.imagepicker.pkg.R;
import wzg.imagepicker.task.ImageLoadTask;
import wzg.imagepicker.task.MediaLoadCallback;
import wzg.imagepicker.task.MediaLoadTask;
import wzg.imagepicker.task.VideoLoadTask;
import wzg.imagepicker.ui.ImagePick2Activity.K;
import wzg.imagepicker.utils.MediaFileUtil;
import wzg.imagepicker.utils.Utils;
import wzg.imagepicker.view.ImageFolderPopupWindow;
import wzg.imagepicker.view.SquareImageView;
import wzg.imagepicker.view.SquareRelativeLayout;

/**
 * Created by wuzhengu on 2020/02/01 <br/>
 * 多图选择页面
 */
public class ImagePickActivity extends ImageBaseActivity
	implements ImageFoldersAdapter.OnImageFolderChangeListener
{
	private TextView tvTitle;
	private TextView tvSubmit;
	private TextView tvImageTime;
	private RecyclerView vList;
	private TextView tvFolders;
	private ImageFolderPopupWindow mPopupWindow;
	private ProgressDialog mProgressDialog;
	private RelativeLayout vBottom;
	private GridLayoutManager vGrid;
	private Adapter mAdapter;
	//图片数据源
	private List<MediaFile> mFileList;
	//文件夹数据源
	private List<MediaFolder> mFolderList;
	//是否显示时间
	private boolean showTime;
	//表示屏幕亮暗
	private static final int LIGHT_OFF = 0;
	private static final int LIGHT_ON = 1;
	private Handler mMyHandler = new Handler();
	private Runnable mHideRunnable = new Runnable()
	{
		@Override
		public void run(){
			hideImageTime();
		}
	};
	/** 拍照相关 */
	private String mFilePath;
	/** 大图预览选择 */
	private static final int REQ_PREVIEW = 0x01;
	/** 点击拍照 */
	private static final int REQ_CAPTURE = 0x02;
	/** 裁剪图片 */
	private static final int REQ_CROP = 0x03;
	
	/** 权限相关 */
	private static final int REQ_PERMISSION = 0x03;
	private final List<String> mPermissions=new ArrayList<>();
	
	private int mCheckItem = -1;
	private int mMaxCount;
	private boolean mShowVideo;
	private boolean mShowGif;
	private boolean mShowImage;
	private boolean mShowCamera;
	static ImageLoader imageLoader = null;
	
	public static ImageLoader getImageLoader(Intent intent){
		String clsName = intent==null? null: K1.LOADER.get(intent, null);
		if(clsName!=null){
			try{
				Class<?> cls = Class.forName(clsName);
				if(!cls.isInstance(imageLoader)){
					imageLoader = (ImageLoader)cls.getConstructor().newInstance();
				}
			}catch(Throwable ex){
				ex.printStackTrace();
			}
		}
		if(imageLoader==null) imageLoader = new DefaultImageLoader();
		return imageLoader;
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.activity_image_pick;
	}
	
	@Override
	protected void onCreate(@Nullable Bundle state){
		super.onCreate(state);
		Intent intent = getIntent();
		mMaxCount = K1.MAX_COUNT.get(intent, 1);
		mShowVideo = K1.SHOW_VIDEO.get(intent, true);
		mShowGif = K1.SHOW_GIF.get(intent, true);
		mShowImage = K1.SHOW_IMAGE.get(intent, true);
		mShowCamera = K1.SHOW_CAMERA.get(intent, true);
		SelectionManager.getInstance().select(K1.ITEMS.get(intent, null));
		getImageLoader(intent);
		mProgressDialog = ProgressDialog.show(this, null, getString(R.string.scanner_image));
		//顶部栏相关
		String title = K1.TITLE.get(getIntent(), null);
		tvTitle = findViewById(R.id.image_title_center);
		if(title!=null) tvTitle.setText(title);
		else tvTitle.setText(getString(R.string.image_pick));
		tvSubmit = findViewById(R.id.image_title_right);
		//悬浮标题相关
		tvImageTime = findViewById(R.id.tv_image_time);
		//底部栏相关
		vBottom = findViewById(R.id.image_bottom_bar);
		tvFolders = findViewById(R.id.tv_main_imageFolders);
		//列表相关
		vList = findViewById(R.id.rv_main_images);
		vGrid = new GridLayoutManager(this, 4);
		vList.setLayoutManager(vGrid);
		vList.setHasFixedSize(true); //避免RecyclerView重新计算大小
		vList.setItemViewCacheSize(8);
		mFileList = new ArrayList<>();
		mAdapter = new Adapter(mFileList);
		vList.setAdapter(mAdapter);
		findViewById(R.id.image_title_left).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		tvSubmit.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				clickCommit();
			}
		});
		tvFolders.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view){
				if(mPopupWindow!=null){
					setLightMode(LIGHT_OFF);
					mPopupWindow.showAsDropDown(vBottom, 0, 0);
				}
			}
		});
		vList.addOnScrollListener(new RecyclerView.OnScrollListener()
		{
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState){
				super.onScrollStateChanged(recyclerView, newState);
				updateImageTime();
			}
			
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy){
				super.onScrolled(recyclerView, dx, dy);
				updateImageTime();
			}
		});
		//进行权限的判断
		mPermissions.add(permission.READ_EXTERNAL_STORAGE);
		if(mShowCamera) mPermissions.add(Manifest.permission.CAMERA);
		boolean hasPermission = true;
		for(String item : mPermissions){
			if(ContextCompat.checkSelfPermission(this, item)!=PackageManager.PERMISSION_GRANTED){
				hasPermission=false;
				break;
			}
		}
		if(hasPermission) startScannerTask();
		else{
			String[] permissions = mPermissions.toArray(new String[0]);
			ActivityCompat.requestPermissions(this, permissions, REQ_PERMISSION);
		}
	}
	
	/**
	 * 权限申请回调
	 */
	@Override
	public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] results){
		super.onRequestPermissionsResult(reqCode, permissions, results);
		if(reqCode==REQ_PERMISSION){
			boolean hasPermission=true;
			for(int item : results) if(item!=PackageManager.PERMISSION_GRANTED){
				hasPermission=false;
				break;
			}
			if(hasPermission) startScannerTask();
			else{
				//没有权限
				Toast.makeText(this, R.string.permission_tip, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
	
	/**
	 * 开启扫描任务
	 */
	private void startScannerTask(){
		Runnable task = null;
		MediaLoadCallback cb = new MediaLoadCallback()
		{
			@Override
			public void onMediaLoad(final List<MediaFolder> list){
				runOnUiThread(new Runnable()
				{
					@Override
					public void run(){
						if(!list.isEmpty()){
							//默认加载全部照片
							mFileList.addAll(list.get(0).getMediaFileList());
							mAdapter.notifyDataSetChanged();
							//图片文件夹数据
							mFolderList = new ArrayList<>(list);
							mPopupWindow =
								new ImageFolderPopupWindow(ImagePickActivity.this, mFolderList);
							mPopupWindow.setAnimationStyle(R.style.imageFolderAnimator);
							mPopupWindow
								.getAdapter()
								.setOnImageFolderChangeListener(ImagePickActivity.this);
							mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
							{
								@Override
								public void onDismiss(){
									setLightMode(LIGHT_ON);
								}
							});
							updateCommitButton();
						}
						mProgressDialog.cancel();
					}
				});
			}
		};
		if(mShowImage){
			if(!mShowVideo){
				task = new ImageLoadTask(this, cb).showGif(mShowGif); //只加载图片
			}
		}else{
			if(mShowVideo){
				task = new VideoLoadTask(this, cb); //只加载视频
			}
		}
		if(task==null) task = new MediaLoadTask(this, cb).showGif(mShowGif); //照片、视频全部加载
		CommonExecutor.getInstance().execute(task);
	}
	
	/**
	 * 隐藏时间
	 */
	private void hideImageTime(){
		if(showTime){
			showTime = false;
			ObjectAnimator.ofFloat(tvImageTime, "alpha", 1, 0).setDuration(300).start();
		}
	}
	
	/**
	 * 显示时间
	 */
	private void showImageTime(){
		if(!showTime){
			showTime = true;
			ObjectAnimator.ofFloat(tvImageTime, "alpha", 0, 1).setDuration(300).start();
		}
	}
	
	/**
	 * 更新时间
	 */
	private void updateImageTime(){
		int position = vGrid.findFirstVisibleItemPosition();
		if(position!=RecyclerView.NO_POSITION){
			MediaFile mediaFile = mAdapter.getMediaFile(position);
			if(mediaFile!=null){
				if(tvImageTime.getVisibility()!=View.VISIBLE){
					tvImageTime.setVisibility(View.VISIBLE);
				}
				String time = Utils.getImageTime(mediaFile.getDateToken());
				tvImageTime.setText(time);
				showImageTime();
				mMyHandler.removeCallbacks(mHideRunnable);
				mMyHandler.postDelayed(mHideRunnable, 1500);
			}
		}
	}
	
	/**
	 * 设置屏幕的亮度模式
	 */
	private void setLightMode(int lightMode){
		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		switch(lightMode){
		case LIGHT_OFF:
			layoutParams.alpha = 0.7f;
			break;
		case LIGHT_ON:
			layoutParams.alpha = 1.0f;
			break;
		}
		getWindow().setAttributes(layoutParams);
	}
	
	/**
	 * 点击图片
	 */
	public void onMediaClick(View view, int position){
		if(mShowCamera){
			if(position==0){
				if(!SelectionManager.getInstance().isCanChoose()){
					String msg = getString(R.string.select_image_max, mMaxCount);
					Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
					return;
				}
				showCamera();
				return;
			}
		}
		if(mFileList!=null){
			Intent intent = new Intent(this, ImagePick2Activity.class);
			intent.putExtras(getIntent());
			K1.ITEMS.set(intent, null);
			K.ITEMS.set(intent, mFileList);
			K.INDEX.set(intent, mShowCamera? position-1: position);
			startActivityForResult(intent, REQ_PREVIEW);
		}
	}
	
	/**
	 * 选中/取消选中图片
	 */
	public void onMediaCheck(View view, int position){
		if(mShowCamera){
			if(position==0){
				if(!SelectionManager.getInstance().isCanChoose()){
					Toast
						.makeText(this,
							String.format(getString(R.string.select_image_max), mMaxCount),
							Toast.LENGTH_SHORT)
						.show();
					return;
				}
				showCamera();
				return;
			}
		}
		//执行选中/取消操作
		MediaFile mediaFile = mAdapter.getMediaFile(position);
		if(mediaFile!=null){
			String imagePath = mediaFile.getPath();
			if(mMaxCount==1){
				//如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
				List<String> list = SelectionManager.getInstance().getList();
				if(!list.isEmpty()){
					//判断选中集合中第一项是否为视频
					if(!SelectionManager.isCanAddSelectionPaths(imagePath, list.get(0))){
						//类型不同
						Toast
							.makeText(this, getString(R.string.single_type_choose),
								Toast.LENGTH_SHORT)
							.show();
						return;
					}
				}
			}
			boolean ok = SelectionManager.getInstance().select(imagePath);
			if(!ok){
				Toast
					.makeText(this, getString(R.string.select_image_max, mMaxCount),
						Toast.LENGTH_SHORT)
					.show();
				return;
			}
			if(mCheckItem!=-1 && mCheckItem!=position) mAdapter.notifyItemChanged(mCheckItem);
			mAdapter.notifyItemChanged(position);
			mCheckItem = position;
		}
		updateCommitButton();
	}
	
	boolean checkCrop(){
		if(mShowVideo) return false;
		return ImageCrop.checkIntent(getIntent());
	}
	
	/**
	 * 更新确认按钮状态
	 */
	private void updateCommitButton(){
		//改变确定按钮UI
		int selectCount = SelectionManager.getInstance().getList().size();
		if(selectCount==0){
			tvSubmit.setEnabled(false);
			if(checkCrop()){
				tvSubmit.setText(R.string.confirm_next);
				return;
			}
			tvSubmit.setText(getString(R.string.confirm));
			return;
		}
		if(selectCount<mMaxCount){
			tvSubmit.setEnabled(true);
			tvSubmit.setText(
				String.format(getString(R.string.confirm_msg), selectCount, mMaxCount));
			return;
		}
		if(selectCount==mMaxCount){
			tvSubmit.setEnabled(true);
			if(mMaxCount==1){
				if(checkCrop()){
					tvSubmit.setText(R.string.confirm_next);
					return;
				}
			}
			tvSubmit.setText(
				String.format(getString(R.string.confirm_msg), selectCount, mMaxCount));
			return;
		}
	}
	
	void clickCommit(){
		if(checkCrop()){
			String image = SelectionManager.getInstance().getList().get(0);
			ImageCrop
				.with(this)
				.set(getIntent())
				.set(K1.ITEMS, null)
				.set(K2.IMAGE, image)
				.start(REQ_CROP);
			return;
		}
		List<String> list = SelectionManager.getInstance().getList();
		ImagePick.submit(getActivity(), list);
	}
	
	/**
	 * 跳转相机拍照
	 */
	private void showCamera(){
		if(mMaxCount==1){
			//如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
			List<String> list = SelectionManager.getInstance().getList();
			if(!list.isEmpty()){
				if(MediaFileUtil.isVideoFileType(list.get(0))){
					//如果存在视频，就不能拍照了
					Toast
						.makeText(this, getString(R.string.single_type_choose), Toast.LENGTH_SHORT)
						.show();
					return;
				}
			}
		}
		//拍照存放路径
		File fileDir = new File(Environment.getExternalStorageDirectory(), "DCIM/Camera");
		fileDir.mkdir();
		File file = Utils.createFile(fileDir, "", ".jpg");
		mFilePath = file.getAbsolutePath();
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Uri uri;
		if(Build.VERSION.SDK_INT<24) uri = Uri.fromFile(file);
		else uri = ImagePickFileProvider.toUri(this, file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, REQ_CAPTURE);
	}
	
	/**
	 * 当图片文件夹切换时，刷新图片列表数据源
	 */
	@Override
	public void onImageFolderChange(View view, int position){
		MediaFolder mediaFolder = mFolderList.get(position);
		//更新当前文件夹名
		String folderName = mediaFolder.getFolderName();
		if(!TextUtils.isEmpty(folderName)){
			tvFolders.setText(folderName);
		}
		//更新图片列表数据源
		mFileList.clear();
		mFileList.addAll(mediaFolder.getMediaFileList());
		mAdapter.notifyDataSetChanged();
		mPopupWindow.dismiss();
	}
	
	/**
	 * 拍照回调
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent){
		if(resultCode!=RESULT_OK) return;
		switch(requestCode){
		case REQ_CAPTURE:
			Uri uri = Uri.parse("file://"+mFilePath); //通知媒体库刷新
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
			//添加到选中集合
			SelectionManager.getInstance().select(mFilePath);
			clickCommit();
			break;
		case REQ_PREVIEW:
			clickCommit();
			break;
		case REQ_CROP:
			String image = K2.IMAGE.get(intent, null);
			ImagePick.submit(this, Arrays.asList(image));
			break;
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		mAdapter.notifyDataSetChanged();
		updateCommitButton();
	}
	
	@Override
	public void onBackPressed(){
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		SelectionManager.getInstance().removeAll();
	}
	
	class Adapter extends RecyclerView.Adapter<Adapter.BaseHolder>
	{
		private List<MediaFile> mMediaFileList;
		
		public Adapter(List<MediaFile> list){
			this.mMediaFileList = list;
		}
		
		@Override
		public int getItemViewType(int position){
			if(mShowCamera){
				if(position==0){
					return ItemType.ITEM_TYPE_CAMERA;
				}
				//如果有相机存在，position位置需要-1
				position--;
			}
			if(mMediaFileList.get(position).getDuration()>0){
				return ItemType.ITEM_TYPE_VIDEO;
			}else{
				return ItemType.ITEM_TYPE_IMAGE;
			}
		}
		
		@Override
		public int getItemCount(){
			if(mMediaFileList==null){
				return 0;
			}
			return mShowCamera? mMediaFileList.size()+1: mMediaFileList.size();
		}
		
		/**
		 * 获取item所对应的数据源
		 */
		public MediaFile getMediaFile(int position){
			if(mShowCamera){
				if(position==0){
					return null;
				}
				return mMediaFileList.get(position-1);
			}
			return mMediaFileList.get(position);
		}
		
		@NonNull
		@Override
		public BaseHolder onCreateViewHolder(ViewGroup vg, int viewType){
			Context ctxt = vg.getContext();
			View view;
			if(viewType==ItemType.ITEM_TYPE_CAMERA){
				view = LayoutInflater.from(ctxt).inflate(R.layout.item_recyclerview_camera, null);
				return new BaseHolder(view);
			}
			if(viewType==ItemType.ITEM_TYPE_IMAGE){
				view = LayoutInflater.from(ctxt).inflate(R.layout.item_recyclerview_image, null);
				return new ImageHolder(view);
			}
			if(viewType==ItemType.ITEM_TYPE_VIDEO){
				view = LayoutInflater.from(ctxt).inflate(R.layout.item_recyclerview_video, null);
				return new VideoHolder(view);
			}
			return null;
		}
		
		@Override
		public void onBindViewHolder(@NonNull BaseHolder holder, final int position){
			int itemType = getItemViewType(position);
			MediaFile mediaFile = getMediaFile(position);
			switch(itemType){
			//图片、视频Item
			case ItemType.ITEM_TYPE_IMAGE:
			case ItemType.ITEM_TYPE_VIDEO:
				MediaHolder mediaHolder = (MediaHolder)holder;
				bindMedia(mediaHolder, mediaFile);
				break;
			//相机Item
			default:
				break;
			}
			//设置点击事件监听
			holder.mSquareRelativeLayout.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view){
					onMediaClick(view, position);
				}
			});
			if(holder instanceof MediaHolder){
				((MediaHolder)holder).mImageCheck.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view){
						onMediaCheck(view, position);
					}
				});
			}
		}
		
		/**
		 * 绑定数据（图片、视频）
		 */
		private void bindMedia(MediaHolder h, MediaFile mediaFile){
			Context ctxt = h.itemView.getContext();
			String imagePath = mediaFile.getPath();
			if(!TextUtils.isEmpty(imagePath)){
				//选择状态（仅是UI表现，真正数据交给SelectionManager管理）
				if(SelectionManager.getInstance().isImageSelect(imagePath)){
					h.mImageView.setColorFilter(Color.parseColor("#77000000"));
					h.mImageCheck.setImageDrawable(
						ctxt.getResources().getDrawable(R.mipmap.icon_image_checked));
				}else{
					h.mImageView.setColorFilter(null);
					h.mImageCheck.setImageDrawable(
						ctxt.getResources().getDrawable(R.mipmap.icon_image_check));
				}
				try{
					getImageLoader(null).loadImage(h.mImageView, imagePath);
				}catch(Exception e){
					e.printStackTrace();
				}
				if(h instanceof ImageHolder){
					//如果是gif图，显示gif标识
					String suffix = imagePath.substring(imagePath.lastIndexOf(".")+1);
					if(suffix.toUpperCase().equals("GIF")){
						((ImageHolder)h).mImageGif.setVisibility(View.VISIBLE);
					}else{
						((ImageHolder)h).mImageGif.setVisibility(View.GONE);
					}
				}
				if(h instanceof VideoHolder){
					//如果是视频，需要显示视频时长
					String duration = Utils.getVideoDuration(mediaFile.getDuration());
					((VideoHolder)h).mVideoDuration.setText(duration);
				}
			}
		}
		
		/**
		 * 图片Item
		 */
		class ImageHolder extends MediaHolder
		{
			public ImageView mImageGif;
			
			public ImageHolder(View itemView){
				super(itemView);
				mImageGif = itemView.findViewById(R.id.iv_item_gif);
			}
		}
		
		/**
		 * 视频Item
		 */
		class VideoHolder extends MediaHolder
		{
			TextView mVideoDuration;
			
			VideoHolder(View itemView){
				super(itemView);
				mVideoDuration = itemView.findViewById(R.id.tv_item_videoDuration);
			}
		}
		
		/**
		 * 媒体Item
		 */
		class MediaHolder extends BaseHolder
		{
			SquareImageView mImageView;
			ImageView mImageCheck;
			
			MediaHolder(View itemView){
				super(itemView);
				mImageView = itemView.findViewById(R.id.iv_item_image);
				mImageCheck = itemView.findViewById(R.id.image_item_check);
			}
		}
		
		/**
		 * 基础Item
		 */
		class BaseHolder extends RecyclerView.ViewHolder
		{
			SquareRelativeLayout mSquareRelativeLayout;
			
			BaseHolder(View itemView){
				super(itemView);
				mSquareRelativeLayout = itemView.findViewById(R.id.srl_item);
			}
		}
	}
}
