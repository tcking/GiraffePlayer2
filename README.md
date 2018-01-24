# GiraffePlayer2

Out of the box android video player base on [ijkplayer 0.8.4](https://github.com/Bilibili/ijkplayer)

this project is total refactor of [GiraffePlayer](https://github.com/tcking/GiraffePlayer/) to support in ListView/RecyclerView and improve the performance，all player tasks do in worker thread.

[release history](https://github.com/tcking/GiraffePlayer2/blob/master/VERSIONS.md)


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
10. support float window

# how to import library
 ``` gradle
    //step 1: add jcenter repositories in your root poject build file
    repositories {
        ...
        jcenter()
    }

    //step 2: add dependency
    compile 'com.github.tcking:giraffeplayer2:0.1.15'

    // if need more decoder using: compile 'com.github.tcking:giraffeplayer2:0.1.15-full'


 ```
 
 **support more ABI:** In most cases your app only need to support `armeabi-v7a`. some articles about ABI :
 1. [How to use 32-bit native libaries on 64-bit Android device](http://stackoverflow.com/questions/30782848/how-to-use-32-bit-native-libaries-on-64-bit-android-device)
 2. [What you should know about .so files](http://ph0b.com/android-abis-and-so-files/)
 3. [关于Android的.so文件你所需要知道的](http://www.jianshu.com/p/cb05698a1968)

to support different ABI:

``` gradle

    compile 'com.github.tcking:ijkplayer-arm64:0.8.4' //support arm64
    compile 'com.github.tcking:ijkplayer-armv5:0.8.4' //support armv5
    compile 'com.github.tcking:ijkplayer-x86:0.8.4' //support x86
    compile 'com.github.tcking:ijkplayer-x86_64:0.8.4' //support x86_64

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
all the configurations on **VideoInfo** :

1. `videoInfo.setAspectRatio()` set video view aspect radio
1. `videoInfo.setFingerprint()` in list must call this to distinguish items
1. `videoInfo.addOption` add player init option
1. `videoInfo.setPortraitWhenFullScreen()` control Portrait when full screen
1. `videoInfo.setRetryInterval()` retry to play again interval (in second,<=0 will disable retry)
1. `videoInfo.setShowTopBar()` show top bar(back arrow and title) when user tap the view
1. `videoInfo.VideoInfo()` set video title
1. `videoInfo.setUri()` set video Uri
1. `videoInfo.setBgColor()` set video background color
1. `videoInfo.setPlayerImpl()` VideoInfo.PLAYER_IMPL_IJK:using ijkplayer for decoder,VideoInfo.PLAYER_IMPL_SYSTEM:using android mediaplayer for decoder
1. `videoInfo.addOption()` set extra options，only for ijkplayer，eg:addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1L))
1. `videoInfo.setFullScreenAnimation()` true for play an animation when enter fullscreen or exit fullscreen,only for setPortraitWhenFullScreen(false) and API>=19


# API:

**GiraffePlayer**
1. `player.start()`
1. `player.pause()`
1. `player.seekTo()`
1. `player.setPlayerListener()` // in RecyclerView,player will create and release on demand,set listener on videoView:`videoView.setPlayerListener()`
1. `player.stop()` //same as release
1. `player.release()` //release the player
1. `player.setVolume()` //set volume
1. `player.getTrackInfo()` //get all tracks
1. `player.selectTrack()` //select track by track index
1. `player.deselectTrack()` // deselect track by track index
1. `player.setMute()`
1. `player.isMute()`
1. `player.getCurrentState()` //get current player state
1. `player.setDisplayModel()` //set display model:GiraffePlayer.DISPLAY_NORMAL | GiraffePlayer.DISPLAY_FULL_WINDOW | GiraffePlayer.DISPLAY_FLOAT

**VideoView** (player's display container and media controller)
1. `videoView.getPlayer()` get or create bind player
1. `videoView.setFingerprint()` delegate of bind videoInfo setFingerprint
1. `videoView.setVideoPath()` delegate of bind videoInfo setUri
1. `videoView.isCurrentActivePlayer()` is bind player active
1. `videoView.getMediaController()` return bind mediaController
1. `videoView.inListView()` is video view in ListView or RecyclerView
1. `videoView.setPlayerListener()` set player Listener (in ListView or RecyclerView you should call this method rather than player.setPlayerListener


**PlayerManager** (manage all players,make sure only one player is active)
1. `PlayerManager.getInstance().getCurrentPlayer()`  return current active player, return null if there is no active player
1. `PlayerManager.getInstance().releaseCurrent()`  release current active player
1. `PlayerManager.getInstance().isCurrentPlayer(fingerprint)`  judge player is active by fingerprint
1. `PlayerManager.getInstance().getPlayer(VideoView)`  get player by video view (will create if not exists)

**PlayerListener** (player event callback)

``` java

void onPreparing();

void onPrepared(GiraffePlayer giraffePlayer);

void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent);

boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra);

void onCompletion(GiraffePlayer giraffePlayer);

void onSeekComplete(GiraffePlayer giraffePlayer);

boolean onError(GiraffePlayer giraffePlayer,int what, int extra);

void onPause(GiraffePlayer giraffePlayer);

void onRelease(GiraffePlayer giraffePlayer);

void onStart(GiraffePlayer giraffePlayer);

void onTargetStateChange(int oldState, int newState);

void onCurrentStateChange(int oldState, int newState);

void onDisplayModelChange(int oldModel, int newModel);

```

# screenshot
![](https://raw.githubusercontent.com/tcking/GiraffePlayer2/master/screencap/s6.gif)

# TODO