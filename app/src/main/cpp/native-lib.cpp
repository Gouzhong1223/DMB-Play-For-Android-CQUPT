#include <jni.h>
#include <string>
#include "android/log.h"
#include "mp2decoder.h"

#include "RSCode.h"
#include "TpegCode.h"
#include "Interleaver.h"

#define TPEG_LEN 96

Interleaver interleaver(14, 112);
RSCode rsCode(255, 239, 112);

#define LOGD(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"jni",FORMAT,##__VA_ARGS__);


void tpegPacketDecode(unsigned char *tpegPacket, unsigned char *data, uint32_t *info) {
    static uint32_t auxInfoLen = 0; /* 辅助数据长度，辅助数据位于有效数据的前面 */
    static uint32_t segmentIndex = 0;

    info[0] = info[1] = info[2] = 0;

    /* crc校验，失败字节返回 */
    uint16_t crcResult, crcPacket;
    crcResult = TpegCode::tpegCrc(tpegPacket, TPEG_LEN - 2);

    crcPacket = ((*(tpegPacket + TPEG_LEN - 2)) << 8) + (*(tpegPacket + TPEG_LEN - 1));
    if (crcPacket != crcResult) {
        return;
    }
    /* 取出标志位，并把数组索引指向有效数据区 */
    uint32_t index = 3; /* 数组的索引 */
    uint32_t datagroupType = *(tpegPacket + index) & 0x0f;
    uint32_t extensionFlag = *(tpegPacket + index) & 0x80;
    uint32_t segmentFlag = *(tpegPacket + index) & 0x20;
    uint32_t accessFlag = *(tpegPacket + index) & 0x10;
    uint32_t segmentNumber, segmentSize;
    uint32_t lastFlag;
    index += 2;
    if (datagroupType != 4) {
        return;
    }
    if (extensionFlag == 0x80) {
        index += 2;
    }
    if (segmentFlag == 0x20) {
        segmentNumber = ((*(tpegPacket + index) & 0x7f) << 8) + (*(tpegPacket + index + 1));
        lastFlag = (*(tpegPacket + index) & 0x80) >> 7;
        index += 2;
    }
    if (accessFlag == 0x10) {
        uint32_t access_len = *(tpegPacket + index) & 0x0f;
        index += (1 + access_len);
    }

    if (segmentNumber != segmentIndex) {
        segmentIndex = 0;
        return;
    }
    segmentIndex++;
    segmentSize = ((*(tpegPacket + index) & 0x1f) << 8) + (*(tpegPacket + index + 1));
    index += 2;
    if ((segmentNumber == 0) && ((tpegPacket[index] != 0xff) || (tpegPacket[index + 1] != 0x0f))) {
        segmentIndex = 0;
        return;
    }

    if (segmentNumber == 0) {
        index += 37;
        auxInfoLen = (((uint32_t) tpegPacket[index] & 0x0FF) << 8)
                     + ((uint32_t) tpegPacket[index + 1] & 0x0FF);
        index += 8;
        segmentSize -= 45; /* 这45个字节不知道是用来干什么的 */
    }
    if (segmentSize > TPEG_LEN - index) {
        return;
    }

    if (auxInfoLen != 0) { /* 如果有辅助数据没有取完 */
        if (segmentSize >= auxInfoLen) { /* 可以一次取完 */
            segmentSize -= auxInfoLen;
            index += auxInfoLen; /* 指向新的有效数据 */
            auxInfoLen = 0;
        } else { /* 无法一次取完 */
            auxInfoLen -= segmentSize;

            memcpy(data, (tpegPacket + 12), 35);
            info[0] = 2;
            info[1] = 35;
            info[2] = 0;
            return;
        }
    }

    if (auxInfoLen == 0) {
        memcpy(data, (tpegPacket + index), segmentSize);
        info[1] = segmentSize;
        info[2] = 0;
        if (segmentNumber == 0) {
            info[0] = 2;
        } else if (lastFlag == 1) {
            info[0] = 3;
            segmentIndex = 0;
        } else {
            info[0] = 1;
        }
    }
}

