package wzg.imagepicker.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import wzg.imagepicker.ImageCrop;
import wzg.imagepicker.ImageFileProvider;
import wzg.imagepicker.ImagePicker;
import wzg.imagepicker.ImagePreview;
import wzg.imagepicker.pkg.*;
import wzg.imagepicker.adapter.ImageFoldersAdapter;
import wzg.imagepicker.adapter.ImagePickerAdapter;
import wzg.imagepicker.data.MediaFile;
import wzg.imagepicker.data.MediaFolder;
import wzg.imagepicker.executors.CommonExecutor;
import wzg.imagepicker.listener.MediaLoadCallback;
import wzg.imagepicker.manager.ConfigManager;
import wzg.imagepicker.manager.SelectionManager;
import wzg.imagepicker.task.ImageLoadTask;
import wzg.imagepicker.task.MediaLoadTask;
import wzg.imagepicker.task.VideoLoadTask;
import wzg.imagepicker.utils.*;
import wzg.imagepicker.view.ImageFolderPopupWindow;

/**
 Created by wuzhengu on 2020/02/01 <br/>
 多图选择页面
 */
public class ImagePickActivity extends ImageBaseActivity
		implements ImagePickerAdapter.OnItemClickListener, ImageFoldersAdapter.OnImageFolderChangeListener
{
	/**
	 启动参数
	 */
	private String mTitle;
	private boolean isShowCamera;
	private boolean isShowImage;
	private boolean isShowVideo;
	private boolean isSingleType;
	private int mMaxCount;
	/**
	 界面UI
	 */
	private TextView mTvTitle;
	private TextView mTvCommit;
	private TextView mTvImageTime;
	private RecyclerView mRecyclerView;
	private TextView mTvImageFolders;
	private ImageFolderPopupWindow mImageFolderPopupWindow;
	private ProgressDialog mProgressDialog;
	private RelativeLayout mRlBottom;
	private GridLayoutManager mGridLayoutManager;
	private ImagePickerAdapter mAdapter;
	//图片数据源
	private List<MediaFile> mFileList;
	//文件夹数据源
	private List<MediaFolder> mFolderList;
	//是否显示时间
	private boolean isShowTime;
	//表示屏幕亮暗
	private static final int LIGHT_OFF=0;
	private static final int LIGHT_ON=1;
	private Handler mMyHandler=new Handler();
	private Runnable mHideRunnable=new Runnable()
	{
		@Override
		public void run(){
			hideImageTime();
		}
	};
	/** 拍照相关 */
	private String mFilePath;
	/** 权限相关 */
	private static final int REQ_CAMERA_PERMISSION=0x03;
	/** 大图预览选择 */
	private static final int REQ_PREVIEW=0x01;
	/** 点击拍照 */
	private static final int REQ_CAPTURE=0x02;
	/** 裁剪图片 */
	private static final int REQ_CROP=0x03;
	ImageCrop mCrop;
	private int mCheckItem=-1;
	
	@Override
	protected int getLayoutId(){
		return R.layout.activity_image_pick;
	}
	
	/**
	 初始化配置
	 */
	@Override
	protected void initConfig(){
		ConfigManager config=ConfigManager.getInstance();
		mTitle=config.getTitle();
		isShowCamera=config.isShowCamera();
		isShowImage=config.isShowImage();
		isShowVideo=config.isShowVideo();
		isSingleType=config.isSingleType();
		mMaxCount=config.getMaxCount();
		SelectionManager.getInstance().setMaxCount(mMaxCount);
		//载入历史选择记录
		List<String> list=config.getImagePaths();
		SelectionManager.getInstance().select(list);
		mCrop=ImageCrop.from(getIntent());
		if(mMaxCount>1 || config.isShowVideo()) mCrop.setCrop(false);
	}
	
	/**
	 初始化布局控件
	 */
	@Override
	protected void initView(){
		mProgressDialog=ProgressDialog.show(this, null, getString(R.string.scanner_image));
		//顶部栏相关
		mTvTitle=findViewById(R.id.image_title_center);
		if(!TextUtils.isEmpty(mTitle)) mTvTitle.setText(mTitle);
		else mTvTitle.setText(getString(R.string.image_pick));
		mTvCommit=findViewById(R.id.image_title_right);
		//悬浮标题相关
		mTvImageTime=findViewById(R.id.tv_image_time);
		//底部栏相关
		mRlBottom=findViewById(R.id.image_bottom_bar);
		mTvImageFolders=findViewById(R.id.tv_main_imageFolders);
		//列表相关
		mRecyclerView=findViewById(R.id.rv_main_images);
		mGridLayoutManager=new GridLayoutManager(this, 4);
		mRecyclerView.setLayoutManager(mGridLayoutManager);
		mRecyclerView.setHasFixedSize(true); //避免RecyclerView重新计算大小
		mRecyclerView.setItemViewCacheSize(8);
		mFileList=new ArrayList<>();
		mAdapter=new ImagePickerAdapter(this, mFileList);
		mAdapter.setOnItemClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
	}
	
	/**
	 初始化控件监听事件
	 */
	@Override
	protected void initListener(){
		findViewById(R.id.image_title_left).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		mTvCommit.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				clickCommit();
			}
		});
		mTvImageFolders.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view){
				if(mImageFolderPopupWindow!=null){
					setLightMode(LIGHT_OFF);
					mImageFolderPopupWindow.showAsDropDown(mRlBottom, 0, 0);
				}
			}
		});
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
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
	}
	
	/**
	 获取数据源
	 */
	@Override
	protected void getData(){
		//进行权限的判断
		boolean hasPermission=PermissionUtil.checkPermission(this);
		if(!hasPermission){
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
					REQ_CAMERA_PERMISSION);
		}else{
			startScannerTask();
		}
	}
	
	/**
	 权限申请回调
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode==REQ_CAMERA_PERMISSION){
			if(grantResults.length>=1){
				int cameraResult=grantResults[0];//相机权限
				int sdResult=grantResults[1];//sd卡权限
				boolean cameraGranted=cameraResult==PackageManager.PERMISSION_GRANTED;//拍照权限
				boolean sdGranted=sdResult==PackageManager.PERMISSION_GRANTED;//拍照权限
				if(cameraGranted && sdGranted){
					//具有拍照权限，sd卡权限，开始扫描任务
					startScannerTask();
				}else{
					//没有权限
					Toast.makeText(this, getString(R.string.permission_tip), Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}
	}
	
	/**
	 开启扫描任务
	 */
	private void startScannerTask(){
		Runnable mediaLoadTask=null;
		//照片、视频全部加载
		if(isShowImage && isShowVideo){
			mediaLoadTask=new MediaLoadTask(this, new MediaLoader());
		}
		//只加载视频
		if(!isShowImage && isShowVideo){
			mediaLoadTask=new VideoLoadTask(this, new MediaLoader());
		}
		//只加载图片
		if(isShowImage && !isShowVideo){
			mediaLoadTask=new ImageLoadTask(this, new MediaLoader());
		}
		//不符合以上场景，采用照片、视频全部加载
		if(mediaLoadTask==null){
			mediaLoadTask=new MediaLoadTask(this, new MediaLoader());
		}
		CommonExecutor.getInstance().execute(mediaLoadTask);
	}
	
	/**
	 处理媒体数据加载成功后的UI渲染
	 */
	class MediaLoader implements MediaLoadCallback
	{
		@Override
		public void loadMediaSuccess(final List<MediaFolder> mediaFolderList){
			runOnUiThread(new Runnable()
			{
				@Override
				public void run(){
					if(!mediaFolderList.isEmpty()){
						//默认加载全部照片
						mFileList.addAll(mediaFolderList.get(0).getMediaFileList());
						mAdapter.notifyDataSetChanged();
						//图片文件夹数据
						mFolderList=new ArrayList<>(mediaFolderList);
						mImageFolderPopupWindow=new ImageFolderPopupWindow(ImagePickActivity.this, mFolderList);
						mImageFolderPopupWindow.setAnimationStyle(R.style.imageFolderAnimator);
						mImageFolderPopupWindow.getAdapter().setOnImageFolderChangeListener(ImagePickActivity.this);
						mImageFolderPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
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
	}
	
	/**
	 隐藏时间
	 */
	private void hideImageTime(){
		if(isShowTime){
			isShowTime=false;
			ObjectAnimator.ofFloat(mTvImageTime, "alpha", 1, 0).setDuration(300).start();
		}
	}
	
	/**
	 显示时间
	 */
	private void showImageTime(){
		if(!isShowTime){
			isShowTime=true;
			ObjectAnimator.ofFloat(mTvImageTime, "alpha", 0, 1).setDuration(300).start();
		}
	}
	
	/**
	 更新时间
	 */
	private void updateImageTime(){
		int position=mGridLayoutManager.findFirstVisibleItemPosition();
		if(position!=RecyclerView.NO_POSITION){
			MediaFile mediaFile=mAdapter.getMediaFile(position);
			if(mediaFile!=null){
				if(mTvImageTime.getVisibility()!=View.VISIBLE){
					mTvImageTime.setVisibility(View.VISIBLE);
				}
				String time=Utils.getImageTime(mediaFile.getDateToken());
				mTvImageTime.setText(time);
				showImageTime();
				mMyHandler.removeCallbacks(mHideRunnable);
				mMyHandler.postDelayed(mHideRunnable, 1500);
			}
		}
	}
	
	/**
	 设置屏幕的亮度模式
	 */
	private void setLightMode(int lightMode){
		WindowManager.LayoutParams layoutParams=getWindow().getAttributes();
		switch(lightMode){
		case LIGHT_OFF:
			layoutParams.alpha=0.7f;
			break;
		case LIGHT_ON:
			layoutParams.alpha=1.0f;
			break;
		}
		getWindow().setAttributes(layoutParams);
	}
	
	/**
	 点击图片
	 */
	@Override
	public void onMediaClick(View view, int position){
		if(isShowCamera){
			if(position==0){
				if(!SelectionManager.getInstance().isCanChoose()){
					Toast.makeText(this, String.format(getString(R.string.select_image_max), mMaxCount),
							Toast.LENGTH_SHORT).show();
					return;
				}
				showCamera();
				return;
			}
		}
		if(mFileList!=null){
			DataUtil.getInstance().setMediaData(mFileList);
			Intent intent=new Intent(this, ImagePrePickActivity.class).putExtras(mCrop);
			if(isShowCamera){
				intent.putExtra(ImagePreview.KEY_POSITION, position-1);
			}else{
				intent.putExtra(ImagePreview.KEY_POSITION, position);
			}
			startActivityForResult(intent, REQ_PREVIEW);
		}
	}
	
	/**
	 选中/取消选中图片
	 */
	@Override
	public void onMediaCheck(View view, int position){
		if(isShowCamera){
			if(position==0){
				if(!SelectionManager.getInstance().isCanChoose()){
					Toast.makeText(this, String.format(getString(R.string.select_image_max), mMaxCount),
							Toast.LENGTH_SHORT).show();
					return;
				}
				showCamera();
				return;
			}
		}
		//执行选中/取消操作
		MediaFile mediaFile=mAdapter.getMediaFile(position);
		if(mediaFile!=null){
			String imagePath=mediaFile.getPath();
			if(isSingleType){
				//如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
				List<String> list=SelectionManager.getInstance().getSelectList();
				if(!list.isEmpty()){
					//判断选中集合中第一项是否为视频
					if(!SelectionManager.isCanAddSelectionPaths(imagePath, list.get(0))){
						//类型不同
						Toast.makeText(this, getString(R.string.single_type_choose), Toast.LENGTH_SHORT).show();
						return;
					}
				}
			}
			boolean ok=SelectionManager.getInstance().select(imagePath);
			if(!ok){
				Toast.makeText(this, getString(R.string.select_image_max, mMaxCount), Toast.LENGTH_SHORT).show();
				return;
			}
			if(mCheckItem!=-1 && mCheckItem!=position) mAdapter.notifyItemChanged(mCheckItem);
			mAdapter.notifyItemChanged(position);
			mCheckItem=position;
		}
		updateCommitButton();
	}
	
	/**
	 更新确认按钮状态
	 */
	private void updateCommitButton(){
		//改变确定按钮UI
		int selectCount=SelectionManager.getInstance().getSelectList().size();
		if(selectCount==0){
			mTvCommit.setEnabled(false);
			if(mCrop.needCrop()){
				mTvCommit.setText(R.string.confirm_next);
				return;
			}
			mTvCommit.setText(getString(R.string.confirm));
			return;
		}
		if(selectCount<mMaxCount){
			mTvCommit.setEnabled(true);
			mTvCommit.setText(String.format(getString(R.string.confirm_msg), selectCount, mMaxCount));
			return;
		}
		if(selectCount==mMaxCount){
			mTvCommit.setEnabled(true);
			if(mMaxCount==1){
				if(mCrop.needCrop()){
					mTvCommit.setText(R.string.confirm_next);
					return;
				}
			}
			mTvCommit.setText(String.format(getString(R.string.confirm_msg), selectCount, mMaxCount));
			return;
		}
	}
	
	void clickCommit(){
		if(mCrop.needCrop()){
			String image=SelectionManager.getInstance().getSelectList().get(0);
			startActivityForResult(mCrop.setImage(image).intent(getContext()), REQ_CROP);
			return;
		}
		ImagePicker.commitSelection(getActivity());
	}
	
	/**
	 跳转相机拍照
	 */
	private void showCamera(){
		if(isSingleType){
			//如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
			List<String> list=SelectionManager.getInstance().getSelectList();
			if(!list.isEmpty()){
				if(MediaFileUtil.isVideoFileType(list.get(0))){
					//如果存在视频，就不能拍照了
					Toast.makeText(this, getString(R.string.single_type_choose), Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}
		//拍照存放路径
		File fileDir=new File(Environment.getExternalStorageDirectory(), "DCIM/Camera");
		fileDir.mkdir();
		File file=Utils.createFile(fileDir, "", ".jpg");
		mFilePath=file.getAbsolutePath();
		Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Uri uri;
		if(Build.VERSION.SDK_INT<24) uri=Uri.fromFile(file);
		else uri=FileProvider.getUriForFile(this, ImageFileProvider.getFileProviderName(this), file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, REQ_CAPTURE);
	}
	
	/**
	 当图片文件夹切换时，刷新图片列表数据源
	 */
	@Override
	public void onImageFolderChange(View view, int position){
		MediaFolder mediaFolder=mFolderList.get(position);
		//更新当前文件夹名
		String folderName=mediaFolder.getFolderName();
		if(!TextUtils.isEmpty(folderName)){
			mTvImageFolders.setText(folderName);
		}
		//更新图片列表数据源
		mFileList.clear();
		mFileList.addAll(mediaFolder.getMediaFileList());
		mAdapter.notifyDataSetChanged();
		mImageFolderPopupWindow.dismiss();
	}
	
	/**
	 拍照回调
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent){
		if(resultCode!=RESULT_OK) return;
		switch(requestCode){
		case REQ_CAPTURE:
			//通知媒体库刷新
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+mFilePath)));
			//添加到选中集合
			SelectionManager.getInstance().select(mFilePath);
			clickCommit();
			break;
		case REQ_PREVIEW:
			clickCommit();
			break;
		case REQ_CROP:
			String image=ImageCrop.from(intent).getImage();
			SelectionManager.getInstance().getSelectList().set(0, image);
			ImagePicker.commitSelection(this);
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
		ImagePicker.getInstance().setCrop(false);
		ConfigManager.getInstance().clear(this);
		SelectionManager.getInstance().removeAll();
	}
}
