#ifndef INTERLEAVER_H
#define INTERLEAVER_H

#include <cstdint>
#include <vector>

class Interleaver{
private:
    int encPos;
    int decPos;
    std::vector<std::vector<uint8_t> > encBlock;
    std::vector<std::vector<uint8_t> > decBlock;
public:
    int with;
    int depth;
    Interleaver(int _depth, int _with);
    void resetDecoder();
    void resetEncoder();
    void encode(uint8_t *msg,uint8_t *code);
    void decode(uint8_t *recd,uint8_t *msg);
};

#endif //INTERLEAVER_H