package wzg.lib.imagepicker.listener;

import java.util.List;
import wzg.lib.imagepicker.data.MediaFolder;

public interface MediaLoadCallback
{
	void loadMediaSuccess(List<MediaFolder> mediaFolderList);
}
