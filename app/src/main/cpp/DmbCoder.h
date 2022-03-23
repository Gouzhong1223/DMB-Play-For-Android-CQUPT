#ifndef _DMB_CODER_H_
#define _DMB_CODER_H_

#include "TpegCode.h"
#include "RSCode.h"
#include "Interleaver.h"
#include "LdpcCode.h"

void defaultCallback(int process);

void encodeFile(std::string filename, void(*callback)(int process) = defaultCallback);

void decodeFile(std::string filename);

#endif //_DMB_CODER_H_
