package cn.edu.cqupt.dmb.player.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是生成对话提示框的工具类
 * @Date : create by QingSong in 2022-04-25 11:04
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.4
 */
public class DialogUtil {

    /**
     * 生成一个对话康
     *
     * @param context         调用方法的 context
     * @param title           对话标题
     * @param message         对话消息
     * @param positiveButtons 按钮数组
     * @return AlertDialog.Builder
     */
    public static AlertDialog.Builder generateDialog(Context context,
                                                     String title,
                                                     String message,
                                                     List<PositiveButton> positiveButtons) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message);
        if (positiveButtons != null && Objects.requireNonNull(positiveButtons).size() != 0) {
            positiveButtons.forEach(e -> builder.setPositiveButton(e.getBtnText(), e.getListener()));
        }
        return builder;
    }

    public static AlertDialog.Builder generateDialog(Context context,
                                                     String title,
                                                     String message,
                                                     PositiveButton... positiveButtons) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message);
        if (positiveButtons.length != 0) {
            for (PositiveButton positiveButton : positiveButtons) {
                builder.setPositiveButton(positiveButton.getBtnText(), positiveButton.getListener());
            }
        }
        return builder;
    }

    /**
     * 获取按钮列表
     *
     * @param positiveButtons 按钮,可变数组
     * @return List<PositiveButton>
     */
    public static List<PositiveButton> getPositiveButtonList(PositiveButton... positiveButtons) {
        if (positiveButtons.length != 0) {
            return new ArrayList<>(Arrays.asList(positiveButtons));
        }
        return null;
    }

    /**
     * 装载按钮以及监听器
     */
    public static class PositiveButton {

        /**
         * 按钮的监听器
         */
        private final DialogInterface.OnClickListener listener;
        /**
         * 按钮文本
         */
        private final String btnText;

        public PositiveButton(DialogInterface.OnClickListener listener, String btnText) {
            this.listener = listener;
            this.btnText = btnText;
        }

        public DialogInterface.OnClickListener getListener() {
            return listener;
        }

        public String getBtnText() {
            return btnText;
        }
    }

}
