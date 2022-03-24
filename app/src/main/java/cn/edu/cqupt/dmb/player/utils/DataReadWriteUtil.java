package cn.edu.cqupt.dmb.player.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 读取数据的工具类
 * @Date : create by QingSong in 2022-03-16 15:18
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DataReadWriteUtil {

    private static volatile PipedOutputStream pipedOutputStream;
    private static volatile PipedInputStream pipedInputStream;
    private static volatile BufferedInputStream bufferedInputStream;

    public static volatile boolean initFlag = false;

    static {
        pipedOutputStream = new PipedOutputStream();
        pipedInputStream = new PipedInputStream(1024 * 2);
        bufferedInputStream = new BufferedInputStream(pipedInputStream);
        try {
            pipedOutputStream.connect(pipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从输入流中读取固定长度的数据
     *
     * @param inputStream 输入流
     * @param bytes       接收数组
     * @return 成功返回true, 失败返回false
     */
    public static boolean readTpegFrame(InputStream inputStream, byte[] bytes) {
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
            /* 读取固定长度的字节 */
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
            return false;
        }
        return true;
    }

    public static boolean readTpegFrame(byte[] bytes) {
        return readTpegFrame(bufferedInputStream, bytes);
    }

    public static PipedOutputStream getPipedOutputStream() {
        return pipedOutputStream;
    }


    public static PipedInputStream getPipedInputStream() {
        return pipedInputStream;
    }

}
