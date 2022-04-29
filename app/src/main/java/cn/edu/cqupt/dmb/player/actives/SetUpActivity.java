package cn.edu.cqupt.dmb.player.actives;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.utils.DialogUtil;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

public class SetUpActivity extends Activity implements View.OnClickListener {

    private static final String DEFAULT_ID_KEY = "id_key";
    private static final String DEFAULT_FREQUENCY_KEY = "frequency_key";

    private EditText editTextId;
    private EditText editTextFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_set_up);
        initView();
    }

    private void initView() {
        editTextId = findViewById(R.id.setUpEditId);
        editTextFrequency = findViewById(R.id.setUpEditFrequency);
    }

    @Override
    public void onClick(View view) {
        String id = editTextId.getText().toString();
        if (id.equals("")) {
            DialogUtil.generateDialog(this,
                            "设备ID未输入",
                            "请输入设备ID,否则无法接收DMB数据!",
                            DialogUtil.getPositiveButtonList(
                                    new DialogUtil.PositiveButton(null, "确定")))
                    .show();
            return;
        }
        String frequency = editTextFrequency.getText().toString();
        if (frequency.equals("")) {
            DialogUtil.generateDialog(this,
                            "设备工作频点未输入",
                            "请输入设备工作频点,否则无法接收 DMB 数据!",
                            DialogUtil.getPositiveButtonList(
                                    new DialogUtil.PositiveButton(null, "确定")))
                    .show();
            return;
        }
        DmbUtil.putInt(this, DEFAULT_ID_KEY, Integer.parseInt(id));
        DmbUtil.putInt(this, DEFAULT_FREQUENCY_KEY, Integer.parseInt(frequency));
        MainActivity.id = Integer.parseInt(id);
        MainActivity.frequency = Integer.parseInt(frequency);
        DialogUtil.generateDialog(this,
                        "保存成功",
                        "下次启动 APP 是将会应用您的设置并自动进入图片显示界面!",
                        DialogUtil.getPositiveButtonList(
                                new DialogUtil.PositiveButton(null, "确定")
                                , new DialogUtil.PositiveButton((dialogInterface, i) -> {
                                    Intent intent = new Intent();
                                    intent.setClass(this, CarouselActivity.class);
                                    startActivity(intent);
                                }, "跳转到图片显示页面")))
                .show();
    }
}
