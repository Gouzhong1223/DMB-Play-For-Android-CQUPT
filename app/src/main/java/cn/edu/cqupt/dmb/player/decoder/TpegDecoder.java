package cn.edu.cqupt.dmb.player.decoder;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.Arrays;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-18 15:44
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class TpegDecoder extends Thread {

    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 2; /* file size should not be greater than 2M */
    private static final int TPEG_SIZE = 112;
    private static final int DATA_SIZE = 80;
    private static final int TPEG_INFO_SIZE = 3;
    private static final int FIRST_FRAME = 2;
    private static final int MIDDLE_FRAME = 1;
    private static final int LAST_FRAME = 3;
    private static final String TAG = "TpegDecoder";

    private final PipedInputStream pipedInputStream;
    private final BufferedInputStream inputStream;
    private final Listener listener;
    private boolean isStop;


    public TpegDecoder(Listener listener) {
        pipedInputStream = new PipedInputStream(1024 * 2);
        inputStream = new BufferedInputStream(pipedInputStream);
        this.listener = listener;
    }

    private boolean readTpegFrame(InputStream inputStream, byte[] bytes) {
        int nRead = 0;
        try {
            while ((nRead = inputStream.read(bytes, 3, 1)) > 0) {
                if (bytes[1] == (byte) 0x01 && bytes[2] == (byte) 0x5b && bytes[3] == (byte) 0xF4) {
                    break;
                }
                for (int i = 0; i < 3; i++) {
                    bytes[i] = bytes[i + 1];
                }
            }
            if (nRead <= 0) {
                return false;
            }
            /* read n bytes method, according to unix network programming page 72 */
            int nLeft = 108; /* read left data of the frame */
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


    public void stopDecode() {
        isStop = true;
    }

    @Override
    public void run() {
        isStop = false;
        int length = 0;
        int total = 0;
        byte[] tpegBuffer = new byte[TPEG_SIZE];
        byte[] tpegData = new byte[DATA_SIZE];
        int[] tpegInfo = new int[TPEG_INFO_SIZE];
        byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];
        byte[] temp = new byte[FILE_BUFFER_SIZE];
        boolean isReceiveFirstFrame = false;
        String fileName = null;
        Log.e(TAG, "tpeg decoder start");
        NativeMethod.tpegInit();
        while (!isStop) {
            tpegBuffer[0] = tpegBuffer[1] = tpegBuffer[2] = (byte) 0;
            if (!readTpegFrame(inputStream, tpegBuffer)) {
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
//                    Log.e(TAG,Arrays.toString(tpegData));
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
//                        Log.e(TAG,"total = "+total);
//                        if(fileName != null && !mReceivedFile.contains(Utils.CACHE_DIRECTORY + fileName)){
//                            Utils.writeFile(fileBuffer,0,total,Utils.CACHE_DIRECTORY + fileName);
//                            mReceivedFile.add(Utils.CACHE_DIRECTORY + fileName);
//                        }
                        if (listener != null) {
                            listener.onSuccess(fileName, fileBuffer, total);
                        }
                        isReceiveFirstFrame = false;
                        fileName = null;
                    }
                    break;
                default:
                    break;
            }
        }
        Log.e(TAG, "tpeg decoder end");
    }


    public PipedInputStream getPipedInputStream() {
        return pipedInputStream;
    }

    public interface Listener {
        void onSuccess(String fileName, byte[] bytes, int length);
    }
}
