#README

整合项目：  
- https://github.com/jeasonlzy/ImagePicker  
- https://github.com/Lichenwei-Dev/ImagePicker  

实现功能：
- 图片选择  
  class ImagePicker 
- 图片裁剪  
  class ImageCrop 
- 图片浏览  
  class ImagePreview

代码示例：

	final int REQ_PICK=100;  
	  
	void clickStart(){  
	　　ImagePicker.getInstance()  
	　　　　.setImageLoader(new DefaultImageLoader())  
	　　　　.setCrop(true) //需要裁剪  
	　　　　.showCamera(true) //显示相机  
	　　　　.showImage(true) //显示图片  
	　　　　.showVideo(false) //显示视频  
	　　　　.setCropSize(360, 360) //裁剪宽高  
	　　　　.setCropDir("sdcard/DCIM/test") //保存路径  
	　　　　.start(this, REQ_PICK);  
	}  
	  
	public void onActivityResult(int reqCode, int resCode, Intent intent){  
	　　if(resCode!=RESULT_OK) return;  
	　　if(reqCode==REQ_PICK){  
	　　　　List<String> list=intent.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES);  
	　　　　startActivity(new ImagePreview().setImages(list).intent(getContext())); //浏览选取的图片  
	　　}  
	}  