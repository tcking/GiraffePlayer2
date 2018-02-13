package tcking.github.com.giraffeplayer.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.VideoView;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Created by tcking on 2017
 */

public class ListExample2Activity extends ListExampleActivity {
    private VideoView videoView;
    private int[] f_xy = new int[2];
    private int[] recyclerView_xy = new int[2];


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        fileName = "sample2.json";
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init() {
        final RecyclerView recyclerView = $.id(R.id.list).view();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        final VideoAdapter videoAdapter = new VideoAdapter();
        recyclerView.setAdapter(videoAdapter);
        //auto stop & play after recyclerView scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    if (videoView != null) {
                        if (layoutManager.findFirstVisibleItemPosition() == 0) {//No visible part > 50%,play float
                            videoView.getPlayer().setDisplayModel(GiraffePlayer.DISPLAY_NORMAL);
                        } else {
                            videoView.getPlayer().setDisplayModel(GiraffePlayer.DISPLAY_FLOAT);
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }

        });

        getSampleData(new Tom() {
            @Override
            public void onNext(List<VideoAdapter.VideoItem> items) {
                videoAdapter.load(items);
                //play first video
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.getLocationInWindow(recyclerView_xy);
                        videoView = (VideoView) layoutManager.findViewByPosition(0).findViewById(R.id.video_view);
                        if (videoView != null) {
                            videoView.getPlayer().start();
                        }
                    }
                });
            }
        });
    }
}
