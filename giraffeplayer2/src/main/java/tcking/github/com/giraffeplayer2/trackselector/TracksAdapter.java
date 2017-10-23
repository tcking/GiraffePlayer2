package tcking.github.com.giraffeplayer2.trackselector;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.github.tcking.giraffeplayer2.R;
import com.github.tcking.viewquery.ViewQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerManager;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * Created by TangChao on 2017/10/11.
 */

public class TracksAdapter extends BaseExpandableListAdapter {
    private String fingerprint;
    private Map<Integer, TrackGroup> dataIndex = new HashMap<>();
    private List<TrackGroup> data = new ArrayList<>();

    @Override

    public int getGroupCount() {
        return data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return data.get(groupPosition).getTracks().size();
    }

    @Override
    public TrackGroup getGroup(int groupPosition) {
        return data.get(groupPosition);
    }

    @Override
    public TrackInfoWrapper getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).getTracks().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChild(groupPosition, childPosition).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TrackGroup group = getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.giraffe_track_selector_group, parent, false);
        }
        ViewQuery $ = new ViewQuery(convertView);
        $.id(R.id.app_video_track_group).text(group.getTrackTypeName());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TrackGroup group = getGroup(groupPosition);
        TrackInfoWrapper child = getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.giraffe_track_selector_child, parent, false);
            convertView.findViewById(R.id.app_video_track_group_child).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TrackInfoWrapper track = (TrackInfoWrapper) v.getTag();
                    TrackGroup trackGroup = dataIndex.get(track.getTrackType());
                    if (trackGroup.getSelectedTrackIndex() != track.getIndex()) {
                        trackGroup.setSelectedTrackIndex(track.getIndex());
                        notifyDataSetChanged();

                        GiraffePlayer player = PlayerManager.getInstance().getPlayerByFingerprint(track.getFingerprint());
                        if (player != null) {
                            if (track.getIndex() >= 0) {
                                player.selectTrack(track.getIndex());
                            } else {
                                player.deselectTrack(player.getSelectedTrack(track.getTrackType()));
                            }
                        }
                    }
                }
            });
        }
        ViewQuery $ = new ViewQuery(convertView);
        $.id(R.id.app_video_track_group_child).text(child.getInfo()).checked(group.getSelectedTrackIndex() == child.getIndex()).view().setTag(child);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void load(String fingerprint) {
        if (TextUtils.isEmpty(fingerprint)) {
            return;
        }
        GiraffePlayer player = PlayerManager.getInstance().getPlayerByFingerprint(fingerprint);
        if (player == null) {
            return;
        }
        dataIndex.clear();
        data.clear();

        this.fingerprint = fingerprint;
        ITrackInfo[] tracks = player.getTrackInfo();
        for (int i = 0; i < tracks.length; i++) {
            ITrackInfo track = tracks[i];
            int trackType = track.getTrackType();
            if (trackType == ITrackInfo.MEDIA_TRACK_TYPE_AUDIO ||
                    trackType == ITrackInfo.MEDIA_TRACK_TYPE_VIDEO ||
                    trackType == ITrackInfo.MEDIA_TRACK_TYPE_SUBTITLE ||
                    trackType == ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
                TrackGroup trackGroup = dataIndex.get(trackType);
                if (trackGroup == null) {
                    int selectedTrack = player.getSelectedTrack(trackType);
                    trackGroup = new TrackGroup(trackType, selectedTrack);
//                    if (trackType == ITrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
//                        trackGroup.getTracks().add(TrackInfoWrapper.OFF(fingerprint,trackType));
//                    }
                    dataIndex.put(trackType, trackGroup);
                    data.add(trackGroup);
                }
                TrackInfoWrapper e = new TrackInfoWrapper(fingerprint, track, i, trackType);
                trackGroup.getTracks().add(e);
            }
        }
        notifyDataSetChanged();
    }
}
