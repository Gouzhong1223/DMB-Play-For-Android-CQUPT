#include "TpegCode.h"
#include "android/log.h"
#define LOGD(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"jni",FORMAT,##__VA_ARGS__);

int TpegCode::TPEG_HEAD_LENGTH = 14;
int TpegCode::TPEG_DATA_LENGTH = 80;
int TpegCode::TPEG_TAIL_LENGTH = 2;
bool TpegCode::crc16Init = false;
uint16_t TpegCode::CRC16TAB[256];

std::unordered_map <std::string, uint8_t> TpegCode::NAME_TO_TYPE = {
        {"jpg",(uint8_t)1},
        {"mp4",(uint8_t)2},
        {"avi",(uint8_t)2},
        {"txt",(uint8_t)3}
};

std::unordered_map <uint8_t, std::string> TpegCode::TYPE_TO_NAME = {
        {(uint8_t)1,"jpg"},
        {(uint8_t)2,"mp4"},
        {(uint8_t)3,"txt"}
};

uint8_t TpegCode::getFileType(std::string filename){
    int pos = 0;
    for(pos=filename.size()-1;pos>=0;pos--){
        if(filename[pos] == '.'){
            break;
        }
    }
    if(pos < 0){
        return (uint8_t)0;
    }
    std::string suffix = filename.substr(pos+1);
    if(NAME_TO_TYPE.find(suffix) != NAME_TO_TYPE.end()){
        return NAME_TO_TYPE[suffix];
    }
    return (uint8_t)0;
}


std::string TpegCode::getFilename(TpegInfo info){
    if(TYPE_TO_NAME.find(info.type) != TYPE_TO_NAME.end()){
        std::stringstream ioss;
        std::string res;
        ioss << std::hex << info.fileId;
        ioss >> res;
        std::string s(sizeof(uint32_t) * 2 - res.size(), '0');
        res = s + res;
        res += ".";
        res += TYPE_TO_NAME[info.type];
        return res;
    }
    return "";
}

uint32_t TpegCode::getFileLength(FILE *file){
    fseek(file, 0, SEEK_END);
    long ret = ftell(file);
    uint32_t total = (unsigned int)(ret / TPEG_DATA_LENGTH);
    if (ret % TPEG_DATA_LENGTH != 0){
        total++;
    }
    fseek(file, 0, SEEK_SET);
    return total;
}

uint32_t TpegCode::getFIleId(const char *filename){
    uint32_t seed = 131;
    uint32_t hash = 0;
    while (*filename){
        hash = hash * seed + (*filename++);
    }
    return (hash & 0x7FFFFFFF);
}

uint16_t TpegCode::tpegCrc(uint8_t *msg, int length){
    if(crc16Init == false){
        initCrc16Tab();
        crc16Init = true;
    }
    uint16_t crc = (uint16_t)0xFFFF;
    int i;
    for(i = 0; i <length; i++){
        crc = (crc << 8) ^ CRC16TAB[(crc >> 8) ^ (uint16_t)msg[i]];
    }
    return crc ^ (uint16_t)0xFFFF;
}



