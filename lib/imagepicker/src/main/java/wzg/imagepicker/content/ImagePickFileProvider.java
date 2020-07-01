package wzg.imagepicker.content;

import android.content.Context;
import android.net.Uri;
import java.io.File;

public class ImagePickFileProvider extends android.support.v4.content.FileProvider
{
	public static String authority(Context ctxt){
		return ctxt.getPackageName()+".ImagePickFileProvider";
	}
	
	public static Uri toUri(Context ctxt, File file){
		return android.support.v4.content.FileProvider.getUriForFile(ctxt, authority(ctxt), file);
	}
}
