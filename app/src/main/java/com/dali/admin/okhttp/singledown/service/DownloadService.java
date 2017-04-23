package com.dali.admin.okhttp.singledown.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dali.admin.okhttp.singledown.module.FileInfos;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by admin on 2017/3/31.
 */

public class DownloadService extends Service {

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String FILE_INFO = "FILE_INFO";
    public static final String FILE_LEN = "FILE_LEN";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String TAG = "DownloadService";
    public static final int MSG_INIT = 0;
    private DownloadTask mTask = null;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //获取Activity传递的参数
        if (ACTION_START.equals(intent.getAction())) {
            FileInfos fileInfos = (FileInfos) intent.getSerializableExtra(FILE_INFO);

            Log.e(TAG, "start: " + fileInfos.toString());

            new InitThread(fileInfos).start();

        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfos fileInfos = (FileInfos) intent.getSerializableExtra(FILE_INFO);

            Log.e(TAG, "stop: " + fileInfos.toString());

            if (mTask != null) {
                mTask.isPause = true;
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_INIT:
                    FileInfos fileInfos = (FileInfos) msg.obj;
                    Log.e(TAG, "MSG_INIT: " + fileInfos.toString());
                    //启动下载任务
                    mTask = new DownloadTask(DownloadService.this, fileInfos);
                    mTask.download();
            }

        }
    };

    /**
     * 初始化子线程
     */
    class InitThread extends Thread {
        private FileInfos mFileInfo;

        public InitThread(FileInfos fileInfos) {
            mFileInfo = fileInfos;
        }

        @Override
        public void run() {
            super.run();

            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            Intent intent;
            try {
                //连接网络文件
                URL url = new URL(mFileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                int len = -1;
                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    //获取文件长度
                    len = conn.getContentLength();

                    intent = new Intent(FILE_LEN);
                    intent.putExtra("length", len);
                    startActivity(intent);

                    System.out.println("service---" + len);

                }
                if (len <= 0) {
                    return;
                }

                //在本地创建相同大小的文件
                File dir = new File(FILE_PATH);

                if (!dir.exists())
                    dir.mkdir();

                File file = new File(dir,mFileInfo.getFilename());
                raf = new RandomAccessFile(file, "rwd");

                //设置文件长度
                raf.setLength(len);
                mFileInfo.setLength(len);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (raf!=null){
                        raf.close();
                    }if (conn!=null){
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
