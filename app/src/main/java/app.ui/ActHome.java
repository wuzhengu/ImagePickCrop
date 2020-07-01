package app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import wzg.app.R;

public class ActHome extends BaseActivity
{
	final int REQ_PICK=100;
	
	@Override
	public void onCreate(Bundle state){
		super.onCreate(state);
		setContentView(R.layout.act_home);
		findViewById(R.id.home_start).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				clickStart();
			}
		});
	}
	
	void clickStart(){
		//ImagePicker.getInstance()
		//		.setImageLoader(new DefaultImageLoader())
		//		.setCrop(true) //需要裁剪
		//		.showCamera(true) //显示相机
		//		.showImage(true) //显示图片
		//		.showVideo(false) //显示视频
		//		.setCropSize(360, 360) //裁剪宽高
		//		.setCropDir("sdcard/DCIM/test") //保存路径
		//		.start(this, REQ_PICK);
	}
	
	@Override
	public void onActivityResult(int reqCode, int resCode, Intent intent){
		if(resCode!=RESULT_OK) return;
		if(reqCode==REQ_PICK){
			//List<String> list=intent.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES);
			//startActivity(new ImagePreview().setImages(list).intent(getContext())); //浏览选取的图片
		}
	}
}
