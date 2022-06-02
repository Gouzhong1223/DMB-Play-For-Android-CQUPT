#ifndef _MP2G_DEC_H_
#define _MP2G_DEC_H_

#ifdef __cplusplus
extern "C" {
#endif

/* initial decoder */
void mp2DecoderInit();
/* in:mp2 frame, len: mp2 frame length, out: pcm buffer, info: pcm information */
int decodeMp2Frame(unsigned char *in, int len, unsigned char *out, unsigned int *info);

#ifdef __cplusplus
}
#endif

#endif//_MP2G_DEC_H_
