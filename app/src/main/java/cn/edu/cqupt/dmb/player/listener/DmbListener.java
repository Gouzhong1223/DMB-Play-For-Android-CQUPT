package cn.edu.cqupt.dmb.player.listener;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是 DMB 数据解码器的监听器接口
 * @Date : create by QingSong in 2022-03-22 21:22
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public interface DmbListener {
    /**
     * 这个是 DMB 数据编码成功之后的回调方法
     *
     * @param fileName 文件名称
     * @param bytes    文件数组
     * @param length   文件长度
     */
    void onSuccess(String fileName, byte[] bytes, int length);

    void onReceiveMessage(String msg);
}
