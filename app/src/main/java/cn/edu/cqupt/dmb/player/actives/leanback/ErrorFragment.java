package cn.edu.cqupt.dmb.player.actives.leanback;

import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.leanback.app.ErrorSupportFragment;

import cn.edu.cqupt.dmb.player.R;

/**
 * @author qingsong
 */
public class ErrorFragment extends ErrorSupportFragment {
    private static final String TAG = "ErrorFragment";
    private static final boolean TRANSLUCENT = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.app_name));
    }

    void setErrorContent() {
        setImageDrawable(ContextCompat.getDrawable(requireContext(), androidx.leanback.R.drawable.lb_ic_sad_cloud));
        setMessage(getResources().getString(R.string.error_fragment_message));
        setDefaultBackground(TRANSLUCENT);

        setButtonText(getResources().getString(R.string.dismiss_error));
        setButtonClickListener(
                arg0 -> {
                    assert getFragmentManager() != null;
                    getFragmentManager().beginTransaction().remove(ErrorFragment.this).commit();
                });
    }
}
