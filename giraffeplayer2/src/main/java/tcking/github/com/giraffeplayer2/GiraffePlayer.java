package tcking.github.com.giraffeplayer2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.github.tcking.giraffeplayer2.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


/**
 * Created by tcking on 2017
 */

public class GiraffePlayer implements MediaController.MediaPlayerControl {
    public static final String TAG = "GiraffePlayer";
    public static boolean debug = false;

    // Internal messages
    private static final int MSG_CTRL_PLAYING = 1;
    private static final int MSG_CTRL_PAUSE = 2;
    private static final int MSG_CTRL_SEEK = 3;
    private static final int MSG_CTRL_RELEASE = 9;


    private static final int MSG_SET_DISPLAY = 12;


    // all possible internal states
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;
    private final HandlerThread internalPlaybackThread;

    private int currentBufferPercentage = 0;
    private boolean canPause = true;
    private boolean canSeekBackward = true;
    private boolean canSeekForward = true;
    private int audioSessionId;
    private int seekWhenPrepared;


    private int currentState = STATE_IDLE;
    private int targetState = STATE_IDLE;
    private Uri uri;
    private Map<String, String> headers;
    private Context context;

    private IMediaPlayer mediaPlayer;
    private volatile boolean released;
    private Handler handler;
    private Handler uiHandler=new Handler(Looper.getMainLooper());
    private ProxyPlayerListener proxyListener;
    private WeakReference<FrameLayout> displayGroupRef;
    private WeakReference<ViewGroup> containerRef;

    public static final int DISPLAY_NORMAL = 0;
    public static final int DISPLAY_FULL_WINDOW = 1;

    public int getDisplayModel() {
        return displayModel;
    }

    private int displayModel = DISPLAY_NORMAL;
    private VideoInfo videoInfo;


    private ProxyPlayerListener proxyListener(){
        return proxyListener;
    }



