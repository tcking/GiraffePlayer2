package tcking.github.com.giraffeplayer2.trackselector;

import android.text.TextUtils;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerManager;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * Created by TangChao on 2017/10/11.
 */

public class TrackInfoWrapper {
    private ITrackInfo innerTrack;
    private int index=-1;
    private int trackType;

    public int getIndex() {
        return index;
    }

    public int getTrackType() {
        return trackType;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    private String fingerprint;

    public TrackInfoWrapper(String fingerprint,ITrackInfo track,int index,int trackType) {
        this.fingerprint = fingerprint;
        this.innerTrack = track;
        this.index = index;
        this.trackType = trackType;
    }

    public String getInfo() {
        return innerTrack.getInfoInline();
    }

    public boolean selected() {
        if (TextUtils.isEmpty(fingerprint)) {
            return false;
        }
        GiraffePlayer player = PlayerManager.getInstance().getPlayerByFingerprint(fingerprint);
        if (player == null) {
            return false;
        }
        return player.getSelectedTrack(trackType) == index;
    }
}
