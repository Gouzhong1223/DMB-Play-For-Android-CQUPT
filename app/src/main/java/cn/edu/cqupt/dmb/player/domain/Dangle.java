package cn.edu.cqupt.dmb.player.domain;


import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : Dangle 交互类
 * @Date : create by QingSong in 2022-03-15 14:08
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.domain
 * @ProjectName : DMB Player For Android 
 * @Version : 1.0.0
 */
public class Dangle {


    /**
     * 清空Dangle寄存器，初始化Dangle，在设置频点和节目之前，需要先执行初始化操作
     */
    public boolean clearRegister() {
        return true;
    }

    /**
     * 设置频点，频点的单位为KHz
     */
    public boolean setFrequency(int frequency) {
        return true;
    }

    /**
     * 设置频道 channelInfo可由Fic解码得到
     */
    public boolean SetChannel(ChannelInfo channelInfo) {
        byte[] bytes = new byte[48];
        UsbUtil.usbDeviceConnection.bulkTransfer(UsbUtil.usbEndpointOut, bytes, bytes.length, 0);
        return true;
    }


}
