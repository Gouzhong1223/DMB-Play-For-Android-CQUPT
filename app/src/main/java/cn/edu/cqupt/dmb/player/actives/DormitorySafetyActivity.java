package cn.edu.cqupt.dmb.player.actives;

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
    public void initView() {
        banner = findViewById(R.id.dormitory_banner);
        useBanner();
    }

    @Override
    protected void startDecode() {

    }

    /**
     * 播放轮播图
     */
    public void useBanner() {
        //添加生命周期观察者
        banner.addBannerLifecycleObserver(this).setAdapter(new ImageAdapter(BannerDataBean.getHelloViewData())).setIndicator(new CircleIndicator(this));
    }

    @Override
    protected void onDestroy() {
        if (banner != null) {
            banner.destroy();
        }
        super.onDestroy();
    }
}
