package tcking.github.com.giraffeplayer2;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by tcking on 2017
 */

public class PlayerManager {
    public static final String TAG = "GiraffePlayerManager";
    private volatile String currentPlayerFingerprint;
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;

    private WeakHashMap<String, VideoView> videoViewsRef = new WeakHashMap<>();
    private Map<String, GiraffePlayer> playersRef = new ConcurrentHashMap<>();



    public static final PlayerManager instance = new PlayerManager();


    public static PlayerManager getInstance() {
        return instance;
    }

    public GiraffePlayer getCurrentPlayer() {
        return currentPlayerFingerprint == null ? null : playersRef.get(currentPlayerFingerprint);
    }

    private GiraffePlayer createPlayer(VideoView videoView) {
        VideoInfo videoInfo = videoView.getVideoInfo();
        log(videoInfo.getFingerprint(), "createPlayer");
        videoViewsRef.put(videoInfo.getFingerprint(), videoView);
        registerActivityLifecycleCallbacks(((Activity) videoView.getContext()).getApplication());
        GiraffePlayer player = GiraffePlayer.createPlayer(videoView.getContext(), videoInfo);
        playersRef.put(videoInfo.getFingerprint(), player);
        return player;
    }

    private synchronized void registerActivityLifecycleCallbacks(Application context) {
        if (activityLifecycleCallbacks != null) {
            return;
        }
        activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                GiraffePlayer currentPlayer = getCurrentPlayer();
                if (currentPlayer != null) {
                    currentPlayer.onActivityResumed();
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                GiraffePlayer currentPlayer = getCurrentPlayer();
                if (currentPlayer != null) {
                    currentPlayer.onActivityPaused();
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                GiraffePlayer currentPlayer = getCurrentPlayer();
                if (currentPlayer != null) {
                    currentPlayer.onActivityDestroyed();
                }
            }
        };
        context.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    public void releaseCurrent() {
        log(currentPlayerFingerprint, "releaseCurrent");
        GiraffePlayer currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            if (currentPlayer.getProxyPlayerListener() != null) {
                currentPlayer.getProxyPlayerListener().onCompletion(currentPlayer);
            }
            currentPlayer.release();
        }
        currentPlayerFingerprint = null;
    }


    public boolean isCurrentPlayer(String fingerprint) {
        return fingerprint != null && fingerprint.equals(this.currentPlayerFingerprint);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        GiraffePlayer currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            currentPlayer.onConfigurationChanged(newConfig);
        }
    }

    public boolean onBackPressed() {
        GiraffePlayer currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            return currentPlayer.onBackPressed();
        }
        return false;
    }

    public VideoView getVideoView(VideoInfo videoInfo) {
        return videoViewsRef.get(videoInfo.getFingerprint());
    }


    public void setCurrentPlayer(GiraffePlayer giraffePlayer) {
        VideoInfo videoInfo = giraffePlayer.getVideoInfo();
        log(videoInfo.getFingerprint(),"setCurrentPlayer");

        //if choose a new playerRef
        String fingerprint=videoInfo.getFingerprint();
        if (!isCurrentPlayer(fingerprint)) {
            try {
                log(videoInfo.getFingerprint(),"not same release before one:"+currentPlayerFingerprint);
                releaseCurrent();
                currentPlayerFingerprint = fingerprint;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            log(videoInfo.getFingerprint(),"is currentPlayer");
        }
    }

    public GiraffePlayer getPlayer(VideoView videoView) {
        VideoInfo videoInfo=videoView.getVideoInfo();
        GiraffePlayer player = playersRef.get(videoInfo.getFingerprint());
        if (player == null) {
            player = createPlayer(videoView);
        }
        return player;
    }

    public PlayerManager releaseByFingerprint(String fingerprint) {
        GiraffePlayer player = playersRef.get(fingerprint);
        if (player != null) {
            player.release();
        }
        return this;
    }

    public void removePlayer(String fingerprint) {
        playersRef.remove(fingerprint);
    }

    private void log(String fingerprint,String msg) {
        if (GiraffePlayer.debug) {
            Log.d(TAG, String.format("[setFingerprint:%s] %s",fingerprint,msg));
        }
    }
}