void TpegCode::initCrc16Tab(){
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

void TpegCode::encode(uint8_t *msg, TpegInfo info, uint8_t *code){
    code[0] = info.type;
    code[1] = (uint8_t)((info.syncBytes >> 16) & 0xFF);
    code[2] = (uint8_t)((info.syncBytes >> 8) & 0xFF);
    code[3] = (uint8_t)(info.syncBytes & 0xFF);

    code[4] = (uint8_t)((info.index >> 16) & 0xFF);
    code[5] = (uint8_t)((info.index >> 8) & 0xFF);
    code[6] = (uint8_t)(info.index & 0xFF);

    code[7] = (uint8_t)((info.total >> 16) & 0xFF);
    code[8] = (uint8_t)((info.total >> 8) & 0xFF);
    code[9] = (uint8_t)(info.total & 0xFF);

    code[10] = (uint8_t)((info.fileId >> 24) & 0xFF);
    code[11] = (uint8_t)((info.fileId >> 16) & 0xFF);
    code[12] = (uint8_t)((info.fileId >> 8) & 0xFF);
    code[13] = (uint8_t)(info.fileId & 0xFF);

    for(int i=0;i<TPEG_DATA_LENGTH;i++){
        code[i+14] = msg[i];
    }

    uint16_t crc = tpegCrc(code,14 + TPEG_DATA_LENGTH);
    code[14 + TPEG_DATA_LENGTH] = (uint8_t)((crc >> 8) & 0xFF);
    code[14 + TPEG_DATA_LENGTH + 1] = (uint8_t)(crc & 0xFF);
}


bool TpegCode::decode(uint8_t *recd, uint8_t *msg, TpegInfo &info){
    uint16_t recdCrc = (uint16_t)(recd[14+TPEG_DATA_LENGTH] << 8 | recd[14 + TPEG_DATA_LENGTH + 1]);
    uint16_t crc = tpegCrc(recd,14+TPEG_DATA_LENGTH);
    if(recdCrc != crc){
        return false;
    }
    info.type = recd[0];
    info.index = (recd[4] << 16) + (recd[5] << 8) + recd[6];
    info.total = (recd[7] << 16) + (recd[8] << 8) + recd[9];
    info.fileId =(recd[10] << 24) + (recd[11] << 16) + (recd[12] << 8) + recd[13];
    for(int i=0;i<TPEG_DATA_LENGTH;i++){
        msg[i] = recd[14 + i];
    }
    return true;
}

void TpegCode::writePacket(uint8_t *msg, TpegInfo info, std::string directory){
    static FILE *fp = NULL;
    static uint32_t fileID = 0;
    static uint32_t index = 0;

    std::string filename = directory;
    filename += getFilename(info);

    if(fp == NULL || fileID != info.fileId){
        if(fp != NULL){
            fflush(fp);
            fclose(fp);
        }
        fileID = info.fileId;
        //打开文件，取得index
        int ret = access(filename.c_str(),F_OK);
        if(ret != 0){ //文件不存在
            fp = fopen(filename.c_str(),"wb+");
            uint8_t buffer[TPEG_DATA_LENGTH];
            for(int i=0;i<TPEG_DATA_LENGTH;i++){
                buffer[i] = uint8_t(i);
            }
            for(int i=0;i<info.total;i++){
                fwrite(buffer,1,TPEG_DATA_LENGTH,fp);
                fflush(fp);
            }
            index = 0;
            fwrite(&index,1,4,fp);
        }else{ //文件存在
            fp = fopen(filename.c_str(),"rb+");
            fseek(fp,info.total * TPEG_DATA_LENGTH, SEEK_SET);
            fread(&index,1,4,fp);
        }
    }

    //fp fileId index都和info保持一致了
    if(index == info.total){ //文件的读写已经完成
        return;
    }
    fseek(fp,info.index * TPEG_DATA_LENGTH,SEEK_SET);
    fwrite(msg,1,TPEG_DATA_LENGTH,fp);
}

uint32_t TpegCode::checkFile(TpegInfo info, std::string directory){
    std::string filename = directory;
    filename += getFilename(info);
    FILE *fp = fopen(filename.c_str(),"rb+");
    if(fp == NULL){
        LOGD("fp == NULL");
        return 0;
    }
    uint8_t buffer[TPEG_DATA_LENGTH];
    fseek(fp,info.total * TPEG_DATA_LENGTH, SEEK_SET);
    uint32_t index;
    fread(&index,1,4,fp);

    fseek(fp,index * TPEG_DATA_LENGTH,SEEK_SET);
    for(index;index<info.total;index++){
        fread(buffer,1,TPEG_DATA_LENGTH,fp);
        int i = 0;
        for(i = 0;i<TPEG_DATA_LENGTH;i++){
            if(buffer[i] != (uint8_t)i){
                break;
            }
        }
        if(i >= TPEG_DATA_LENGTH){
            break;
        }
    }
    fseek(fp,info.total * TPEG_DATA_LENGTH, SEEK_SET);
    fwrite(&index,1,4,fp);
    fflush(fp);
    fclose(fp);
    return index;
}