    private GiraffePlayer(Context context,VideoInfo videoInfo) {
        this.context = context.getApplicationContext();
        this.videoInfo = videoInfo;
        log("new GiraffePlayer");
        this.proxyListener = new ProxyPlayerListener(videoInfo);
        internalPlaybackThread = new HandlerThread("GiraffePlayerInternal:Handler", Process.THREAD_PRIORITY_AUDIO);
        internalPlaybackThread.start();
        handler = new Handler(internalPlaybackThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //init mediaPlayer before any actions
                if (mediaPlayer == null || released) {
                    handler.removeCallbacks(null);
                    init();
                    handler.sendMessage(Message.obtain(msg));
                    return true;
                }
                switch (msg.what) {
                    case MSG_CTRL_PLAYING:
                        if(currentState == STATE_PLAYBACK_COMPLETED){
                            mediaPlayer.seekTo(0);
                            mediaPlayer.start();
                            currentState(STATE_PLAYING);
                        } else {
                            if (isInPlaybackState()) {
                                mediaPlayer.start();
                                currentState(STATE_PLAYING);
                            }
                        }
                        break;
                    case MSG_CTRL_PAUSE:
                        mediaPlayer.pause();
                        currentState = STATE_PAUSED;
                        break;
                    case MSG_CTRL_SEEK:
                        mediaPlayer.seekTo((int) msg.obj);
                        break;
                    case MSG_SET_DISPLAY:
                        if (msg.obj == null) {
                            mediaPlayer.setDisplay(null);
                        }else if (msg.obj instanceof SurfaceTexture) {
                            mediaPlayer.setSurface(new Surface((SurfaceTexture) msg.obj));
                        } else if (msg.obj instanceof SurfaceView) {
                            mediaPlayer.setDisplay(((SurfaceView) msg.obj).getHolder());
                        }
                        break;
                    case MSG_CTRL_RELEASE:
                        handler.removeCallbacks(null);
                        doRelease(((String) msg.obj));
                        break;
                    default:
                }
                return true;
            }
        });
        PlayerManager.getInstance().setCurrentPlayer(this);
    }




    private boolean isInPlaybackState() {
        return (mediaPlayer != null &&
                currentState != STATE_ERROR &&
                currentState != STATE_IDLE &&
                currentState != STATE_PREPARING);
    }


    @Override
    public void start() {
        targetState(STATE_PLAYING);
        handler.sendEmptyMessage(MSG_CTRL_PLAYING);
        proxyListener().onStart(this);
    }

    private void targetState(int newState) {
        proxyListener().onTargetStateChange(targetState,newState);
        targetState = newState;
    }

    private void currentState(int newState) {
        proxyListener().onCurrentStateChange(currentState,newState);
        currentState = newState;
    }

    @Override
    public void pause() {
        targetState(STATE_PAUSED);
        handler.sendEmptyMessage(MSG_CTRL_PAUSE);
        proxyListener().onPause(this);
    }

    @Override
    public int getDuration() {
        if (mediaPlayer == null) {
            return 0;
        }
        return (int) mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        if (mediaPlayer == null) {
            return 0;
        }
        return (int) mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        handler.obtainMessage(MSG_CTRL_SEEK, pos).sendToTarget();
    }

    @Override
    public boolean isPlaying() {
        //mediaPlayer.isPlaying()
        return currentState == STATE_PLAYING;
    }

    @Override
    public int getBufferPercentage() {
        return currentBufferPercentage;
    }

    @Override
    public boolean canPause() {
        return canPause;
    }

    @Override
    public boolean canSeekBackward() {
        return canSeekBackward;
    }

    @Override
    public boolean canSeekForward() {
        return canSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        if (audioSessionId == 0) {
            audioSessionId = mediaPlayer.getAudioSessionId();
        }
        return audioSessionId;
    }


    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    private GiraffePlayer setVideoPath(String path) throws IOException {
        return setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    private GiraffePlayer setVideoURI(Uri uri) throws IOException {
        return setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    private GiraffePlayer setVideoURI(Uri uri, Map<String, String> headers) throws IOException {
        this.uri = uri;
        this.headers = headers;
        seekWhenPrepared = 0;
        return this;
    }

    private void init() {
        log("init");
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                proxyListener().onPreparing();
            }
        });
        mediaPlayer = new IjkMediaPlayer(Looper.getMainLooper());
        IjkMediaPlayer.native_setLogLevel(debug ? IjkMediaPlayer.IJK_LOG_DEBUG : IjkMediaPlayer.IJK_LOG_ERROR);
        released = false;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                currentState(STATE_PREPARED);
                proxyListener().onPrepared(GiraffePlayer.this);
                if (targetState == STATE_PLAYING) {
                    iMediaPlayer.start();
                    currentState(STATE_PLAYING);
                }
            }
        });
        initInternalListener();
        VideoView videoView = PlayerManager.getInstance().getVideoView(videoInfo);
        if (videoView != null) {
            createDisplay(videoView);
        }
        try {
            uri = videoInfo.getUri();
            mediaPlayer.setDataSource(context, uri, headers);
            currentState(STATE_PREPARING);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initInternalListener() {
        //proxyListener fire on main thread
        mediaPlayer.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
                proxyListener().onBufferingUpdate(GiraffePlayer.this,percent);
            }
        });
        mediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            //https://developer.android.com/reference/android/media/MediaPlayer.OnInfoListener.html
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
                if (debug) {
                    log("onInfo:what:"+what+",extra:"+extra);
                }
                return proxyListener().onInfo(GiraffePlayer.this,what,extra);
            }
        });
        mediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                currentState = STATE_PLAYBACK_COMPLETED;
                proxyListener().onCompletion(GiraffePlayer.this);
            }
        });
        mediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
                if (debug) {
                    log("onError:what:"+what+",extra:"+extra);
                }
                return proxyListener().onError(GiraffePlayer.this,what,extra);
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                proxyListener().onSeekComplete(GiraffePlayer.this);
            }
        });
        mediaPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(final IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                if (debug) {
                    log("onVideoSizeChanged:width:"+width+",height:"+height);
                }
                int videoWidth = mp.getVideoWidth();
                int videoHeight = mp.getVideoHeight();
//                int videoSarNum = mp.getVideoSarNum();
//                int videoSarDen = mp.getVideoSarDen();
                if (videoWidth != 0 && videoHeight != 0) {
                    View currentDisplay = getCurrentDisplay();
                    if (currentDisplay != null && currentDisplay instanceof ScalableDisplay) {
                        ScalableDisplay scalableDisplay = (ScalableDisplay) currentDisplay;
                        scalableDisplay.setVideoSize(videoWidth, videoHeight);
                    }
                }
            }
        });
    }

    public static GiraffePlayer createPlayer(Context context,VideoInfo videoInfo) {
        return new GiraffePlayer(context,videoInfo);
    }

    private GiraffePlayer displayOn(final TextureView textureView) {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            private SurfaceTexture surface;
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (this.surface == null) {
                    handler.obtainMessage(MSG_SET_DISPLAY, surface).sendToTarget();
                    this.surface = surface;
                } else {
                    textureView.setSurfaceTexture(this.surface);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;//全屏时会发生view的移动，会触发此回调，必须为false（true表示系统负责销毁，此view将不再可用）
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        return this;
    }

    public PlayerListener getPlayerListener() {
        return this.proxyListener.getOuterListener();
    }

    public PlayerListener getProxyPlayerListener() {
        return this.proxyListener;
    }

    public void setPlayerListener(PlayerListener playerListener) {
        this.proxyListener.setOuterListener(playerListener);
    }

    /**
     * create video display controllerView
     * @param container
     */
    public GiraffePlayer createDisplay(final ViewGroup container) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    doCreateDisplay(container);
                }
            });
        } else {
            doCreateDisplay(container);
        }
        return this;
    }

    private void doCreateDisplay(ViewGroup container) {
        log("doCreateDisplay");
        View old = container.findViewById(R.id.player_display_group);
        if (old != null) {
            container.removeView(old);
        }
        FrameLayout displayGroup = new FrameLayout(container.getContext());
        displayGroup.setId(R.id.player_display_group);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        );
        ScalableTextureView textureView = new ScalableTextureView(container.getContext());
        textureView.setAspectRatio(videoInfo.aspectRatio());
        textureView.setId(R.id.player_display);
        displayGroup.addView(textureView,lp);
        container.addView(displayGroup,0,lp);
        displayOn(textureView);
        displayGroupRef = new WeakReference<>(displayGroup);
        containerRef = new WeakReference<>(container);
    }

    private void log(String msg) {
        if (debug) {
            Log.d(TAG, String.format("[fingerprint:%s] %s",videoInfo.getFingerprint(),msg));
        }
    }


