package cn.edu.cqupt.dmb.player.decoder;


import java.io.UnsupportedEncodingException;

import cn.edu.cqupt.dmb.player.domain.ChannelInfo;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-17 20:31
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.utils
 * @ProjectName : Android TV Tset-1
 * @Version : 1.0.0
 */
public class FicDecoder {

    private static final String TAG = "FicDecoder";
    private static final int CHANNEL_SIZE = 64;
    private static final short[] CRC16TAB = new short[256];


    /* decoder config */
    private final int mId;
    private final boolean mIsEncrypted;

    /* fib information */
    private final byte[] mFib;
    private int mFigHeader;
    private int mFigLength;

    /* decode data buffer */
    private final byte[] mFicData = new byte[200];
    private int mFicCh = 0xFF;
    private int mFicDataLen;
    private final ChannelInfo[] channelInfos;
    public int mYear, mMonth, mDay, mHour, mMinute, mSecond;


    public FicDecoder(int id, boolean isEncrypted) {
        mId = id;
        mIsEncrypted = isEncrypted;
        mFib = new byte[32];
        channelInfos = new ChannelInfo[CHANNEL_SIZE];
        for (int i = 0; i < CHANNEL_SIZE; i++) {
            channelInfos[i] = new ChannelInfo();
            channelInfos[i].setSubCh(i + 1);
        }
        initCrc16Tab();
    }

