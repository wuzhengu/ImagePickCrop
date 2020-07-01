package wzg.imagepicker;

import android.content.Context;
import android.support.v4.content.FileProvider;

public class ImageFileProvider extends FileProvider
{
	public static String getFileProviderName(Context context){
		return context.getPackageName()+".ImageFileProvider";
	}
}
