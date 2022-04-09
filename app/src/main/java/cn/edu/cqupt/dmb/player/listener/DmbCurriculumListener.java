package cn.edu.cqupt.dmb.player.listener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.banner.bean.CurriculumImageCache;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是监听课表数据的监听器
 * @Date : create by QingSong in 2022-04-07 14:52
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbCurriculumListener implements DmbListener {

    private static final String TAG = "DmbCurriculumListener";

    private final Object WAIT_IMAGE_LOCK_OBJECT;

    /**
     * 文件缓冲区
     */
    private final byte[] fileBuffer = new byte[1024 * 1024 * 2];

    public DmbCurriculumListener(Object lockObject) {
        this.WAIT_IMAGE_LOCK_OBJECT = lockObject;
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length) {
        FrequencyModule frequencyModule = DataReadWriteUtil.getActiveFrequencyModule();
        if (frequencyModule == null) {
            Log.e(TAG, "出错啦!现在没有设置活跃的组件,所以这里的回调方法就直接抛弃!");
            return;
        }
        if (!frequencyModule.getModuleName().startsWith("CURRICULUM")) {
            // 活跃组件不是课表,直接返回
            Log.e(TAG, "当前活跃的组件不是课表");
            return;
        }
        String needBuilding = frequencyModule.getModuleName().split("-")[1];
        if (!fileName.contains(needBuilding)) {
            // 如果不是需要的课表,就直接返回
            return;
        }
        System.arraycopy(bytes, 0, fileBuffer, 0, length);
        Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuffer, 0, length);
        if (bitmap != null) {
            synchronized (WAIT_IMAGE_LOCK_OBJECT) {
                while (CurriculumImageCache.getBannerCache().peek() != null) {
                    try {
                        Log.i(TAG, "现在课表缓存里面还有图片,监听器放弃了锁");
                        WAIT_IMAGE_LOCK_OBJECT.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                BannerBitmapDataBean bannerBitmapDataBean = new BannerBitmapDataBean(bitmap, fileName, 1);
                Log.i(TAG, "重新往缓存里面添加了一下课表图片");
                CurriculumImageCache.getBannerCache().offer(bannerBitmapDataBean);
                Log.i(TAG, Thread.currentThread().getName() + "线程正在唤醒课表更新线程");
                WAIT_IMAGE_LOCK_OBJECT.notifyAll();
            }
        } else {
            Log.e(TAG, "生成 bitmap 错误啦!");
        }
    }

    @Override
    public void onReceiveMessage(String msg) {

    }
}
