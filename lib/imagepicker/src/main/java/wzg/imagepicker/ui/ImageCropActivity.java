package wzg.imagepicker.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import wzg.imagepicker.ImageCrop.K2;
import wzg.imagepicker.pkg.R;
import wzg.imagepicker.view.CropImageView;

/**
 * Created by wuzhengu on 2020/02/01 <br/>
 * 图片裁剪
 */
public class ImageCropActivity extends ImageBaseActivity
	implements View.OnClickListener, CropImageView.OnBitmapSaveCompleteListener
{
	private CropImageView ivCrop;
	private int mCropWidth;
	private int mCropHeight;
	private boolean mCropRound;
	private File mDir;
	
	@Override
	protected int getLayoutId(){
		return R.layout.activity_image_crop;
	}
	
	@Override
	protected void onCreate(Bundle state){
		super.onCreate(state);
		//初始化View
		findViewById(R.id.image_title_left).setOnClickListener(this);
		Button btn_ok = findViewById(R.id.image_title_right);
		btn_ok.setOnClickListener(this);
		TextView tv_des = findViewById(R.id.image_title_center);
		tv_des.setText(getString(R.string.image_crop));
		ivCrop = findViewById(R.id.image_crop_view);
		ivCrop.setOnBitmapSaveCompleteListener(this);
		//获取需要的参数
		Intent intent = getIntent();
		mCropWidth = K2.WIDTH.get(intent, 0);
		mCropHeight = K2.HEIGHT.get(intent, 0);
		mCropRound = K2.ROUND.get(intent, false);
		String image = K2.IMAGE.get(intent, null);
		String dirPath = K2.DIR.get(intent, null);
		if(dirPath!=null) mDir = new File(dirPath);
		else mDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		ImagePickActivity.getImageLoader(intent).loadImage(ivCrop, image);
	}
	
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
		int width = options.outWidth;
		int height = options.outHeight;
		int inSampleSize = 1;
		if(height>reqHeight || width>reqWidth){
			if(width>height){
				inSampleSize = width/reqWidth;
			}else{
				inSampleSize = height/reqHeight;
			}
		}
		return inSampleSize;
	}
	
	@Override
	public void onClick(View v){
		int id = v.getId();
		if(id==R.id.image_title_left){
			setResult(RESULT_CANCELED);
			finish();
		}else if(id==R.id.image_title_right){
			ivCrop.saveBitmapToFile(mDir, mCropWidth, mCropHeight, mCropRound);
		}
	}
	
	@Override
	public void onBitmapSaveSuccess(File file){
		//裁剪后替换掉返回数据的内容，但是不要改变全局中的选中数据
		String path = file.getAbsolutePath();
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+path)));
		Intent intent = new Intent();
		K2.IMAGE.set(intent, path);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	public void onBitmapSaveError(File file){
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		ivCrop.setOnBitmapSaveCompleteListener(null);
	}
}
