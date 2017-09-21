package tcking.github.com.giraffeplayer.example;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import tcking.github.com.giraffeplayer2.DefaultPlayerListener;
import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoView;

/**
 * Created by TangChao on 2017/6/15.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoItemHolder> {
    private List<VideoItem> data = new LinkedList<>();
    private Context context;
    private PlayerListener playerListener=new DefaultPlayerListener(){//example of using playerListener
        @Override
        public void onPreparing(GiraffePlayer giraffePlayer) {
            Toast.makeText(context, "start playing:"+giraffePlayer.getVideoInfo().getUri(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCompletion(GiraffePlayer giraffePlayer) {
            Toast.makeText(context, "play completion:"+giraffePlayer.getVideoInfo().getUri(),Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public VideoItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context=parent.getContext();
        if (viewType == VideoItem.TYPE_EMBED) {
            return new VideoItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_embed,parent,false));
        }else {
            throw new RuntimeException("unknown type:" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(VideoItemHolder holder, int position) {
        VideoItem videoItem = data.get(position);
        if (videoItem.type == VideoItem.TYPE_EMBED) {
            holder.name.setText(videoItem.name);
            holder.url.setText(videoItem.uri);
            holder.videoView.getCoverView().setImageResource(R.drawable.cover1);
            holder.videoView.getVideoInfo().setPortraitWhenFullScreen(false);
            holder.videoView.setVideoPath(videoItem.uri).setFingerprint(position);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

    public void load(List<VideoItem> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    class VideoItemHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView url;
        VideoView videoView;
        public VideoItemHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_name);
            url = (TextView) itemView.findViewById(R.id.tv_url);
            videoView = (VideoView) itemView.findViewById(R.id.video_view);
            videoView.setPlayerListener(playerListener);
        }
    }

    static class VideoItem {
        public static final int TYPE_EMBED = 0;
        int type;
        String name;
        String uri;
    }
}
