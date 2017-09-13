package tcking.github.com.giraffeplayer2;

/**
 * Created by tcking on 2017
 */

public class ProxyPlayerListener implements PlayerListener {
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
        return outerListener == null ? DefaultPlayerListener.INSTANCE : outerListener;
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
        listener().onPrepared(giraffePlayer);
        outerListener().onPrepared(giraffePlayer);
    }

    @Override
    public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {
        listener().onBufferingUpdate(giraffePlayer,percent);
        outerListener().onBufferingUpdate(giraffePlayer,percent);
    }

    @Override
    public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
        listener().onInfo(giraffePlayer,what,extra);
        return outerListener().onInfo(giraffePlayer,what,extra);
    }

    @Override
    public void onCompletion(GiraffePlayer giraffePlayer) {
        listener().onCompletion(giraffePlayer);
        outerListener().onCompletion(giraffePlayer);
    }

    @Override
    public void onSeekComplete(GiraffePlayer giraffePlayer) {
        listener().onSeekComplete(giraffePlayer);
        outerListener().onSeekComplete(giraffePlayer);

    }

    @Override
    public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
        listener().onError(giraffePlayer,what,extra);
        return outerListener().onError(giraffePlayer,what,extra);
    }

    @Override
    public void onPause(GiraffePlayer giraffePlayer) {
        listener().onPause(giraffePlayer);
        outerListener().onPause(giraffePlayer);
    }

    @Override
    public void onRelease(GiraffePlayer giraffePlayer) {
        listener().onRelease(giraffePlayer);
        outerListener().onRelease(giraffePlayer);

    }

    @Override
    public void onStart(GiraffePlayer giraffePlayer) {
        listener().onStart(giraffePlayer);
        outerListener().onStart(giraffePlayer);
    }

    @Override
    public void onTargetStateChange(int oldState, int newState) {
        listener().onTargetStateChange(oldState,newState);
        outerListener().onTargetStateChange(oldState,newState);
    }

    @Override
    public void onCurrentStateChange(int oldState, int newState) {
        listener().onCurrentStateChange(oldState,newState);
        outerListener().onCurrentStateChange(oldState,newState);
    }

    @Override
    public void onDisplayModelChange(int oldModel, int newModel) {
        listener().onDisplayModelChange(oldModel,newModel);
        outerListener().onDisplayModelChange(oldModel,newModel);
    }

    public void onPreparing() {
        listener().onPreparing();
        outerListener().onPreparing();
    }
}
