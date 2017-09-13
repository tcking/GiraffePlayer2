package tcking.github.com.giraffeplayer.example;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import tcking.github.com.giraffeplayer2.VideoView;

/**
 * Created by TangChao on 2017/6/15.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoItemHolder> {
    private List<VideoItem> data = new LinkedList<>();

    @Override
    public VideoItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
            holder.videoView.setVideoPath(videoItem.uri).setFingerprint(position);
        }
    }

    @Override
    public void onViewRecycled(VideoItemHolder holder) {
        super.onViewRecycled(holder);
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
        }
    }

    static class VideoItem {
        public static final int TYPE_EMBED = 0;
        int type;
        String name;
        String uri;
    }
}
