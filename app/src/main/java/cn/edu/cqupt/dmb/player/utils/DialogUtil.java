package cn.edu.cqupt.dmb.player.utils;

import android.content.Context;

import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog.SingleButtonCallback;

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
     * @return QMUIDialog.MessageDialogBuilder
     */
    public static MaterialDialog.Builder generateDialog(Context context, String title, String message, List<PositiveButton> positiveButtons) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).title(title).content(message);
        if (positiveButtons != null && Objects.requireNonNull(positiveButtons).size() != 0) {
            positiveButtons.forEach(e -> {
                if (e.getButtonEnum() == DialogButtonEnum.POSITIVE) {
                    builder.onPositive(e.getListener());
                    builder.positiveText(e.getBtnText());
                } else if (e.getButtonEnum() == DialogButtonEnum.NEGATIVE) {
                    builder.onNegative(e.getListener());
                    builder.negativeText(e.getBtnText());
                } else {
                    builder.onNeutral(e.getListener());
                    builder.neutralText(e.getBtnText());
                }
            });
        }
        return builder;
    }

    /**
     * 生成对话框
     *
     * @param context         调用方法的 context
     * @param title           对话框标题
     * @param message         对话框消息
     * @param positiveButtons 可变数组,传的是按钮
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder generateDialog(Context context, String title, String message, PositiveButton... positiveButtons) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).title(title).content(message);
        if (positiveButtons.length != 0) {
            for (PositiveButton e : positiveButtons) {
                if (e.getButtonEnum() == DialogButtonEnum.POSITIVE) {
                    builder.onPositive(e.getListener());
                    builder.positiveText(e.getBtnText());
                } else if (e.getButtonEnum() == DialogButtonEnum.NEGATIVE) {
                    builder.onNegative(e.getListener());
                    builder.negativeText(e.getBtnText());
                } else {
                    builder.onNeutral(e.getListener());
                    builder.neutralText(e.getBtnText());
                }
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

    public enum DialogButtonEnum {
        POSITIVE, NEGATIVE, NEUTRAL
    }

    /**
     * 装载按钮以及监听器
     */
    public static class PositiveButton {

        /**
         * 按钮的类型
         */
        private final DialogButtonEnum buttonEnum;

        /**
         * 按钮的监听器
         */
        private final SingleButtonCallback listener;
        /**
         * 按钮文本
         */
        private final String btnText;

        public PositiveButton(DialogButtonEnum buttonEnum, SingleButtonCallback listener, String btnText) {
            this.buttonEnum = buttonEnum;
            this.listener = listener;
            this.btnText = btnText;
        }

        public SingleButtonCallback getListener() {
            return listener;
        }

        public String getBtnText() {
            return btnText;
        }

        public DialogButtonEnum getButtonEnum() {
            return buttonEnum;
        }
    }

}
