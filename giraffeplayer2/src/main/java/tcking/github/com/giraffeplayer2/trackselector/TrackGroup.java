package tcking.github.com.giraffeplayer2.trackselector;

import com.github.tcking.giraffeplayer2.R;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * Created by TangChao on 2017/10/11.
 */

public class TrackGroup {
    private int trackType;

    public int getSelectedTrackIndex() {
        return selectedTrackIndex;
    }

    public void setSelectedTrackIndex(int selectedTrackIndex) {
        this.selectedTrackIndex = selectedTrackIndex;
    }

    private int selectedTrackIndex=-1;

    public int getTrackType() {
        return trackType;
    }


    public TrackGroup(int trackType,int selectedTrackIndex) {
        this.trackType = trackType;
        this.selectedTrackIndex = selectedTrackIndex;
    }

    public List<TrackInfoWrapper> getTracks() {
        return tracks;
    }

    private List<TrackInfoWrapper> tracks = new ArrayList<>();

    public int getTrackTypeName() {
        if (trackType==ITrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
            return R.string.giraffe_player_track_type_audio;
        } else if (trackType == ITrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
            return R.string.giraffe_player_track_type_video;
        } else if (trackType == ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
            return R.string.giraffe_player_track_type_timed_text;
        } else if (trackType == ITrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
            return R.string.giraffe_player_track_type_subtitle;
        } else {
            return R.string.giraffe_player_track_type_unknown;

        }
    }
}
