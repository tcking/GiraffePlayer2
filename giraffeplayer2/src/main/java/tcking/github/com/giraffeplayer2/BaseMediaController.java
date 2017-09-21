package tcking.github.com.giraffeplayer2;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.tcking.viewquery.ViewQuery;

/**
 * base of media controller
 * Created by tcking on 2017.
 */

public abstract class BaseMediaController extends DefaultPlayerListener implements MediaController,Handler.Callback {

    protected static final int MESSAGE_SHOW_PROGRESS = 1;
    protected static final int MESSAGE_FADE_OUT = 2;
    protected static final int MESSAGE_SEEK_NEW_POSITION = 3;
    protected static final int MESSAGE_HIDE_CENTER_BOX = 4;
    protected static final int MESSAGE_RESTART_PLAY = 5;

    protected final Context context;
    protected final AudioManager audioManager;
    protected ViewQuery $;

    protected int defaultTimeout = 3 * 1000;
    protected Handler handler;
    protected VideoView videoView;
    protected View controllerView;

    public BaseMediaController(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        handler = new Handler(Looper.getMainLooper(),this);
    }


    @Override
    public void bind(VideoView videoView) {
        this.videoView = videoView;
        controllerView = makeControllerView();
        $ = new ViewQuery(controllerView);
        initView(controllerView);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.videoView.getContainer().addView(controllerView, layoutParams);
    }

    protected abstract View makeControllerView();

    protected abstract void initView(View view);

}
