package cn.edu.cqupt.dmb.player.actives.leanback;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.actives.fragment.SceneDetailsFragment;

/**
 * @author qingsong
 */
public class DetailsActivity extends FragmentActivity {
    public static final String SHARED_ELEMENT_NAME = "hero";
    public static final String SCENE_VO = "SceneVO";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_fragment, new SceneDetailsFragment())
                    .commitNow();
        }
    }

}
