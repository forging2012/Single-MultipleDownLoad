package com.dali.admin.okhttp.singledown.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dali.admin.okhttp.singledown.module.ThreadInfos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/4/1.
 */

public class ThreadDAOImpl implements ThreadDAO{


    private DBHelper mDBHelper = null;

    public ThreadDAOImpl(Context context) {
        mDBHelper = new DBHelper(context);
    }

    @Override
    public void insertThreadInfo(ThreadInfos threadInfos) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{threadInfos.getId(),threadInfos.getUrl(),threadInfos.getStart(),threadInfos.getEnd(),threadInfos.getComplete()});
        db.close();
    }

    @Override
    public void deleteThreadInfo(String url, int threadId) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ? and thread_id = ?",
                new Object[]{url,threadId});
        db.close();
    }

    @Override
    public void updateThreadInfo(String url, int threadId, int completeSize) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
                new Object[]{completeSize,url,completeSize});
        db.close();
    }

    @Override
    public List<ThreadInfos> queryThreadInfo(String url) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<ThreadInfos> infosList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?",
                new String[]{url});
        while (cursor.moveToNext()){
            ThreadInfos threadInfos = new ThreadInfos();
            threadInfos.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            threadInfos.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            threadInfos.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            threadInfos.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
            threadInfos.setComplete(cursor.getInt(cursor.getColumnIndex("finished")));

            infosList.add(threadInfos);
        }

        return infosList;
    }

    @Override
    public boolean isExists(String url, int threadId) {

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url,threadId+""});
        boolean exists = cursor.moveToNext();
        cursor.close();
        db.close();
        return exists;
    }
}
