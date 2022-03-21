package cn.edu.cqupt.dmb.player.processor;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 默认的 DMB 数据处理器
 * @Date : create by QingSong in 2022-03-20 23:55
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DefaultDataProcessor implements DataProcessing {
    @Override
    public void processData(byte[] usbData) {
        System.out.println("未知的 DMB 数据");
    }
}
