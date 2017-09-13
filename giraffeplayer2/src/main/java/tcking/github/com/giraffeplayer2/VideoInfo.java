package tcking.github.com.giraffeplayer2;

import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;

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

    private HashSet<Option> options = new HashSet<>();
    private boolean showTopBar = false;
    private Uri uri;
    private String fingerprint = DEFAULT_FINGERPRINT;
    private boolean portraitWhenFullScreen = true;
    private String title;
    private int aspectRatio = AR_ASPECT_FIT_PARENT;
    private String lastFingerprint;
    private Uri lastUri;
    private int retryInterval=0;
    private int bgColor = Color.DKGRAY;

    public int getBgColor() {
        return bgColor;
    }

    /**
     * player background color default is Color.DKGRAY
     * @param bgColor ColorInt
     * @return
     */
    public VideoInfo setBgColor(@ColorInt int bgColor) {
        this.bgColor = bgColor;
        return this;
    }



    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * retry to play again interval (in second)
     * @param retryInterval interval in second <=0 will disable retry
     * @return VideoInfo
     */
    public VideoInfo setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }


    public HashSet<Option> getOptions() {
        return options;
    }

    /**
     * add player init option
     * @param option option
     * @return VideoInfo
     */
    public VideoInfo addOption(Option option) {
        this.options.add(option);
        return this;
    }

    public boolean isShowTopBar() {
        return showTopBar;
    }

    /**
     * show top bar(back arrow and title) when user tap the view
     * @param showTopBar true to show
     * @return VideoInfo
     */
    public VideoInfo setShowTopBar(boolean showTopBar) {
        this.showTopBar = showTopBar;
        return this;
    }

    public boolean isPortraitWhenFullScreen() {
        return portraitWhenFullScreen;
    }

    /**
     * control Portrait when full screen
     * @param portraitWhenFullScreen true portrait when full screen
     * @return VideoInfo
     */
    public VideoInfo setPortraitWhenFullScreen(boolean portraitWhenFullScreen) {
        this.portraitWhenFullScreen = portraitWhenFullScreen;
        return this;
    }

    public String getTitle() {
        return title;
    }

    /**
     * video title
     * @param title title
     * @return VideoInfo
     */
    public VideoInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getAspectRatio() {
        return aspectRatio;
    }

    public VideoInfo setAspectRatio(int aspectRatio) {
        this.aspectRatio = aspectRatio;
        return this;
    }

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
        retryInterval = in.readInt();
        bgColor = in.readInt();
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


    public VideoInfo setFingerprint(Object fingerprint) {
        if (lastFingerprint!=null && !lastFingerprint.equals(fingerprint)) {
            //different from last setFingerprint, release last
            PlayerManager.getInstance().releaseByFingerprint(lastFingerprint);
        }
        this.fingerprint = ""+fingerprint;
        lastFingerprint = this.fingerprint;
        return this;
    }

    /**
     * A Fingerprint represent a player
     * @return setFingerprint
     */
    public String getFingerprint() {
        return fingerprint;
    }

    public Uri getUri() {
        return uri;
    }

    /**
     * set video uri
     * @param uri uri
     * @return VideoInfo
     */
    public VideoInfo setUri(Uri uri) {
        if (lastUri!=null && !lastUri.equals(uri)) {
            //different from last uri, release last
            PlayerManager.getInstance().releaseByFingerprint(lastFingerprint);
        }
        this.uri = uri;
        this.lastUri = this.uri;
        return this;
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
        dest.writeInt(retryInterval);
        dest.writeInt(bgColor);
    }
}
