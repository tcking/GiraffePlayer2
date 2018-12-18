package tcking.github.com.giraffeplayer.example;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.github.tcking.viewquery.ViewQuery;

import tcking.github.com.giraffeplayer2.DefaultMediaController;
import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.MediaController;
import tcking.github.com.giraffeplayer2.Option;
import tcking.github.com.giraffeplayer2.PlayerManager;
import tcking.github.com.giraffeplayer2.VideoInfo;
import tcking.github.com.giraffeplayer2.VideoView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by TangChao on 2017/6/15.
 */

public class MainFragment extends Fragment {
    private ViewQuery $;
    private int aspectRatio = VideoInfo.AR_ASPECT_FIT_PARENT;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set global configuration: turn on multiple_requests
        PlayerManager.getInstance().getDefaultVideoInfo().addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "multiple_requests", 1L));
//        PlayerManager.getInstance().getDefaultVideoInfo().addOptions(Option.preset4Realtime());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        $ = new ViewQuery(view);

        String testUrl = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
//        testUrl = "file:///sdcard/tmp/o.mp4"; //test local file;
//        testUrl = "https://tungsten.aaplimg.com/VOD/bipbop_adv_example_v2/master.m3u8"; //test live stream;
//        testUrl = "http://playertest.longtailvideo.com/adaptive/oceans_aes/oceans_aes.m3u8"; //test live stream;
        testUrl = "http://zhibo.hkstv.tv/livestream/zb2yhapo/playlist.m3u8"; //test live stream;

        final VideoView videoView = $.id(R.id.video_view).view();
        videoView.setVideoPath(testUrl);

        $.id(R.id.et_url).text(testUrl);
        CheckBox cb = $.id(R.id.cb_pwf).view();
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                videoView.getVideoInfo().setPortraitWhenFullScreen(isChecked);
            }
        });

        RadioGroup rb = $.id(R.id.rg_ra).view();
        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.rb_4_3) {
                    aspectRatio = VideoInfo.AR_4_3_FIT_PARENT;
                } else if (checkedId == R.id.rb_16_9) {
                    aspectRatio = VideoInfo.AR_16_9_FIT_PARENT;
                } else if (checkedId == R.id.rb_fill_parent) {
                    aspectRatio = VideoInfo.AR_ASPECT_FILL_PARENT;
                } else if (checkedId == R.id.rb_fit_parent) {
                    aspectRatio = VideoInfo.AR_ASPECT_FIT_PARENT;
                } else if (checkedId == R.id.rb_wrap_content) {
                    aspectRatio = VideoInfo.AR_ASPECT_WRAP_CONTENT;
                } else if (checkedId == R.id.rb_match_parent) {
                    aspectRatio = VideoInfo.AR_MATCH_PARENT;
                }
                videoView.getPlayer().aspectRatio(aspectRatio);

            }
        });

        RadioGroup rb2 = $.id(R.id.rg_mc).view();
        rb2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes final int checkedId) {
                PlayerManager.getInstance().setMediaControllerGenerator(new PlayerManager.MediaControllerGenerator() {
                    @Override
                    public MediaController create(Context context, VideoInfo videoInfo) {
                        return checkedId == R.id.rb_mc_default ? new DefaultMediaController(context) : new SimpleMediaController(context);
                    }
                });
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getId() == R.id.btn_play) {
                    if (videoView.getPlayer().isPlaying()) {
                        videoView.getPlayer().pause();
                    } else {
                        videoView.getPlayer().start();
                    }
                } else if (v.getId() == R.id.btn_full) {
                    videoView.getPlayer().toggleFullScreen();
                } else if (v.getId() == R.id.btn_play_float) {
                    videoView.getPlayer().setDisplayModel(GiraffePlayer.DISPLAY_FLOAT);
                } else if (v.getId() == R.id.btn_list) {
                    startActivity(new Intent(getActivity(), ListExampleActivity.class));
                } else if (v.getId() == R.id.btn_list2) {
                    startActivity(new Intent(getActivity(), ListExample2Activity.class));
                } else if (v.getId() == R.id.btn_play_in_standalone) {
                    VideoInfo videoInfo = new VideoInfo(Uri.parse($.id(R.id.et_url).text()))
                            .setTitle("test video")
                            .setAspectRatio(aspectRatio)
//                            .setFullScreenOnly(true)
//                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 30000000L))
//                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1L))
                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1L))
                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "multiple_requests", 1L))
//                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "headers", "Connection: keep-alive\r\nuser-agent: okhttp\r\n"))
//                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1L))
//                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect_at_eof", 1L))
//                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect_streamed", 1L))
//                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect_delay_max", 1L))
//                            .setPlayerImpl(VideoInfo.PLAYER_IMPL_SYSTEM) //using android media player
                            .setShowTopBar(true);

                    GiraffePlayer.play(getContext(), videoInfo);
                    getActivity().overridePendingTransition(0, 0);
                }
            }
        };
        $.id(R.id.btn_play).view().setOnClickListener(onClickListener);
        $.id(R.id.btn_play_float).view().setOnClickListener(onClickListener);
        $.id(R.id.btn_full).view().setOnClickListener(onClickListener);
        $.id(R.id.btn_play_in_standalone).view().setOnClickListener(onClickListener);
        $.id(R.id.btn_list).view().setOnClickListener(onClickListener);
        $.id(R.id.btn_list2).view().setOnClickListener(onClickListener);


    }


}
