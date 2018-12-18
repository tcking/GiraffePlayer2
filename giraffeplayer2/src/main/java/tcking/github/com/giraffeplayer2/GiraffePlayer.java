package tcking.github.com.giraffeplayer2;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;


/**
 * Created by tcking on 2017
 */

public class GiraffePlayer implements MediaController.MediaPlayerControl {
    public static final String TAG = "GiraffePlayer";
    public static final String ACTION = "tcking.github.com.giraffeplayer2.action";
    public static boolean debug = false;
    public static boolean nativeDebug = false;
    // Internal messages
    private static final int MSG_CTRL_PLAYING = 1;

    private static final int MSG_CTRL_PAUSE = 2;
    private static final int MSG_CTRL_SEEK = 3;
    private static final int MSG_CTRL_RELEASE = 4;
    private static final int MSG_CTRL_RETRY = 5;
    private static final int MSG_CTRL_SELECT_TRACK = 6;
    private static final int MSG_CTRL_DESELECT_TRACK = 7;
    private static final int MSG_CTRL_SET_VOLUME = 8;


    private static final int MSG_SET_DISPLAY = 12;


    // all possible internal states
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;
    public static final int STATE_RELEASE = 6;
    public static final int STATE_LAZYLOADING = 7;
    private final HandlerThread internalPlaybackThread;
    private final IntentFilter intentFilter = new IntentFilter(ACTION);
    ;

    private int currentBufferPercentage = 0;
    private boolean canPause = true;
    private boolean canSeekBackward = true;
    private boolean canSeekForward = true;
    private int audioSessionId;
    private int seekWhenPrepared;

    private int currentState = STATE_IDLE;
    private int targetState = STATE_IDLE;
    private Uri uri;
    private Map<String, String> headers = new HashMap<>();
    private Context context;

    private IMediaPlayer mediaPlayer;
    private volatile boolean released;
    private Handler handler;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private ProxyPlayerListener proxyListener;

    public static final int DISPLAY_NORMAL = 0;
    public static final int DISPLAY_FULL_WINDOW = 1;
    public static final int DISPLAY_FLOAT = 2;
    private volatile int startPosition = -1;
    private boolean mute = false;
    private WeakReference<? extends ViewGroup> displayBoxRef;
    private int ignoreOrientation = -100;

    public int getDisplayModel() {
        return displayModel;
    }

    private int displayModel = DISPLAY_NORMAL;
    private int lastDisplayModel = displayModel;
    private VideoInfo videoInfo;
    private WeakReference<? extends ViewGroup> boxContainerRef;


    private ProxyPlayerListener proxyListener() {
        return proxyListener;
    }


