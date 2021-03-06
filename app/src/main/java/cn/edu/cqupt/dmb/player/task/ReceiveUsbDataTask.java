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

package cn.edu.cqupt.dmb.player.task;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.DongleType;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessing;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;


/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是一个从USB中读取数据的线程,启动依赖{@link java.util.concurrent.ScheduledExecutorService}的调度
 * @Date : create by QingSong in 2022-03-11 16:21
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class ReceiveUsbDataTask implements Runnable {

    private static final String TAG = "ReceiveUsbDataTask";

    /**
     * 存储从USB中读取到的数据
     */
    private final byte[] bytes;
    /**
     * 读取USB数据的端口
     */
    private final UsbEndpoint usbEndpointIn;
    /**
     * 已经打开的USB链接
     */
    private final UsbDeviceConnection usbDeviceConnection;

    /**
     * STM32型号 Dongle 单次 IO 从 USB 中读取数据的次数
     */
    private final Integer readTime;

    /**
     * Dongle 类型
     */
    private final DongleType dongleType;

    /**
     * PIP 输出流
     */
    private final PipedOutputStream pipedOutputStream;

    /**
     * 定时任务构造器
     *
     * @param bytes               用于存储从USB中读取到的数据
     * @param usbEndpointIn       读取USB数据的端口
     * @param usbDeviceConnection 已经打开的USB链接
     * @param readTime            从 USB 中的读取次数
     * @param dongleType          Dongle 类型
     * @param pipedOutputStream   PIP输出流
     */
    public ReceiveUsbDataTask(byte[] bytes, UsbEndpoint usbEndpointIn, UsbDeviceConnection usbDeviceConnection, Integer readTime, DongleType dongleType, PipedOutputStream pipedOutputStream) {
        this.bytes = bytes;
        this.usbEndpointIn = usbEndpointIn;
        this.usbDeviceConnection = usbDeviceConnection;
        this.readTime = readTime;
        this.dongleType = dongleType;
        this.pipedOutputStream = pipedOutputStream;
    }

    @Override
    public void run() {
        // 必须是USB设备已经就绪的情况下才执行,如果USB设备是未就绪或是终端没有插入USB的情况下就直接退出
        while (DataReadWriteUtil.USB_READY && !DataReadWriteUtil.inMainActivity) {
            // 初始化一个packetBuf用于装载单个 DMB 数据
            // 从 USB 中读取数据装载在bytes中
            int readLength = usbDeviceConnection.bulkTransfer(usbEndpointIn, bytes, bytes.length, DmbPlayerConstant.DEFAULT_READ_TIME_OUT.getDmbConstantValue());
            if (readLength == -1) {
                // 这里是从 USB 中读取数据的时候失败了,所以直接返回,等待下一个任务来读取
                Log.e(TAG, "从 USB 中读取数据失败!");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (dongleType == DongleType.STM32) {
                byte[] packetBuf = new byte[DmbPlayerConstant.DEFAULT_DMB_DATA_SIZE.getDmbConstantValue()];
                // 由于 bytes 中包含 DmbPlayerConstant.DMB_READ_TIME 个 DMB 数据包,所以这里采用一个循环的方式分包,分成 DmbPlayerConstant.DMB_READ_TIME 个
                // 20220324更新,这里从 USB 中读取的次数现在依赖于成员变量 READ_TIME
                for (int i = 0; i < readTime; i++) {
                    // 分包
                    System.arraycopy(bytes, i * DmbPlayerConstant.DEFAULT_DMB_DATA_SIZE.getDmbConstantValue()
                            , packetBuf, 0, DmbPlayerConstant.DEFAULT_DMB_DATA_SIZE.getDmbConstantValue());
                    // 从数据处理器的静态工程获取数据处理器
                    DataProcessing dataProcessor = DataProcessingFactory.getDataProcessor(packetBuf[3]);
                    // 处理数据
                    dataProcessor.processData(packetBuf, dongleType, pipedOutputStream);
                }
            } else {
                DataProcessing dataProcessor = DataProcessingFactory.getDataProcessor(bytes[3]);
                dataProcessor.processData(bytes, dongleType, pipedOutputStream);
            }
        }
        DataReadWriteUtil.inMainActivity = true;
        Log.i(TAG, "run: 从 USB 接收数据的任务被关闭了");
    }
}

