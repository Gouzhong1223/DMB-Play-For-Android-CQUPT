#include "Interleaver.h"

Interleaver::Interleaver(int _depth, int _with) : with(_with), depth(_depth) {
    encBlock = std::vector<std::vector<uint8_t>>(depth, std::vector<uint8_t>(with, 0));
    decBlock = std::vector<std::vector<uint8_t>>(depth, std::vector<uint8_t>(with, 0));
    encPos = depth - 1;
    decPos = depth - 1;
    //这里如果不这样初始化的话，接收端无法识别第一帧
    for (int i = 0; i < depth; i++) {
        encBlock[i][1] = 0x01;
        encBlock[i][2] = 0x5b;
        encBlock[i][3] = 0xf4;
    }
}

void Interleaver::resetDecoder() {
    decPos = depth - 1;
}

void Interleaver::resetEncoder() {
    encPos = depth - 1;
    //这里如果不这样初始化的话，接收端无法识别第一帧
    for (int i = 0; i < depth; i++) {
        encBlock[i][1] = 0x01;
        encBlock[i][2] = 0x5b;
        encBlock[i][3] = 0xf4;
    }
}

void Interleaver::encode(uint8_t *msg, uint8_t *code) {
    int i, j;
    for (i = 0; i < with; i++) {
        encBlock[encPos][i] = msg[i];
    }
    j = encPos;
    for (i = 0; i < with; i++) {
        code[i] = encBlock[j][i];
        if (--j < 0) {
            j = depth - 1;
        }
    }
    encPos = (encPos + 1) % depth;
}

void Interleaver::decode(uint8_t *recd, uint8_t *msg) {
    int i, j;
    j = decPos;
    for (i = 0; i < with; i++) {
        decBlock[j][i] = recd[i];
        if (--j < 0) {
            j = depth - 1;
        }
    }
    decPos = (decPos + 1) % depth;
    for (int i = 0; i < with; i++) {
        msg[i] = decBlock[decPos][i];
    }
}
