package com.dali.admin.okhttp.file;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据访问接口实现类
 * Created by admin on 2017/3/30.
 */

public class ThreadDAOImpl implements ThreadDAO {

    private DBHelper mDBHelper = null;

    public ThreadDAOImpl(Context context) {
        mDBHelper = DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertThreadInfo(ThreadInfo threadInfo) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values (?,?,?,?,?)"
                , new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});

        db.close();
    }

    @Override
    public synchronized void deleteThread(String url, int thread_id) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ? and thread_id = ?"
                , new Object[]{url,thread_id});

        db.close();
    }

    @Override
    public void deleteThread(String url) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ?"
                , new Object[]{url});

        db.close();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?"
                , new Object[]{finished, url, thread_id});

        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<ThreadInfo> infoList = new ArrayList<>();

        Cursor cursor = db.rawQuery("select * from thread_info where url = ?",
                new String[]{url});
        while (cursor.moveToNext()){
            ThreadInfo threadInfo = new ThreadInfo();
            threadInfo.setId(cursor.getInt(0));
            threadInfo.setUrl(cursor.getString(1));
            threadInfo.setStart(cursor.getInt(2));
            threadInfo.setEnd(cursor.getInt(3));
            threadInfo.setFinished(cursor.getInt(4));
            infoList.add(threadInfo);
        }

        db.close();
        return infoList;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url,thread_id+""});
       boolean isExists = cursor.moveToNext();
        cursor.close();
        db.close();
        return isExists;
    }
}
