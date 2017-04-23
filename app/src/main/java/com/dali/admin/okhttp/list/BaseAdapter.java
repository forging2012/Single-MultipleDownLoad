package com.dali.admin.okhttp.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by admin on 2017/3/18.
 */

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {

    protected List<T> mDatas;
    protected Context mContext;
    protected int mLayoutId;

    public BaseAdapter(List<T> datas, Context context, int layoutId) {
        mDatas = datas;
        mContext = context;
        this.mLayoutId = layoutId;
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas == null ? null : mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BaseViewHolder holder = BaseViewHolder.getViewHolder(mContext, convertView, parent, mLayoutId, position);

        T t = mDatas.get(position);

        bindData(holder, t);

        return holder.getConvertView();
    }

    public abstract void bindData(BaseViewHolder holder, T t);
}
