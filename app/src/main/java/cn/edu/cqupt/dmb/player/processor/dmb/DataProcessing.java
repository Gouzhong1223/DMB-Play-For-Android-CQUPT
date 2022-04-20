package cn.edu.cqupt.dmb.player.processor.dmb;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是处理DMB数据的接口
 * @Date : create by QingSong in 2022-03-20 23:33
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public interface DataProcessing {

    /**
     * 处理 DMB 数据
     *
     * @param usbData 从 USB 中读取到的一段 DMB 数据
     */
    void processData(byte[] usbData);

}
