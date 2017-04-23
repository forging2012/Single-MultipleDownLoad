package com.dali.admin.okhttp.file;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.dali.admin.okhttp.R;
import com.dali.admin.okhttp.list.ListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 基本UI定义
 * 数据库操作，存储下载进度 读写更新
 * 四大组件的应用
 * service启动
 * 前台Activity向后台Service传递参数
 * 进度条 使用广播回传数据到Activity
 * 线程和Handler的使用
 * 网络操作
 * <p>
 * <p>
 * service和activity都是在主线程运行，创建子线程下载文件
 * 进度写入本地文件
 * 进度信息通过广播传递到activity进行更新ui
 * <p>
 * 网络下载关键点：
 * 获取网络文件长度
 * 在本地创建一个文件，设置其长度，网络文件长度定义本地文件长度，使两个文件大小一样
 * 从数据库中获得上次下载到进度
 * 从上次下载位置下载数据，同时保存进度到数据库
 * 将下载进度回传给activity
 * 下载完成后删除下载信息
 *
 *
 * Activity与service交互
 */
public class DownLoadActivity extends Activity {

//    private ProgressBar mProgressBar;
//    private TextView mTextViewName;
    //创建文件信息对象
    FileInfo fileInfo;
    private ThreadDAO mDAO;
    private String path = "http://music.163.com/api/android/download/latest2";

    private String TAG = "DownLoadActivity";

    private ListView mListView;

    private List<FileInfo> mFileInfos;
    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);

//        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
//        mTextViewName = (TextView) findViewById(R.id.tv_name);

        mListView = (ListView) findViewById(R.id.list);

        mFileInfos = new ArrayList<>();
        FileInfo fileInfo = new FileInfo(0,"http://apk.hiapk.com/appdown/com.tencent.mtt","QQ浏览器",0,0);
        FileInfo fileInfo1 = new FileInfo(1,"http://apk.hiapk.com/appdown/com.gtgroup.gtdollar","gtdollar",0,0);
        FileInfo fileInfo2 = new FileInfo(2,"http://apk.hiapk.com/appdown/com.ss.android.article.news","今日头条",0,0);
        FileInfo fileInfo3 = new FileInfo(3,"http://apk.hiapk.com/appdown/com.xunyu.vr_game","VR游戏汇",0,0);
        FileInfo fileInfo4 = new FileInfo(4,"http://apk.hiapk.com/appdown/com.tencent.news","腾讯新闻",0,0);

        mFileInfos.add(fileInfo);
        mFileInfos.add(fileInfo1);
        mFileInfos.add(fileInfo2);
        mFileInfos.add(fileInfo3);
        mFileInfos.add(fileInfo4);

        mAdapter = new ListAdapter(mFileInfos,this);

        mListView.setAdapter(mAdapter);

//        mProgressBar.setMax(100);
//        fileInfo = new FileInfo(0, path, "网易云音乐.apk", 0, 0);
//
//        mTextViewName.setText(fileInfo.getFileName());

        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
    }

//    public void doDownloadFile(View view) {
//        Intent intent = new Intent(DownLoadActivity.this, DownloadService.class);
//        intent.setAction(DownloadService.ACTION_START);
//        intent.putExtra(DownloadService.ACTION_NAME, fileInfo);
//        startService(intent);
//    }
//
//    public void doStopDownloadFile(View v) {
//        Intent intent = new Intent(DownLoadActivity.this, DownloadService.class);
//        intent.setAction(DownloadService.ACTION_STOP);
//        intent.putExtra(DownloadService.ACTION_NAME, fileInfo);
//        startService(intent);
//    }

    /**
     * 更新UI的广播接收器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {

                int finished = intent.getIntExtra("finished", 0);
                Log.e(TAG, "finished: " + finished);
//                mTextViewName.setText(fileInfo.getFileName() + "        "+finished + " / " + (fileInfo.getLength() * 100 / fileInfo.getLength()));

                int thread_finished = intent.getIntExtra("thread_finished", 0);
//                mProgressBar.setProgress(finished);
//                mProgressBar.setProgress(thread_finished);

                int id = intent.getIntExtra("id",0);

                mAdapter.updateProgress(id,finished);

            }else if (DownloadService.ACTION_FINISH.equals(intent.getAction())){
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");

                //下载完之后设置进度为0
                mAdapter.updateProgress(fileInfo.getId(),0);
                Toast.makeText(DownLoadActivity.this,"下载完毕",Toast.LENGTH_SHORT).show();
            }


        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
