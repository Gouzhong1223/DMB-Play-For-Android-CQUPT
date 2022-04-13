#include <jni.h>
#include <string>
#include "mp2decoder.h"
#include "tpegdec.h"
#include "tpeg.h"
#include "mpeg_dec.h"
#include "ts_interleaver.h"

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
    jbyte *in = env->GetByteArrayElements(in_, nullptr);
    jbyte *out = env->GetByteArrayElements(out_, nullptr);
    jint *info = env->GetIntArrayElements(info_, nullptr);

    unsigned char tpeg_buf[112];
    unsigned char data_buf[96];
    deinterleaver((unsigned char *) in, tpeg_buf);
    rsDecode(tpeg_buf, data_buf);
    tpegPacketDecode(data_buf, (unsigned char *) out, (uint32_t *) info);
    env->ReleaseByteArrayElements(in_, in, 0);
    env->ReleaseByteArrayElements(out_, out, 0);
    env->ReleaseIntArrayElements(info_, info, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_edu_cqupt_dmb_player_jni_NativeMethod_tpegInit(JNIEnv *env, jclass type) {
    tpegInit();
}

extern "C"
JNIEXPORT jint JNICALL
Java_cn_edu_cqupt_dmb_player_jni_NativeMethod_decodeMpegTsFrame(JNIEnv *env, jclass clazz,
                                                                jbyteArray ts_buf_204,
                                                                jbyteArray ts_buf_188) {
    jbyte *in = env->GetByteArrayElements(ts_buf_204, nullptr);
    jbyte *out = env->GetByteArrayElements(ts_buf_188, nullptr);
    // 初始化ts解交织器
    interleave_init();
    if (do_interleaver((unsigned char *) in)) {
        // 如果解交织没有通过就直接返回了
        return -1;
    }
    // 解RS码
    return rsDecode(reinterpret_cast<unsigned char *>(in), reinterpret_cast<unsigned char *>(out));
}
