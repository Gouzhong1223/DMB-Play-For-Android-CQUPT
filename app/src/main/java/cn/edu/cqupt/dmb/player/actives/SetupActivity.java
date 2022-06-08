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

package cn.edu.cqupt.dmb.player.actives;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.actives.fragment.PlaySettingFragment;
import cn.edu.cqupt.dmb.player.actives.fragment.PresetFragment;
import cn.edu.cqupt.dmb.player.actives.fragment.SceneManagementFragment;

/**
 * @author qingsong
 */
public class SetupActivity extends Activity {

    private final String[] sensesList = new String[]{"添加场景", "场景管理", "播放设置", "帮助及反馈"};
    private FragmentManager fragmentManager;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_setup2);
        initView();
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
        fragmentManager = getFragmentManager();
        listView = findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.dmb_list_item, sensesList));
        listView.setSelection(0);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0: {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frameLayout, new PresetFragment());
                        fragmentTransaction.commit();
                        break;
                    }
                    case 1: {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frameLayout, new SceneManagementFragment());
                        fragmentTransaction.commit();
                        break;
                    }
                    case 2: {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frameLayout, new PlaySettingFragment());
                        fragmentTransaction.commit();
                        break;
                    }
                    case 3: {
                        break;
                    }
                    default:
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            switch (i) {
                case 0: {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, new PresetFragment());
                    fragmentTransaction.commit();
                    break;
                }
                case 1: {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, new SceneManagementFragment());
                    fragmentTransaction.commit();
                    break;
                }
                case 2: {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, new PlaySettingFragment());
                    fragmentTransaction.commit();
                    break;
                }
                default:
            }
        });
    }
}
