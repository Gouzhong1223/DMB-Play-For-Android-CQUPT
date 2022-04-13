package cn.edu.cqupt.dmb.player.jni;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是定义 Native 方法的类
 * @Date : create by QingSong in 2022-03-18 13:55
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.jni
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class NativeMethod {

    static {
        System.loadLibrary("native-lib");
    }

    /**
     * 初始化 Mp2 解码器
     */
    public static native void mp2DecoderInit();

    /**
     * 对一个 MP2 帧进行解码
     *
     * @param in   需要译码的数组
     * @param len  译码长度
     * @param out  译码完成的数组
     * @param info 消息类型
     * @return 译码长度
     */
    public static native int decodeMp2Frame(byte[] in, int len, byte[] out, int[] info);


    /**
     * TPEG 解码器初始化
     */
    public static native void tpegInit();


    /**
     * 对一个tdc进行译码，译码的结果存储在out中
     * info[0] 类型 1 中间帧 2 头帧 3 尾帧
     * info[1] 消息长度
     *
     * @param in   需要译码的数组
     * @param out  译码完成额数组
     * @param info 用于存储消息类型
     */
    public static native void decodeTpegFrame(byte[] in, byte[] out, int[] info);

    /**
     * 解码一个加密的MPEG-TS包
     *
     * @param tsBuf_204 原始流
     * @param tsBuf_188 解码流
     * @return 解码状态码
     */
    public static native int decodeMpegTsFrame(byte[] tsBuf_204, byte[] tsBuf_188);
}
