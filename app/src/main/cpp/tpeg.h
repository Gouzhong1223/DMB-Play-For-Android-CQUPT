#ifndef _TPEG_H
#define _TPEG_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

uint16_t crc16(unsigned char *data, int offset, int len);

void rsInit();

void rsEncode(unsigned char *msg, unsigned char *code);

int rsDecode(unsigned char *recd, unsigned char *data);

void interleaverInit();

void deinterleaverInit();

void interleaver(unsigned char *in, unsigned char *out);

void deinterleaver(unsigned char *in, unsigned char *out);

void tpegInit();

void tpegPacketDecode(unsigned char *tpegPacket, unsigned char *data, uint32_t *info);

void tpegDecode(unsigned char *tpeg, unsigned char *data, uint32_t *info);

#ifdef __cplusplus
}
#endif


#endif
