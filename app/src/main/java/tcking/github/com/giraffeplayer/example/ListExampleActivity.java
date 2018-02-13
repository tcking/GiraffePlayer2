package tcking.github.com.giraffeplayer.example;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.github.tcking.viewquery.ViewQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer2.BasePlayerActivity;
import tcking.github.com.giraffeplayer2.VideoView;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Created by tcking on 2017
 */

public class ListExampleActivity extends BasePlayerActivity {
    protected ViewQuery $;
    protected String  fileName = "sample.json";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        $ = new ViewQuery(this);
        init();
    }



    protected void init() {
        final RecyclerView recyclerView= $.id(R.id.list).view();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        final VideoAdapter videoAdapter = new VideoAdapter();
        recyclerView.setAdapter(videoAdapter);

        //auto stop & play after recyclerView scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    int playPosition = layoutManager.findFirstVisibleItemPosition();
                    if (playPosition == -1) {//no visible item
                        return;
                    }
                    int firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                    int lastCompletelyVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();

                    for (int i = firstCompletelyVisibleItemPosition; i <=lastCompletelyVisibleItemPosition; i++) {
                        View viewByPosition = layoutManager.findViewByPosition(i);
                        if (viewByPosition != null) {
                            VideoView videoView = (VideoView) viewByPosition.findViewById(R.id.video_view);
                            if (videoView!=null && videoView.isCurrentActivePlayer()) {
                                return;//current active player is visible,do nothing
                            }
                        }
                    }


                    //try find first visible item (visible part > 50%)
                    if (firstCompletelyVisibleItemPosition >= 0 && playPosition != firstCompletelyVisibleItemPosition) {
                        int[] recyclerView_xy = new int[2];
                        int[] f_xy = new int[2];

                        VideoView videoView = (VideoView) layoutManager.findViewByPosition(playPosition).findViewById(R.id.video_view);
                        videoView.getLocationInWindow(f_xy);
                        recyclerView.getLocationInWindow(recyclerView_xy);
                        int unVisibleY = f_xy[1] - recyclerView_xy[1];

                        if (unVisibleY < 0 && Math.abs(unVisibleY) * 1.0 / videoView.getHeight() > 0.5) {//No visible part > 50%,play next
                            playPosition = firstCompletelyVisibleItemPosition;
                        }
                    }
                    VideoView videoView = (VideoView) layoutManager.findViewByPosition(playPosition).findViewById(R.id.video_view);
                    if (videoView != null) {
                        videoView.getPlayer().start();
                    }

                }

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
                        VideoView videoView = (VideoView) layoutManager.findViewByPosition(0).findViewById(R.id.video_view);
                        if (videoView != null) {
                            videoView.getPlayer().start();
                        }
                    }
                });
            }
        });
    }

    @NonNull
    protected void getSampleData(final Tom tom) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream open = getAssets().open(fileName);
                    byte[] buf = new byte[open.available()];
                    open.read(buf);
                    JSONArray ja = new JSONArray(new String(buf, "UTF-8"));
                    final List<VideoAdapter.VideoItem> sample = new ArrayList<>(ja.length());
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jb = ja.optJSONObject(i);
                        VideoAdapter.VideoItem videoItem = new VideoAdapter.VideoItem();
                        videoItem.type = jb.optInt("type",0);
                        videoItem.name = jb.optString("name");
                        videoItem.uri = jb.optString("uri");
                        sample.add(videoItem);
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            tom.onNext(sample);
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"load data error:"+e,Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    interface Tom{
        void onNext(List<VideoAdapter.VideoItem> items);
    }
}
