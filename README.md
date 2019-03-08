# GiraffePlayer2


Out of the box android video player base on [ijkplayer 0.8.8](https://github.com/Bilibili/ijkplayer) (for flutter please visit [GPlayer](https://github.com/tcking/GPlayer))

this project is total refactor of [GiraffePlayer](https://github.com/tcking/GiraffePlayer/) to support in ListView/RecyclerView and improve the performance，all player tasks do in worker thread.

[release history](https://github.com/tcking/GiraffePlayer2/blob/master/VERSIONS.md)

[download demo apk (~700kb)](https://raw.githubusercontent.com/tcking/GiraffePlayer2/master/demo/demo.apk)


# features
1. base on ijkplayer,support RTMP , HLS (http & https) , MP4,M4A etc.
2. gestures for volume control
3. gestures for brightness control
4. gestures for forward or backward
5. fullscreen by manual or sensor (with animation)
6. try to replay when error(only for live video)
7. specify video scale type
8. support in ListView/RecyclerView (in Activity or Fragment)
9. never block UI thread
10. support select track
11. support float window
12. support lazy load (download player on demand,increase apk size only about 180K)
13. easy to custom media controller (without change this project source code)

# how to import library
 ``` gradle
    //step 1: add jcenter repositories in your root poject build file
    repositories {
        ...
        jcenter()
    }

    //step 2: add dependency,there are 3 type of aar,see the flow table
    api 'com.github.tcking:giraffeplayer2:0.1.25-lazyLoad'

 ```

aar type|aar size|decoders|support abi
----|-----|-----|----
`api 'com.github.tcking:giraffeplayer2:0.1.25'`| 2.2M |common decoders|default armeabi，can add manually
`api 'com.github.tcking:giraffeplayer2:0.1.25-lazyLoad'`| 180K| all decoders|download player(so files) on demand by device abi
`api 'com.github.tcking:giraffeplayer2:0.1.25-full'`| 5.4M | all decoders|default armeabi，can add manually


if you using `compile 'com.github.tcking:giraffeplayer2:0.1.25'` or `compile 'com.github.tcking:giraffeplayer2:0.1.25-full'` and want to support more ABI:


``` gradle
    //for common decoders
    api 'com.github.tcking:ijkplayer-arm64:0.8.8' //support arm64
    api 'com.github.tcking:ijkplayer-armv5:0.8.8' //support armv5
    api 'com.github.tcking:ijkplayer-x86:0.8.8' //support x86
    api 'com.github.tcking:ijkplayer-x86_64:0.8.8' //support x86_64

    //for all decoders
    api 'com.github.tcking:ijkplayer-arm64:0.8.8-full' //support arm64
    api 'com.github.tcking:ijkplayer-armv5:0.8.8-full' //support armv5
    api 'com.github.tcking:ijkplayer-x86:0.8.8-full' //support x86
    api 'com.github.tcking:ijkplayer-x86_64:0.8.8-full' //support x86_64

```



# How to use ([example code](https://github.com/tcking/GiraffePlayer2/blob/master/app/src/main/java/tcking/github/com/giraffeplayer/example/MainFragment.java))
## case 1: only want to play a video fullscreen
just call `GiraffePlayer.play(getContext(), new VideoInfo("video url"));`,all is done.

## case 2: embed a player in a layout (ListView/RecyclerView)
### step 1: add `VideoView` in your layout xml file
``` xml

<tcking.github.com.giraffeplayer2.VideoView
    android:id="@+id/video_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>

```

### step 2: get player and play
``` java

VideoView videoView = (VideoView) findViewById(R.id.video_view);
videoView.setVideoPath(videoUri).getPlayer().start();


```

# player in ListView or RecyclerView [example code](https://github.com/tcking/GiraffePlayer2/blob/master/app/src/main/java/tcking/github/com/giraffeplayer/example/ListExampleActivity.java)
in ListView or RecyclerView,you need do one more thing: call `videoView.setFingerprint()`,
the fingerprint is the key that player distinguish list items,you can using list `position` or list data's `hashcode` as `fingerprint`,eg:

``` java
public void onBindViewHolder(VideoItemHolder holder, int position) {
        VideoItem videoItem = data.get(position);
        holder.name.setText(videoItem.name);
        holder.url.setText(videoItem.uri);
        holder.videoView.setVideoPath(videoItem.uri).setFingerprint(position);// or using:setFingerprint(videoItem.hashCode())
    }

```

# config player
all the configurations in VideoInfo,you can get VideoInfo and then set configurations,eg:
``` java
//standalone player
VideoInfo videoInfo = new VideoInfo("http://xxx.mp4")
                            .setTitle("test video") //config title
                            .setAspectRatio(aspectRatio) //aspectRatio
                            .setShowTopBar(true) //show mediacontroller top bar
                            .setPortraitWhenFullScreen(true);//portrait when full screen

GiraffePlayer.play(getContext(), videoInfo);

//in RecyclerView or embed player
public void onBindViewHolder(VideoItemHolder holder, int position) {
        VideoItem videoItem = data.get(position);
        holder.name.setText(videoItem.name);
        holder.url.setText(videoItem.uri);
        holder.videoView.getVideoInfo().setBgColor(Color.GRAY).setAspectRatio(VideoInfo.AR_MATCH_PARENT);//config player
        holder.videoView.setVideoPath(videoItem.uri).setFingerprint(position);
    }
```

# Proguard Configurationr

```
-dontwarn tv.danmaku.ijk.media.player.**
-keep class tv.danmaku.ijk.media.player.** { *; }
-keep interface tv.danmaku.ijk.media.player.* { *; }
```

# screenshot

lazy load

![](https://raw.githubusercontent.com/tcking/GiraffePlayer2/master/screencap/s10.gif)


![](https://raw.githubusercontent.com/tcking/GiraffePlayer2/master/screencap/s6.gif)


![](https://raw.githubusercontent.com/tcking/GiraffePlayer2/master/screencap/s7.gif)
