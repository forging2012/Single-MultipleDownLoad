package com.dali.admin.okhttp.singledown.module;

/**
 * Created by admin on 2017/3/31.
 */

public class ThreadInfos {

    private int id;//线程id
    private String url;//url 和文件url一致
    private int start;//开始位置
    private int end;//结束位置
    private int complete;//完成长度

    @Override
    public String toString() {
        return "ThreadInfos{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", complete=" + complete +
                '}';
    }

    public ThreadInfos() {
    }

    public ThreadInfos(int id, String url, int start, int end, int complete) {
        this.id = id;
        this.url = url;
        this.start = start;
        this.end = end;
        this.complete = complete;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }
}
