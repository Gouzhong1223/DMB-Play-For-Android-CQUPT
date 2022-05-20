package cn.edu.cqupt.dmb.player.actives;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.actives.fragment.MainFragment;


/**
 * @author qingsong
 */
public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";
    /**
     * 设备存储权限
     */
    private static final int WRITE_STORAGE_REQUEST_CODE = 100;

    /**
     * 根据预设场景类型获取对应的 Activity<br/>
     * {"视频", "轮播图", "音频", "安全信息", "课表"}
     *
     * @param sceneType 预设场景类型
     * @return Activity
     */
    public static Class<?> getActivityBySceneType(Integer sceneType) {
        switch (sceneType) {
            case 0: {
                return VideoActivity.class;
            }
            case 1: {
                return CarouselActivity.class;
            }
            case 2: {
                return null;
            }
            case 3: {
                return DormitorySafetyActivity.class;
            }
            case 4: {
                return CurriculumActivity.class;
            }
            default:
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);
        // 在 UI 线程中替换 Fragment
        runOnUiThread(() -> {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_browse_fragment, new MainFragment())
                        .commitNow();
            }
        });
        // 请求存储设备的读写权限
        requestPermissions(this);
    }

    /**
     * 请求获取存储设备的读写权限<br/>
     * 如果有权限就直接跳过<br/>
     * 如果没有权限就请求用户授予<br/>
     *
     * @param context Context
     */
    private void requestPermissions(@NonNull Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "没有授权，申请权限");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_STORAGE_REQUEST_CODE);
        } else {
            Log.i(TAG, "有权限，打开文件");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_STORAGE_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "申请权限成功，打开");
            } else {
                Log.i(TAG, "申请权限失败");
            }
        }
    }
}
