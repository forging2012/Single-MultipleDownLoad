package com.dali.admin.okhttp.singledown.module;

import java.io.Serializable;

/**
 * Created by admin on 2017/3/31.
 */

public class FileInfos implements Serializable{

    private int id;
    private String url;//url
    private String filename;//文件名
    private int length;//文件长度
    private int completeSize;//完成进度

    @Override
    public String toString() {
        return "FileInfos{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", filename='" + filename + '\'' +
                ", length=" + length +
                ", completeSize=" + completeSize +
                '}';
    }

    public FileInfos() {
    }

    public FileInfos(int id, String url, String filename, int length, int completeSize) {
        this.id = id;
        this.url = url;
        this.filename = filename;
        this.length = length;
        this.completeSize = completeSize;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getCompleteSize() {
        return completeSize;
    }

    public void setCompleteSize(int completeSize) {
        this.completeSize = completeSize;
    }
}
