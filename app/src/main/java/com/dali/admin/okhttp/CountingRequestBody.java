package com.dali.admin.okhttp;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by admin on 2017/3/28.
 */

public class CountingRequestBody extends RequestBody{


    //代理
    private RequestBody mDelegate;
    private Listener mListener;
    private CountingSink mCountingSink;

    public CountingRequestBody(RequestBody delegate, Listener listener) {
        this.mDelegate = delegate;
        this.mListener = listener;
    }

    public interface Listener{
        //                  已写字节数            总共字节数
        void onRequestProgress(long byteWrited,long contentLength);

    }

    @Override
    public MediaType contentType() {
        return mDelegate.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {

        mCountingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(mCountingSink);
        mDelegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink{

        private long bytesWritten;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);

            bytesWritten += byteCount;

            mListener.onRequestProgress(bytesWritten,contentLength());
        }
    }

    @Override
    public long contentLength() {
        try {
            return mDelegate.contentLength();
        } catch (IOException e) {
            return -1;
        }
    }
}