void tpeg_packet_dec(unsigned char *pbuf, unsigned char *jpegBuf, int info[]) {
    static unsigned int segment_index = 0;
    static unsigned int nTableLen = 0;
    static unsigned int nTableLeft = 0;
    unsigned int last_flag = 0;
    unsigned int n;
    unsigned short packet_len;
    unsigned short crc_result, packet_crc, station = 1;     //useful_data_len,
    unsigned short datagroup_type, extension_flag, segment_flag, access_flag, access_len;
    unsigned int segment_number = 0;
    unsigned int segment_size;
    unsigned int disp_time_flag;
    unsigned int old_address = 0, new_address = 0;

    info[0] = 0;//type
    info[1] = 0;//out length
    info[2] = 1;//display time flag
    packet_len = 93;
    station = *(pbuf) & 0x0c;
    if ((station == 0x08) || (station == 0x0c) || (station == 0x00) || (station == 0x04)) {
        crc_result = TpegCode::tpegCrc(pbuf, packet_len + 1);
        packet_crc = ((*(pbuf + packet_len + 1)) << 8) + (*(pbuf + packet_len + 2));

        if (crc_result == packet_crc) {
            if ((station == 0x08) || (station == 0x0c)) {
                new_address = (((*pbuf) & 0x03) << 8) + ((*(pbuf + 1)) & 0xff);
                nTableLen = 0;
            }
            if (((station == 0x00) || (station == 0x04))) {
                old_address = (((*pbuf) & 0x03) << 8) + ((*(pbuf + 1)) & 0xff);
            }
            n = 3;
            datagroup_type = *(pbuf + n) & 0x0f;
            extension_flag = *(pbuf + n) & 0x80;
            segment_flag = *(pbuf + n) & 0x20;
            access_flag = *(pbuf + n) & 0x10;
            n += 1;
            disp_time_flag = *(pbuf + n);
            n += 1;
            if (datagroup_type != 4) {
                return;
            }
            if (extension_flag == 0x80) {
                n += 2;
            }

            if (segment_flag == 0x20) {
                segment_number = ((*(pbuf + n) & 0x7f) << 8) + (*(pbuf + n + 1));
                last_flag = (*(pbuf + n) & 0x80) >> 7;
                n += 2;
            }

            if (access_flag == 0x10) {
                access_len = *(pbuf + n) & 0x0f;
                n += (1 + access_len);
            }
            if (segment_number == segment_index) {

                segment_index++;
                segment_size = ((*(pbuf + n) & 0x1f) << 8) + (*(pbuf + n + 1));
                n += 2;  //Ìø¹ýsegment_sizeËùÕŒµÄ2žö×ÖœÚ
                if ((segment_number == 0) &&
                    ((pbuf[n] != 0xff) || (pbuf[n + 1] != 0x0f))) {
                    segment_index = 0;
                    return;
                }
                if (segment_number == 0) {
                    n += 37;
                    nTableLen = (((unsigned int) pbuf[n] & 0x0FF) << 8)
                                + ((unsigned int) pbuf[n + 1] & 0x0FF);
                    nTableLeft = nTableLen;
                    n += 8;
                    segment_size -= 45;

                }

                if (segment_size <= (96 - n)) {
                    if (nTableLeft) {
                        if (segment_size >= nTableLeft) {
                            segment_size -= nTableLeft;
                            n += nTableLeft;
                            nTableLeft = 0;
                            info[1] = segment_size;
                        } else {
                            nTableLeft -= segment_size;
                            n += segment_size;
                            segment_size = 0;
                            info[1] = segment_size;
                        }

                        if (segment_number == 0) {
                            info[0] = 2;//֡ͷ��־
                        } else if (last_flag == 1) {
                            info[0] = 3;//֡β��־
                            segment_index = 0;
                        } else {
                            info[0] = 1;
                        }
                    }

                    if (nTableLeft == 0) {
                        memcpy(jpegBuf, (pbuf + n), segment_size);
                        info[1] = segment_size;
                        info[2] = disp_time_flag;
                        if (segment_number == 0) {
                            info[0] = 2;//֡ͷ��־
                        } else if (last_flag == 1) {
                            info[0] = 3;//֡β��־
                            segment_index = 0;
                        } else {
                            info[0] = 1;//���ݱ�־
                        }
                    }
                } else { ;
                    //                        LOGE("value of segment_size err: segment_size=%d, n=%d",segment_size,n);
                }


            } else {
                if (segment_index != 0) {
//			LOGE("lost package: segment_index=%d",segment_index);
                    segment_index = 0;
                }
            }
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_edu_cqupt_dmb_player_jni_NativeMethod_mp2DecoderInit(JNIEnv *env, jclass type) {
    mp2DecoderInit();
}

extern "C"
JNIEXPORT jint JNICALL
Java_cn_edu_cqupt_dmb_player_jni_NativeMethod_decodeMp2Frame(JNIEnv *env,
                                                             jclass type, jbyteArray in_, jint len,
                                                             jbyteArray out_, jintArray info_) {
    jbyte *in = env->GetByteArrayElements(in_, NULL);
    jbyte *out = env->GetByteArrayElements(out_, NULL);
    jint *info = env->GetIntArrayElements(info_, NULL);

    int ret = decodeMp2Frame((unsigned char *) in, len, (unsigned char *) out,
                             (unsigned int *) info);

    env->ReleaseByteArrayElements(in_, in, 0);
    env->ReleaseByteArrayElements(out_, out, 0);
    env->ReleaseIntArrayElements(info_, info, 0);

    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_edu_cqupt_dmb_player_jni_NativeMethod_decodeTpegFrame(JNIEnv *env, jclass type,
                                                              jbyteArray in_,
                                                              jbyteArray out_, jintArray info_) {
    jbyte *in = env->GetByteArrayElements(in_, NULL);
    jbyte *out = env->GetByteArrayElements(out_, NULL);
    jint *info = env->GetIntArrayElements(info_, NULL);

    unsigned char tpegBuffer[112];
    unsigned char dataBuffer[96];

    TpegCode::TpegInfo tpegInfo{};
    uint8_t msg[TpegCode::TPEG_DATA_LENGTH];
    interleaver.decode((unsigned char *) in, tpegBuffer);
    rsCode.decode(tpegBuffer, dataBuffer);
    tpegPacketDecode(dataBuffer, reinterpret_cast<unsigned char *>(out),
                     reinterpret_cast<uint32_t *>(info));
    env->ReleaseByteArrayElements(in_, in, 0);
    env->ReleaseByteArrayElements(out_, out, 0);
    env->ReleaseIntArrayElements(info_, info, 0);
}
