package cn.edu.cqupt.dmb.player.broadcast;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.processor.dmb.FicDataProcessor;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是接收 DMB USB 设备插拔的广播接收器
 * @Date : create by QingSong in 2022-03-25 12:58
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.broadcast
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION_USB_PERMISSION = DmbPlayerConstant.ACTION_USB_PERMISSION.getDmbConstantDescribe();

    private UsbManager usbManager;

    private final Context context;

    @SuppressLint("StaticFieldLeak")
    private static volatile DmbBroadcastReceiver dmbBroadcastReceiver;

    private static final int VID = DmbPlayerConstant.DMB_V_ID.getDmbConstantValue();
    private static final int PID = DmbPlayerConstant.DMB_P_ID.getDmbConstantValue();

    private DmbBroadcastReceiver(Context context) {
        this.context = context;
    }


    /**
     * 获取 DmbBroadcastReceiver 实例
     *
     * @param context context
     * @return 单例模式下创建的 DmbBroadcastReceiver
     */
    public static DmbBroadcastReceiver getInstance(Context context) {
        if (dmbBroadcastReceiver == null) {
            synchronized (DmbBroadcastReceiver.class) {
                if (dmbBroadcastReceiver == null) {
                    dmbBroadcastReceiver = new DmbBroadcastReceiver(context);
                }
            }
        }
        return dmbBroadcastReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // 是否接收到自定义广播
        if (ACTION_USB_PERMISSION.equals(action)) {
            // 这里之所以加这么一个判断,是因为在测试的过程中发现了一个问题
            // 就是如果在运行的过程中拔出USB,然后再插入,那么这个方法会被调用多次,次数是1+2^n次,n代表USB插拔的次数
            // 所以现在用一个状态位来控制线程池的调度,如果USB已经是就绪的状态,就不再往线程池提交任务了
            if (DataReadWriteUtil.USB_READY) {
                return;
            }
            // 如果收到这个广播就证明用户已经授予了USB读取的权限
            synchronized (this) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Log.e(TAG, System.currentTimeMillis() + "---接收到广播,action:" + action);
                // 是否授权成功
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (usbDevice != null) {
                        DataReadWriteUtil.USB_READY = true;
                        // 打开USB设备并开始读取数据
                        openDevice();
                    }
                } else {
                    Log.e(TAG, System.currentTimeMillis() + "---USB权限已被拒绝，Permission denied for device" + usbDevice);
                }
            }
        } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            // 如果走到这里,有两种情况
            // 1.说明用户暂时没有授予USB读取的权限
            // 2.用户之前授予过USB权限,做了一次插拔动作,这是授予权限之后拔出之后再插入的分支
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            assert usbDevice != null;
            if (VID == usbDevice.getVendorId() && PID == usbDevice.getProductId()) {
                // 所以在这里要多做一步判断,就是如果用户已经授予权限,那就直接打开USB设备
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    DataReadWriteUtil.USB_READY = false;
                    FicDataProcessor.isSelectId = false;
                    DataReadWriteUtil.initFlag = false;
                    // 关闭USB设备
                    closeDevice();
                } else {
                    // 如果暂时还没有授予权限,那就发起授权申请
                    // 然后再向用户发起授予权限的请求
                    Log.e(TAG, System.currentTimeMillis() + "---USB权限已被拒绝，Permission denied for device" + usbDevice);
                    openDeviceAndRequestDevice();
                }
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            // 这个广播是用户拔出USB的通知
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            assert device != null;
            if (VID == device.getVendorId() && PID == device.getProductId()) {
                DataReadWriteUtil.USB_READY = false;
                FicDataProcessor.isSelectId = false;
                DataReadWriteUtil.initFlag = false;
                // 由于是直接拔出,所以直接执行关闭USB的后置方法
                Log.e(TAG, System.currentTimeMillis() + "---USB 设备已被拔出!");
                closeDevice();
            }
        }
    }

    /**
     * 尝试直接从USB中读取数据
     */
    public void tryConnectUsbDeviceAndLoadUsbData() {
        if (usbManager == null) {
            usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        }
        // 如果当前终端没有USB设备接入,就直接返回
        if (usbManager.getDeviceList().size() == 0) {
            return;
        }
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            // 如果有就开始再设备列表里面查找DMB接收机
            if (device.getVendorId() == VID && device.getProductId() == PID) {
                // 发送自定义广播也就是发起权限请求的广播
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent =
                        PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);
                // 弹出权限框，进行权限申请
                usbManager.requestPermission(device, pendingIntent);
            }
        }
    }

    /**
     * 此方法是用于发起用户USB读取权限授予请求的方法
     * 在用户同意权限授予之后,会发送{@link DmbPlayerConstant#ACTION_USB_PERMISSION}广播
     * 次广播用于唤醒读取USB数据的线程
     */
    private void openDeviceAndRequestDevice() {
        if (usbManager == null) {
            usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        }
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            // 判断USB设备是否合法
            if (device.getVendorId() == VID && device.getProductId() == PID) {
                // 发送自定义广播
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent =
                        PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);
                // 弹出权限框，进行权限申请
                usbManager.requestPermission(device, pendingIntent);
            }
        }
    }

    /**
     * 拔出USB设备之后执行的方法
     */
    private void closeDevice() {
        // 关闭线程池,停止从USB中读取数据
        // 注意,这里是立马关闭线程!而不是简单的让线程池暂停接受任务,如果是暂停接受任务,里面已经接收的任务会继续执行!
        UsbUtil.getScheduledExecutorService().shutdownNow();
        UsbUtil.getExecutorService().shutdownNow();
        new AlertDialog.Builder(
                context)
                .setTitle("DMB设备异常")
                .setMessage("DMB设备已经被拔出,请重新插上DMB设备")
                .setPositiveButton("确定", null)
                .show();
    }

    /**
     * 插入USB设备并且通过权限申请之后执行的方法
     */
    private void openDevice() {
        UsbUtil usbUtil = new UsbUtil();
        // 开始从USB中读取数据
        usbUtil.initUsb(usbManager);
    }
}
