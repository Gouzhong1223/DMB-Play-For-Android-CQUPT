package cn.edu.cqupt.dmb.player.utils;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是解码TDC数据的工具类
 * @Date : create by QingSong in 2022-03-14 14:25
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class TdcDecodeUtil {


    /**
     * TDC数据解码成JPEG数据
     * 首先,一个完成的TDC数据包是由以下数据结构组成
     * 00/04/08	01 5B F4 file_crc_highBytes(2) SegNo15位以上	SegNo低15位 00 SegSize(2) data(80) file_crc_lowBytes(2) packet_crc(2)
     *
     * @param jpegData JPEG数据数据
     * @param tdcData  TDC数
     */
    public void decodeTdc2Jpeg(char[] jpegData, char[] tdcData) {

    }


    /**
     * 对tpeg数据包做CRC校验
     *
     * @param pData tpeg数据包
     * @param len   数据长度
     * @return CRC返回值
     */
    public short tpegPacketCrc(char[] pData, int len) {

        int in_data, xc, mask;
        int i, j;

        xc = 0xffff;
        for (i = 0; i < len; i++) {
            in_data = pData[i] << 8;
            for (j = 0; j < 8; j++) {
                mask = (in_data ^ xc) & 0x8000;
                mask = (mask >> 3) + (mask >> 10) + (mask >> 15);
                xc = (xc << 1) ^ mask;
                in_data = in_data << 1;
            }
        }
        return (short) ((xc ^ 0xffff) & 0xffff);
    }
}
