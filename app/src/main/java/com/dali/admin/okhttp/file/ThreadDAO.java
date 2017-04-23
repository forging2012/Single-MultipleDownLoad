package com.dali.admin.okhttp.file;

import java.util.List;

/**
 * 数据访问接口
 * Created by admin on 2017/3/30.
 */

public interface ThreadDAO {

    /**
     * 插入线程信息
     * @param threadInfo
     */
    void insertThreadInfo(ThreadInfo threadInfo);

    /**
     * 删除线程
     * @param url
     * @param thread_id
     */
    void deleteThread(String url, int thread_id);

    /**
     * 删除线程
     * @param url
     */
    void deleteThread(String url);

    /**
     * 更新线程下载进度
     * @param url
     * @param thread_id
     * @param finished
     */
    void updateThread(String url, int thread_id, int finished);

    /**
     * 查询线程，以集合形式返回
     * @param url
     * @return
     */
    List<ThreadInfo> getThreads(String url);

    /**
     * 线程信息是否存在
     * @param url
     * @param thread_id
     * @return
     */
    boolean isExists(String url, int thread_id);
}
