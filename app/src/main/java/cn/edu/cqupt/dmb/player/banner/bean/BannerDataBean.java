package cn.edu.cqupt.dmb.player.banner.bean;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.edu.cqupt.dmb.player.R;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-02 15:16
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.banner
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class BannerDataBean {
    public Integer imageRes;
    public String title;
    public int viewType;

    public BannerDataBean(Integer imageRes, String title, int viewType) {
        this.imageRes = imageRes;
        this.title = title;
        this.viewType = viewType;
    }

    public static List<BannerDataBean> getHelloViewData() {
        List<BannerDataBean> list = new ArrayList<>();
        list.add(new BannerDataBean(R.drawable.bacg2k, "这个是一个欢迎页.轮播图刚刚启动的时候会播放这个", 1));
        return list;
    }

    public static List<String> getColors(int size) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(getRandColor());
        }
        return list;
    }

    /**
     * 获取十六进制的颜色代码.例如  "#5A6677"
     * 分别取R、G、B的随机值，然后加起来即可
     *
     * @return String
     */
    public static String getRandColor() {
        String R, G, B;
        Random random = new Random();
        R = Integer.toHexString(random.nextInt(256)).toUpperCase();
        G = Integer.toHexString(random.nextInt(256)).toUpperCase();
        B = Integer.toHexString(random.nextInt(256)).toUpperCase();

        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        return "#" + R + G + B;
    }
}