    /**
     * init crc 16 table
     */
    private void initCrc16Tab() {
        /* polynomial x^16 + x^12 + x^5 + 1 Recommendation ITU-T X.25*/
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

    /**
     * calculate crc 16 value
     */
    private short crc16(byte[] bytes, int length) {
        short data;
        short crc = (short) 0xFFFF;
        for (int i = 0; i < length; i++) {
            data = (short) bytes[i];
            crc = (short) ((short) (crc << 8) ^ (short) (CRC16TAB[((crc >>> 8) ^ data) & 0x00FF]));
        }
        crc = (short) (crc & 0xFFFF);
        crc = (short) (crc ^ 0xFFFF);
        return crc;
    }

    /**
     * decode a complete frame, 32byte fic data
     */
    public void decode(byte[] bytes) {
        System.arraycopy(bytes, 0, mFib, 0, 32);
        /* crc check */
        short dataCrc = crc16(mFib, 30);
        short preferenceCrc = (short) ((mFib[30] << 8) | mFib[31] & 0x00ff);
        if (dataCrc != preferenceCrc) { /* crc check fail frequently */
            return;
        }
        /* if the data is encrypted, get the original data */
        if (mIsEncrypted) {
            for (int i = 0; i < 30; i++) {
                mFib[i] = (byte) (mFib[i] ^ DAB_ENCRYPT_CODE[i]);
            }
        }
        /* find a FIG(fast information group), and decode it according to it's type */
        mFigHeader = 0;
        while (mFigHeader < 30 && mFib[mFigHeader] != (byte) 0xFF) { /* check end mark */
            int mFigType = (mFib[mFigHeader] >>> 5) & 0x07;
            mFigLength = mFib[mFigHeader] & 0x1f;
            switch (mFigType) {
                case 0: /* standard fib type 0 */
                    fibType0();
                    break;
                case 1: /* standard fib type 1 */
                    decodeLabel();
                    break;
                case 3:/*  custom types, id select check */
                    selectSubCh();
                    break;
                case 5:
                    decodeFicData();
                    break;
                default:
                    break;
            }
            mFigHeader += mFigLength + 1; /* point to next fig header */
        }
    }

    private void selectSubCh() {
        int index = mFigHeader + 1;
        int groupId = (mFib[index] & 0xE0) >>> 5;
        int groupFlag = mFib[index + 1] & 0x03;
        int subChId = mFib[index + 1] >>> 2;
        int groupIndex = (mId - 1) / 200;
        int byteIndex = ((mId - 1) % 200) / 8;
        int bitIndex = ((mId - 1) % 200) & 0x07;
        byte bitMask = (byte) (((byte) 0x80) >>> bitIndex);
        if (index + 2 + byteIndex > 31) {
            return;
        }
        if (groupId == groupIndex && (mFib[index + 2 + byteIndex] & bitMask) != 0) {
            if (groupFlag == 0x01 && channelInfos[subChId].getLabel() != null
                    && channelInfos[subChId].getSubChOrganization()[6] != 0) { /* msc id */
                channelInfos[subChId].setSelect(true);
            }
            if (groupFlag == 0x02 && mFicCh > subChId) { /* sub id */
                mFicCh = subChId;
            }
        }
    }

    public ChannelInfo getSelectChannelInfo() {
        for (int i = 0; i < CHANNEL_SIZE; i++) {
            if (channelInfos[i].isSelect()) {
                if (channelInfos[i].getSubChOrganization()[6] > 0) {
                    return channelInfos[i];
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private void decodeFicData() {
        int index = mFigHeader + 1;
        int ficId = (mFib[index] >>> 5) & 0x07;
        int exNum = mFib[index] & 0x1F;
        if (ficId != mFicCh || exNum != 2) {
            return;
        }
        /* decode if receiver id equals fic id */
        int segNo = (mFib[index + 2] >>> 5) & 0x07;
        int len = mFib[index + 2] & 0x1F;
        int total = mFib[index + 3] & 0xFF;
        if (len <= 2) {
            return;
        }
        for (int i = 0; i < len; i++) {
            mFicData[segNo * 25 + i] = mFib[index + 4 + i];
        }
        mFicDataLen = total;
    }

    private void decodeLabel() {
        int index = mFigHeader + 1;
        int oe = mFib[index] & 0x08; /* char set is same */
        int extensionType = mFib[index] & 0x07;
        int serviceId;
        if (oe == 0) { /* just decode current ensemble */
            if (extensionType == 1) { /* short format */
                serviceId = (((int) mFib[index + 1]) << 8) + mFib[index + 2];
                index += 3;
            } else { /* long format */
                if (index + 4 >= 32)
                    return;
                serviceId = (((int) mFib[index + 1]) << 24) + (((int) mFib[index + 2]) << 16) +
                        (((int) mFib[index + 3]) << 8) + (((int) mFib[index + 4]));
                index += 5;
            }
            for (int i = 0; i < CHANNEL_SIZE; i++) {
                if (channelInfos[i].getServiceId() == serviceId) {
                    try {
                        String s = new String(mFib, index, 16, "gb2312");
                        /* delete padding zero in the last of the string */
                        int j;
                        for (j = s.length() - 1; j >= 0; j--) {
                            if (s.charAt(j) != 0x0) {
                                break;
                            }
                        }
                        s = s.substring(0, j + 1);
                        // Log.e(TAG, "serviceId = " + serviceId + "  label = " + s);
                        channelInfos[i].setLabel(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void fibType0() {
        int header = ((mFib[mFigHeader + 1] >>> 5) & 0x0007); /* C/N, OE, P/D, standard page 25 */
        int extension = (mFib[mFigHeader + 1] & 0x001F);
        switch (header) {
            case 0:
            case 1:
            case 4:
                switch (extension) {
                    case 1: /* sub-channel organization */
                        decodeSubChannel();
                        break;
                    case 2: /* service organization */
                        decodeServiceId();
                        break;
                    case 9: /* Country, LTO & International table */
                        decodeLto();
                        break;
                    case 10: /* date & time */
                        decodeTime();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /* decode service id according to standard page 45 */
    private void decodeServiceId() {
        int header = ((mFib[mFigHeader + 1] >>> 5) & 0x0001);
        int index = mFigHeader + 2;
        int number, serviceId;
        while (index < mFigHeader + mFigLength + 1) {
            number = mFib[index + 2 + header * 2] & 0x0F;
            if (header == 0) { /* 16bit */
                serviceId = ((int) mFib[index] << 8) + mFib[index + 1];
            } else { /* 32bit */
                serviceId = ((int) mFib[index] << 24) + ((int) mFib[index + 1] << 16)
                        + ((int) mFib[index + 2] << 8) + ((int) mFib[index + 3]);
            }
            index += header * 2 + 3;
            for (int i = 0; i < number; i += 2) {
                int type = ((mFib[index + i] & 0xc0) >>> 6);
                int subChId = ((mFib[index + i + 1] >>> 2) & 0x3F);
                channelInfos[subChId].setServiceId(serviceId);
                channelInfos[subChId].setType(type);
//                Log.e(TAG,type+" subChId "+ subChId + " service Id "+ serviceId);
            }
            index += number * 2;
        }
    }

    /* standard page 41 */
    private void decodeSubChannel() {
        int index = mFigHeader + 2;
        int form;
        int subChId;
        int temp;
        for (int i = 0; i < mFigLength - 1; i += form) { /* length has a header */
            form = (mFib[index + 2 + i] & 0x80) == 0 ? 3 : 4;
            subChId = (int) ((mFib[index + i] >>> 2) & 0x003f);
            channelInfos[subChId].subChOrganization[0] = ((mFib[index + i] & 0x0ff) << 8);
            channelInfos[subChId].subChOrganization[0] += mFib[index + i + 1] & 0x0ff;
            channelInfos[subChId].subChOrganization[0] &= 0x03FF;/* start address */
            if (form == 3) { /* short form */
                int tableIndex = mFib[index + i + 2] & 0x3f;
                for (int j = 0; j < 6; j++) {
                    channelInfos[subChId].subChOrganization[j + 1] = UEP_TABLE[tableIndex][j];
                }
            } else { /* long form */
                channelInfos[subChId].subChOrganization[4] = 0;
                channelInfos[subChId].subChOrganization[5] = 0;
                /* Sub-channel size */
                channelInfos[subChId].subChOrganization[1] = (int) ((mFib[index + 2 + i] & 0x03) << 8 | mFib[index + 3 + i] & 0x0FF);
                int options = mFib[index + 2 + i] & 0x7c;
                switch (options) {
                    case 0x0: /*  protection level 1-A */
                        temp = (channelInfos[subChId].subChOrganization[1]) / 12;
                        channelInfos[subChId].subChOrganization[2] = 192 * temp - 72;
                        channelInfos[subChId].subChOrganization[3] = 119;
                        channelInfos[subChId].subChOrganization[6] = 8 * temp;
                        break;
                    case 0x04: /*  protection level 2-A */
                        temp = (channelInfos[subChId].subChOrganization[1]) / 8;
                        if (temp == 1) {
                            channelInfos[subChId].subChOrganization[2] = 173;
                            channelInfos[subChId].subChOrganization[3] = 44;
                            channelInfos[subChId].subChOrganization[6] = 8;
                        } else {
                            channelInfos[subChId].subChOrganization[2] = 64 * temp - 82;
                            channelInfos[subChId].subChOrganization[3] = 128 * temp + 109;
                            channelInfos[subChId].subChOrganization[6] = 8 * temp;
                        }
                        break;
                    case 0x08: /*  protection level 3-A */
                        temp = (channelInfos[subChId].subChOrganization[1]) / 6;
                        channelInfos[subChId].subChOrganization[2] = 192 * temp - 88;
                        channelInfos[subChId].subChOrganization[3] = 103;
                        channelInfos[subChId].subChOrganization[6] = 8 * temp;
                        break;
                    case 0x0C: /*  protection level 4-A */
                        temp = (channelInfos[subChId].subChOrganization[1]) / 4;
                        channelInfos[subChId].subChOrganization[2] = 128 * temp - 93;
                        channelInfos[subChId].subChOrganization[3] = 64 * temp + 98;
                        channelInfos[subChId].subChOrganization[6] = 8 * temp;
                        break;
                    case 0x10: /*  protection level 1-B */
                        temp = (channelInfos[subChId].subChOrganization[1]) / 27;
                        channelInfos[subChId].subChOrganization[2] = 768 * temp - 86;
                        channelInfos[subChId].subChOrganization[3] = 105;
                        channelInfos[subChId].subChOrganization[6] = 32 * temp;
                        break;
                    case 0x14: /*  protection level 2-B */
                        temp = (channelInfos[subChId].subChOrganization[1]) / 21;
                        channelInfos[subChId].subChOrganization[2] = 768 * temp - 90;
                        channelInfos[subChId].subChOrganization[3] = 101;
                        channelInfos[subChId].subChOrganization[6] = 32 * temp;
                        break;
                    case 0x18: /*  protection level 3-B */
                        temp = (channelInfos[subChId].subChOrganization[1]) / 18;
                        channelInfos[subChId].subChOrganization[2] = 768 * temp - 92;
                        channelInfos[subChId].subChOrganization[3] = 99;
                        channelInfos[subChId].subChOrganization[6] = 32 * temp;
                        break;
                    case 0x1C: /*  protection level 4-B */
                        temp = (channelInfos[subChId].subChOrganization[1]) / 15;
                        channelInfos[subChId].subChOrganization[2] = 768 * temp - 94;
                        channelInfos[subChId].subChOrganization[3] = 97;
                        channelInfos[subChId].subChOrganization[6] = 32 * temp;
                        break;
                    default:
                        break;
                }
//                Log.e(TAG,mChannelInfos[subChId].toString());
            }
        }
    }

    /* decode local time offset, according to standard page 69 */
    private void decodeLto() {
        int lto = mFib[mFigHeader + 2] & 0x3F;
        if (lto == 0 || mHour + mMinute + mSecond == 0) {
            return;
        }
        int negative = (lto >>> 5) & 0x1;
        int hour = (lto >>> 1) & 0x0F;
        int minute = (lto & 0x01) * 30;
        if (negative == 1) {
            mHour -= hour;
            mMinute -= minute;
        } else {
            mHour += hour;
            mMinute += minute;
        }
        if (mMinute < 0) {
            mMinute = 60 - mMinute;
            mHour--;
        }
        if (mMinute > 60) {
            mMinute -= 60;
            mHour++;
        }
        if (mHour > 24) {
            mHour -= 24;
        }
        if (mHour < 0) {
            mHour = 23;
        }
    }

    /**
     * decode date and time, according to standard page 68
     */
    private void decodeTime() {
        int index = mFigHeader + 2; /* change index to real data */

        /* get mjd and decode it */
        long mjd = 0; /* mjd, 1-16byte */
        mjd = (mjd + ((int) mFib[index] & 0x7f)) << 8; /* 1-7bit */
        mjd = (mjd + ((int) mFib[index + 1] & 0x0FF)) << 2; /* 8-15bit */
        mjd = mjd + ((((int) mFib[index + 2] & 0x0FF) >>> 6) & 0x03); /* 16-17bit */

        /* mjd format change to normal format */
        long year, month, day, i, j, k;
        year = (mjd * 20 - 301564) / 7305;
        month = (mjd * 10 - 149561 - (year * 365 + (year >>> 2)) * 10) * 1000 / 306001;
        i = year * 365 + (year >>> 2);
        j = (month * 306001) / 10000;
        day = mjd - 14956 - i - j;
        k = (month == 14 || month == 15) ? 1 : 0;
        year = year + k;
        month = month - 1 - k * 12;
        mYear = (int) year + 1900;
        mMonth = (int) month;
        mDay = (int) day;

        /* get utc and decode it, it is simple,21bit flag,
        then fallow 5bit hour,6bit minute, 6bit second,10bit millisecond */
        long utc;
        if ((mFib[index + 2] & 0x08) == 0) {
            utc = ((int) (mFib[index + 2] & 0x0f)) << 8; /* 20 - 23bit */
            utc = (utc + ((int) mFib[index + 3] & 0x0FF)) << 20; /* 24 - 31bit */
        } else {
            utc = ((int) ((mFib[index + 2] & 0x0f))) << 8;
            utc = (utc + ((int) mFib[index + 3] & 0x0FF)) << 8;
            utc = (utc + ((int) mFib[index + 4] & 0x0FF)) << 12;
        }
        mHour = (int) ((utc & 0x7C000000) >>> 26);
        mMinute = (int) ((utc & 0x03F00000) >>> 20);
        if ((utc & 0x80000000) != 0) {
            mSecond = (int) ((utc & 0x000FC000) >>> 14);
        }
    }

    /**
     * get fic message
     */
    public String getFicMessage() {
        if (mFicDataLen <= 0 || mFicDataLen > 200) {
            return null;
        }
        short crc = (short) ((mFicData[mFicDataLen - 2] << 8) | mFicData[mFicDataLen - 1] & 0x00ff);
        String ficMessage = null;
        if (crc16(mFicData, mFicDataLen - 2) == crc) {
            try {
                ficMessage = new String(mFicData, 0, mFicDataLen - 2, "gb2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return ficMessage;
    }


    private final byte[] DAB_ENCRYPT_CODE = {
            48, 49, 50, 51, 52, 53, 54, 55,
            56, 57, 97, 98, 99, 100, 101, 102,
            48, 49, 50, 51, 52, 53, 54, 55,
            56, 57, 97, 98, 99, 100, 101, 102
    };

    private final short[][] UEP_TABLE = {
            {16, 101, 131, 546, 0, 32},
            {21, 107, 102, 581, 0, 32},
            {24, 111, 137, 454, 104, 32},
            {29, 118, 141, 456, 109, 32},
            {35, 120, 177, 428, 113, 32},
            {24, 133, 100, 834, 99, 48},
            {29, 105, 134, 836, 102, 48},
            {35, 111, 138, 838, 105, 48},
            {42, 120, 142, 840, 111, 48},
            {52, 120, 178, 813, 114, 48},
            {29, 197, 324, 738, 99, 56},
            {35, 201, 326, 740, 101, 56},
            {42, 208, 391, 678, 105, 56},
            {52, 215, 333, 744, 109, 56},
            {32, 197, 291, 994, 67, 64},
            {42, 203, 294, 1061, 0, 64},
            {48, 208, 392, 870, 105, 64},
            {58, 215, 333, 936, 109, 64},
            {70, 216, 370, 908, 114, 64},
            {40, 198, 323, 1314, 99, 80},
            {52, 203, 326, 1317, 102, 80},
            {58, 208, 360, 1286, 103, 80},
            {70, 215, 333, 1320, 109, 80},
            {84, 216, 337, 1324, 114, 80},
            {48, 229, 292, 1698, 100, 96},
            {58, 233, 326, 1668, 102, 96},
            {70, 208, 393, 1638, 106, 96},
            {84, 214, 332, 1705, 108, 96},
            {104, 216, 434, 1613, 115, 96},
            {58, 453, 548, 1602, 101, 112},
            {70, 361, 678, 1572, 104, 112},
            {84, 368, 744, 1510, 105, 112},
            {104, 375, 684, 1577, 110, 112},
            {64, 389, 611, 1986, 100, 128},
            {84, 363, 678, 1957, 103, 128},
            {96, 368, 713, 1926, 106, 128},
            {116, 374, 684, 1961, 110, 128},
            {140, 376, 657, 1997, 115, 128},
            {80, 357, 612, 2786, 100, 160},
            {104, 363, 742, 2661, 105, 160},
            {116, 368, 776, 2630, 107, 160},
            {140, 374, 683, 2729, 109, 160},
            {168, 376, 722, 2700, 115, 160},
            {96, 358, 644, 3522, 101, 192},
            {116, 362, 710, 3460, 105, 192},
            {140, 368, 778, 3398, 107, 192},
            {168, 374, 653, 3529, 109, 192},
            {208, 376, 692, 3501, 120, 192},
            {116, 392, 710, 4194, 102, 224},
            {140, 396, 840, 4068, 107, 224},
            {168, 368, 650, 4295, 105, 224},
            {208, 376, 720, 4234, 111, 224},
            {232, 376, 788, 4172, 116, 224},
            {128, 358, 773, 4930, 101, 256},
            {168, 364, 777, 4933, 106, 256},
            {192, 368, 874, 4839, 106, 256},
            {232, 376, 718, 5002, 109, 256},
            {280, 376, 851, 4878, 109, 256},
            {160, 360, 837, 6402, 102, 320},
            {208, 365, 809, 6437, 106, 320},
            {280, 376, 849, 6409, 113, 320},
            {192, 360, 870, 7906, 103, 384},
            {280, 368, 777, 8007, 106, 384},
            {416, 408, 916, 7854, 119, 384}
    };


}

