package tcking.github.com.giraffeplayer2;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashSet;

/**
 * Created by tcking on 2017
 */

public class VideoInfo implements Parcelable {
    public static final int AR_ASPECT_FIT_PARENT = 0; // without clip
    public static final int AR_ASPECT_FILL_PARENT = 1; // may clip
    public static final int AR_ASPECT_WRAP_CONTENT = 2;
    public static final int AR_MATCH_PARENT = 3;
    public static final int AR_16_9_FIT_PARENT = 4;
    public static final int AR_4_3_FIT_PARENT = 5;
    public static final String DEFAULT_FINGERPRINT = "-1";

    public HashSet<Option> options() {
        return options;
    }

    public VideoInfo option(Option option) {
        this.options.add(option);
        return this;
    }

    private HashSet<Option> options = new HashSet<>();

    public boolean showTopBar() {
        return showTopBar;
    }

    public VideoInfo showTopBar(boolean showTopBar) {
        this.showTopBar = showTopBar;
        return this;
    }

    private boolean showTopBar = false;


    private String fingerprint = DEFAULT_FINGERPRINT;
    private Uri uri;

    public VideoInfo() {
    }

    public VideoInfo(Uri uri) {
        this.uri = uri;
    }

    protected VideoInfo(Parcel in) {
        fingerprint = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        title = in.readString();
        portraitWhenFullScreen = in.readByte() != 0;
        aspectRatio = in.readInt();
        lastFingerprint = in.readString();
        lastUri = in.readParcelable(Uri.class.getClassLoader());
        options = (HashSet<Option>) in.readSerializable();
        showTopBar = in.readByte() != 0;
    }

    public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };

    public String title() {
        return title;
    }

    public VideoInfo title(String title) {
        this.title = title;
        return this;
    }

    private String title;

    public boolean isPortraitWhenFullScreen() {
        return portraitWhenFullScreen;
    }

    public VideoInfo portraitWhenFullScreen(boolean portraitWhenFullScreen) {
        this.portraitWhenFullScreen = portraitWhenFullScreen;
        return this;
    }

    private boolean portraitWhenFullScreen = true;

    public int aspectRatio() {
        return aspectRatio;
    }

    public VideoInfo aspectRatio(int aspectRatio) {
        this.aspectRatio = aspectRatio;
        return this;
    }

    private int aspectRatio = AR_ASPECT_FIT_PARENT;
    private String lastFingerprint;
    private Uri lastUri;

    public VideoInfo fingerprint(Object fingerprint) {
        if (lastFingerprint!=null && !lastFingerprint.equals(fingerprint)) {
            //different from last fingerprint, release last
            PlayerManager.getInstance().releaseByFingerprint(lastFingerprint);
        }
        this.fingerprint = ""+fingerprint;
        lastFingerprint = this.fingerprint;
        return this;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        if (lastUri!=null && !lastUri.equals(uri)) {
            //different from last uri, release last
            PlayerManager.getInstance().releaseByFingerprint(lastFingerprint);
        }
        this.uri = uri;
        this.lastUri = this.uri;
    }

    /**
     * A Fingerprint represent a player
     * @return
     */
    public String getFingerprint() {
//        if (listPosition == -1) {
//            return "DEFAULT_FINGERPRINT";
//        }
        return fingerprint;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fingerprint);
        dest.writeParcelable(uri, flags);
        dest.writeString(title);
        dest.writeByte((byte) (portraitWhenFullScreen ? 1 : 0));
        dest.writeInt(aspectRatio);
        dest.writeString(lastFingerprint);
        dest.writeParcelable(lastUri, flags);
        dest.writeSerializable(options);
        dest.writeByte((byte) (showTopBar ? 1 : 0));
    }
}
