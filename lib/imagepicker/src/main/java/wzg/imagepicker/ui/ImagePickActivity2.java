package wzg.imagepicker.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.List;
import wzg.imagepicker.ImageCrop;
import wzg.imagepicker.ImagePick.K1;
import wzg.imagepicker.ImagePreview.K3;
import wzg.imagepicker.adapter.ImagePageAdapter;
import wzg.imagepicker.content.ImagePickFileProvider;
import wzg.imagepicker.data.MediaFile;
import wzg.imagepicker.manager.SelectionManager;
import wzg.imagepicker.pkg.R;
import wzg.imagepicker.utils.DataUtil;
import wzg.imagepicker.view.PinchImageView;

/**
 * Created by wuzhengu on 2020/02/01 <br/>
 * 大图查看及选择
 */
public class ImagePickActivity2 extends ImageBaseActivity
{
	private TextView vTitleCenter;
	private TextView vTitleRight;
	private ViewPager vPager;
	private ImageView vPlay;
	private LinearLayout vSelect;
	private ImageView vCheck;
	private List<MediaFile> mImageList;
	private int mPosition;
	private int mMaxCount;
	private boolean mShowVideo;
	
	@Override
	protected int getLayoutId(){
		return R.layout.activity_image_pre_pick;
	}
	
	@Override
	protected void onCreate(Bundle state){
		super.onCreate(state);
		Intent intent = getIntent();
		mPosition = K3.INDEX.get(intent, 0);
		mMaxCount = K1.MAX_COUNT.get(intent, 1);
		mShowVideo = K1.SHOW_VIDEO.get(intent, true);
		mImageList = DataUtil.getInstance().getMediaData();
		vTitleCenter = findViewById(R.id.image_title_center);
		vTitleRight = findViewById(R.id.image_title_right);
		vPlay = findViewById(R.id.image_video_play);
		vPager = findViewById(R.id.image_view_pager);
		vSelect = findViewById(R.id.image_item_select);
		vCheck = findViewById(R.id.image_item_check);
		findViewById(R.id.image_title_left).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				finish();
			}
		});
		vPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels){
			}
			
			@Override
			public void onPageSelected(int position){
				mPosition = position;
				vTitleCenter.setText(String.format("%d/%d", position+1, mImageList.size()));
				setIvPlayShow(mImageList.get(position));
				updateSelectButton(mImageList.get(position).getPath());
			}
			
			@Override
			public void onPageScrollStateChanged(int state){
			}
		});
		vSelect.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				//如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
				Context ctxt = v.getContext();
				if(mMaxCount==1){
					List<String> list = SelectionManager.getInstance().getList();
					if(!list.isEmpty()){
						//判断选中集合中第一项是否为视频
						if(!SelectionManager.isCanAddSelectionPaths(
							mImageList.get(vPager.getCurrentItem()).getPath(), list.get(0))){
							//类型不同
							Toast.makeText(ctxt, getString(R.string.single_type_choose),
								Toast.LENGTH_SHORT).show();
							return;
						}
					}
				}
				boolean addSuccess = SelectionManager.getInstance()
					.select(mImageList.get(vPager.getCurrentItem()).getPath());
				if(addSuccess){
					updateSelectButton(mImageList.get(vPager.getCurrentItem()).getPath());
					updateCommitButton();
				}else{
					Toast.makeText(ctxt, String.format(getString(R.string.select_image_max),
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
		vPlay.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				//实现播放视频的跳转逻辑(调用原生视频播放器)
				Intent intent = new Intent(Intent.ACTION_VIEW);
				File file = new File(mImageList.get(vPager.getCurrentItem()).getPath());
				Uri uri = ImagePickFileProvider.toUri(v.getContext(), file);
				intent.setDataAndType(uri, "video/*");
				//给所有符合跳转条件的应用授权
				List<ResolveInfo> resInfoList =
					getPackageManager().queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
				for(ResolveInfo resolveInfo : resInfoList){
					String packageName = resolveInfo.activityInfo.packageName;
					grantUriPermission(packageName, uri,
						Intent.FLAG_GRANT_READ_URI_PERMISSION|
						Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				}
				startActivity(intent);
			}
		});
		vTitleCenter.setText(String.format("%d/%d", mPosition+1, mImageList.size()));
		vPager.setAdapter(new ImagePageAdapter<MediaFile, PinchImageView>(mImageList)
		{
			@Override
			public PinchImageView bindView(ViewGroup pa, int p, PinchImageView view){
				if(view==null) view = new PinchImageView(getContext());
				else view.reset();
				try{
					ImagePickActivity.imageLoader.loadImage(view, getItem(p).getPath());
				}catch(Throwable ex){
					ex.printStackTrace();
				}
				return view;
			}
		});
		vPager.setCurrentItem(mPosition);
		//更新当前页面状态
		setIvPlayShow(mImageList.get(mPosition));
		updateSelectButton(mImageList.get(mPosition).getPath());
		updateCommitButton();
	}
	
	void clickCommit(){
		if(SelectionManager.getInstance().getList().isEmpty()){
			SelectionManager.getInstance().select(mImageList.get(mPosition).getPath());
		}
		setResult(RESULT_OK, new Intent());
		finish();
	}
	
	boolean checkCrop(){
		if(mShowVideo) return false;
		return ImageCrop.checkIntent(getIntent());
	}
	
	/**
	 * 更新确认按钮状态
	 */
	private void updateCommitButton(){
		int maxCount = SelectionManager.getInstance().getMaxCount();
		//改变确定按钮UI
		int selectCount = SelectionManager.getInstance().getList().size();
		if(selectCount==0){
			vTitleRight.setEnabled(true);
			if(checkCrop()){
				vTitleRight.setText(R.string.confirm_next);
				return;
			}
			vTitleRight.setText(getString(R.string.confirm));
			return;
		}
		if(selectCount<maxCount){
			vTitleRight.setEnabled(true);
			vTitleRight.setText(
				String.format(getString(R.string.confirm_msg), selectCount, maxCount));
			return;
		}
		if(selectCount==maxCount){
			vTitleRight.setEnabled(true);
			if(checkCrop()){
				vTitleRight.setText(R.string.confirm_next);
				return;
			}
			vTitleRight.setText(
				String.format(getString(R.string.confirm_msg), selectCount, maxCount));
			return;
		}
	}
	
	/**
	 * 更新选择按钮状态
	 */
	private void updateSelectButton(String imagePath){
		boolean isSelect = SelectionManager.getInstance().isImageSelect(imagePath);
		if(isSelect){
			vCheck.setImageDrawable(getResources().getDrawable(R.mipmap.icon_image_checked));
		}else{
			vCheck.setImageDrawable(getResources().getDrawable(R.mipmap.icon_image_check));
		}
	}
	
	/**
	 * 设置是否显示视频播放按钮
	 */
	private void setIvPlayShow(MediaFile mediaFile){
		if(mediaFile.getDuration()>0){
			vPlay.setVisibility(View.VISIBLE);
		}else{
			vPlay.setVisibility(View.GONE);
		}
	}
}
