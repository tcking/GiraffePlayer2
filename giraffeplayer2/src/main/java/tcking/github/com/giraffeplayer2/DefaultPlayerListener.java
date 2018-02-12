package tcking.github.com.giraffeplayer2;

import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * Created by tcking on 2017
 */

public class DefaultPlayerListener implements PlayerListener {

    public static final DefaultPlayerListener INSTANCE = new DefaultPlayerListener();

    @Override
    public void onPrepared(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {

    }

    @Override
    public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
        return true;
    }

    @Override
    public void onCompletion(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onSeekComplete(GiraffePlayer giraffePlayer) {

    }

    @Override
    public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
        return true;
    }

    @Override
    public void onPause(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onRelease(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onStart(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onTargetStateChange(int oldState, int newState) {

    }

    @Override
    public void onCurrentStateChange(int oldState, int newState) {

    }

    @Override
    public void onDisplayModelChange(int oldModel, int newModel) {

    }

    @Override
    public void onPreparing(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onTimedText(GiraffePlayer giraffePlayer, IjkTimedText text) {

    }

    @Override
    public void onLazyLoadError(GiraffePlayer giraffePlayer, String message) {

    }

    @Override
    public void onLazyLoadProgress(GiraffePlayer giraffePlayer,int progress) {

    }
}
