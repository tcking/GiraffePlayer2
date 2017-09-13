package tcking.github.com.giraffeplayer2;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by tcking on 2017
 */

public abstract class BasePlayerActivity extends AppCompatActivity {
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        PlayerManager.getInstance().onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (PlayerManager.getInstance().onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
