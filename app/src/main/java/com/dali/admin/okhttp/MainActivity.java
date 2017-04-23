package com.dali.admin.okhttp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

/**
 * 1.拿到OkHttpClient对象，单例
 * 2.构造Request
 * 2.1构造RequestBody
 * 2.2包装RequestBody
 * 3.Call -> execute
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private String mBaseUrl = "http://192.168.43.238:8080/okhttptest/";

    private OkHttpClient mHttpClient = new OkHttpClient();

    private Handler mHandler = new Handler();

    private TextView tv;
    private ImageView iv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHttpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        tv = (TextView) findViewById(R.id.tv);
        iv = (ImageView) findViewById(R.id.iv);


    }

    public void doPostForm(View view){

        RequestBody body = new FormEncodingBuilder()
                .add("username","dali")
                .add("password","1234").build();
        final Request request = new Request
                .Builder()
                .post(body)
                .url(mBaseUrl + "postForm")
                .build();


        //将 Request 封装成 call
        //执行 call
        executeRequest(request);
    }

    public void doCacheControl(View view) {
        //创建缓存对象
        CacheControl.Builder builder = new CacheControl.Builder();
        builder.maxAge(10, TimeUnit.MILLISECONDS);
        CacheControl cacheControl = builder.build();

        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        File cacheDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/cache");
        Cache cache = new Cache(cacheDirectory, cacheSize);

        System.out.println("cache: "+cacheDirectory.getAbsolutePath());

        final Request request = new Request
                .Builder()
                .get()
                .cacheControl(cacheControl)
                .url("http://publicobject.com/helloworld.txt")
                .build();

        mHttpClient.setCache(cache);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Call call1 = mHttpClient.newCall(request);
                    Response response1 = call1.execute();
                    String s = response1.body().string();
                    System.out.println(s);
                    System.out.println("response1.cacheResponse()" + response1.cacheResponse());
                    System.out.println("response1.networkResponse()" + response1.networkResponse());

                    Call call2 = mHttpClient.newCall(request);
                    Response response2 = call2.execute();
                    String s1 = response2.body().string();
                    System.out.println(s1);
                    System.out.println("response2.cacheResponse()" + response2.cacheResponse());
                    System.out.println("response2.networkResponse()" + response2.networkResponse());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                mHttpClient.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Request request, IOException e) {
//
//                    }
//
//                    @Override
//                    public void onResponse(Response response) throws IOException {
//
//                        String string = response.body().string();
//                        System.out.println(string);
//
//                        System.out.println(response.cacheResponse() + ","+response.networkResponse());
//
////                        //下载进度
////                        final long total = response.body().contentLength();
////                        long sum = 0;
////
////                        InputStream is = response.body().byteStream();
////
////                        File file = new File(Environment.getExternalStorageDirectory(), "dalidali.jpg");
////                        FileOutputStream fos = new FileOutputStream(file);
////                        int len = 0;
////                        byte[] buf = new byte[1024];
////
////                        while ((len = is.read(buf)) != -1) {
////
////                            fos.write(buf, 0, len);
////
////                            sum += len;
////
////                            Log.e(TAG, sum + " / " + total);
////
////                            final long finalSum = sum;
////                            runOnUiThread(new Runnable() {
////                                @Override
////                                public void run() {
////                                    tv.setText(finalSum + " / " + total);
////                                }
////                            });
////                        }
////
////                        fos.flush();
////                        fos.close();
////                        is.close();
//                    }
//                });
//
//            }
//        });

    }

    class CacheInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());

            return response.newBuilder().removeHeader("pragma")
                    .header("Cache-Control","max-age=60")
                    .build();
        }
    }

    //    判断网络是否链接
    private boolean isNetworkConnected(Context context) {

        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isAvailable();
            }
        }
        return false;
    }

    public void onGet(View view) {

        final Request request = new Request
                .Builder()
                .get()
                .url(mBaseUrl + "login?username=dali&password=1234")
                .build();

        //将 Request 封装成 call
        //执行 call
        executeRequest(request);
    }

    public void doUpload(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "/DCIM/Camera/dali.jpg");
        Log.e(TAG, "path:    " + file.getAbsolutePath());
        if (!file.exists()) {
            Log.e(TAG, file.getAbsolutePath() + " is not exits !");
            return;
        }

        MultipartBuilder multipartBuilder = new MultipartBuilder();

        RequestBody requestBody = multipartBuilder
                .type(MultipartBuilder.FORM)
                .addFormDataPart("username", "dali")
                .addFormDataPart("password", "1234")
                .addFormDataPart("mPic", "dali2.jpg", RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        CountingRequestBody countingRequestBody = new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long byteWrited, long contentLength) {
                Log.e(TAG, byteWrited + " / " + contentLength);
            }
        });

        Request request = new Request.Builder()
                .post(countingRequestBody)
                .url(mBaseUrl + "uploadFile")
                .build();

        executeRequest(request);
    }

    public void doDownload(View view) {

        final Request request = new Request
                .Builder()
                .get()
                .url(mBaseUrl + "files/dali.jpg")
                .build();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.e(TAG, "onFailure");
                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {

                        Log.e(TAG, "onResponse");

                        if (response.isSuccessful()) {

                            //下载进度
                            final long total = response.body().contentLength();
                            long sum = 0;

                            InputStream is = response.body().byteStream();

                            File file = new File(Environment.getExternalStorageDirectory(), "dalidali.jpg");
                            FileOutputStream fos = new FileOutputStream(file);
                            int len = 0;
                            byte[] buf = new byte[1024];

                            while ((len = is.read(buf)) != -1) {

                                fos.write(buf, 0, len);

                                sum += len;

                                Log.e(TAG, sum + " / " + total);

                                final long finalSum = sum;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv.setText(finalSum + " / " + total);
                                    }
                                });
                            }

                            fos.flush();
                            fos.close();
                            is.close();


                            Log.e(TAG, "download success !");
                        }
                    }
                });
            }
        });
    }

    public void doDownloadImage(View view) {

        final Request request = new Request
                .Builder()
                .get()
                .url(mBaseUrl + "files/dali.jpg")
                .build();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.e(TAG, "onFailure");
                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {

                        Log.e(TAG, "onResponse");

                        if (response.isSuccessful()) {

                            final long total = response.body().contentLength();
                            InputStream is = response.body().byteStream();
                            InputStream iss = response.body().byteStream();
                            int sum = 0;

                            File file = new File(Environment.getExternalStorageDirectory(), "dalidali.jpg");
                            FileOutputStream fos = new FileOutputStream(file);
                            int len = 0;
                            byte[] buf = new byte[1024];

                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);

                                sum += len;

                                Log.e(TAG, sum + "/" + total);

                                final int finalSum = sum;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv.setText(finalSum + "/" + total);
                                    }
                                });
                            }

                            final Bitmap bitmap = BitmapFactory.decodeStream(iss);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    iv.setImageBitmap(bitmap);
                                }
                            });

                            fos.flush();
                            fos.close();
                            is.close();

                            Log.e(TAG, "download success !");
                        }
                    }
                });
            }
        });
    }

    //post提交String
    public void onPostString(View view) {

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), "{\"username\":\"dali\",\"password\":\"1234\"}");

        final Request request = new Request
                .Builder()
                .post(requestBody)
                .url(mBaseUrl + "postString")
                .build();

        executeRequest(request);
    }

    //post提交文件
    public void onPostFile(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "/DCIM/Camera/dali.jpg");
        Log.e(TAG, "path:    " + file.getAbsolutePath());
        if (!file.exists()) {
            Log.e(TAG, file.getAbsolutePath() + " is not exits !");
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        Request request = new Request.Builder()
                .post(requestBody)
                .url(mBaseUrl + "postFile")
                .build();

        executeRequest(request);

    }


    public void onPost(View view) {

        FormEncodingBuilder builder = new FormEncodingBuilder();
        //构造Request
        //2.1 构造RequestBody
        RequestBody requestBody = builder.addEncoded("username", "dali").add("password", "1234").build();

        final Request request = new Request
                .Builder()
                .post(requestBody)
                .url(mBaseUrl + "login")
                .build();

        executeRequest(request);

    }

    private void executeRequest(final Request request) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.e(TAG, "onFailure");
                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {

                        Log.e(TAG, "onResponse");

                        if (response.isSuccessful()) {
                            final String res = response.body().string();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(res);
                                }
                            });
                        }
                    }
                });
            }
        });

    }
}

