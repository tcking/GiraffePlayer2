package tcking.github.com.giraffeplayer2.trackselector;

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
        return innerTrack == null ? "OFF" : innerTrack.getInfoInline();
    }

    public static TrackInfoWrapper OFF(String fingerprint, int trackType) {
        return new TrackInfoWrapper(fingerprint,null,-1,trackType);
    }
}