    private GiraffePlayer(final Context context, final VideoInfo videoInfo) {
        this.context = context.getApplicationContext();
        this.videoInfo = videoInfo;
        log("new GiraffePlayer");
        VideoView videoView = PlayerManager.getInstance().getVideoView(videoInfo);
        boxContainerRef = new WeakReference<>(videoView != null ? videoView.getContainer() : null);
        if (boxContainerRef.get() != null) {
            boxContainerRef.get().setBackgroundColor(videoInfo.getBgColor());
        }
        this.proxyListener = new ProxyPlayerListener(videoInfo);
        internalPlaybackThread = new HandlerThread("GiraffePlayerInternal:Handler", Process.THREAD_PRIORITY_AUDIO);
        internalPlaybackThread.start();
        handler = new Handler(internalPlaybackThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //init mediaPlayer before any actions
                log("handleMessage:" + msg.what);
                if (msg.what == MSG_CTRL_RELEASE) {
                    if (!released) {
                        handler.removeCallbacks(null);
                        currentState(STATE_RELEASE);
                        doRelease(((String) msg.obj));
                    }
                    return true;
                }
                if (mediaPlayer == null || released) {
                    handler.removeCallbacks(null);
                    try {
                        init(true);
                        handler.sendMessage(Message.obtain(msg));
                    } catch (UnsatisfiedLinkError e) {
                        log("UnsatisfiedLinkError:" + e);
                        currentState(STATE_LAZYLOADING);
                        LazyLoadManager.Load(context, videoInfo.getFingerprint(), Message.obtain(msg));
                    }
                    return true;
                }
                switch (msg.what) {
                    case MSG_CTRL_PLAYING:
                        if (currentState == STATE_ERROR) {
                            handler.sendEmptyMessage(MSG_CTRL_RETRY);
                        } else if (isInPlaybackState()) {
                            if (canSeekForward) {
                                if (currentState == STATE_PLAYBACK_COMPLETED) {
                                    startPosition = 0;
                                }
                                if (startPosition >= 0) {
                                    mediaPlayer.seekTo(startPosition);
                                    startPosition = -1;
                                }
                            }
                            mediaPlayer.start();
                            currentState(STATE_PLAYING);
                        }
                        break;
                    case MSG_CTRL_PAUSE:
                        mediaPlayer.pause();
                        currentState(STATE_PAUSED);
                        break;
                    case MSG_CTRL_SEEK:
                        if (!canSeekForward) {
                            break;
                        }
                        int position = (int) msg.obj;
                        mediaPlayer.seekTo(position);
                        break;
                    case MSG_CTRL_SELECT_TRACK:
                        int track = (int) msg.obj;
                        if (mediaPlayer instanceof IjkMediaPlayer) {
                            ((IjkMediaPlayer) mediaPlayer).selectTrack(track);
                        } else if (mediaPlayer instanceof AndroidMediaPlayer) {
                            ((AndroidMediaPlayer) mediaPlayer).getInternalMediaPlayer().selectTrack(track);
                        }
                        break;
                    case MSG_CTRL_DESELECT_TRACK:
                        int deselectTrack = (int) msg.obj;
                        if (mediaPlayer instanceof IjkMediaPlayer) {
                            ((IjkMediaPlayer) mediaPlayer).deselectTrack(deselectTrack);
                        } else if (mediaPlayer instanceof AndroidMediaPlayer) {
                            ((AndroidMediaPlayer) mediaPlayer).getInternalMediaPlayer().deselectTrack(deselectTrack);
                        }
                        break;
                    case MSG_SET_DISPLAY:
                        if (msg.obj == null) {
                            mediaPlayer.setDisplay(null);
                        } else if (msg.obj instanceof SurfaceTexture) {
                            mediaPlayer.setSurface(new Surface((SurfaceTexture) msg.obj));
                        } else if (msg.obj instanceof SurfaceView) {
                            mediaPlayer.setDisplay(((SurfaceView) msg.obj).getHolder());
                        }
                        break;
                    case MSG_CTRL_RETRY:
                        init(false);
                        handler.sendEmptyMessage(MSG_CTRL_PLAYING);
                        break;
                    case MSG_CTRL_SET_VOLUME:
                        Map<String, Float> pram = (Map<String, Float>) msg.obj;
                        mediaPlayer.setVolume(pram.get("left"), pram.get("right"));
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
        if (currentState == STATE_PLAYBACK_COMPLETED && !canSeekForward) {
            releaseMediaPlayer();
        }
        targetState(STATE_PLAYING);
        handler.sendEmptyMessage(MSG_CTRL_PLAYING);
        proxyListener().onStart(this);
    }

    private void targetState(final int newState) {
        final int oldTargetState = targetState;
        targetState = newState;
        if (oldTargetState != newState) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    proxyListener().onTargetStateChange(oldTargetState, newState);
                }
            });
        }
    }

    private void currentState(final int newState) {
        final int oldCurrentState = currentState;
        currentState = newState;
        if (oldCurrentState != newState) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    proxyListener().onCurrentStateChange(oldCurrentState, newState);

                }
            });
        }
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
        this.headers.clear();
        this.headers.putAll(headers);
        seekWhenPrepared = 0;
        return this;
    }

    private void init(boolean createDisplay) {
        log("init createDisplay:" + createDisplay);
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                proxyListener().onPreparing(GiraffePlayer.this);
            }
        });
        releaseMediaPlayer();
        mediaPlayer = createMediaPlayer();
        if (mediaPlayer instanceof IjkMediaPlayer) {
            IjkMediaPlayer.native_setLogLevel(nativeDebug ? IjkMediaPlayer.IJK_LOG_DEBUG : IjkMediaPlayer.IJK_LOG_ERROR);
        }
        setOptions();
        released = false;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(videoInfo.isLooping());
        mediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                boolean live = mediaPlayer.getDuration() == 0;
                canSeekBackward = !live;
                canSeekForward = !live;
                currentState(STATE_PREPARED);
                proxyListener().onPrepared(GiraffePlayer.this);
                if (targetState == STATE_PLAYING) {
                    handler.sendEmptyMessage(MSG_CTRL_PLAYING);
                }
            }
        });
        initInternalListener();
        if (createDisplay) {
            VideoView videoView = PlayerManager.getInstance().getVideoView(videoInfo);
            if (videoView != null && videoView.getContainer() != null) {
                createDisplay(videoView.getContainer());
            }
        }
        try {
            uri = videoInfo.getUri();
            mediaPlayer.setDataSource(context, uri, headers);
            currentState(STATE_PREPARING);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            currentState(STATE_ERROR);
            e.printStackTrace();
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    proxyListener().onError(GiraffePlayer.this, 0, 0);
                }
            });
        }

    }

    private IMediaPlayer createMediaPlayer() {
        if (VideoInfo.PLAYER_IMPL_SYSTEM.equals(videoInfo.getPlayerImpl())) {
            return new AndroidMediaPlayer();
        }
        return new IjkMediaPlayer(Looper.getMainLooper());
    }

    private void setOptions() {
        headers.clear();
        if (videoInfo.getOptions().size() <= 0) {
            return;
        }
        //https://ffmpeg.org/ffmpeg-protocols.html#http
        if (mediaPlayer instanceof IjkMediaPlayer) {
            IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) mediaPlayer;
            for (Option option : videoInfo.getOptions()) {
                if (option.getValue() instanceof String) {
                    ijkMediaPlayer.setOption(option.getCategory(), option.getName(), ((String) option.getValue()));
                } else if (option.getValue() instanceof Long) {
                    ijkMediaPlayer.setOption(option.getCategory(), option.getName(), ((Long) option.getValue()));
                }
            }
        } else if (mediaPlayer instanceof AndroidMediaPlayer) {
            for (Option option : videoInfo.getOptions()) {
                if (IjkMediaPlayer.OPT_CATEGORY_FORMAT == option.getCategory() && "headers".equals(option.getName())) {
                    String h = "" + option.getValue();
                    String[] hs = h.split("\r\n");
                    for (String hd : hs) {
                        String[] kv = hd.split(":");
                        String v = kv.length >= 2 ? kv[1] : "";
                        headers.put(kv[0], v);
                        log("add header " + kv[0] + ":" + v);
                    }
                    break;
                }
            }
        }
    }

    private void initInternalListener() {
        //proxyListener fire on main thread
        mediaPlayer.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
                currentBufferPercentage = percent;
                proxyListener().onBufferingUpdate(GiraffePlayer.this, percent);
            }
        });
        mediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            //https://developer.android.com/reference/android/media/MediaPlayer.OnInfoListener.html
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
                if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                    ScalableTextureView currentDisplay = getCurrentDisplay();
                    if (currentDisplay != null) {
                        currentDisplay.setRotation(extra);
                    }
                }
                return proxyListener().onInfo(GiraffePlayer.this, what, extra);
            }
        });
        mediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                currentState(STATE_PLAYBACK_COMPLETED);
                proxyListener().onCompletion(GiraffePlayer.this);
            }
        });
        mediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
                currentState(STATE_ERROR);
                boolean b = proxyListener().onError(GiraffePlayer.this, what, extra);
                int retryInterval = videoInfo.getRetryInterval();
                if (retryInterval > 0) {
                    log("replay delay " + retryInterval + " seconds");
                    handler.sendEmptyMessageDelayed(MSG_CTRL_RETRY, retryInterval * 1000);
                }
                return b;

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
                    log("onVideoSizeChanged:width:" + width + ",height:" + height);
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
        mediaPlayer.setOnTimedTextListener(new IMediaPlayer.OnTimedTextListener() {
            @Override
            public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
                proxyListener().onTimedText(GiraffePlayer.this, text);
            }
        });
    }

    public static GiraffePlayer createPlayer(Context context, VideoInfo videoInfo) {
        return new GiraffePlayer(context, videoInfo);
    }

    private GiraffePlayer bindDisplay(final TextureView textureView) {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            private SurfaceTexture surface;

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                log("onSurfaceTextureAvailable");
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
                log("onSurfaceTextureDestroyed");
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
     *
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
        isolateDisplayBox();
        FrameLayout displayBox = new FrameLayout(container.getContext());
        displayBox.setId(R.id.player_display_box);
        displayBox.setBackgroundColor(videoInfo.getBgColor());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        );
        ScalableTextureView textureView = new ScalableTextureView(container.getContext());
        textureView.setAspectRatio(videoInfo.getAspectRatio());
        textureView.setId(R.id.player_display);
        displayBox.addView(textureView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ));
        container.addView(displayBox, 0, lp);
        bindDisplay(textureView);
        displayBoxRef = new WeakReference<>(displayBox);
    }

    /**
     * isolate display box from parent
     *
     * @return
     */
    private GiraffePlayer isolateDisplayBoxContainer() {
        if (boxContainerRef != null) {
            ViewGroup box = boxContainerRef.get();
            removeFromParent(box);
        }
        return this;
    }

    /**
     * isolate display box from parent
     *
     * @return
     */
    private GiraffePlayer isolateDisplayBox() {
        if (displayBoxRef != null) {
            ViewGroup box = displayBoxRef.get();
            removeFromParent(box);
        }
        return this;
    }

    private void log(String msg) {
        if (debug) {
            Log.d(TAG, String.format("[fingerprint:%s] %s", videoInfo.getFingerprint(), msg));
        }
    }


    private void doRelease(String fingerprint) {
        if (released) {
            return;
        }
        log("doRelease");
        PlayerManager.getInstance().removePlayer(fingerprint);
        //1. quit handler thread
        internalPlaybackThread.quit();
        //2. remove display group
        releaseDisplayBox();
        releaseMediaPlayer();
        released = true;
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            log("releaseMediaPlayer");
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void release() {
        log("try release");
        String fingerprint = videoInfo.getFingerprint();
        PlayerManager.getInstance().removePlayer(fingerprint);
        proxyListener().onRelease(this);
        handler.obtainMessage(MSG_CTRL_RELEASE, fingerprint).sendToTarget();
    }

    private void releaseDisplayBox() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doReleaseDisplayBox();
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    doReleaseDisplayBox();
                }
            });
        }
    }

    private void doReleaseDisplayBox() {
        log("doReleaseDisplayBox");
        ScalableTextureView currentDisplay = getCurrentDisplay();
        if (currentDisplay != null) {
            currentDisplay.setSurfaceTextureListener(null);
        }
        isolateDisplayBox();
    }

    public ScalableTextureView getCurrentDisplay() {
        if (displayBoxRef != null) {
            ViewGroup box = displayBoxRef.get();
            if (box != null) {
                return (ScalableTextureView) box.findViewById(R.id.player_display);
            }
        }
        return null;
    }

    /**
     * @return
     */
    public GiraffePlayer toggleFullScreen() {
        if (displayModel == DISPLAY_NORMAL) {
            setDisplayModel(DISPLAY_FULL_WINDOW);
        } else {
            setDisplayModel(DISPLAY_NORMAL);
        }
        return this;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public GiraffePlayer setDisplayModel(int targetDisplayModel) {
        if (targetDisplayModel == displayModel) {
            return this;
        }

        //if no display box container,nothing can do
        if (boxContainerRef == null || boxContainerRef.get() == null) {
            return this;
        }
        lastDisplayModel = displayModel;

        if (targetDisplayModel == DISPLAY_FULL_WINDOW) {
            Activity activity = getActivity();
            if (activity == null) {
                return this;
            }

            //orientation & action bar
            UIHelper uiHelper = UIHelper.with(activity);
            if (videoInfo.isPortraitWhenFullScreen()) {
                uiHelper.requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ignoreOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
            uiHelper.showActionBar(false).fullScreen(true);
            ViewGroup activityBox = (ViewGroup) activity.findViewById(android.R.id.content);

            animateIntoContainerAndThen(activityBox, new VideoViewAnimationListener() {

                @Override
                public void onStart(ViewGroup src, ViewGroup target) {
                    removeFloatContainer();
                }

                @Override
                public void onEnd(ViewGroup src, ViewGroup target) {
                    proxyListener().onDisplayModelChange(displayModel, DISPLAY_FULL_WINDOW);
                    displayModel = DISPLAY_FULL_WINDOW;
                }

            });


        } else if (targetDisplayModel == DISPLAY_NORMAL) {
            final Activity activity = getActivity();
            if (activity == null) {
                return this;
            }
            final VideoView videoView = PlayerManager.getInstance().getVideoView(videoInfo);
            if (videoView == null) {
                return this;
            }
            //change orientation & action bar
            UIHelper uiHelper = UIHelper.with(activity);
            if (videoInfo.isPortraitWhenFullScreen()) {
                uiHelper.requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ignoreOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
            uiHelper.showActionBar(true).fullScreen(false);


            animateIntoContainerAndThen(videoView, new VideoViewAnimationListener() {

                @Override
                public void onStart(ViewGroup src, ViewGroup target) {
                    removeFloatContainer();
                }

                @Override
                public void onEnd(ViewGroup src, ViewGroup target) {
                    proxyListener().onDisplayModelChange(displayModel, DISPLAY_NORMAL);
                    displayModel = DISPLAY_NORMAL;
                }

            });
        } else if (targetDisplayModel == DISPLAY_FLOAT) {
            Activity activity = getActivity();
            if (activity == null) {
                return this;
            }

            //change orientation & action bar
            UIHelper uiHelper = UIHelper.with(activity);
            if (videoInfo.isPortraitWhenFullScreen()) {
                uiHelper.requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ignoreOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
            uiHelper.showActionBar(true).fullScreen(false);

            final ViewGroup floatBox = createFloatBox();
            floatBox.setVisibility(View.INVISIBLE);
            animateIntoContainerAndThen(floatBox, new VideoViewAnimationListener() {
                @Override
                void onEnd(ViewGroup src, ViewGroup target) {
                    floatBox.setVisibility(View.VISIBLE);
                    proxyListener().onDisplayModelChange(displayModel, DISPLAY_FLOAT);
                    displayModel = DISPLAY_FLOAT;
                }
            });

        }
        return this;
    }

    GiraffePlayer doMessage(Message message) {
        handler.sendMessage(message);
        return this;
    }


    void lazyLoadProgress(final int progress) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                proxyListener().onLazyLoadProgress(GiraffePlayer.this, progress);
            }
        });
    }

    public void lazyLoadError(final String message) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                proxyListener().onLazyLoadError(GiraffePlayer.this, message);
            }
        });
    }

    class VideoViewAnimationListener {
        void onStart(ViewGroup src, ViewGroup target) {
        }

        void onEnd(ViewGroup src, ViewGroup target) {
        }
    }

    @SuppressLint("NewApi")
    private void animateIntoContainerAndThen(final ViewGroup container, final VideoViewAnimationListener listener) {
        final ViewGroup displayBoxContainer = boxContainerRef.get();

        boolean usingAnim = usingAnim();

        if (!usingAnim) {//no animation
            listener.onStart(displayBoxContainer, container);
            if (displayBoxContainer.getParent() != container) {
                isolateDisplayBoxContainer();
                container.addView(displayBoxContainer, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            }
            listener.onEnd(displayBoxContainer, container);
            return;
        }

        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        //这里用post确保在调用此函数之前的ui操作都已经ok
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup activityBox = (ViewGroup) activity.findViewById(android.R.id.content);


                int[] targetXY = new int[]{0, 0};
                int[] activityBoxXY = new int[]{0, 0};

                //set src LayoutParams
                activityBox.getLocationInWindow(activityBoxXY);


                if (displayBoxContainer.getParent() != activityBox) {
                    int[] srcXY = new int[]{0, 0};
                    FrameLayout.LayoutParams srcLayoutParams = new FrameLayout.LayoutParams(displayBoxContainer.getWidth(), displayBoxContainer.getHeight());
                    displayBoxContainer.getLocationInWindow(srcXY);
                    srcLayoutParams.leftMargin = srcXY[0] - activityBoxXY[0];
                    srcLayoutParams.topMargin = srcXY[1] - activityBoxXY[1];
                    isolateDisplayBoxContainer();
                    activityBox.addView(displayBoxContainer, srcLayoutParams);
                }

                //2.set target LayoutParams
                final FrameLayout.LayoutParams targetLayoutParams = new FrameLayout.LayoutParams(container.getLayoutParams());
                container.getLocationInWindow(targetXY);
                targetLayoutParams.leftMargin = targetXY[0] - activityBoxXY[0];
                targetLayoutParams.topMargin = targetXY[1] - activityBoxXY[1];


                final Transition transition = new ChangeBounds();
                transition.setStartDelay(200);
                transition.addListener(new Transition.TransitionListener() {

                    private void afterTransition() {
                        //fire listener
                        if (displayBoxContainer.getParent() != container) {
                            isolateDisplayBoxContainer();
                            container.addView(displayBoxContainer, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                        }
                        listener.onEnd(displayBoxContainer, container);
                    }

                    @Override
                    public void onTransitionStart(Transition transition) {

                    }


                    @Override
                    public void onTransitionEnd(Transition transition) {
                        afterTransition();
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {
                        afterTransition();
                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });

//                    must put the action to queue so the beginDelayedTransition can take effect
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStart(displayBoxContainer, container);
                        TransitionManager.beginDelayedTransition(displayBoxContainer, transition);
                        displayBoxContainer.setLayoutParams(targetLayoutParams);
                    }
                });
            }
        });


    }


    private ViewGroup createFloatBox() {
        removeFloatContainer();
        Activity topActivity = PlayerManager.getInstance().getTopActivity();
        ViewGroup topActivityBox = (ViewGroup) topActivity.findViewById(android.R.id.content);
        ViewGroup floatBox = (ViewGroup) LayoutInflater.from(topActivity.getApplication()).inflate(R.layout.giraffe_float_box, null);
        floatBox.setBackgroundColor(videoInfo.getBgColor());

        FrameLayout.LayoutParams floatBoxParams = new FrameLayout.LayoutParams(VideoInfo.floatView_width, VideoInfo.floatView_height);
        if (VideoInfo.floatView_x == Integer.MAX_VALUE || VideoInfo.floatView_y == Integer.MAX_VALUE) {
            floatBoxParams.gravity = Gravity.BOTTOM | Gravity.END;
            floatBoxParams.bottomMargin = 20;
            floatBoxParams.rightMargin = 20;
        } else {
            floatBoxParams.gravity = Gravity.TOP | Gravity.START;
            floatBoxParams.leftMargin = (int) VideoInfo.floatView_x;
            floatBoxParams.topMargin = (int) VideoInfo.floatView_y;
        }
        topActivityBox.addView(floatBox, floatBoxParams);

        floatBox.setOnTouchListener(new View.OnTouchListener() {
            float ry;
            float oy;

            float rx;
            float ox;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 获取相对屏幕的坐标，即以屏幕左上角为原点
//                System.out.println("MotionEvent:action:"+event.getAction()+",raw:["+event.getRawX()+","+event.getRawY()+"],xy["+event.getX()+","+event.getY()+"]");

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ry = event.getRawY();
                        oy = v.getTranslationY();

                        rx = event.getRawX();
                        ox = v.getTranslationX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float y = oy + event.getRawY() - ry;
                        if (y > 0) {
//                            y = 0;
                        }
                        v.setTranslationY(y);

                        float x = ox + event.getRawX() - rx;
                        if (x > 0) {
//                            x = 0;
                        }
                        v.setTranslationX(x);
                        break;
                }
                return true;
            }
        });
        return floatBox;
    }

    private void removeFloatContainer() {
        Activity activity = getActivity();
        if (activity != null) {
            View floatBox = activity.findViewById(R.id.player_display_float_box);
            if (floatBox != null) {
                VideoInfo.floatView_x = floatBox.getX();
                VideoInfo.floatView_y = floatBox.getY();
            }
            removeFromParent(floatBox);
        }
    }

    private void removeFromParent(View view) {
        if (view != null) {
            ViewParent parent = view.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(view);
            }
        }
    }


    private boolean usingAnim() {
        return videoInfo.isFullScreenAnimation() && !videoInfo.isPortraitWhenFullScreen() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }


    public VideoInfo getVideoInfo() {
        return videoInfo;
    }


    private Activity getActivity() {
        VideoView videoView = PlayerManager.getInstance().getVideoView(videoInfo);
        if (videoView != null) {
            return (Activity) videoView.getContext();
        }
        return null;
    }

    public GiraffePlayer onConfigurationChanged(Configuration newConfig) {
        log("onConfigurationChanged");
        if (ignoreOrientation == newConfig.orientation) {
            log("onConfigurationChanged ignore");
            return this;
        }
        if (videoInfo.isPortraitWhenFullScreen()) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setDisplayModel(lastDisplayModel);
            } else {
                setDisplayModel(DISPLAY_FULL_WINDOW);
            }
        }
        return this;
    }

    public boolean onBackPressed() {
        log("onBackPressed");
        if (videoInfo.isFullScreenOnly()) {
            return false;
        }
        if (displayModel == DISPLAY_FULL_WINDOW) {
            setDisplayModel(lastDisplayModel);
            return true;
        }
        return false;
    }

    public void onActivityResumed() {
        log("onActivityResumed");
        if (targetState == STATE_PLAYING) {
            start();
        } else if (targetState == STATE_PAUSED) {
            if (canSeekForward && startPosition >= 0) {
                seekTo(startPosition);
            }
        }

//        if (targetState == STATE_PLAYING) {
//            start();
//        }
    }

    public void onActivityPaused() {
        log("onActivityPaused");
        if (mediaPlayer == null) {
            return;
        }
        if (targetState == STATE_PLAYING
                || currentState == STATE_PLAYING
                || targetState == STATE_PAUSED
                || currentState == STATE_PAUSED) {

            startPosition = (int) mediaPlayer.getCurrentPosition();
            releaseMediaPlayer();
        }
    }

    public void onActivityDestroyed() {
        log("onActivityDestroyed");
        release();
    }

    public void stop() {
        release();
    }

    public boolean isReleased() {
        return released;
    }

    public static void play(Context context, VideoInfo videoInfo) {
        Intent intent = new Intent(context, PlayerActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra("__video_info__", videoInfo);
        PlayerManager.getInstance().releaseCurrent();
        context.startActivity(intent);
    }

    public void aspectRatio(int aspectRatio) {
        log("aspectRatio:" + aspectRatio);
        videoInfo.setAspectRatio(aspectRatio);
        ScalableDisplay display = getCurrentDisplay();
        if (display != null) {
            display.setAspectRatio(aspectRatio);
        }
    }

    public ITrackInfo[] getTrackInfo() {
        if (mediaPlayer == null || released) {
            return new ITrackInfo[0];
        }
        return mediaPlayer.getTrackInfo();
    }

    public int getSelectedTrack(int trackType) {
        if (mediaPlayer == null || released) {
            return -1;
        }
        if (mediaPlayer instanceof IjkMediaPlayer) {
            return ((IjkMediaPlayer) mediaPlayer).getSelectedTrack(trackType);
        } else if (mediaPlayer instanceof AndroidMediaPlayer) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return ((AndroidMediaPlayer) mediaPlayer).getInternalMediaPlayer().getSelectedTrack(trackType);
            }
        }
        return -1;
    }

    public GiraffePlayer selectTrack(int track) {
        if (mediaPlayer == null || released) {
            return this;
        }
        handler.removeMessages(MSG_CTRL_SELECT_TRACK);
        handler.obtainMessage(MSG_CTRL_SELECT_TRACK, track).sendToTarget();
        return this;
    }

    public GiraffePlayer deselectTrack(int selectedTrack) {
        if (mediaPlayer == null || released) {
            return this;
        }
        handler.removeMessages(MSG_CTRL_DESELECT_TRACK);
        handler.obtainMessage(MSG_CTRL_DESELECT_TRACK, selectedTrack).sendToTarget();
        return this;
    }

    /**
     * get current player state
     *
     * @return state
     */
    public int getCurrentState() {
        return currentState;
    }


    /**
     * set volume
     *
     * @param left  [0,1]
     * @param right [0,1]
     * @return GiraffePlayer
     */
    public GiraffePlayer setVolume(float left, float right) {
        if (mediaPlayer == null || released) {
            return this;
        }
        HashMap<String, Float> pram = new HashMap<>();
        pram.put("left", left);
        pram.put("right", right);
        handler.removeMessages(MSG_CTRL_SET_VOLUME);
        handler.obtainMessage(MSG_CTRL_SET_VOLUME, pram).sendToTarget();
        return this;
    }

    /**
     * set mute
     *
     * @param mute
     * @return GiraffePlayer
     */
    public GiraffePlayer setMute(boolean mute) {
        this.mute = mute;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
        return this;
    }

    /**
     * is mute
     *
     * @return true if mute
     */
    public boolean isMute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            return audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        } else {
            return mute;
        }
    }

    /**
     * set looping play
     *
     * @param looping
     * @return
     */
    public GiraffePlayer setLooping(boolean looping) {
        if (mediaPlayer != null && !released) {
            mediaPlayer.setLooping(looping);
        }
        return this;
    }

    /**
     * @return is looping play
     */
    public boolean isLooping() {
        if (mediaPlayer != null && !released) {
            return mediaPlayer.isLooping();
        }
        return false;
    }

}
