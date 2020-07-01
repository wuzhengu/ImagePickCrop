package wzg.imagepicker.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import java.util.*;

/**
 Created by wuzhengu on 2020/02/01 <br/>
 */
public class DefaultImageLoader implements ImageLoader
{
	static final int cacheLimit=50;
	static final Map<Object, Bitmap> cache=new LinkedHashMap<Object, Bitmap>(cacheLimit, 0.75f, true)
	{
		@Override
		protected boolean removeEldestEntry(Entry<Object, Bitmap> old){
			boolean full=size()>cacheLimit;
			return full;
		}
	};
	static final List<Task> tasks=new ArrayList<>();
	Handler handler=new Handler();
	
	@Override
	public void loadImage(ImageView view, Object image){
		if(image==null){
			view.setImageDrawable(null);
			return;
		}
		if(image instanceof Integer){
			int res=(int)image;
			try{
				view.getResources().getResourceName(res);
				view.setImageResource(res);
			}catch(Throwable ex){
				view.setImageDrawable(new ColorDrawable(res));
			}
			return;
		}
		if(image instanceof Uri){
			view.setImageURI((Uri)image);
			return;
		}
		Bitmap bitmap;
		synchronized(cache){
			bitmap=cache.get(image);
		}
		if(bitmap!=null){
			view.setImageBitmap(bitmap);
			return;
		}
		view.setImageDrawable(null);
		Task task=new Task();
		task.handler=handler;
		task.view=view;
		task.image=image.toString();
		start(task);
	}
	
	@Override
	public void clearCache(Context ctxt){
		synchronized(tasks){
			for(Task item: tasks) item.cancel();
			tasks.clear();
		}
		synchronized(cache){
			cache.clear();
		}
	}
	
	static void start(Task task){
		synchronized(tasks){
			for(Task item: tasks){
				if(item.view==task.view){
					if(item.image.equals(task.image)) return;
					item.cancel();
				}
			}
			tasks.add(task);
			if(tasks.size()==1) new Thread()
			{
				@Override
				public void run(){
					while(true){
						Task task=null;
						synchronized(tasks){
							if(tasks.size()>0) task=tasks.get(0);
							if(task==null) break;
							if(task.view==null){
								tasks.remove(0);
								continue;
							}
						}
						String image=task.image;
						Bitmap bitmap=decode(image);
						Log.d("____", cache.size()+", "+bitmap.getWidth()+"x"+bitmap.getHeight()+", "+image);
						synchronized(cache){
							cache.put(image, bitmap);
						}
						List<Task> postTasks=null;
						synchronized(tasks){
							Iterator<Task> it=tasks.iterator();
							while(it.hasNext()){
								Task item=it.next();
								if(item.image.equals(image)){
									it.remove();
									if(item.view!=null && item.handler!=null){
										if(postTasks==null) postTasks=new ArrayList<>();
										postTasks.add(item);
									}
								}
							}
						}
						if(postTasks!=null){
							for(Task item: postTasks) item.post();
							postTasks.clear();
						}
					}
				}
			}.start();
		}
	}
	
	static Bitmap decode(String image){
		BitmapFactory.Options op=new BitmapFactory.Options();
		op.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(image, op);
		boolean isJpg=("image/jpeg".equals(op.outMimeType));
		if(isJpg) op.inPreferredConfig=Bitmap.Config.RGB_565;
		int size=Math.min(op.outWidth, op.outHeight);
		float scale=size/720f;
		if(scale>=2){
			double log=Math.floor(Math.log(scale)/Math.log(2));
			op.inSampleSize=(int)Math.pow(2, log);
			scale/=op.inSampleSize;
		}
		op.inJustDecodeBounds=false;
		Bitmap bitmap=BitmapFactory.decodeFile(image, op);
		if(scale>1){
			int width=Math.round(bitmap.getWidth()/scale);
			int height=Math.round(bitmap.getHeight()/scale);
			Bitmap bitmap2=Bitmap.createScaledBitmap(bitmap, width, height, true);
			bitmap.recycle();
			bitmap=bitmap2;
		}
		if(isJpg) try{
			ExifInterface exif=new ExifInterface(image);
			int rotate=exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch(rotate){
			case ExifInterface.ORIENTATION_NORMAL:
				rotate=0;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate=90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate=180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate=270;
				break;
			}
			if(rotate>0){
				Matrix matrix=new Matrix();
				matrix.postRotate(rotate);
				Bitmap bitmap2=Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				bitmap.recycle();
				bitmap=bitmap2;
			}
		}catch(Throwable ex){
			//ex.printStackTrace();
		}
		return bitmap;
	}
	
	static class Task implements Runnable
	{
		Handler handler;
		ImageView view;
		String image;
		
		void cancel(){
			handler=null;
			view=null;
		}
		
		void post(){
			if(handler!=null) handler.post(this);
		}
		
		@Override
		public void run(){
			Bitmap bitmap;
			synchronized(cache){
				bitmap=cache.get(image);
			}
			if(view!=null) view.setImageBitmap(bitmap);
		}
	}
}
