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

package cn.edu.cqupt.dmb.player.utils;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

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
     * @param context       调用方法的 context
     * @param title         对话标题
     * @param message       对话消息
     * @param dialogButtons 按钮数组
     * @return QMUIDialog.MessageDialogBuilder
     */
    public static MaterialDialog.Builder generateDialog(Context context, String title, String message, List<DialogButton> dialogButtons) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).title(title).content(message);
        if (dialogButtons != null && Objects.requireNonNull(dialogButtons).size() != 0) {
            dialogButtons.forEach(e -> {
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
     * @param context       调用方法的 context
     * @param title         对话框标题
     * @param message       对话框消息
     * @param dialogButtons 可变数组,传的是按钮
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder generateDialog(Context context, String title, String message, DialogButton... dialogButtons) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).title(title).content(message);
        if (dialogButtons.length != 0) {
            for (DialogButton e : dialogButtons) {
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
     * 生成对话框
     *
     * @param context       调用方法的 context
     * @param title         对话框标题
     * @param message       对话框消息
     * @param dialogButtons 可变数组,传的是按钮
     * @return MaterialDialog.Builder
     */
    public static AlertDialog.Builder generateDialog(Context context, int themeResId, String title, String message, AlertDialogButton... dialogButtons) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, themeResId).setTitle(title).setMessage(message);
        if (dialogButtons.length != 0) {
            for (AlertDialogButton e : dialogButtons) {
                if (e.getButtonEnum() == DialogButtonEnum.POSITIVE) {
                    builder.setPositiveButton(e.getBtnText(), e.getListener());
                } else if (e.getButtonEnum() == DialogButtonEnum.NEGATIVE) {
                    builder.setNegativeButton(e.getBtnText(), e.getListener());
                } else {
                    builder.setNeutralButton(e.getBtnText(), e.getListener());
                }
            }
        }
        return builder;
    }

    /**
     * 获取按钮列表
     *
     * @param dialogButtons 按钮,可变数组
     * @return List<DialogButton>
     */
    public static List<DialogButton> getPositiveButtonList(DialogButton... dialogButtons) {
        if (dialogButtons.length != 0) {
            return new ArrayList<>(Arrays.asList(dialogButtons));
        }
        return null;
    }

    public enum DialogButtonEnum {
        /**
         * 确定按钮
         */
        POSITIVE,
        /**
         * 取消按钮
         */
        NEGATIVE,
        /**
         * 中性按钮
         */
        NEUTRAL
    }

    /**
     * 普通 Dialog 对话框的按钮
     */
    public static class AlertDialogButton {
        /**
         * 按钮的类型
         */
        private final DialogButtonEnum buttonEnum;

        /**
         * 按钮的监听器
         */
        private final DialogInterface.OnClickListener listener;
        /**
         * 按钮文本
         */
        private final String btnText;

        public AlertDialogButton(DialogButtonEnum buttonEnum, DialogInterface.OnClickListener listener, String btnText) {
            this.buttonEnum = buttonEnum;
            this.listener = listener;
            this.btnText = btnText;
        }

        public DialogButtonEnum getButtonEnum() {
            return buttonEnum;
        }

        public DialogInterface.OnClickListener getListener() {
            return listener;
        }

        public String getBtnText() {
            return btnText;
        }
    }

    /**
     * 装载按钮以及监听器
     */
    public static class DialogButton {

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

        public DialogButton(DialogButtonEnum buttonEnum, SingleButtonCallback listener, String btnText) {
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
