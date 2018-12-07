package tcking.github.com.giraffeplayer.example;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import tcking.github.com.giraffeplayer2.DefaultMediaController;
import tcking.github.com.giraffeplayer2.GiraffePlayer;

public class SimpleMediaController extends DefaultMediaController {


    public SimpleMediaController(Context context) {
        super(context);
    }

    @Override
    protected View makeControllerView() {
        return LayoutInflater.from(context).inflate(R.layout.simple_media_controller, videoView, false);
    }

    @Override
    protected void initView(View view) {
        final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                GiraffePlayer player = videoView.getPlayer();
                if (player != null) {
                    if (player.isPlaying()) {
                        player.pause();
                    } else {
                        player.start();
                    }
                }
                return true;
            }


            @Override
            public boolean onDoubleTap(MotionEvent e) {
                GiraffePlayer player = videoView.getPlayer();
                if (player != null) {
                    player.toggleFullScreen();
                }
                return true;
            }
        });

        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

    }

    @Override
    protected void statusChange(int status) {
        super.statusChange(status);
        Log.d("test","test"+status);
        switch (status) {
            case STATUS_LOADING:
            case STATUS_PLAYING:
            case STATUS_ERROR:
                $.id(R.id.app_video_pause).gone();
                break;
            case STATUS_PAUSE:
            case STATUS_COMPLETED:
                $.id(R.id.app_video_pause).visible();

            default:
        }

    }
}
