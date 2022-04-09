package cn.edu.cqupt.dmb.player.banner.bean;

import com.google.common.collect.EvictingQueue;

import java.util.Queue;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是用于缓存课表图片的 Cache
 * @Date : create by QingSong in 2022-04-09 15:51
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.banner.bean
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class CurriculumImageCache {

    /**
     * 长度为 1 的有界队列
     */
    private static final Queue<BannerBitmapDataBean> bannerCache = EvictingQueue.create(5);

    public static Queue<BannerBitmapDataBean> getBannerCache() {
        return bannerCache;
    }
}
