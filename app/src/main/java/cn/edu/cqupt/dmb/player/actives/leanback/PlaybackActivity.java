package cn.edu.cqupt.dmb.player.actives.leanback;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import cn.edu.cqupt.dmb.player.actives.fragment.PlaybackVideoFragment;

/**
 * Loads {@link PlaybackVideoFragment}.
 *
 * @author qingsong
 */
public class PlaybackActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new PlaybackVideoFragment())
                    .commit();
        }
    }
}
