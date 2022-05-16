package cn.edu.cqupt.dmb.player.actives.leanback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.actives.fragment.ErrorFragment;
import cn.edu.cqupt.dmb.player.actives.fragment.MainFragment;

/**
 * @author qingsong
 */
public class BrowseErrorActivity extends FragmentActivity {
    private static final int TIMER_DELAY = 3000;
    private static final int SPINNER_WIDTH = 100;
    private static final int SPINNER_HEIGHT = 100;

    private ErrorFragment mErrorFragment;
    private SpinnerFragment mSpinnerFragment;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_browse_fragment, new MainFragment())
                    .commitNow();
        }
        testError();
    }

    private void testError() {
        mErrorFragment = new ErrorFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_browse_fragment, mErrorFragment)
                .commit();

        mSpinnerFragment = new SpinnerFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_browse_fragment, mSpinnerFragment)
                .commit();

        final Handler handler = new Handler(Looper.myLooper());
        handler.postDelayed(() -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mSpinnerFragment)
                    .commit();
            mErrorFragment.setErrorContent();
        }, TIMER_DELAY);
    }

    public static class SpinnerFragment extends Fragment {
        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            assert container != null;
            ProgressBar progressBar = new ProgressBar(container.getContext());
            if (container instanceof FrameLayout) {
                FrameLayout.LayoutParams layoutParams =
                        new FrameLayout.LayoutParams(SPINNER_WIDTH, SPINNER_HEIGHT, Gravity.CENTER);
                progressBar.setLayoutParams(layoutParams);
            }
            return progressBar;
        }
    }
}
