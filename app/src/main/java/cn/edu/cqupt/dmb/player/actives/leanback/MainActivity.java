package cn.edu.cqupt.dmb.player.actives.leanback;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import cn.edu.cqupt.dmb.player.R;


/**
 * @author qingsong
 */
public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_browse_fragment, new MainFragment())
                    .commitNow();
        }
    }
}
