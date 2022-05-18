package cn.edu.cqupt.dmb.player.utils;

import android.os.Environment;

import java.io.File;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : DMB 通用工具类
 * @Date : create by QingSong in 2022-03-17 22:45
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbUtil {

    public static final String CACHE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DMB/";
    public static final String CHARACTER_SET = "gb2312";

    /* init directory */
    static {
        File file = new File(CACHE_DIRECTORY);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }
}

