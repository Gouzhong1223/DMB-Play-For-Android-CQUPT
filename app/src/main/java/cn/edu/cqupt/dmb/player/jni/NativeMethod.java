/*
 *
 *              Copyright 2022 By Gouzhong1223
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package cn.edu.cqupt.dmb.player.jni;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是定义 Native 方法的类
 * @Date : create by QingSong in 2022-03-18 13:55
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.jni
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class NativeMethod {

    static {
        System.loadLibrary("native-lib");
    }

    /**
     * TPEG 解码器初始化
     */
    public static native void tpegInit();


    /**
     * 对一个tdc进行译码，译码的结果存储在out中
     * info[0] 类型 1 中间帧 2 头帧 3 尾帧
     * info[1] 消息长度
     *
     * @param in   需要译码的数组
     * @param out  译码完成额数组
     * @param info 用于存储消息类型
     */
    public static native void decodeTpegFrame(byte[] in, byte[] out, int[] info);
}
