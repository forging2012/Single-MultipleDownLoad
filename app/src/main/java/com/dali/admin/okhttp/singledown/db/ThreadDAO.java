package com.dali.admin.okhttp.singledown.db;

import com.dali.admin.okhttp.singledown.module.ThreadInfos;

import java.util.List;

/**
 * Created by admin on 2017/4/1.
 */

public interface ThreadDAO {

    /**
     * 插入线程信息
     * @param threadInfos
     */
    void insertThreadInfo(ThreadInfos threadInfos);

    /**
     * 删除线程信息
     * @param url
     * @param threadId
     */
    void deleteThreadInfo(String url,int threadId);

    /**
     * 更新线程下载进度
     * @param url
     * @param threadId
     * @param completeSize
     */
    void updateThreadInfo(String url,int threadId,int completeSize);

    /**
     * 查询线程信息
     * @param url
     * @return
     */
    List<ThreadInfos> queryThreadInfo(String url);


    /**
     * 判断线程信息是否存在
     * @param url
     * @param threadId
     * @return
     */
    public boolean isExists(String url,int threadId);
}
