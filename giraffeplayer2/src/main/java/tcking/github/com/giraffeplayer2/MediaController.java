package tcking.github.com.giraffeplayer2;


/**
 * Created by tcking on 2017
 */

public interface MediaController extends PlayerListener {
    /**
     * bind this media controller to video controllerView
     */
    void bind(VideoView videoView);
}
