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

package cn.edu.cqupt.dmb.player.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import cn.edu.cqupt.dmb.player.actives.MainActivity;

/**
 * @author qingsong
 */
public class AutoStartReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoStartReceiver";
    /**
     * 开机广播的频道
     */
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    /**
     * 接收广播消息后都会进入 onReceive 方法，然后要做的就是对相应的消息做出相应的处理
     *
     * @param context 表示广播接收器所运行的上下文
     * @param intent  表示广播接收器收到的Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, intent.getAction());
        Toast.makeText(context, intent.getAction(), Toast.LENGTH_LONG).show();
        // 如果系统启动的消息,则启动 APP 主页活动
        if (ACTION_BOOT.equals(intent.getAction())) {
            Log.i(TAG, "onReceive: 接收到开机的广播");
            Intent intentMainActivity = new Intent(context, MainActivity.class);
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentMainActivity);
            Toast.makeText(context, "启动完毕~", Toast.LENGTH_LONG).show();
        }
    }
}
