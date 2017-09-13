package tcking.github.com.giraffeplayer2;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ScrollingView;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.FrameLayout;

/**
 * Created by tcking on 2017
 */

public class VideoView extends FrameLayout{


    private MediaController mediaController;
    private PlayerListener playerListener;

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public VideoView setPlayerListener(PlayerListener playerListener) {
        this.playerListener = playerListener;
        return this;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public VideoView videoInfo(VideoInfo videoInfo) {
        if (this.videoInfo.getUri() != null && !this.videoInfo.getUri().equals(videoInfo.getUri())) {
            PlayerManager.getInstance().releaseByFingerprint(this.videoInfo.getFingerprint());
        }
        this.videoInfo = videoInfo;
        return this;
    }

    private VideoInfo videoInfo=new VideoInfo();

    public VideoView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private Activity activity;

    private void init(Context context) {
        activity= (Activity) context;
        initMediaController();
        setBackgroundColor(videoInfo.getBgColor());
    }



    private void initMediaController() {
        mediaController = new DefaultMediaController(getContext());
        mediaController.bind(this);
    }


    public VideoView setFingerprint(Object fingerprint) {
        videoInfo.setFingerprint(fingerprint);
        return this;
    }

    public VideoView setVideoPath(String uri) {
        videoInfo.setUri(Uri.parse(uri));
        return this;
    }

    public GiraffePlayer getPlayer() {
        if (videoInfo.getUri() == null) {
            throw new RuntimeException("player uri is null");
        }
        return PlayerManager.getInstance().getPlayer(this);
    }

    /**
     * is current active player (in list controllerView there are many players)
     * @return
     */
    public boolean isCurrentActivePlayer() {
        return PlayerManager.getInstance().isCurrentPlayer(videoInfo.getFingerprint());
    }

    public MediaController getMediaController() {
        return mediaController;
    }

    /**
     * is video controllerView in 'list' controllerView
     * @return
     */
    public boolean inListView() {
        for (ViewParent vp = getParent(); vp != null; vp = vp.getParent()) {
            if (vp instanceof AbsListView || vp instanceof ScrollingView) {
                return true;
            }
        }
        return false;
    }
}
