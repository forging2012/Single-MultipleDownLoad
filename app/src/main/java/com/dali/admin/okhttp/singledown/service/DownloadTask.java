package com.dali.admin.okhttp.singledown.service;

import android.content.Context;
import android.content.Intent;

import com.dali.admin.okhttp.singledown.db.ThreadDAO;
import com.dali.admin.okhttp.singledown.db.ThreadDAOImpl;
import com.dali.admin.okhttp.singledown.module.FileInfos;
import com.dali.admin.okhttp.singledown.module.ThreadInfos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by admin on 2017/4/1.
 */

public class DownloadTask {


    private Context mContext = null;

    private FileInfos mFileInfos;

    private ThreadDAO mDAO;

    public boolean isPause = false;

    private int mComplete = 0;

    public DownloadTask(Context context, FileInfos fileInfos) {
        mContext = context;
        mFileInfos = fileInfos;
        mDAO = new ThreadDAOImpl(context);
    }


    public void download(){
        //读取数据线程信息
        List<ThreadInfos> infosList = mDAO.queryThreadInfo(mFileInfos.getUrl());

        ThreadInfos threadInfos;
        //第一次下载
        if (infosList.size() == 0){
            //初始化线程信息
            threadInfos = new ThreadInfos(0,mFileInfos.getUrl(),0,mFileInfos.getLength(),mFileInfos.getCompleteSize());
        }else {
            threadInfos = infosList.get(0);
        }

        //创建子线程进行下载
        new DownloadThread(threadInfos).start();

    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread{

        private ThreadInfos mThreadInfos;

        public DownloadThread(ThreadInfos threadInfos) {
            mThreadInfos = threadInfos;
        }

        @Override
        public void run() {
            super.run();

            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream in = null;

            //向数据库插入线程信息
            if (!mDAO.isExists(mThreadInfos.getUrl(),mThreadInfos.getId())){
                mDAO.insertThreadInfo(mThreadInfos);
            }

            //从上次下载到位置进行下载，设置下载位置
            try {
                URL url = new URL(mThreadInfos.getUrl());

                conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");

                int start = mThreadInfos.getStart()+mThreadInfos.getComplete();
                conn.setRequestProperty("Range","bytes="+start+"-"+mThreadInfos.getEnd());

                //找到文件写入位置
                File file = new File(DownloadService.FILE_PATH,mFileInfos.getFilename());
                raf = new RandomAccessFile(file,"rwd");
                raf.seek(start);
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);

                //开始下载
                mComplete += mThreadInfos.getComplete();

                if (conn.getResponseCode() == 200){
                    //读取数据
                    in = conn.getInputStream();
                    byte[] buf = new byte[1024];
                    int len = -1;

                    long time = System.currentTimeMillis();
                    while ((len = in.read(buf)) != -1){
                        //写入文件

                        raf.write(buf,0,len);

                        //把下载的进度发送广播给activity
                        mComplete += len;

                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mComplete * 100 / mFileInfos.getLength());
                            mContext.sendBroadcast(intent);
                        }

                    }
                    //下载暂停时保存下载进度

                    if (isPause){
                        mDAO.updateThreadInfo(mThreadInfos.getUrl(),mThreadInfos.getId(),mComplete);
                        return;
                    }
                }

                //删除线程信息
                mDAO.deleteThreadInfo(mThreadInfos.getUrl(),mThreadInfos.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                conn.disconnect();
                try {
                    raf.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
