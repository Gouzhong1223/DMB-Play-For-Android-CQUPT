package cn.edu.cqupt.dmb.player.actives;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.banner.adapter.ImageAdapter;
import cn.edu.cqupt.dmb.player.banner.bean.BannerDataBean;

/**
 * @author qingsong
 */
public class DormitorySafetyActivity extends BaseActivity {

    /**
     * 宿舍安全信息中间的轮播图
     */
    private Banner banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dormitory);
        initView();
    }

    private void initView() {
        banner = findViewById(R.id.dormitory_banner);
        useBanner();
    }

    /**
     * 播放轮播图
     */
    public void useBanner() {
        //添加生命周期观察者
        banner.addBannerLifecycleObserver(this)
                .setAdapter(new ImageAdapter(BannerDataBean.getHelloViewData()))
                .setIndicator(new CircleIndicator(this));
    }

    @Override
    protected void onDestroy() {
        if (banner != null) {
            banner.destroy();
        }
        super.onDestroy();
    }
}