//    /**
//     * Defines a message and a target {@link PlayerComponent} to receive it.
//     */
//    final class PlayerMessage {
//
//        /**
//         * The target to receive the message.
//         */
//        public final PlayerComponent target;
//        /**
//         * The type of the message.
//         */
//        public final int messageType;
//        /**
//         * The message.
//         */
//        public final Object message;
//
//        /**
//         * @param target      The target of the message.
//         * @param messageType The message type.
//         * @param message     The message.
//         */
//        public PlayerMessage(PlayerComponent target, int messageType, Object message) {
//            this.target = target;
//            this.messageType = messageType;
//            this.message = message;
//        }
//
//    }
//
//
//    /**
//     * A component of an {@link Player} that can receive messages on the playback thread.
//     * <p>
//     * Messages can be delivered to a component via {@link #sendMessages} and
//     * {@link #blockingSendMessages}.
//     */
//    interface PlayerComponent {
//
//        /**
//         * Handles a message delivered to the component. Called on the playback thread.
//         *
//         * @param messageType The message type.
//         * @param message     The message.
//         */
//        void handleMessage(int messageType, Object message);
//    }
//
//
//    public GiraffePlayer sendMessages(PlayerMessage... messages) {
//        if (released) {
//            Log.w(TAG, "Ignoring messages sent after release.");
//            return null;
//        }
//        customMessagesSent++;
//        handler.obtainMessage(MSG_CUSTOM, messages).sendToTarget();
//        return this;
//    }

    private void doRelease(String fingerprint) {
        if (released) {
            return;
        }
        log("doRelease");
        PlayerManager.getInstance().removePlayer(fingerprint);
        //1. quit handler thread
        internalPlaybackThread.quit();
        //2. remove display group
        removeDisplayGroupFromParent();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        released = true;
        //3. fire proxyListener
        proxyListener().onRelease(this);
    }

    public GiraffePlayer release() {
        log("try release");
        String fingerprint = videoInfo.getFingerprint();
        PlayerManager.getInstance().removePlayer(fingerprint);
        handler.obtainMessage(MSG_CTRL_RELEASE, fingerprint).sendToTarget();
        return this;
    }

    private void removeDisplayGroupFromParent() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doRemoveDisplayGroupFromParent();
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    doRemoveDisplayGroupFromParent();
                }
            });
        }
    }

    private void doRemoveDisplayGroupFromParent() {
        if (displayGroupRef !=null) {
            ViewGroup displayGroup = displayGroupRef.get();
            if (displayGroup != null) {
                ViewParent parent = displayGroup.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(displayGroup);
                }
            }
        }
    }

    private View getCurrentDisplay() {
        ViewGroup container = displayGroupRef.get();
        if (container != null) {
            return container.findViewById(R.id.player_display);
        }
        return null;
    }

    /**
     *
     * @return
     */
    public GiraffePlayer toggleFullScreen() {
        if (displayModel == DISPLAY_NORMAL) {
            setDisplayModel(DISPLAY_FULL_WINDOW);
        } else if (displayModel == DISPLAY_FULL_WINDOW) {
            setDisplayModel(DISPLAY_NORMAL);
        }
        return this;
    }

    private GiraffePlayer setDisplayModel(int targetDisplayModel) {
        if (displayGroupRef == null) {
            return this;
        }
        ViewGroup displayGroup = displayGroupRef.get();
        if (displayGroup == null) {
            return this;
        }
        removeDisplayGroupFromParent();
        Activity activity = (Activity) displayGroup.getContext();
        ViewGroup top = (ViewGroup) activity.findViewById(android.R.id.content);

        if (targetDisplayModel == DISPLAY_FULL_WINDOW) {
            UIHelper uiHelper = UIHelper.with(getActivity());
            if (videoInfo.isPortraitWhenFullScreen()) {
                uiHelper.requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            uiHelper.showActionBar(false).fullScreen(true);
            top.addView(displayGroup);
            proxyListener().onDisplayModelChange(displayModel, DISPLAY_FULL_WINDOW);
            displayModel = DISPLAY_FULL_WINDOW;
        } else if (targetDisplayModel == DISPLAY_NORMAL) {
            UIHelper uiHelper = UIHelper.with(getActivity());
            if (videoInfo.isPortraitWhenFullScreen()) {
                uiHelper.requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            uiHelper.showActionBar(true).fullScreen(false);
            ViewGroup container = containerRef.get();
            if (container != null) {
                container.addView(displayGroup, 0);
            }
            proxyListener().onDisplayModelChange(displayModel, DISPLAY_NORMAL);
            displayModel = DISPLAY_NORMAL;
        }
        return this;
    }


    public VideoInfo getVideoInfo() {
        return videoInfo;
    }


    private Activity getActivity() {
        ViewGroup viewGroup = containerRef.get();
        if (viewGroup == null) {
            return null;
        }
        return (Activity) viewGroup.getContext();
    }

    public GiraffePlayer onConfigurationChanged(Configuration newConfig) {
        log("onConfigurationChanged");
        if (videoInfo.isPortraitWhenFullScreen()) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setDisplayModel(DISPLAY_NORMAL);
            } else {
                setDisplayModel(DISPLAY_FULL_WINDOW);
            }
        }
        return this;
    }

    public boolean onBackPressed() {
        log("onBackPressed");
        if (displayModel == DISPLAY_FULL_WINDOW) {
            setDisplayModel(DISPLAY_NORMAL);
            return true;
        }
        return false;
    }

    public void onActivityResumed() {
        log("onActivityResumed");

//        if (targetState == STATE_PLAYING) {
//            start();
//        }
    }

    public void onActivityPaused() {
        log("onActivityPaused");
        if (targetState == STATE_PLAYING || currentState == STATE_PLAYING) {
            pause();
        }
    }

    public void onActivityDestroyed() {
        log("onActivityDestroyed");
        release();
    }

    public boolean isReleased() {
        return released;
    }

    public static void play(Context context,VideoInfo videoInfo) {
        Intent intent = new Intent(context,PlayerActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra("__video_info__", videoInfo);
        PlayerManager.getInstance().releaseCurrent();
        context.startActivity(intent);
    }

    public void aspectRatio(int aspectRatio) {
        log("aspectRatio:"+aspectRatio);
        videoInfo.aspectRatio(aspectRatio);
        if (displayGroupRef != null) {
            FrameLayout group = displayGroupRef.get();
            if (group != null) {
                ScalableDisplay display = (ScalableDisplay) group.findViewById(R.id.player_display);
                if (display != null) {
                    display.setAspectRatio(aspectRatio);
                }
            }
        }
    }
}
