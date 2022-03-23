#ifndef TPEG_CODE_H
#define TPEG_CODE_H

#include <string>
#include <unordered_map>
#include <cstdio>
#include <sstream>
#include <unistd.h>
class TpegCode{

public:
    struct TpegInfo{
        uint8_t type; //1字节
        uint32_t syncBytes; //3字节
        uint32_t index; //3字节
        uint32_t total; //3字节
        uint32_t fileId; //4字节
    };
    static uint16_t CRC16TAB[256];
    static bool crc16Init;
    static int TPEG_HEAD_LENGTH;
    static int TPEG_DATA_LENGTH;
    static int TPEG_TAIL_LENGTH;
    static std::unordered_map <std::string, uint8_t> NAME_TO_TYPE;
    static std::unordered_map <uint8_t, std::string> TYPE_TO_NAME;
    static uint8_t getFileType(std::string filename);
    static std::string getFilename(TpegInfo info);
    static uint32_t getFileLength(FILE *file);
    static uint32_t getFIleId(const char *filename);
    static uint16_t tpegCrc(uint8_t *msg, int length);
    static void encode(uint8_t *msg, TpegInfo info, uint8_t *code);
    static bool decode(uint8_t *recd, uint8_t *msg, TpegInfo &info);
    static void writePacket(uint8_t *msg, TpegInfo info ,std::string directory);
    static uint32_t checkFile(TpegInfo info, std::string directory);
    static void initCrc16Tab();
};

#endif //TPEG_CODE_H