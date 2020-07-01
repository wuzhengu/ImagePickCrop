package wzg.imagepicker.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.io.File;
import java.util.List;
import wzg.imagepicker.ImageCrop;
import wzg.imagepicker.ImageFileProvider;
import wzg.imagepicker.ImagePreview;
import wzg.imagepicker.pkg.*;
import wzg.imagepicker.adapter.ImagePageAdapter;
import wzg.imagepicker.data.MediaFile;
import wzg.imagepicker.manager.ConfigManager;
import wzg.imagepicker.manager.SelectionManager;
import wzg.imagepicker.utils.DataUtil;
import wzg.imagepicker.view.PinchImageView;

/**
 Created by wuzhengu on 2020/02/01 <br/>
 大图查看及选择
 */
public class ImagePrePickActivity extends ImageBaseActivity
{
	private List<MediaFile> mImageList;
	private int mPosition;
	private TextView vTitleCenter;
	private TextView vTitleRight;
	private ViewPager mViewPager;
	private ImageView vVideoPlay;
	private LinearLayout vImageSelect;
	private ImageView vImageCheck;
	
	@Override
	protected int getLayoutId(){
		return R.layout.activity_image_pre_pick;
	}
	
	@Override
	protected void initView(){
		vTitleCenter=(TextView)findViewById(R.id.image_title_center);
		vTitleRight=(TextView)findViewById(R.id.image_title_right);
		vVideoPlay=(ImageView)findViewById(R.id.image_video_play);
		mViewPager=(ViewPager)findViewById(R.id.image_view_pager);
		vImageSelect=(LinearLayout)findViewById(R.id.image_item_select);
		vImageCheck=(ImageView)findViewById(R.id.image_item_check);
	}
	
	@Override
	protected void initListener(){
		findViewById(R.id.image_title_left).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				finish();
			}
		});
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
			}
			@Override
			public void onPageSelected(int position){
				mPosition=position;
				vTitleCenter.setText(String.format("%d/%d", position+1, mImageList.size()));
				setIvPlayShow(mImageList.get(position));
				updateSelectButton(mImageList.get(position).getPath());
			}
			
			@Override
			public void onPageScrollStateChanged(int state){
			}
		});
		vImageSelect.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				//如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
				if(ConfigManager.getInstance().isSingleType()){
					List<String> list=SelectionManager.getInstance().getSelectList();
					if(!list.isEmpty()){
						//判断选中集合中第一项是否为视频
						if(!SelectionManager.isCanAddSelectionPaths(
								mImageList.get(mViewPager.getCurrentItem()).getPath(), list.get(0))){
							//类型不同
							Toast.makeText(ImagePrePickActivity.this, getString(R.string.single_type_choose),
									Toast.LENGTH_SHORT).show();
							return;
						}
					}
				}
				boolean addSuccess=SelectionManager.getInstance()
						.select(mImageList.get(mViewPager.getCurrentItem()).getPath());
				if(addSuccess){
					updateSelectButton(mImageList.get(mViewPager.getCurrentItem()).getPath());
					updateCommitButton();
				}else{
					Toast.makeText(ImagePrePickActivity.this, String.format(getString(R.string.select_image_max),
							SelectionManager.getInstance().getMaxCount()), Toast.LENGTH_SHORT).show();
				}
			}
		});
		vTitleRight.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				clickCommit();
			}
		});
		vVideoPlay.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				//实现播放视频的跳转逻辑(调用原生视频播放器)
				Intent intent=new Intent(Intent.ACTION_VIEW);
				Uri uri=FileProvider.getUriForFile(ImagePrePickActivity.this,
						ImageFileProvider.getFileProviderName(ImagePrePickActivity.this),
						new File(mImageList.get(mViewPager.getCurrentItem()).getPath()));
				intent.setDataAndType(uri, "video/*");
				//给所有符合跳转条件的应用授权
				List<ResolveInfo> resInfoList=
						getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
				for(ResolveInfo resolveInfo : resInfoList){
					String packageName=resolveInfo.activityInfo.packageName;
					grantUriPermission(packageName, uri,
							Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				}
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void getData(){
		mImageList=DataUtil.getInstance().getMediaData();
		mPosition=getIntent().getIntExtra(ImagePreview.KEY_POSITION, 0);
		vTitleCenter.setText(String.format("%d/%d", mPosition+1, mImageList.size()));
		mViewPager.setAdapter(new ImagePageAdapter<MediaFile, PinchImageView>(mImageList)
		{
			@Override
			public PinchImageView bindView(ViewGroup pa, int p, PinchImageView view){
				if(view==null) view=new PinchImageView(getContext());
				else view.reset();
				try{
					ConfigManager.getInstance().getImageLoader().loadImage(view, getItem(p).getPath());
				}catch(Throwable ex){
					ex.printStackTrace();
				}
				return view;
			}
		});
		mViewPager.setCurrentItem(mPosition);
		//更新当前页面状态
		setIvPlayShow(mImageList.get(mPosition));
		updateSelectButton(mImageList.get(mPosition).getPath());
		updateCommitButton();
	}
	
	void clickCommit(){
		if(SelectionManager.getInstance().getSelectList().isEmpty()){
			SelectionManager.getInstance().select(mImageList.get(mPosition).getPath());
		}
		setResult(RESULT_OK, new Intent());
		finish();
	}
	
	/**
	 更新确认按钮状态
	 */
	private void updateCommitButton(){
		int maxCount=SelectionManager.getInstance().getMaxCount();
		//改变确定按钮UI
		int selectCount=SelectionManager.getInstance().getSelectList().size();
		if(selectCount==0){
			vTitleRight.setEnabled(true);
			if(ImageCrop.from(getIntent()).needCrop()){
				vTitleRight.setText(R.string.confirm_next);
				return;
			}
			vTitleRight.setText(getString(R.string.confirm));
			return;
		}
		if(selectCount<maxCount){
			vTitleRight.setEnabled(true);
			vTitleRight.setText(String.format(getString(R.string.confirm_msg), selectCount, maxCount));
			return;
		}
		if(selectCount==maxCount){
			vTitleRight.setEnabled(true);
			if(ImageCrop.from(getIntent()).needCrop()){
				vTitleRight.setText(R.string.confirm_next);
				return;
			}
			vTitleRight.setText(String.format(getString(R.string.confirm_msg), selectCount, maxCount));
			return;
		}
	}
	
	/**
	 更新选择按钮状态
	 */
	private void updateSelectButton(String imagePath){
		boolean isSelect=SelectionManager.getInstance().isImageSelect(imagePath);
		if(isSelect){
			vImageCheck.setImageDrawable(getResources().getDrawable(R.mipmap.icon_image_checked));
		}else{
			vImageCheck.setImageDrawable(getResources().getDrawable(R.mipmap.icon_image_check));
		}
	}
	
	/**
	 设置是否显示视频播放按钮
	 */
	private void setIvPlayShow(MediaFile mediaFile){
		if(mediaFile.getDuration()>0){
			vVideoPlay.setVisibility(View.VISIBLE);
		}else{
			vVideoPlay.setVisibility(View.GONE);
		}
	}
}
