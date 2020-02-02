package wzg.lib.imagepicker.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils
{
	/**
	 获取屏幕的宽和高
	 */
	public static int[] getScreenSize(Context context){
		WindowManager windowManager=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displayMetrics=new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
	}

	/**
	 获取图片格式化时间
	 */
	public static String getImageTime(long timestamp){
		Calendar currentCalendar=Calendar.getInstance();
		currentCalendar.setTime(new Date());
		Calendar imageCalendar=Calendar.getInstance();
		imageCalendar.setTimeInMillis(timestamp);
		if(currentCalendar.get(Calendar.DAY_OF_YEAR)==imageCalendar.get(Calendar.DAY_OF_YEAR) &&
		   currentCalendar.get(Calendar.YEAR)==imageCalendar.get(Calendar.YEAR)){
			return "今天";
		}else if(currentCalendar.get(Calendar.WEEK_OF_YEAR)==imageCalendar.get(Calendar.WEEK_OF_YEAR) &&
		         currentCalendar.get(Calendar.YEAR)==imageCalendar.get(Calendar.YEAR)){
			return "本周";
		}else if(currentCalendar.get(Calendar.MONTH)==imageCalendar.get(Calendar.MONTH) &&
		         currentCalendar.get(Calendar.YEAR)==imageCalendar.get(Calendar.YEAR)){
			return "本月";
		}else{
			Date date=new Date(timestamp);
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM");
			return sdf.format(date);
		}
	}

	/**
	 获取视频时长（格式化）
	 */
	public static String getVideoDuration(long timestamp){
		if(timestamp<1000){
			return "00:01";
		}
		Date date=new Date(timestamp);
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("mm:ss");
		return simpleDateFormat.format(date);
	}
	
	/** 根据系统时间、前缀、后缀产生一个文件 */
	public static File createFile(File folder, String prefix, String suffix){
		folder.mkdirs();
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
		String name=dateFormat.format(new Date(System.currentTimeMillis()));
		if(!TextUtils.isEmpty(prefix)) name=prefix+name;
		if(!TextUtils.isEmpty(suffix)) name=name+suffix;
		return new File(folder, name);
	}
}
