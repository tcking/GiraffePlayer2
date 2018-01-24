package tcking.github.com.giraffeplayer.example;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.github.tcking.viewquery.ViewQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import tcking.github.com.giraffeplayer2.BasePlayerActivity;
import tcking.github.com.giraffeplayer2.VideoView;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Created by tcking on 2017
 */

public class ListExampleActivity extends BasePlayerActivity {
    private ViewQuery $;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        $ = new ViewQuery(this);

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
                        VideoView videoView = (VideoView) layoutManager.findViewByPosition(i).findViewById(R.id.video_view);
                        if (videoView.isCurrentActivePlayer()) {
                            return;//current active player is visible,do nothing
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

        getSampleData().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<VideoAdapter.VideoItem>>() {
            @Override
            public void call(List<VideoAdapter.VideoItem> videoItems) {
                videoAdapter.load(videoItems);

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
    private Observable<List<VideoAdapter.VideoItem>> getSampleData() {
        return Observable.create(new Observable.OnSubscribe<List<VideoAdapter.VideoItem>>() {
            @Override
            public void call(Subscriber<? super List<VideoAdapter.VideoItem>> subscriber) {
                try {
                    InputStream open = getAssets().open("sample.json");
                    byte[] buf = new byte[open.available()];
                    open.read(buf);
                    JSONArray ja = new JSONArray(new String(buf, "UTF-8"));
                    List<VideoAdapter.VideoItem> sample = new ArrayList<>(ja.length());
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jb = ja.optJSONObject(i);
                        VideoAdapter.VideoItem videoItem = new VideoAdapter.VideoItem();
                        videoItem.type = jb.optInt("type",0);
                        videoItem.name = jb.optString("name");
                        videoItem.uri = jb.optString("uri");
                        sample.add(videoItem);
                        subscriber.onNext(sample);
                        subscriber.onCompleted();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}
