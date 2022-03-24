#include <string.h>
#include "tpeg.h"

#define RS_LEN 112
#define TPEG_LEN 96
#define DATA_LEN 80

#include "tpegdec.h"
#include "android/log.h"

#define LOGD(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"jni",FORMAT,##__VA_ARGS__);

void tpegInit(){
    rsInit();
    interleaverInit();
    deinterleaverInit();
}

void tpegPacketDecode(unsigned char *tpegPacket, unsigned char *data, uint32_t *info){
    static uint32_t auxInfoLen = 0; /* 辅助数据长度，辅助数据位于有效数据的前面 */
    static uint32_t segmentIndex = 0;

    info[0] = info[1] = info[2] = 0;

    /* crc校验，失败字节返回 */
    uint16_t crcResult, crcPacket;
    crcResult = crc16(tpegPacket,0,TPEG_LEN - 2);
    crcPacket = ((*(tpegPacket + TPEG_LEN - 2)) << 8) + (*(tpegPacket + TPEG_LEN - 1));
    if(crcPacket != crcResult){
        return;
    }
    //LOGD("crc pass");
    /* 取出标志位，并把数组索引指向有效数据区 */
    uint32_t index = 3; /* 数组的索引 */
    uint32_t datagroupType = *(tpegPacket + index) & 0x0f;
    uint32_t extensionFlag = *(tpegPacket + index) & 0x80;
    uint32_t segmentFlag = *(tpegPacket + index) & 0x20;
    uint32_t accessFlag = *(tpegPacket + index) & 0x10;
    uint32_t segmentNumber,segmentSize;
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

    if(segmentNumber != segmentIndex){
        segmentIndex = 0;
        return;
    }
    segmentIndex++;
    segmentSize = ((*(tpegPacket + index) & 0x1f) << 8) + (*(tpegPacket + index + 1));
    index += 2;
    if ((segmentNumber == 0) &&((tpegPacket[index] != 0xff) || (tpegPacket[index + 1] != 0x0f))) {
        segmentIndex = 0;
        return;
    }

    if (segmentNumber == 0) {
        index += 37;
        auxInfoLen = (((uint32_t)tpegPacket[index]&0x0FF)<<8)
                                + ((uint32_t)tpegPacket[index+1]&0x0FF);
        index += 8;
        segmentSize -= 45; /* 这45个字节不知道是用来干什么的 */
    }
    if(segmentSize > TPEG_LEN - index){
        return;
    }

    if (auxInfoLen != 0) { /* 如果有辅助数据没有取完 */
        if (segmentSize >= auxInfoLen) { /* 可以一次取完 */
            segmentSize -= auxInfoLen;
            index += auxInfoLen; /* 指向新的有效数据 */
            auxInfoLen = 0;
        } else { /* 无法一次取完 */
            auxInfoLen -= segmentSize;
            index += segmentSize;
            segmentSize = 0;

            /* 取出文件名，这里我感觉写的相当拙劣，总是在后面35个字节 */
            memcpy(data, (tpegPacket +12+45), 35);
            info[0] = 2;
            info[1] = 35;
            info[2] = 0;
            return;
        }
    }

    if (auxInfoLen == 0){
        memcpy(data, (tpegPacket + index), segmentSize);
        info[1] = segmentSize;
        info[2] = 0;
        if (segmentNumber == 0) {
            info[0] = 2;
        }else if (lastFlag == 1) {
            info[0] = 3;
            segmentIndex = 0;
        }else {
            info[0] = 1;
        }
    }
}

void tpegDecode(unsigned char *tpeg, unsigned char *data, uint32_t *info){
    unsigned char rs[RS_LEN];
    unsigned char tpegData[TPEG_LEN];
    deinterleaver(tpeg,rs);
//    int ret = rsDecode(rs,tpegData);
//    if(ret != 0){
//        info[0] = 1;
//        info[1] = 0;
//        return;
//    }
//    int i;
//    for(i=0;i<96;i++){
//        data[i] = tpeg[i];
//    }
    decode_rs(tpeg,data);
//    tpegPacketDecode(tpeg,data,info);
}
