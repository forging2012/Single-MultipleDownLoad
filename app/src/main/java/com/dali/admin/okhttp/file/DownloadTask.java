package com.dali.admin.okhttp.file;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 下载任务类
 * Created by admin on 2017/3/30.
 */

public class DownloadTask {

    private Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDAO mDAO = null;
    private int finished = 0;//已经下载的进度
    private String TAG = "DownloadTask";
    private int mThreadCount = 1;//线程数量

    //线程池
    public static ExecutorService mExecutorService = Executors.newCachedThreadPool();

    private List<DownLoadThread> mThreadList;//管理线程集合

    public boolean isPause = false;

    public DownloadTask(Context context, FileInfo fileInfo, int threadCount) {
        this.mContext = context;
        this.mFileInfo = fileInfo;
        this.mThreadCount = threadCount;
        mDAO = new ThreadDAOImpl(mContext);
    }

    //下载
    public void download() {

        //读取数据库的线程下载信息
        List<ThreadInfo> threads = mDAO.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
//        if (threads.size() == 0) {//第一次下载，初始化线程信息
//            //初始化线程信息对象
//            threadInfo = new ThreadInfo(0,mFileInfo.getUrl(),0,mFileInfo.getLength(),0);
//
//        }else {
//            threadInfo = threads.get(0);//单线程，只有一个
//        }

        if (threads.size() == 0) {
            //获取每个线程下载的长度
            int length = mFileInfo.getLength() / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                //创建线程信息
                threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), length * i, (i + 1) * length - 1, 0);
                if (i == mThreadCount - 1) {//最后一个线程，结束位置就是文件的长度
                    threadInfo.setEnd(mFileInfo.getLength());
                }

//                添加到线程集合中
                threads.add(threadInfo);

                //向数据库插入线程信息
                mDAO.insertThreadInfo(threadInfo);
            }
        }

        mThreadList = new ArrayList<>();

        //启动多个线程进行下载
        for (ThreadInfo info : threads) {
            DownLoadThread thread = new DownLoadThread(info);
//            thread.start();

            DownloadTask.mExecutorService.execute(thread);
            //添加线程到集合
            mThreadList.add(thread);
        }


        //开启下载线程
        new DownLoadThread(threadInfo).start();
    }

    /**
     * 判断是否所有线程执行完毕
     */
    private synchronized void checkAllThreadFinished() {
        boolean allFinished = true;
        for (DownLoadThread thread : mThreadList) {
            //遍历线程集合，判断线程是否执行完毕
            if (!thread.isFinidhed) {
                allFinished = false;
                break;
            }
        }

        if (allFinished) {
            mDAO.deleteThread(mFileInfo.getUrl());
            //发送广播通知UI下载任务结束
            Intent intent = new Intent(DownloadService.ACTION_FINISH);
            intent.putExtra("fileInfo", mFileInfo);
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 下载线程
     */
    class DownLoadThread extends Thread {
        private ThreadInfo mThreadInfo = null;
        private boolean isFinidhed = false;//线程是否执行完毕

        public DownLoadThread(ThreadInfo threadInfo) {
            mThreadInfo = threadInfo;
        }

        @Override
        public void run() {
            super.run();

            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream in = null;
//            //向数据库插入线程信息
//            if (!mDAO.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
//                mDAO.insertThreadInfo(mThreadInfo);
//            }

            try {
                //设置下载位置
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");

                //设置下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                //下载范围  开始到结束
                conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());


                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");

                //seek 在读写的时候跳过设置好的字节数，从下一个字节数开始读写
                raf.seek(start);

                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                finished += mThreadInfo.getFinished();


                //开始下载
                //找到上次下载位置进行下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    //读取数据
                    in = conn.getInputStream();
                    byte[] buf = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = in.read(buf)) != -1) {
                        //写入文件
                        raf.write(buf, 0, len);

                        finished += len;

                        //累加每个线程完成到进度
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + len);

                        //500毫秒发送一次广播
                        if (System.currentTimeMillis() - time > 1000) {

//                            Log.e(TAG,"finished--"+finished);

                            //把下载进度发送广播到activity，以百分百形式发送
                            intent.putExtra("finished", finished * 100 / mFileInfo.getLength());
//                            intent.putExtra("thread_finished", finished * 100 / mThreadInfo.getEnd());

                            intent.putExtra("id",mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                        }

                        //在下载暂停时保存进度
                        if (isPause) {
                            mDAO.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), finished);

                            Log.e(TAG, "updateThread:--------" + mThreadInfo.getFinished());
                            return;
                        }
                    }

//                    标识线程执行完毕
                    isFinidhed = true;

                    //删除线程信息
                    mDAO.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());

                    //监测下载任务是否执行完毕
                    checkAllThreadFinished();

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.disconnect();
                    in.close();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
