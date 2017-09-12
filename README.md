# GiraffePlayer2
out of the box android video player base on [ijkplayer](https://github.com/Bilibili/ijkplayer)

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

# how to import library
 ``` gradle
    //1. add jcenter repositories in your root poject build file
    repositories {
        ...
        jcenter()
    }

    //2. add dependency
    compile 'com.github.tcking:giraffeplayer2:1.0.0'

    //compile 'com.github.tcking:ijkplayer-x86:0.8.2' //support ABI:x86 (compile with openSSL)


 ```
 
## notice:
 the player default support 6 CPU architecture:ARMv5, ARMv7, ARMv8,x86 and 86_64,if your project need't support all of the architectures,you can remove the folder in `ijkplayer-java/src/main/jniLibs` to generate a light APK.
 read this first:[How to use 32-bit native libaries on 64-bit Android device](http://stackoverflow.com/questions/30782848/how-to-use-32-bit-native-libaries-on-64-bit-android-device),[What you should know about .so files](http://ph0b.com/android-abis-and-so-files/),[关于Android的.so文件你所需要知道的](http://www.jianshu.com/p/cb05698a1968)

# How to use ([example code](https://github.com/tcking/GiraffePlayer/blob/master/app/src/main/java/tcking/github/com/giraffeplayer/example/MainActivity.java))
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
in ListView or RecyclerView ,you need do one more thing: call `videoView.fingerprint()`,
the fingerprint is the key that player distinguish list data,you can using list position or list data hashcode as fingerprint,eg:

``` java
public void onBindViewHolder(VideoItemHolder holder, int position) {
        VideoItem videoItem = data.get(position);
        holder.name.setText(videoItem.name);
        holder.url.setText(videoItem.uri);
        holder.videoView.videoPath(videoItem.uri).fingerprint(position);//fingerprint(videoItem.hashCode())
    }

```

# config player
all the configurations in VideoInfo,eg:
``` java
VideoInfo videoInfo = new VideoInfo("http://xxx.mp4")
                            .title("test video") //config title
                            .aspectRatio(aspectRatio) //aspectRatio
                            .showTopBar(true) //show mediacontroller top bar
                            .portraitWhenFullScreen(true);//portrait when full screen

                    GiraffePlayer.play(getContext(), videoInfo);

```


# API:
*
# screencap

![](https://github.com/tcking/GiraffePlayer/blob/master/screencap/device-2015-10-28-142934.png)
![](https://github.com/tcking/GiraffePlayer/blob/master/screencap/device-2015-10-28-143207.png)
![](https://github.com/tcking/GiraffePlayer/blob/master/screencap/device-2015-10-28-143304.png)
![](https://github.com/tcking/GiraffePlayer/blob/master/screencap/device-2015-10-28-143343.png)
![](https://github.com/tcking/GiraffePlayer/blob/master/screencap/device-2015-10-28-143722.png)
