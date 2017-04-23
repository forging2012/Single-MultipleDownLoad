package com.dali.admin.okhttp.file;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

/**
 * Created by admin on 2017/3/30.
 */

public class DownloadService extends Service {


    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISH = "ACTION_FINISH";
    public static final String ACTION_NAME = "fileInfo";

    private boolean isDownloading = false;

    private InitThread mInitThread;

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    private DownloadTask mDownloadTask = null;

    //下载任务集合
    private SparseArray<DownloadTask> mDownloadTasks = new SparseArray<>();

    public static final int MSG_INIT = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //获得activity传过来到数据
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(ACTION_NAME);
            Log.e(TAG, "start: " + fileInfo.toString());

            if (!isDownloading) {
                //启动初始化线程
                mInitThread = new InitThread(fileInfo);
                DownloadTask.mExecutorService.execute(mInitThread);

//                isDownloading = true;
            }
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(ACTION_NAME);
            Log.e(TAG, "stop: " + fileInfo.toString());

            //暂停下载
//            if (isDownloading) {
//                if (mDownloadTask != null) {
//                    mDownloadTask.isPause = true;
//                }
//            }

            //从集合中取出下载任务
            DownloadTask downloadTask = mDownloadTasks.get(fileInfo.getId());
            if (downloadTask != null){
                //停止下载任务
                downloadTask.isPause = true;
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
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.e(TAG, "MSG_INIT:  " + fileInfo.toString());
                    //启动下载任务
                    mDownloadTask = new DownloadTask(DownloadService.this, fileInfo,3);
//                    mDownloadTask.download();
                    //将下载任务添加到集合
                    mDownloadTasks.put(fileInfo.getId(),mDownloadTask);

                    mDownloadTask.download();
                    break;
            }
        }
    };


    /**
     * 初始化工作子线程
     */

    class InitThread extends Thread {

        private FileInfo mFileInfo = null;

        public InitThread(FileInfo fileInfo) {
            this.mFileInfo = fileInfo;
        }

        @Override
        public void run() {

            HttpURLConnection conn = null;
            RandomAccessFile raf = null;

            try {
                //连接网络文件
                URL url = new URL(mFileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                //获取文件长度
                int len = -1;
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    len = conn.getContentLength();
                }
                if (len < 0) {
                    return;
                }

                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }

                //在本地创建文件，设置文件长度和网络长度相同
                //r:read   w:write    d:delete
                File file = new File(dir, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");

                //设置文件长度
                raf.setLength(len);

                mFileInfo.setLength(len);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
