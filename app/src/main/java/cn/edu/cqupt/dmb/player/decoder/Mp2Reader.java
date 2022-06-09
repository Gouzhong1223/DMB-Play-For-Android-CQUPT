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

package cn.edu.cqupt.dmb.player.decoder;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.utils.ConvertUtils;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是 MP2 音频的解码器
 * @Date : create by QingSong in 2022-03-18 13:53
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class Mp2Reader extends BaseDmbDecoder {

    private static final String TAG = "Mp2Reader";


    public Mp2Reader(DmbListener dmbListener, Context context, BufferedInputStream bufferedInputStream, Handler handler) {
        super(bufferedInputStream, dmbListener, context, handler);
    }

    @Override
    public void run() {
        Log.i(TAG, Thread.currentThread().getName() + "线程现在开始 MP2 解码");
        byte[] mp2Buffer = new byte[384];
        while (!DataReadWriteUtil.inMainActivity) {
            if (!DataReadWriteUtil.USB_READY) {
                // 如果当前 USB 没有就绪,就直接结束当前线程
                // Log.e(TAG, "现在 USB 还没有就绪!");
                return;
            }
            if (!DataReadWriteUtil.initFlag) {
                // 如果目前还没有接收到 DMB 类型的数据,就直接返回
                // Log.e(TAG, "现在还没有接收到 DMB 类型的数据!");
                continue;
            }
            if (readMp2Frame(mp2Buffer)) {
                // 读取成功之后直接调用监听器的 success 方法
                if (DEBUG) {
                    Log.i(TAG, "run: 接收到一个 MPEG-TS 包" + ConvertUtils.bytes2hex(mp2Buffer));
                }
                dmbListener.onSuccess(null, mp2Buffer, mp2Buffer.length);
            }
        }
        // 解码结束后发送关闭消息
        Log.i(TAG, "run: 解码结束,发送关闭消息");
        handler.sendEmptyMessage(0x77);
    }

    /**
     * 读取一个 MP2 数据包
     *
     * @param bytes 承载 MP2 数据包的容器
     * @return 成功返回 true
     */
    private boolean readMp2Frame(byte[] bytes) {
        int nRead;
        try {
            bytes[0] = bytes[1] = bytes[2] = (byte) 0xFF;
            while ((nRead = bufferedInputStream.read(bytes, 3, 1)) > 0) {
                if ((bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFC && bytes[2] == (byte) 0x84)) {
                    break;
                }
                System.arraycopy(bytes, 1, bytes, 0, 3);
            }
            if (nRead <= 0) {
                return false;
            }
            /* read n bytes method, according to unix network programming page 72 */
            /* read left data of the frame */
            int nLeft = 380;
            int pos = 4;
            while (nLeft > 0) {
                if ((nRead = ((InputStream) bufferedInputStream).read(bytes, pos, nLeft)) <= 0) {
                    return false;
                }
                nLeft -= nRead;
                pos += nRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
