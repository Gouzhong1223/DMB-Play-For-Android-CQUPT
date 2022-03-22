package cn.edu.cqupt.dmb.player.domain;


import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.task.SendDataToUsbTask;
import cn.edu.cqupt.dmb.player.task.ReceiveUsbDataTask;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : Dangle 交互类
 * @Date : create by QingSong in 2022-03-15 14:08
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.domain
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class Dangle {

    private final byte[] reqMsg = new byte[DmbPlayerConstant.DEFAULT_REQ_MSG_SIZE.getDmbConstantValue()];
    private final byte[] reqMsg2 = new byte[DmbPlayerConstant.DEFAULT_REQ_MSG_SIZE.getDmbConstantValue()];
    private final byte[] rxMsg = new byte[DmbPlayerConstant.DEFAULT_DMB_DATA_SIZE.getDmbConstantValue()
            * DmbPlayerConstant.DMB_READ_TIME.getDmbConstantValue()];


    private static final String TAG = "Dangle";
    /**
     * 读取USB数据的UsbEndpoint
     */
    private final UsbEndpoint usbEndpointIn;

    /**
     * 写入USB数据的Endpoint
     */
    private final UsbEndpoint usbEndpointOut;

    /**
     * 已经打开的USB连接
     */
    private final UsbDeviceConnection usbDeviceConnection;

    /**
     * 装载RF信息
     */
    private final static byte[] frequency = new byte[DmbPlayerConstant.DEFAULT_REQ_MSG_SIZE.getDmbConstantValue()];

    /**
     * MX_RF_I2C_ADDRESS
     */
    private static final byte MX_RF_I2C_ADDRESS = (byte) 0xc0;

    /**
     * 重邮DMB频点
     */
    public static final int FREQKHZ = DmbPlayerConstant.FREQKHZ.getDmbConstantValue();

    public Dangle(UsbEndpoint usbEndpointIn, UsbEndpoint usbEndpointOut, UsbDeviceConnection usbDeviceConnection) {
        this.usbEndpointIn = usbEndpointIn;
        this.usbEndpointOut = usbEndpointOut;
        this.usbDeviceConnection = usbDeviceConnection;
    }

    /**
     * 清空Dangle寄存器，初始化Dangle，在设置频点和节目之前，需要先执行初始化操作
     */
    public void clearRegister() {
        boolean ret1 = false;
        boolean ret2 = false;
        boolean ret3 = false;
        boolean ret4 = false;
        byte[] clearBBChReg = {
                (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 32,
                (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x21, (byte) 0xff, (byte) 0xff,
                (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x23, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x24, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x25, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x26, (byte) 0x00, (byte) 0x02,
                (byte) 0x00, (byte) 0x2c, (byte) 0x00, (byte) 0x10
        };
        Future<Integer> futureForClearBBChReg = UsbUtil.getExecutorService()
                .submit(new SendDataToUsbTask(clearBBChReg, usbEndpointOut, usbDeviceConnection));
        try {
            ret1 = futureForClearBBChReg.get() == clearBBChReg.length;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        byte[] clearBBFicData1 = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 32,
                (byte) 0x40, (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x10, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x20, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x30, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x40, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x50, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x60, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x70, (byte) 0xFF, (byte) 0xFF
        };
        Future<Integer> futureForClearBBFicData1 = UsbUtil.getExecutorService()
                .submit(new SendDataToUsbTask(clearBBFicData1, usbEndpointOut, usbDeviceConnection));

        try {
            ret2 = futureForClearBBFicData1.get() == clearBBFicData1.length;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }


        byte[] clearBBFicData2 = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 32,
                (byte) 0x40, (byte) 0x80, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x90, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0xA0, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0xB0, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0xC0, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x03,
                (byte) 0x71, (byte) 0x90, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00
        };
        Future<Integer> futureForClearBBFicData2 = UsbUtil.getExecutorService()
                .submit(new SendDataToUsbTask(clearBBFicData2, usbEndpointOut, usbDeviceConnection));

        try {
            ret3 = futureForClearBBFicData2.get() == clearBBFicData2.length;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        byte[] clearMcuReg = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0x08, (byte) 0x06,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Future<Integer> futureForClearMcuReg = UsbUtil.getExecutorService()
                .submit(new SendDataToUsbTask(clearMcuReg, usbEndpointOut, usbDeviceConnection));
        try {
            ret4 = futureForClearMcuReg.get() == clearMcuReg.length;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if (ret1 && ret2 && ret3 && ret4) {
            Log.i(TAG, "清除 DMB 设置成功!");
            return;
        }
        Log.e(TAG, "清除 DMB 设置失败!");
    }

    /**
     * 设置频点，频点的单位为KHz
     */
    public void setFrequency() {
        // 初始化frequency信息
        initFrequency(frequency);
        Future<Integer> future = UsbUtil.getExecutorService()
                .submit(new SendDataToUsbTask(frequency, usbEndpointOut, usbDeviceConnection));
        try {
            // 如果返回的长度和发送的长度不一致,说明发送到USB的过程中出现错误!
            if (future.get() != frequency.length) {
                Log.e(TAG, "设置频点出错!");
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "设置频点出错!");
            e.printStackTrace();
        }
        Log.i(TAG, "设置频点为:" + DmbPlayerConstant.FREQKHZ.getDmbConstantValue());
    }

    /**
     * 设置频道
     *
     * @param channelInfo 可由Fic解码得到
     * @return isSelectId  如果设置成功,就直接返回true,反之就返回false
     */
    public boolean SetChannel(ChannelInfo channelInfo) {
        byte[] mcu_cmd = new byte[48];
        byte[] bb_cmd = new byte[48];
        byte temp;
        int div, rem1, rem2;
        int nStartCu;
        int br = channelInfo.subChOrganization[6];
        switch (channelInfo.getTransmissionMode()) {
            case 1:
                div = 12;
                rem1 = 4;
                rem2 = 5;
                break;
            case 2:
                div = 6;
                rem1 = 9;
                rem2 = 10;
                break;
            case 3:
                div = 24;
                rem1 = 4;
                rem2 = 5;
                break;
            default:
                div = 48;
                rem1 = 4;
                rem2 = 5;
                break;
        }

        mcu_cmd[0] = (byte) 0xff;
        bb_cmd[0] = (byte) 0xff;
        mcu_cmd[1] = (byte) 0xff;
        bb_cmd[1] = (byte) 0xff;
        mcu_cmd[2] = 0x08;
        bb_cmd[2] = 0x00;
        mcu_cmd[3] = 0x06;
        bb_cmd[3] = 28;
        mcu_cmd[4] = 0x00;
        bb_cmd[4] = (byte) 0x80;
        mcu_cmd[5] = (byte) ((br >>> 8) & 0x00ff);
        bb_cmd[5] = 0x20;
        mcu_cmd[6] = (byte) (br & 0x00ff);
        bb_cmd[6] = (byte) ((channelInfo.subChOrganization[0] >>> 8) & 0x00ff);
        mcu_cmd[7] = 0x01;
        bb_cmd[7] = (byte) (channelInfo.subChOrganization[0] & 0x00ff);
        mcu_cmd[8] = 0x00;
        bb_cmd[8] = (byte) 0x80;
        mcu_cmd[9] = 0x00;
        bb_cmd[9] = 0x21;

        nStartCu = (channelInfo.subChOrganization[0] & 0x03ff); /* start capacity unit address */
        mcu_cmd[10] = 0x00;
        bb_cmd[10] = (byte) ((nStartCu) / div + rem1);
        temp = (byte) ((nStartCu + channelInfo.subChOrganization[1] - 1) / div + rem2);
        if (((temp == (byte) 22) && (channelInfo.getTransmissionMode() == 0)) ||
                ((temp == (byte) 76) && (channelInfo.getTransmissionMode() == 1)) ||
                ((temp == (byte) 153) && (channelInfo.getTransmissionMode() == 2)) ||
                ((temp == (byte) 40) && (channelInfo.getTransmissionMode() == 3))) {
            mcu_cmd[9] = 0x01;
        }
        mcu_cmd[11] = 0x00;
        bb_cmd[11] = temp;
        mcu_cmd[12] = 0x00;
        bb_cmd[12] = (byte) 0x80;
        mcu_cmd[13] = 0x00;
        bb_cmd[13] = 0x22;
        mcu_cmd[14] = 0x00;
        bb_cmd[14] = (byte) ((channelInfo.subChOrganization[2] >>> 8) & 0x00ff);
        mcu_cmd[15] = 0x00;
        bb_cmd[15] = (byte) (channelInfo.subChOrganization[2] & 0x00ff);
        mcu_cmd[16] = 0x00;
        bb_cmd[16] = (byte) 0x80;
        mcu_cmd[17] = 0x00;
        bb_cmd[17] = 0x23;
        mcu_cmd[18] = 0x00;
        bb_cmd[18] = (byte) ((channelInfo.subChOrganization[3] >>> 8) & 0x00ff);
        mcu_cmd[19] = 0x00;
        bb_cmd[19] = (byte) (channelInfo.subChOrganization[3] & 0x00ff);
        mcu_cmd[20] = 0x00;
        bb_cmd[20] = (byte) 0x80;
        mcu_cmd[21] = 0x00;
        bb_cmd[21] = 0x24;
        mcu_cmd[22] = 0x00;
        bb_cmd[22] = (byte) ((channelInfo.subChOrganization[4] >>> 8) & 0x00ff);
        mcu_cmd[23] = 0x00;
        bb_cmd[23] = (byte) (channelInfo.subChOrganization[4] & 0x00ff);
        mcu_cmd[24] = 0x00;
        bb_cmd[24] = (byte) 0x80;
        mcu_cmd[25] = 0x00;
        bb_cmd[25] = 0x25;
        mcu_cmd[26] = 0x00;
        bb_cmd[26] = (byte) ((channelInfo.subChOrganization[5] >>> 8) & 0x00ff);
        mcu_cmd[27] = 0x00;
        bb_cmd[27] = (byte) (channelInfo.subChOrganization[5] & 0x00ff);
        mcu_cmd[28] = 0x00;
        bb_cmd[28] = (byte) 0x80;
        mcu_cmd[29] = 0x00;
        bb_cmd[29] = 0x26;
        mcu_cmd[30] = 0x00;
        bb_cmd[30] = (byte) 0xff;
        mcu_cmd[31] = 0x00;
        bb_cmd[31] = 0x02;

        Future<Integer> futureForPutBbCmdData = UsbUtil.getExecutorService()
                .submit(new SendDataToUsbTask(bb_cmd, usbEndpointOut, usbDeviceConnection));

        try {
            if (futureForPutBbCmdData.get() != bb_cmd.length) {
                Thread.currentThread();
                Thread.sleep(200); /* wait for dangle handle this command */
            }
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }

        Future<Integer> futureForPutMcuCmdData = UsbUtil.getExecutorService()
                .submit(new SendDataToUsbTask(mcu_cmd, usbEndpointOut, usbDeviceConnection));

        try {
            if (futureForPutMcuCmdData.get() != mcu_cmd.length) {
                Thread.currentThread();
                Thread.sleep(200); /* wait for dangle handle this command */
            }
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
        Log.i(TAG, "终于把channelInfo设置成功了!草(一种植物)");
        Log.i(TAG, "isSelectId要被设置成 true 了");
        return true;
    }

    public void dangleConnectCertification() {
        // 生成第一次写入USB的数据
        generateReqMsg(1, reqMsg);
        // 生成第二次写入USB的数据
        generateReqMsg(2, reqMsg2);
        // 在真正读取数据之前,应该先完成一次收,两次发的动作,先后顺序是发收发
        new SendDataToUsbTask(reqMsg, usbEndpointOut, usbDeviceConnection).call();
        new ReceiveUsbDataTask(rxMsg, usbEndpointIn, usbDeviceConnection).run();
        new SendDataToUsbTask(reqMsg2, usbEndpointOut, usbDeviceConnection).call();
        Log.i(TAG, "DMB 完成首次收发");
    }


    /**
     * 设置DMB接收机的频点并且发送
     */
    public void initFrequency(byte[] frequency) {

        int byte_cnt = 0;

        frequency[byte_cnt++] = (byte) 0xff;
        frequency[byte_cnt++] = (byte) 0xff;
        frequency[byte_cnt++] = 0x01;
        frequency[byte_cnt++] = 0x2c;
        frequency[byte_cnt++] = MX_RF_I2C_ADDRESS;
        frequency[byte_cnt++] = 1;
        frequency[byte_cnt++] = 1;
        frequency[byte_cnt++] = 0;
        frequency[byte_cnt++] = 0;
        frequency[byte_cnt++] = 0;
        frequency[byte_cnt++] = 0;
        frequency[byte_cnt++] = 4;
        frequency[byte_cnt++] = (byte) (FREQKHZ >> 24);
        frequency[byte_cnt++] = (byte) (FREQKHZ >> 16);
        frequency[byte_cnt++] = (byte) (FREQKHZ >> 8);
        frequency[byte_cnt] = (byte) (FREQKHZ);
    }

    /**
     * 根据类型生成发送的数据格式
     *
     * @param type   类型
     * @param reqMsg 数据
     */
    private void generateReqMsg(int type, byte[] reqMsg) {
        if (reqMsg == null || reqMsg.length == 0) {
            return;
        }
        reqMsg[0] = (byte) 0xff;
        reqMsg[1] = (byte) 0xff;
        if (type == 1) {
            reqMsg[2] = (byte) 0x10;
        } else if (type == 2) {
            reqMsg[2] = (byte) 0x11;
        }
        reqMsg[3] = (byte) '9';
        reqMsg[4] = (byte) 'C';
        reqMsg[5] = (byte) 'Q';
        reqMsg[6] = (byte) 'U';
        reqMsg[7] = (byte) 'P';
        reqMsg[8] = (byte) 'T';
        reqMsg[9] = (byte) 'D';
        reqMsg[10] = (byte) 'M';
        reqMsg[11] = (byte) 'B';
        reqMsg[12] = (byte) 0x00;
    }


}
