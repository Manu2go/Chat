package chat.manan.chat.helper;
import android.widget.MediaController;
import android.content.Context;
import android.support.v7.view.ContextThemeWrapper;

import chat.manan.chat.R;

public final class video_media_controller extends MediaController {

    public video_media_controller(Context context) {
        super(new ContextThemeWrapper(context, R.style.MusicPlayer));
    }
}