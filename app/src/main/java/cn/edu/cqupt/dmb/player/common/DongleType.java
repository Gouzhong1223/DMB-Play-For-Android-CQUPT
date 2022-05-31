package cn.edu.cqupt.dmb.player.common;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是 Dongle 类型的枚举
 * @Date : create by QingSong in 2022-05-04 21:41
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.common
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public enum DongleType {
    /**
     * STM32 类型 Dongle<br/>
     * 单次接收数据是 64 字节<br/>
     * 单次发送数据书 48 字节
     */
    STM32,
    /**
     * NUC 类型 Dongle<br/>
     * 单次接收数据是 776 字节<br/>
     * 单次发送数据书 48 字节
     */
    NUC,
    /**
     * AT 类型 Dongle<br/>
     * 单次接收数据是 776 字节<br/>
     * 单次发送数据书 48 字节
     */
    AT,
}
