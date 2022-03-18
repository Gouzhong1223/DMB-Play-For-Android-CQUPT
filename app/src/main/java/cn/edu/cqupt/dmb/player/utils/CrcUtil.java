package cn.edu.cqupt.dmb.player.utils;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是一个 CRC 校验的工具类
 * @Date : create by QingSong in 2022-03-14 15:22
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.utils
 * @ProjectName : Android TV Tset-1
 * @Version : 1.0.0
 */
public class CrcUtil {

    private static final short[] CRC16TAB = new short[256];

    static {
        initCcr16Tab();
    }

    /**
     * 校验数据的CRC32值
     *
     * @param data   数据数组
     * @param offset 偏移量
     * @param length 校验长度
     * @return CRC32值
     */
    public static int crc32(byte[] data, int offset, int length) {
        byte i;
        // Initial value
        int crc = 0xffffffff;
        length += offset;
        for (int j = offset; j < length; j++) {
            crc ^= data[j];
            for (i = 0; i < 8; ++i) {
                if ((crc & 1) != 0)
                    // 0xEDB88320= reverse 0x04C11DB7
                    crc = (crc >> 1) ^ 0xEDB88320;
                else
                    crc = (crc >> 1);
            }
        }
        return ~crc;
    }


    /**
     * CRC32/MPEG-2 实现
     *
     * @param data   数据数组
     * @param offset 起始偏移量
     * @param length 校验长度
     * @return CRC32值
     */
    public static int crc32_mpeg_2(byte[] data, int offset, int length) {
        byte i;
        // Initial value
        int crc = 0xffffffff;
        length += offset;
        for (int j = offset; j < length; j++) {
            crc ^= data[j] << 24;
            for (i = 0; i < 8; ++i) {
                if ((crc & 0x80000000) != 0)
                    crc = (crc << 1) ^ 0x04C11DB7;
                else
                    crc <<= 1;
            }
        }
        return crc;
    }


    /**
     * CRC 16 校验
     *
     * @param bytes  需要校验的数组
     * @param offset 起始偏移量
     * @param length 校验长度
     * @return short数组
     */
    private short crc16(byte[] bytes, int offset, int length) {
        short data;
        short crc = (short) 0xFFFF;
        for (int i = offset; i < offset + length; i++) {
            data = (short) bytes[i];
            crc = (short) ((short) (crc << 8) ^ (short) (CRC16TAB[((crc >>> 8) ^ data) & 0x00FF]));
        }
        crc = (short) (crc & 0xFFFF);
        crc = (short) (crc ^ 0xFFFF);
        return crc;
    }


    /**
     * init crc 16 table
     */
    private static void initCcr16Tab() {
        // polynomial x16 + x12 + x5 + 1 Recommendation ITU-T X.25
        short CRC_POLY_16 = (short) 0x1021;
        short i, j, crc;
        for (i = 0; i < 256; i++) {
            crc = (short) (i << 8);
            for (j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = (short) ((crc << 1) ^ CRC_POLY_16);
                } else {
                    crc = (short) (crc << 1);
                }
            }
            CRC16TAB[i] = crc;
        }
    }

}
