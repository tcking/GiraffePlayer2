package tcking.github.com.giraffeplayer2;

import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * Created by tcking on 2017
 */

public interface PlayerListener {

    void onPrepared(GiraffePlayer giraffePlayer);

    /**
     * Called to update status in buffering a media stream received through progressive HTTP download.
     * @param giraffePlayer
     * @param percent nt: the percentage (0-100) of the content that has been buffered or played thus far
     */
    void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent);

    boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra);

    void onCompletion(GiraffePlayer giraffePlayer);

    void onSeekComplete(GiraffePlayer giraffePlayer);

    boolean onError(GiraffePlayer giraffePlayer,int what, int extra);

    void onPause(GiraffePlayer giraffePlayer);

    void onRelease(GiraffePlayer giraffePlayer);

    void onStart(GiraffePlayer giraffePlayer);

    void onTargetStateChange(int oldState, int newState);

    void onCurrentStateChange(int oldState, int newState);

    void onDisplayModelChange(int oldModel, int newModel);

    void onPreparing(GiraffePlayer giraffePlayer);

    /**
     * render subtitle
     * @param giraffePlayer
     * @param text
     */
    void onTimedText(GiraffePlayer giraffePlayer,IjkTimedText text);

    void onLazyLoadProgress(GiraffePlayer giraffePlayer,int progress);

    void onLazyLoadError(GiraffePlayer giraffePlayer, String message);
}
