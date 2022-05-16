package cn.edu.cqupt.dmb.player.actives;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.banner.adapter.ImageAdapter;
import cn.edu.cqupt.dmb.player.banner.bean.BannerDataBean;

/**
 * @author qingsong
 */
public class DormitorySafetyActivity extends FragmentActivity {

    private Banner banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sushe);
        initView();
    }

    private void initView() {
        banner = findViewById(R.id.banner);
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
        super.onDestroy();
    }
}
