package cn.edu.cqupt.dmb.player.decoder;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.Arrays;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.listener.DmbCurriculumListener;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.listener.DmbTpegListener;
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
public class TpegDecoderImprovement extends Thread {


    /* file size should not be greater than 2M */
    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 2;
    private static final int TPEG_SIZE = 112;
    private static final int DATA_SIZE = 80;
    private static final int TPEG_INFO_SIZE = 3;
    private static final int FIRST_FRAME = 2;
    private static final int MIDDLE_FRAME = 1;
    private static final int LAST_FRAME = 3;
    private static final String TAG = "TpegDecoder";

    /**
     * DMB 数据输入缓冲区
     */
    private final BufferedInputStream bufferedInputStream;
    /**
     * DMB 数据处理监听器
     */
    private final DmbListener listener;
    /**
     * DMB 数据输入流
     */
    private static volatile PipedInputStream pipedInputStream;

    /**
     * 默认的 pip 输入流
     */
    private static final PipedInputStream defaultPipedInputStream = new PipedInputStream(1024 * 2);

    public static PipedInputStream getPipedInputStream() {
        return pipedInputStream;
    }
    static {
        // 初始化的时候先将输入流设置成默认的
        pipedInputStream = defaultPipedInputStream;
    }

    public TpegDecoderImprovement(DmbListener listener) {
        if (listener instanceof DmbTpegListener) {
            // 如果监听器是室外屏的监听器,那输入流就应该设置为室外屏对应的监听器
            pipedInputStream = DataReadWriteUtil.getTpegPipedInputStream();
        } else if (listener instanceof DmbCurriculumListener) {
            // 课表监听器
            pipedInputStream = DataReadWriteUtil.getCurriculumPipedInputStream();
        } else {
            // 宿舍监听器对应的输入流
            pipedInputStream = DataReadWriteUtil.getDormitoryPipedInputStream();
        }

        bufferedInputStream = new BufferedInputStream(pipedInputStream);
        this.listener = listener;
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
        Log.e(TAG, "tpeg decoder start");
        NativeMethod.tpegInit();
        while (!this.isInterrupted()) {
            if (!DataReadWriteUtil.USB_READY) {
                // 如果当前 USB 没有就绪,就直接结束当前线程
                return;
            }
            if (!DataReadWriteUtil.initFlag) {
                // 如果目前还没有接收到 DMB 类型的数据,就直接返回
                continue;
            }
            tpegBuffer[0] = tpegBuffer[1] = tpegBuffer[2] = (byte) 0;
            if (!readTpegFrame(bufferedInputStream, tpegBuffer)) {
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
                    Log.e(TAG, "first frame");
                    isReceiveFirstFrame = true;
                    System.arraycopy(tpegData, 0, fileBuffer, 0, tpegInfo[1]);
                    total = tpegInfo[1] - 35;
                    try {
                        fileName = new String(tpegData, 0, 35, DmbUtil.CHARACTER_SET);
                        fileName = fileName.substring(0, fileName.indexOf(0x00));
                        if (fileName.equals("")) {
                            fileName = "building" + tpegData[20] + ".jpg";
                            Log.e(TAG, "fileName is empty,set fileName " + fileName);
                        }
                        Log.e(TAG, total + " " + fileName);
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
                    Log.e(TAG, "last frame");
                    if (isReceiveFirstFrame && total + tpegInfo[1] < FILE_BUFFER_SIZE) {
                        System.arraycopy(tpegData, 0, fileBuffer, total, tpegInfo[1]);
                        total += tpegInfo[1];
                        if (listener != null) {
                            listener.onSuccess(fileName, fileBuffer, total);
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
        Log.i(TAG, "解码 TPEG 完成!");
    }

    private boolean readTpegFrame(InputStream inputStream, byte[] bytes) {
        int nRead;
        try {
            while ((nRead = inputStream.read(bytes, 3, 1)) > 0) {
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
                if ((nRead = inputStream.read(bytes, pos, nLeft)) <= 0) {
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

    @Override
    public void interrupt() {
        Log.e(TAG, "TPEG 解码器被中断了");
        super.interrupt();
    }
}
