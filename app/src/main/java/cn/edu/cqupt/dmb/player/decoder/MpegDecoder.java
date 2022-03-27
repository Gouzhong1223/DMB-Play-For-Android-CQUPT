package cn.edu.cqupt.dmb.player.decoder;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是 TPEG 的解码器任务
 * @Date : create by QingSong in 2022-03-27 20:05
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class MpegDecoder extends Thread{



    @Override
    public void run() {
        // 初始化 MPEG 的解码器
        NativeMethod.decodeMpegInit();
        while (DataReadWriteUtil.USB_READY) {

        }
    }
}
