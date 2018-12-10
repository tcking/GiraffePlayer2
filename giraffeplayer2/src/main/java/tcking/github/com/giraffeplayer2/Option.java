package tcking.github.com.giraffeplayer2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by tcking on 2017
 */

public class Option implements Serializable, Cloneable {
    private int category;
    private String name;
    private Object value;

    private Option(int category, String name, Object value) {
        this.category = category;
        this.name = name;
        this.value = value;
    }

    public static Option create(int category, String name, String value) {
        return new Option(category, name, value);
    }

    public static Option create(int category, String name, Long value) {
        return new Option(category, name, value);
    }

    public int getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        if (category != option.category) return false;
        if (name != null ? !name.equals(option.name) : option.name != null) return false;
        return value != null ? value.equals(option.value) : option.value == null;

    }

    @Override
    public int hashCode() {
        int result = category;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public Option clone() throws CloneNotSupportedException {
        return (Option) super.clone();
    }

    /**
     * preset for realtime stream
     * @return
     */
    public static List<Option> preset4Realtime() {
        List<Option> options = new ArrayList<>();
        options.add(create(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0L));
        options.add(create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0L));
        options.add(create(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 8L));


        options.add(create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L));
        options.add(create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024L));
        options.add(create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L));
        options.add(create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "packet-buffering", 0L));
        options.add(create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "framedrop", 1L));
        return options;
    }
}
