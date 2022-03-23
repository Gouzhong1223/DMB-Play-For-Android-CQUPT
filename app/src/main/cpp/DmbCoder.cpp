#include "DmbCoder.h"

void printError(std::string error) {
    std::cout << error << std::endl;
}

void defaultCallback(int process) {
    std::cout << "\r" << process << "% complete";
}

void encodeFile(std::string filename, void(*callback)(int process)) {
    FILE *file = fopen(filename.c_str(), "rb");
    if (file == NULL) {
        std::string str = "can't open file " + filename;
        printError(str);
        return;
    }
    //取出后缀
    uint8_t fileType = TpegCode::getFileType(filename);
    if (fileType == 0) {
        std::string str = "unknown file type " + filename;
        printError(str);
        return;
    }

    //填写TpegInfo
    TpegCode::TpegInfo info;
    info.type = fileType;
    info.syncBytes = 0x015bf4;
    info.index = 0;
    info.total = TpegCode::getFileLength(file);
    info.fileId = TpegCode::getFIleId(filename.c_str());

    //创建编码器
    RSCode rsCode(255, 239, 112);
    Interleaver interleaver(14, 112);

    //准备读写文件的缓存
    uint8_t msgBuff[80];
    uint8_t tpegBuff[96];
    uint8_t rsBuffer[112];
    uint8_t interleaverBuffer[112];

    int len;
    std::string outName = filename + ".ts";
    FILE *fout = fopen(outName.c_str(), "wb+");
    if (fout == NULL) {
        fclose(file);
        std::string str = "can't create file " + outName;
        printError(str);
        return;
    }

    while ((len = fread(msgBuff, 1, 80, file)) > 0) {
        for (int i = len; i < 80; i++) { //如果没有读到80，末尾补0
            msgBuff[i] = (uint8_t) 0x00;
        }
        TpegCode::encode(msgBuff, info, tpegBuff);
        rsCode.encode(tpegBuff, rsBuffer);
        interleaver.encode(rsBuffer, interleaverBuffer);

        fwrite(interleaverBuffer, 1, 112, fout);
        if (info.index % 10000 == 0) {
            callback((info.index * 100) / info.total);
        }
        info.index++;
    }

    //交织引入了时延长
    for (int i = 0; i < interleaver.depth - 1; i++) {
        interleaver.encode(rsBuffer, interleaverBuffer);
        fwrite(interleaverBuffer, 1, 112, fout);
    }

    callback(100);
    fclose(file);
    fflush(fout);
    fclose(fout);
}


void rscodeTest() {
    RSCode rs(255, 239, 112);
    uint8_t msg[96];
    uint8_t code[112];
    for (int i = 0; i < 96; i++) {
        msg[i] = (uint8_t) i;
    }
    uint8_t recd[96];
    rs.encode(msg, code);
    for (int i = 0; i < 8; i++) {
        code[rand() % 112] = 0;
    }
    rs.decode(code, recd);
    for (int i = 0; i < 96; i++) {
        if (code[i] != recd[i]) {
            std::cout << "error" << std::endl;
        }
    }
}

void ldpcTest() {
    LdpcCode ldpc;
    uint8_t msg[112];
    uint8_t rightCode[168];
    std::ifstream fin("LdpcMsg.txt");
    int num;
    for (int i = 0; i < 112; i++) {
        fin >> num;
        msg[i] = (uint8_t) num;
    }
    fin.close();

    fin = std::ifstream("LdpcCode.txt");
    for (int i = 0; i < 168; i++) {
        fin >> num;
        rightCode[i] = (uint8_t) num;
    }
    fin.close();
    uint8_t code[168];
    ldpc.encode(msg, code);

    for (int i = 0; i < 168; i++) {
        if (rightCode[i] != code[i]) {
            std::cout << "error:not equal at posion " << i << std::endl;
        }
    }

    uint8_t code1[168];
    ldpc.decode(code, code1);
    for (int i = 0; i < 168; i++) {
        if (code[i] != code1[i]) {
            std::cout << "error" << std::endl;
        }
    }
}

void placePacket(uint8_t *msg, TpegCode::TpegInfo info) {
    static FILE *file = NULL;
    static uint32_t fileId = 0;

    if (file == NULL || info.fileId != fileId) {
        if (file != NULL) {
            fclose(file);
        }
        fileId = info.fileId;
        std::string filename = TpegCode::getFilename(info);
        if (filename == "") {
            std::cout << "file type nuknown " << info.type << std::endl;
            return;
        }
        file = fopen(filename.c_str(), "wb+");
        if (file == NULL) {
            std::cout << "can't open or create file " << filename << std::endl;
            return;
        }
    }
    fseek(file, info.index * 80, SEEK_SET);
    fwrite(msg, 1, 80, file);
}

void decodeFile(std::string filename) {
    FILE *fin = fopen(filename.c_str(), "rb");
    uint8_t inBuffer[112];
    uint8_t interBuffer[112];
    uint8_t rsBuffer[96];
    uint8_t msgBuffer[80]; //fix it

    Interleaver deinter(14, 112);
    RSCode rs(255, 239, 112);

    TpegCode::TpegInfo info;
    while (fread(inBuffer, 1, 112, fin) > 0) {
        deinter.decode(inBuffer, interBuffer);
        rs.decode(interBuffer, rsBuffer);
        bool ret = TpegCode::decode(rsBuffer, msgBuffer, info);
        if (ret == false) {
            std::cout << "tpeg return false" << std::endl;
        } else {
            placePacket(msgBuffer, info);
        }
    }
}
