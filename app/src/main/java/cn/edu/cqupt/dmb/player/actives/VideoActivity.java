package cn.edu.cqupt.dmb.player.actives;


import android.app.Activity;
import android.os.Bundle;
import android.widget.VideoView;

import cn.edu.cqupt.dmb.player.R;


public class VideoActivity extends Activity {

    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initView();
    }

    private void initView() {
        videoView = findViewById(R.id.videoView);
    }
}
