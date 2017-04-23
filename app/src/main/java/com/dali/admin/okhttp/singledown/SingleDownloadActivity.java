package com.dali.admin.okhttp.singledown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dali.admin.okhttp.R;
import com.dali.admin.okhttp.singledown.module.FileInfos;
import com.dali.admin.okhttp.singledown.service.DownloadService;

public class SingleDownloadActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private TextView mTvFileName;
    private Button btnStart;
    private Button btnStop;
    private FileInfos mFileInfos;
    private String path = "http://music.163.com/api/android/download/latest2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_download);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTvFileName = (TextView) findViewById(R.id.name);
        btnStart = (Button) findViewById(R.id.start);
        btnStop = (Button) findViewById(R.id.stop);

        Intent intent = new Intent(DownloadService.FILE_LEN);

        int len = intent.getIntExtra("length",0);

        System.out.println(len+"------");

        mFileInfos = new FileInfos(0, path, "cloudMusic.apk", len, 0);

        mTvFileName.setText(mFileInfos.getFilename());

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent传递参数给service
                Intent intent = new Intent(SingleDownloadActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra(DownloadService.FILE_INFO, mFileInfos);
                startService(intent);
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent传递参数给service
                Intent intent = new Intent(SingleDownloadActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra(DownloadService.FILE_INFO, mFileInfos);
                startService(intent);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    /**
     * 更新UI广播接收
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("finished", 0);
            mProgressBar.setProgress(progress);
        }
    };

}
