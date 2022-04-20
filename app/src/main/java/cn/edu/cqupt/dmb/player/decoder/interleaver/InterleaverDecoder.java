package cn.edu.cqupt.dmb.player.decoder.interleaver;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : MPEG-TS 解交织器
 * @Date : create by QingSong in 2022-04-19 15:03
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder.interleaver
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class InterleaverDecoder {

    private static final Integer D = 14;
    private static final Integer N = 204;
    private static final byte[][] deinterleaverBuf = new byte[D][N];
    private static int deinterleaverPos;

    public InterleaverDecoder() {
        deinterleaverInit();
    }

    private void deinterleaverInit() {
        deinterleaverPos = D - 1;
    }

    /**
     * 对 MPEG-TS 包解交织
     *
     * @param in  待解码的包
     * @param out 承载解码结果的数组
     */
    public void deinterleaver(byte[] in, byte[] out) {
        int i, j;
        j = deinterleaverPos;
        for (i = 0; i < N; i++) {
            deinterleaverBuf[j][i] = in[i]; /* 右上角斜着放 */
            if (--j < 0) {
                j = D - 1;
            }
        }
        deinterleaverPos = (deinterleaverPos + 1) % D;
        for (i = 0; i < N; i++) {
            out[i] = deinterleaverBuf[deinterleaverPos][i]; /* 横着输出 */
        }
    }
}
