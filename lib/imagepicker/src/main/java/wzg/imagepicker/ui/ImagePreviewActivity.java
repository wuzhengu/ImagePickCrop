package wzg.imagepicker.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import wzg.imagepicker.ImagePreview;
import wzg.imagepicker.pkg.R;
import wzg.imagepicker.adapter.ImagePageAdapter;
import wzg.imagepicker.manager.ConfigManager;
import wzg.imagepicker.view.PinchImageView;

/**
 Created by wuzhengu on 2020/02/02 <br/>
 图片浏览
 */
public class ImagePreviewActivity extends ImageBaseActivity
{
	private TextView vTitleCenter;
	private TextView vTitleRight;
	private ViewPager mViewPager;
	private List<String> mImageList;
	private int mPosition;
	private int mListSize;
	private String mTitle;
	
	@Override
	protected int getLayoutId(){
		return R.layout.activity_image_preview;
	}
	
	@Override
	protected void onCreate(@Nullable Bundle state){
		super.onCreate(state);
		ImagePreview preview=ImagePreview.from(getIntent());
		mPosition=preview.getPosition();
		mImageList=preview.getImages();
		mListSize=mImageList==null? 0: mImageList.size();
		mTitle=getString(R.string.image_view);
		vTitleCenter=(TextView)findViewById(R.id.image_title_center);
		vTitleRight=(TextView)findViewById(R.id.image_title_right);
		mViewPager=(ViewPager)findViewById(R.id.image_view_pager);
		vTitleCenter.setText(mTitle);
		vTitleRight.setVisibility(View.GONE);
		findViewById(R.id.image_title_left).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				finish();
			}
		});
		initPager();
	}
	
	void initPager(){
		if(mListSize<=0) return;
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			{
				onPageSelected(0);
			}
			
			@Override
			public void onPageSelected(int p){
				String text=mTitle;
				if(mListSize>1) text+=" "+(p+1)+"/"+mListSize;
				vTitleCenter.setText(text);
			}
			
			@Override
			public void onPageScrolled(int p, float ratio, int offset){
			}
			
			@Override
			public void onPageScrollStateChanged(int state){
			}
		});
		mViewPager.setAdapter(new ImagePageAdapter<String, PinchImageView>(mImageList)
		{
			@Override
			public PinchImageView bindView(ViewGroup pa, int p, PinchImageView view){
				if(view==null) view=new PinchImageView(getContext());
				else view.reset();
				try{
					ConfigManager.getInstance().getImageLoader().loadImage(view, getItem(p));
				}catch(Throwable ex){
					ex.printStackTrace();
				}
				return view;
			}
		});
		mViewPager.setCurrentItem(mPosition);
	}
}
