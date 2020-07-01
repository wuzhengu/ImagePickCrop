package app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.util.List;
import wzg.app.R;
import wzg.imagepicker.ImageCrop.K2;
import wzg.imagepicker.ImagePick;
import wzg.imagepicker.ImagePick.K1;
import wzg.imagepicker.ImagePreview;
import wzg.imagepicker.ImagePreview.K3;

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
		ImagePick.with(this)
			.set(K1.SHOW_CAMERA, true) //显示相机
			.set(K1.SHOW_IMAGE, true) //显示图片
			.set(K1.SHOW_VIDEO, false) //隐藏视频
			.set(K2.WIDTH, 360) //裁剪宽度
			.set(K2.HEIGHT, 360) //裁剪高度
			.set(K2.DIR, "sdcard/DCIM/test") //保存路径
			.start(REQ_PICK);
	}
	
	@Override
	public void onActivityResult(int reqCode, int resCode, Intent intent){
		if(resCode!=RESULT_OK) return;
		if(reqCode==REQ_PICK){
			List<String> list = K1.ITEMS.get(intent, null);
			ImagePreview.with(this).set(K3.ITEMS, list).start(-1); //浏览选取的图片
		}
	}
}
