package wzg.imagepicker.task;

import java.util.List;
import wzg.imagepicker.data.MediaFolder;

public interface MediaLoadCallback
{
	void onMediaLoad(List<MediaFolder> list);
}
