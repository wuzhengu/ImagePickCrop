package wzg.imagepicker.listener;

import java.util.List;
import wzg.imagepicker.data.MediaFolder;

public interface MediaLoadCallback
{
	void loadMediaSuccess(List<MediaFolder> mediaFolderList);
}
