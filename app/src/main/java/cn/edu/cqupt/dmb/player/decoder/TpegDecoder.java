package cn.edu.cqupt.dmb.player.decoder;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.processor.tpeg.TpegDataProcessor;
import cn.edu.cqupt.dmb.player.processor.tpeg.TpegDataProcessorFactory;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

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
public class TpegDecoder extends BaseDmbDecoder {


    /* file size should not be greater than 2M */
    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 10;
    /**
     * 单个 TPEG 数据包的长度
     */
    private static final int TPEG_SIZE = 112;
    /**
     * 单个 TPEG 数据包中有效的数据长度
     */
    private static final int DATA_SIZE = 80;
    /**
     * TPEG 信息数组长度
     */
    private static final int TPEG_INFO_SIZE = 3;
    private static final String TAG = "TpegDecoder";
    /**
     * 装载接收到的 TPEG 数据帧
     */
    private final byte[] tpegBuffer = new byte[TPEG_SIZE];
    /**
     * 装载已经解码的 TPEG 数据帧
     */
    private final byte[] tpegData = new byte[DATA_SIZE];
    /**
     * 装载 TPEG 数据包信息
     */
    private final int[] tpegInfo = new int[TPEG_INFO_SIZE];
    /**
     * 装载所有的已经解码的 TPEG 数据
     */
    private final byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];
    /**
     * [备用]装载所有的已经解码的 TPEG 数据
     */
    private final byte[] alternativeBytes = new byte[FILE_BUFFER_SIZE];
    /**
     * 一个fileBuffer中有效的 TPEG 数据包长度
     */
    private int total = 0;
    /**
     * [备用]一个fileBuffer中有效的 TPEG 数据包长度
     */
    private int alternativeTotal = 0;
    /**
     * 是否接收到了第一帧
     */
    private boolean isReceiveFirstFrame = false;
    /**
     * 文件名
     */
    private String fileName = null;

    public TpegDecoder(DmbListener listener, Context context, BufferedInputStream bufferedInputStream) {
        super(bufferedInputStream, listener, context);
    }

    @Override
    public void run() {
        Log.i(TAG, Thread.currentThread().getName() + "线程开始了 TPEG 的解码");
        NativeMethod.tpegInit();
        while (!DataReadWriteUtil.inMainActivity) {
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
            TpegDataProcessor dataProcessor = TpegDataProcessorFactory.getDataProcessor(tpegInfo[0]);
            dataProcessor.processData(this, tpegData, fileBuffer, tpegInfo, alternativeBytes);
        }
        Arrays.fill(tpegBuffer, (byte) 0);
        Arrays.fill(tpegData, (byte) 0);
        Arrays.fill(tpegInfo, (byte) 0);
        Arrays.fill(fileBuffer, (byte) 0);
        Arrays.fill(alternativeBytes, (byte) 0);
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isReceiveFirstFrame() {
        return isReceiveFirstFrame;
    }

    public void setReceiveFirstFrame(boolean receiveFirstFrame) {
        isReceiveFirstFrame = receiveFirstFrame;
    }

    public int getAlternativeTotal() {
        return alternativeTotal;
    }

    public void setAlternativeTotal(int alternativeTotal) {
        this.alternativeTotal = alternativeTotal;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DmbListener getDmbListener() {
        return dmbListener;
    }

    /**
     * 从 PIP 输入流中读取一个 TPEG 数据包
     *
     * @param bytes 承载 TPEG 数据包的容器
     * @return true->读取成功
     */
    private boolean readTpegFrame(byte[] bytes) {
        int nRead;
        try {
            // 寻找 TPEG 数据包同步字节
            if (DataReadWriteUtil.inMainActivity) {
                return false;
            }
            while ((nRead = bufferedInputStream.read(bytes, 3, 1)) > 0) {
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
                // 寻找 TPEG 数据包同步字节
                if (DataReadWriteUtil.inMainActivity) {
                    return false;
                }
                if ((nRead = bufferedInputStream.read(bytes, pos, nLeft)) <= 0) {
                    return false;
                }
                nLeft -= nRead;
                pos += nRead;
            }
        } catch (IOException e) {
            Log.e(TAG, "readTpegFrame: 缓冲流被关闭了");
        }
        return true;
    }
}
