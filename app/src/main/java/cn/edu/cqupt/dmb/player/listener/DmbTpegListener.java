package cn.edu.cqupt.dmb.player.listener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.banner.bean.BannerImageBitmapCache;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-22 21:24
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbTpegListener implements DmbListener {

    private static final String TAG = "DmbTpegListener";
    private final byte[] fileBuffer = new byte[1024 * 1024 * 2];

    private final ArrayList<String> mReceivedFile = new ArrayList<>();
    private final int id = MainActivity.id;

    @Override

    public void onSuccess(String fileName, byte[] bytes, int length) {
        // 生成轮播图的文件名
        fileName = DmbUtil.CACHE_DIRECTORY + fileName;
        System.arraycopy(bytes, 0, fileBuffer, 0, length);
        // 根据数据源生生成 bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuffer, 0, length);
        // 构造 bannerBitmapDataBean
        BannerBitmapDataBean bannerBitmapDataBean = new BannerBitmapDataBean(bitmap, fileName, 1);
        if (bitmap != null) {
            // 添加到有界队列中
            BannerImageBitmapCache.putBitMap(bannerBitmapDataBean);
        } else {
            Log.e(TAG, "生成 bitmap 错误啦!");
        }
    }

    @Override
    public void onReceiveMessage(String msg) {
        //检查文件，如果图片或视频，就加入到播放列表，如果是命令文件，就执行命令文件
        Log.e(TAG, msg);
        if (msg.endsWith("jpg") || msg.endsWith("mp4")) {
            if (!mReceivedFile.contains(msg)) {
                mReceivedFile.add(msg);
            }
        } else if (msg.endsWith("txt")) {
            decodeCommand(msg, id);
        }
    }

    public ArrayList<String> decodeCommand(String path, int id) {
        boolean isSelected = false;
        ArrayList<String> result = new ArrayList<>();
        File cmdFile = new File(path);
        Log.e(TAG, path);
        try {
            FileInputStream input = new FileInputStream(cmdFile);
            Scanner scanner = new Scanner(input, "gb2312");
            String string;
            try {
                while (scanner.hasNextLine()) {
                    string = scanner.nextLine();
                    String[] cmd = string.split(" ");
                    if (cmd[0].equals("id")) {
                        isSelected = false;
                        for (int i = 1; i < cmd.length; i++) {
                            String[] ids = cmd[i].split("-");
                            int bottom = Integer.parseInt(ids[0]);
                            if (ids.length == 1 && id == bottom) {
                                isSelected = true;
                                break;
                            }
                            if (ids.length == 2) {
                                int top = Integer.parseInt(ids[1]);
                                if (bottom <= id && id <= top) {
                                    isSelected = true;
                                    break;
                                }
                            }
                        }
                        continue;
                    }
                    if (!isSelected) {
                        continue;
                    }
                    if (cmd[0].equals("add")) {
                        try {
                            String str = DmbUtil.CACHE_DIRECTORY + DmbUtil.hashCode(cmd[1]);
                            File file = new File(str);
                            if (file.exists()) {
                                result.add(str);
                            }
                            Log.e(TAG, str + cmd[1]);
                        } catch (Exception e) {
                            Log.e(TAG, "open file failed");
                        }
                    }
                    if (cmd[0].equals("delete")) {
                        String fileName = DmbUtil.CACHE_DIRECTORY + DmbUtil.hashCode(cmd[1]);
                        File file = new File(fileName);
                        if (file.exists()) {
                            file.delete();
                        }
                        if (result.contains(fileName)) {
                            result.remove(fileName);
                        }
                    }
                    //todo 支持定时播放
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                input.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "open text file failed");
        }
        return result;
    }
}
