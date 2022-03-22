package cn.edu.cqupt.dmb.player.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : DMB 通用工具类
 * @Date : create by QingSong in 2022-03-17 22:45
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbUtil {

    public static final String CACHE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DMB/";
    public static final String CHARACTER_SET = "gb2312";
    private static final String TAG = "Utils";


    public static void getPermission(Activity activity) {
        int permissionCheck1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }


    private static final String U_DISK_PATH = null;

    /* init directory */
    static {
        File file = new File(CACHE_DIRECTORY);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }

    public static Object readObject(String path) {
        Object obj = null;
        try {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path))) {
                obj = inputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e(TAG, "open input stream failed");
        }
        return obj;
    }

    public static void writeObject(Object obj, String path) {
        try {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
                outputStream.writeObject(obj);
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e(TAG, "open output stream failed");
        }
    }

    public static void writeFile(byte[] bytes, int offset, int length, String path) {
        try {
            try (FileOutputStream outputStream = new FileOutputStream(path)) {
                outputStream.write(bytes, offset, length);
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e(TAG, "open file fail");
        }
    }

    /* xml config file key names */
    private static final String SHARED_PREFERENCES_NAME = "shared_preferences_name";
    public static final String FREQUENCY = "frequency";
    public static final String RECEIVER_ID = "receiver_id";
    public static final String ENCRYPTION = "encryption";
    public static final String FIRST_TIME = "first_time";
    public static final String SIGNAL = "signal";
    public static final String BUILDING = "building";
    /* SharedPreference methods */
    private static SharedPreferences sharedPreferences;

    public static int getInt(Context context, String key, int defValue) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getInt(key, defValue);
    }

    public static void putInt(Context context, String key, int value) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getBoolean(key, defValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static String hashCode(String name) {
        byte[] bytes = new byte[0];
        try {
            bytes = name.getBytes("gb2312");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int seed = 131;
        int hash = 0;
        for (byte aByte : bytes) {
            hash = hash * seed + aByte;
        }
        hash = hash & 0x7FFFFFFF;
        return String.format("%x", hash) + name.substring(name.length() - 4);
    }
}

