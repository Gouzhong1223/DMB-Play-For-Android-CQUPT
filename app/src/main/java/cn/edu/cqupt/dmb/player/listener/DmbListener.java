package cn.edu.cqupt.dmb.player.listener;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-22 21:22
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public interface DmbListener {
    void onSuccess(String fileName, byte[] bytes, int length);

    void onReceiveMessage(String msg);
}
