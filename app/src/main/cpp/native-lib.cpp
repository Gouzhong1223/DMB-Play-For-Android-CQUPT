#include <jni.h>
#include <string>
#include "mp2decoder.h"
#include "tpegdec.h"
#include "tpeg.h"

extern "C"
JNIEXPORT void JNICALL
Java_cn_edu_cqupt_dmb_player_jni_NativeMethod_mp2DecoderInit(JNIEnv *env, jclass type) {
    mp2DecoderInit();
}

extern "C"
JNIEXPORT jint JNICALL
Java_cn_edu_cqupt_dmb_player_jni_NativeMethod_decodeMp2Frame(JNIEnv *env,
                                                             jclass type, jbyteArray in_, jint len,
                                                             jbyteArray out_, jintArray info_) {
    jbyte *in = env->GetByteArrayElements(in_, NULL);
    jbyte *out = env->GetByteArrayElements(out_, NULL);
    jint *info = env->GetIntArrayElements(info_, NULL);

    int ret = decodeMp2Frame((unsigned char *) in, len, (unsigned char *) out,
                             (unsigned int *) info);

    env->ReleaseByteArrayElements(in_, in, 0);
    env->ReleaseByteArrayElements(out_, out, 0);
    env->ReleaseIntArrayElements(info_, info, 0);

    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_edu_cqupt_dmb_player_jni_NativeMethod_decodeTpegFrame(JNIEnv *env, jclass type,
                                                              jbyteArray in_,
                                                              jbyteArray out_, jintArray info_) {
    jbyte *in = env->GetByteArrayElements(in_, NULL);
    jbyte *out = env->GetByteArrayElements(out_, NULL);
    jint *info = env->GetIntArrayElements(info_, NULL);

    unsigned char tpeg_buf[112];
    unsigned char data_buf[96];
//    tpegDecode((unsigned char *)in,(unsigned char *)out,(uint32_t *)info);

//    crc16((unsigned char *)in,0,1);
//    deinterleaverInit()

    deinterleaver((unsigned char *)in,tpeg_buf);
    rsDecode(tpeg_buf,data_buf);
//    decode
//    decode_rs(tpeg_buf,data_buf);
    tpegPacketDecode(data_buf,(unsigned char *)out,(uint32_t*)info);
    env->ReleaseByteArrayElements(in_, in, 0);
    env->ReleaseByteArrayElements(out_, out, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_edu_cqupt_dmb_player_jni_NativeMethod_tpegInit(JNIEnv *env, jclass type) {
    tpegInit();
}
