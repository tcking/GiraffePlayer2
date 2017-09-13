# GiraffePlayer2

Out of the box android video player base on [ijkplayer](https://github.com/Bilibili/ijkplayer)

this project is total refactor of [GiraffePlayer](https://github.com/tcking/GiraffePlayer/) to support in ListView/RecyclerView and improve the performance，all player tasks do in worker thread.


# features
1. base on ijkplayer,support RTMP , HLS (http & https) , MP4,M4A etc.
2. gestures for volume control
3. gestures for brightness control
4. gestures for forward or backward
5. fullscreen by manual or sensor
6. try to replay when error(only for live video)
7. specify video scale type
8. support in ListView/RecyclerView
9. never block UI thread

# how to import library (not link to jcenter yet)
 ``` gradle
    //step 1: add jcenter repositories in your root poject build file
    repositories {
        ...
        jcenter()
    }

    //step 2: add dependency
    compile 'com.github.tcking:giraffeplayer2:1.0.0'


 ```
 
## support more ABI:
In most cases your app only need to support `armeabi-v7a`. A article about ABI :[How to use 32-bit native libaries on 64-bit Android device](http://stackoverflow.com/questions/30782848/how-to-use-32-bit-native-libaries-on-64-bit-android-device),[What you should know about .so files](http://ph0b.com/android-abis-and-so-files/),[关于Android的.so文件你所需要知道的](http://www.jianshu.com/p/cb05698a1968)
there are some aars to support different ABI:
    compile 'com.github.tcking:ijkplayer-arm64:0.8.2' //support arm64
    compile 'com.github.tcking:ijkplayer-armv5:0.8.2' //support armv5
    compile 'com.github.tcking:ijkplayer-x86:0.8.2' //support x86
    compile 'com.github.tcking:ijkplayer-x86_64:0.8.2' //support x86_64



# How to use ([example code](https://github.com/tcking/GiraffePlayer2/blob/master/app/src/main/java/tcking/github/com/giraffeplayer/example/MainFragment.javavideoInfo))
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
videoView.videoPath(videoUri).getPlayer().start();


```

# player in ListView or RecyclerView
in ListView or RecyclerView,you need do one more thing: call `videoView.fingerprint()`,
the fingerprint is the key that player distinguish list items,you can using list `position` or list data's `hashcode` as `fingerprint`,eg:

``` java
public void onBindViewHolder(VideoItemHolder holder, int position) {
        VideoItem videoItem = data.get(position);
        holder.name.setText(videoItem.name);
        holder.url.setText(videoItem.uri);
        holder.videoView.videoPath(videoItem.uri).fingerprint(position);// or using:fingerprint(videoItem.hashCode())
    }

```

# config player
all the configurations in VideoInfo,eg:
``` java
VideoInfo videoInfo = new VideoInfo("http://xxx.mp4")
                            .setTitle("test video") //config title
                            .setAspectRatio(aspectRatio) //aspectRatio
                            .setShowTopBar(true) //show mediacontroller top bar
                            .setPortraitWhenFullScreen(true);//portrait when full screen

                    GiraffePlayer.play(getContext(), videoInfo);


```
all the configurations:

1. `videoInfo.setAspectRatio()` set video view aspect radio
1. `videoInfo.setFingerprint()` in list must call this to distinguish items
1. `videoInfo.addOption` add player init option
1. `videoInfo.setPortraitWhenFullScreen()` control Portrait when full screen
1. `videoInfo.setRetryInterval()` retry to play again interval (in second,<=0 will disable retry)
1. `videoInfo.setShowTopBar()` show top bar(back arrow and title) when user tap the view
1. `videoInfo.VideoInfo()` set video title
1. `videoInfo.setUri()` set video Uri

# API:
TODO
# screenshot
TODO
