package com.dali.admin.okhttp.list;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.dali.admin.okhttp.R;
import com.dali.admin.okhttp.file.DownloadService;
import com.dali.admin.okhttp.file.FileInfo;

import java.util.List;

/**
 * Created by admin on 2017/3/30.
 */

public class ListAdapter extends BaseAdapter<FileInfo> {

    public ListAdapter(List<FileInfo> datas, Context context) {
        super(datas, context, R.layout.list_item);
    }

    @Override
    public void bindData(BaseViewHolder holder, final FileInfo fileInfo) {

        holder.setText(R.id.tv_name,fileInfo.getFileName());
        holder.setProgressBar(R.id.progressBar,fileInfo.getFinished())
                .setProgressBarMax(R.id.progressBar,100);
        holder.setOnClickListener(R.id.btn_download, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra(DownloadService.ACTION_NAME, fileInfo);
                mContext.startService(intent);
            }
        });
        holder.setOnClickListener(R.id.btn_stop, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra(DownloadService.ACTION_NAME, fileInfo);
                mContext.startService(intent);
            }
        });
    }

    //更新列表中的进度条
    public void updateProgress(int id,int progress){
        FileInfo fileInfo = mDatas.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }
}
