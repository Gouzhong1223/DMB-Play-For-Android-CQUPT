package cn.edu.cqupt.dmb.player.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import java.util.concurrent.ExecutionException;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 图片加载工具
 * @Date : create by QingSong in 2022-05-29 14:43
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class GlideUtils {

    private static final String TAG = "GlideUtils";

    public static Bitmap loadBitMap(Context context, byte[] fileBuffer) {

        Bitmap bitmap = null;

        FutureTarget<Bitmap> futureTarget = Glide
                .with(context)
                .asBitmap()
                .load(fileBuffer)
                .centerCrop()
                .submit();

        try {
            bitmap = futureTarget.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "loadBitMap: " + "获取 bitmap 失败");
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap loadBitMap(Context context, String path) {

        Bitmap bitmap = null;

        FutureTarget<Bitmap> futureTarget = Glide
                .with(context)
                .asBitmap()
                .load(path)
                .centerCrop()
                .submit();

        try {
            bitmap = futureTarget.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "loadBitMap: " + "获取 bitmap 失败");
            e.printStackTrace();
        }

        return bitmap;
    }
}
