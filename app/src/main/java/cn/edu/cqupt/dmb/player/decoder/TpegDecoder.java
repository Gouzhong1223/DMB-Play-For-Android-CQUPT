package cn.edu.cqupt.dmb.player.decoder;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.Arrays;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 稍微修改了一下的 TpegDecoder
 * @Date : create by QingSong in 2022-03-25 14:10
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class TpegDecoder extends AbstractDmbDecoder {


    /* file size should not be greater than 2M */
    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 2;
    private static final int TPEG_SIZE = 112;
    private static final int DATA_SIZE = 80;
    private static final int TPEG_INFO_SIZE = 3;
    private static final int FIRST_FRAME = 2;
    private static final int MIDDLE_FRAME = 1;
    private static final int LAST_FRAME = 3;
    private static final String TAG = "TpegDecoder";

    public TpegDecoder(DmbListener listener) {
        super(listener);
    }

    @Override
    public void run() {
        int total = 0;
        byte[] tpegBuffer = new byte[TPEG_SIZE];
        byte[] tpegData = new byte[DATA_SIZE];
        int[] tpegInfo = new int[TPEG_INFO_SIZE];
        byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];
        boolean isReceiveFirstFrame = false;
        String fileName = null;
        Log.i(TAG, Thread.currentThread().getName() + "线程开始了 TPEG 的解码");
        NativeMethod.tpegInit();
        while (true) {
            if (!DataReadWriteUtil.USB_READY) {
                // 如果当前 USB 没有就绪,就直接结束当前线程
                // Log.e(TAG, "现在 USB 还没有就绪!");
                return;
            }
            if (!DataReadWriteUtil.initFlag) {
                // 如果目前还没有接收到 DMB 类型的数据,就直接返回
                // Log.e(TAG, "现在还没有接收到 DMB 类型的数据!");
                continue;
            }
            tpegBuffer[0] = tpegBuffer[1] = tpegBuffer[2] = (byte) 0;
            if (!readTpegFrame(tpegBuffer)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Arrays.fill(tpegData, (byte) 0);
            Arrays.fill(tpegInfo, 0);
            NativeMethod.decodeTpegFrame(tpegBuffer, tpegData, tpegInfo);
            switch (tpegInfo[0]) {
                case FIRST_FRAME:
                    Log.i(TAG, "现在接收到了头帧");
                    isReceiveFirstFrame = true;
                    System.arraycopy(tpegData, 0, fileBuffer, 0, tpegInfo[1]);
                    total = tpegInfo[1] - 35;
                    try {
                        fileName = new String(tpegData, 0, 35, DmbUtil.CHARACTER_SET);
                        fileName = fileName.substring(0, fileName.indexOf(0x00));
                        if (fileName.equals("")) {
                            fileName = "教学楼-" + tpegData[20] + ".jpg";
                            Log.i(TAG, "没有从 TPEG 信息中解码出文件名,所以重命名为:" + fileName);
                        }
                        Log.i(TAG, total + " " + fileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MIDDLE_FRAME:
                    if (total + tpegInfo[1] >= FILE_BUFFER_SIZE) {
                        total = 0;
                    } else {
                        System.arraycopy(tpegData, 0, fileBuffer, total, tpegInfo[1]);
                        total += tpegInfo[1];
                    }
                    break;
                case LAST_FRAME:
                    Log.i(TAG, "现在接收到" + fileName + "的尾帧");
                    if (isReceiveFirstFrame && total + tpegInfo[1] < FILE_BUFFER_SIZE) {
                        System.arraycopy(tpegData, 0, fileBuffer, total, tpegInfo[1]);
                        total += tpegInfo[1];
                        if (dmbListener != null) {
                            dmbListener.onSuccess(fileName, fileBuffer, total);
                        }
                        isReceiveFirstFrame = false;
                        fileName = null;
                    }
                    break;
                default:
                    Log.e(TAG, "未知的 TPEG 类型");
                    break;
            }
        }
    }

    private boolean readTpegFrame(byte[] bytes) {
        int nRead;
        try {
            while ((nRead = ((InputStream) TpegDecoder.bufferedInputStream).read(bytes, 3, 1)) > 0) {
                if (bytes[1] == (byte) 0x01 && bytes[2] == (byte) 0x5b && bytes[3] == (byte) 0xF4) {
                    break;
                }
                System.arraycopy(bytes, 1, bytes, 0, 3);
            }
            if (nRead <= 0) {
                return false;
            }
            /* read n bytes method, according to unix network programming page 72 */
            /* read left data of the frame */
            int nLeft = 108;
            int pos = 4;
            while (nLeft > 0) {
                if ((nRead = ((InputStream) TpegDecoder.bufferedInputStream).read(bytes, pos, nLeft)) <= 0) {
                    return false;
                }
                nLeft -= nRead;
                pos += nRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static PipedInputStream getPipedInputStream() {
        Log.i(TAG, "有线程正在获取TPEG的pipedInputStream");
        return pipedInputStream;
    }
}
