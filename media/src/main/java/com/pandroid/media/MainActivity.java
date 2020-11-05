package com.pandroid.media;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.pandroid.media.R;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("jxffmpegrun");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("avutil");
        System.loadLibrary("swscale");
        System.loadLibrary("fdkaac");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v){
//        pb.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String basePath = Environment.getExternalStorageDirectory().getPath();

                String cmd_transcoding = String.format("ffmpeg -i %s -c:v libx264 %s  -c:a libfdk_aac %s",
                        basePath+"/"+"girl.mp4",
                        "-crf 40",
                        basePath+"/"+"my_girl.mp4");
                int i = ffmpeg_recorder_start(cmd_transcoding);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
//                        pb.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this,"ok了",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    public native int ffmpeg_recorder_start(String[] cmd);
}
