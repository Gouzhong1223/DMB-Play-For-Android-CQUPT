package cn.edu.cqupt.dmb.player.banner.bean;

import com.google.common.collect.EvictingQueue;

import java.util.Queue;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 缓存 Banner 的 Bitmap 图片的
 * @Date : create by QingSong in 2022-03-23 13:15
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.banner.bean
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class CarouselBannerImageBitmapCache {

    private static final Queue<BannerBitmapDataBean> bannerCache = EvictingQueue.create(DmbPlayerConstant.DEFAULT_BANNER_BITMAP_CACHE_SIZE.getDmbConstantValue());

    public static Queue<BannerBitmapDataBean> getBannerCache() {
        return bannerCache;
    }

    public static void putBitMap(BannerBitmapDataBean bannerBitmapDataBean) {
        bannerCache.add(bannerBitmapDataBean);
    }

}
