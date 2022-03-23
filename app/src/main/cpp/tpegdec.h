
#ifndef _DECODE_H_
#define _DECODE_H_

#ifdef __cplusplus
extern "C" {
#endif

unsigned char tpeg_syndrom(unsigned char a[112]);
void tpeg_bm_serial(unsigned char a[16]);
void tpeg_root_finder_rs(unsigned char tau[10]);
void tpeg_forney_2(void);
int decode_rs(unsigned char* ts_buf_112, unsigned char* ts_buf_96);
void  rs_deinterleaver(unsigned char * pRsBuf,unsigned char * pRsBuf1);
void decode_rs_tpeg(unsigned char * rs_Buf,unsigned char * ts_Buf);

#ifdef __cplusplus
}
#endif

#endif

