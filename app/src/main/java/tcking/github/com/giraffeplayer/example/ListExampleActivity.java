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

/**
 * Created by TangChao on 2017/9/12.
 */

public class ListExampleActivity extends BasePlayerActivity {
    private ViewQuery $;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        $ = new ViewQuery(this);

        RecyclerView recyclerView= $.id(R.id.list).view();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final VideoAdapter videoAdapter = new VideoAdapter();
        recyclerView.setAdapter(videoAdapter);

        getSampleData().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<VideoAdapter.VideoItem>>() {
            @Override
            public void call(List<VideoAdapter.VideoItem> videoItems) {
                videoAdapter.load(videoItems);
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
