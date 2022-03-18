package cn.edu.cqupt.dmb.player.jni;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-18 13:55
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.jni
 * @ProjectName : Android TV Tset-1
 * @Version : 1.0.0
 */
public class NativeMethod {

    static {
        System.loadLibrary("native-lib");
    }

    public static native void mp2DecoderInit();

    public static native int decodeMp2Frame(byte[] in, int len, byte[] out, int[] info);

    public static native void tpegInit();

    public static native void decodeTpegFrame(byte[] in, byte[] out, int[] info);
}
