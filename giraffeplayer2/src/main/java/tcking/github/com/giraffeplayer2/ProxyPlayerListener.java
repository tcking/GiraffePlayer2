package tcking.github.com.giraffeplayer2;

import android.util.Log;

import tv.danmaku.ijk.media.player.IjkTimedText;


/**
 * Created by tcking on 2017
 */

public class ProxyPlayerListener implements PlayerListener {
    private static final String TAG = "GiraffeListener";
    private VideoInfo videoInfo;

    public ProxyPlayerListener(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public PlayerListener getOuterListener() {
        return outerListener;
    }

    private PlayerListener outerListener;




    public void setOuterListener(PlayerListener outerListener) {
        this.outerListener = outerListener;
    }

    private PlayerListener outerListener() {
        if (outerListener != null) {
            return outerListener;
        }
        VideoView videoView = PlayerManager.getInstance().getVideoView(videoInfo);
        if (videoView != null && videoView.getPlayerListener() != null) {
            return videoView.getPlayerListener();
        }
        return DefaultPlayerListener.INSTANCE;
    }

    private PlayerListener listener() {
        VideoView videoView = PlayerManager.getInstance().getVideoView(videoInfo);
        if (videoView != null && videoView.getMediaController() != null) {
            return videoView.getMediaController();
        }
        return DefaultPlayerListener.INSTANCE;
    }

    @Override
    public void onPrepared(GiraffePlayer giraffePlayer) {
        log("onPrepared");
        listener().onPrepared(giraffePlayer);
        outerListener().onPrepared(giraffePlayer);
    }

    @Override
    public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {
//        if (GiraffePlayer.debug) {
//            log("onBufferingUpdate:"+percent);
//        }
        listener().onBufferingUpdate(giraffePlayer,percent);
        outerListener().onBufferingUpdate(giraffePlayer,percent);
    }

    @Override
    public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
        if (GiraffePlayer.debug) {
            log("onInfo:"+what+","+extra);
        }
        listener().onInfo(giraffePlayer,what,extra);
        return outerListener().onInfo(giraffePlayer,what,extra);
    }

    @Override
    public void onCompletion(GiraffePlayer giraffePlayer) {
        log("onCompletion");
        listener().onCompletion(giraffePlayer);
        outerListener().onCompletion(giraffePlayer);
    }

    @Override
    public void onSeekComplete(GiraffePlayer giraffePlayer) {
        log("onSeekComplete");
        listener().onSeekComplete(giraffePlayer);
        outerListener().onSeekComplete(giraffePlayer);

    }

    @Override
    public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
        if (GiraffePlayer.debug) {
            log("onError:"+what+","+extra);
        }
        listener().onError(giraffePlayer,what,extra);
        return outerListener().onError(giraffePlayer,what,extra);
    }

    @Override
    public void onPause(GiraffePlayer giraffePlayer) {
        log("onPause");
        listener().onPause(giraffePlayer);
        outerListener().onPause(giraffePlayer);
    }

    @Override
    public void onRelease(GiraffePlayer giraffePlayer) {
        log("onRelease");
        listener().onRelease(giraffePlayer);
        outerListener().onRelease(giraffePlayer);

    }

    @Override
    public void onStart(GiraffePlayer giraffePlayer) {
        log("onStart");
        listener().onStart(giraffePlayer);
        outerListener().onStart(giraffePlayer);
    }

    @Override
    public void onTargetStateChange(int oldState, int newState) {
        if (GiraffePlayer.debug) {
            log("onTargetStateChange:"+oldState+"->"+newState);
        }
        listener().onTargetStateChange(oldState,newState);
        outerListener().onTargetStateChange(oldState,newState);
    }

    @Override
    public void onCurrentStateChange(int oldState, int newState) {
        if (GiraffePlayer.debug) {
            log("onCurrentStateChange:"+oldState+"->"+newState);
        }
        listener().onCurrentStateChange(oldState,newState);
        outerListener().onCurrentStateChange(oldState,newState);
    }

    @Override
    public void onDisplayModelChange(int oldModel, int newModel) {
        if (GiraffePlayer.debug) {
            log("onDisplayModelChange:"+oldModel+"->"+newModel);
        }
        listener().onDisplayModelChange(oldModel,newModel);
        outerListener().onDisplayModelChange(oldModel,newModel);
    }

    public void onPreparing(GiraffePlayer giraffePlayer) {
        log("onPreparing");
        listener().onPreparing(giraffePlayer);
        outerListener().onPreparing(giraffePlayer);
    }

    @Override
    public void onTimedText(GiraffePlayer giraffePlayer, IjkTimedText text) {
        if (GiraffePlayer.debug) {
            log("onTimedText:"+(text!=null?text.getText():"null"));
        }
        listener().onTimedText(giraffePlayer,text);
        outerListener().onTimedText(giraffePlayer,text);
    }

    @Override
    public void onLazyLoadProgress(GiraffePlayer giraffePlayer,int progress) {
        if (GiraffePlayer.debug) {
            log("onLazyLoadProgress:"+progress);
        }
        listener().onLazyLoadProgress(giraffePlayer,progress);
        outerListener().onLazyLoadProgress(giraffePlayer,progress);
    }

    @Override
    public void onLazyLoadError(GiraffePlayer giraffePlayer, String message) {
        if (GiraffePlayer.debug) {
            log("onLazyLoadError:"+message);
        }
        listener().onLazyLoadError(giraffePlayer,message);
        outerListener().onLazyLoadError(giraffePlayer,message);
    }

    private void log(String msg) {
        if (GiraffePlayer.debug) {
            Log.d(TAG, String.format("[fingerprint:%s] %s", videoInfo.getFingerprint(), msg));
        }
    }
}
