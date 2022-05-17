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
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;

import cn.edu.cqupt.dmb.player.common.DangleType;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;
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
    /**
     * 跳转到默认场景的消息
     */
    private static final int MESSAGE_JUMP_DEFAULT_ACTIVITY = DmbPlayerConstant.MESSAGE_JUMP_DEFAULT_ACTIVITY.getDmbConstantValue();
    private static final ArrayList<DmbUsbDevice> DMB_USB_DEVICES = new ArrayList<>();
    /**
     * 广播单例对象
     */
    @SuppressLint("StaticFieldLeak")
    private static volatile DmbBroadcastReceiver dmbBroadcastReceiver;
    /**
     * Dangle 设备类型
     */
    private static DangleType dangleType;

    static {
        // STM32 型号 Dangle
        DMB_USB_DEVICES.add(new DmbUsbDevice(1155, 22336));
        // NUC 型号 Dangle
        DMB_USB_DEVICES.add(new DmbUsbDevice(1003, 24868));
        // AT 型号 Dangle
        DMB_USB_DEVICES.add(new DmbUsbDevice(1046, 20497));
    }

    /**
     * 调用广播的 ctx
     */
    private final Context context;
    /**
     * 主页面的回调处理器
     */
    private final Handler handler;
    /**
     * 默认的使用场景
     */
    private final SceneInfo defaultSceneInfo;
    /**
     * USB 设备管理器
     */
    private UsbManager usbManager;

    private DmbBroadcastReceiver(Context context, Handler handler, SceneInfo defaultSceneInfo) {
        this.handler = handler;
        this.context = context;
        this.defaultSceneInfo = defaultSceneInfo;
    }


    /**
     * 获取 DmbBroadcastReceiver 实例
     *
     * @param context context
     * @return 单例模式下创建的 DmbBroadcastReceiver
     */
    public static DmbBroadcastReceiver getInstance(Context context, Handler handler, SceneInfo defaultSceneInfo) {
        if (dmbBroadcastReceiver == null) {
            synchronized (DmbBroadcastReceiver.class) {
                if (dmbBroadcastReceiver == null) {
                    dmbBroadcastReceiver = new DmbBroadcastReceiver(context, handler, defaultSceneInfo);
                }
            }
        }
        return dmbBroadcastReceiver;
    }

    /**
     * 校验当前插入设备的 USB 是否是合法的 Dangle 设备
     *
     * @param dmbUsbDevice Dangle
     * @return 合法->true
     */
    public static boolean checkUsbDevice(DmbUsbDevice dmbUsbDevice) {
        for (DmbUsbDevice usbDevice : DMB_USB_DEVICES) {
            boolean compare = usbDevice.compare(dmbUsbDevice);
            if (compare) {
                if (usbDevice.VID == 1155) {
                    dangleType = DangleType.STM32;
                } else {
                    dangleType = DangleType.NUC;
                }
                return true;
            }
        }
        return false;
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
                Log.i(TAG, System.currentTimeMillis() + "---接收到广播,action:" + action);
                // 是否授权成功
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (usbDevice != null) {
                        Log.i(TAG, Thread.currentThread().getName() + "线程现在DataReadWriteUtil.USB_READY被设置为 true");
                        DataReadWriteUtil.USB_READY = true;
                        // 发送跳转到默认场景的消息
                        handler.sendEmptyMessage(MESSAGE_JUMP_DEFAULT_ACTIVITY);
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
            DmbUsbDevice dmbUsbDevice = new DmbUsbDevice(usbDevice.getVendorId(), usbDevice.getProductId());
            if (checkUsbDevice(dmbUsbDevice)) {
                // 所以在这里要多做一步判断,就是如果用户已经授予权限,那就直接打开USB设备
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    usbDetached();
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
            DmbUsbDevice dmbUsbDevice = new DmbUsbDevice(device.getVendorId(), device.getProductId());
            if (checkUsbDevice(dmbUsbDevice)) {
                usbDetached();
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
            DmbUsbDevice dmbUsbDevice = new DmbUsbDevice(device.getVendorId(), device.getProductId());
            if (checkUsbDevice(dmbUsbDevice)) {
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
            DmbUsbDevice dmbUsbDevice = new DmbUsbDevice(device.getVendorId(), device.getProductId());
            if (checkUsbDevice(dmbUsbDevice)) {
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
        UsbUtil usbUtil = new UsbUtil(dangleType);
        // 开始从USB中读取数据
        usbUtil.initUsb(usbManager, defaultSceneInfo);
    }

    /**
     * USB设备拔出操作
     */
    private void usbDetached() {
        DataReadWriteUtil.USB_READY = false;
        FicDataProcessor.isSelectId = false;
        DataReadWriteUtil.initFlag = false;
    }

    /**
     * 自定义装载 Dangle VID 和 PID 的类
     */
    public static class DmbUsbDevice {
        /**
         * 厂商 ID
         */
        private final Integer VID;
        /**
         * 设备 ID
         */
        private final Integer PID;

        public DmbUsbDevice(Integer VID, Integer PID) {
            this.VID = VID;
            this.PID = PID;
        }

        public Integer getVID() {
            return VID;
        }

        public Integer getPID() {
            return PID;
        }

        /**
         * 比较两个 Dangle 设备是否是同一类
         *
         * @param dmbUsbDevice Dangle 设备
         * @return 同一类->true
         */
        private boolean compare(DmbUsbDevice dmbUsbDevice) {
            return Objects.equals(PID, dmbUsbDevice.getPID()) && Objects.equals(VID, dmbUsbDevice.getVID());
        }
    }
}
