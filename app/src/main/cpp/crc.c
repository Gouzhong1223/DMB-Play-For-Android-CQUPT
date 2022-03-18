#include <stdint.h>

static uint16_t CRC16TAB[256];
static int crc16Init = 0;

void initCrc16Tab(){
    /* polynomial $x^{16} + x^{12} + x^5 + 1$ Recommendation ITU-T X.25*/
    uint16_t CRC_POLY_16 = (uint16_t)0x1021;
    uint16_t i,j,crc;
    for(i=0;i<256;i++){
        crc = i<<8;
        for(j=0;j<8;j++){
            if((crc & 0x8000) != 0){
                crc = (crc << 1) ^ CRC_POLY_16;
            }else{
                crc = crc << 1;
            }
        }
        CRC16TAB[i] = crc;
    }
}

uint16_t crc16(unsigned char* data, int offset, int len){
    if(crc16Init == 0){
        initCrc16Tab();
        crc16Init = 1;
    }
    uint16_t crc = (uint16_t)0xFFFF;
    int i;
    for(i = offset; i < offset + len; i++){
        crc = (crc << 8) ^ CRC16TAB[(crc >> 8) ^ (uint16_t)data[i]];
    }
    return crc ^ (uint16_t)0xFFFF;
}

