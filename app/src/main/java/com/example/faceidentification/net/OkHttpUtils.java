package com.example.faceidentification.net;

import static java.lang.String.valueOf;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Description : OkHttp网络连接封装工具类
 * Author :
 * Date   : 2020年7月1日15:51:20
 */
public class OkHttpUtils {

    private static final String TAG = "OkHttpUtils";
    //handler主要用于异步请求数据之后更新UI
    private static  Handler handler = new Handler();

    public static void getAsync(String url,ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient();
        Log.i(TAG,"请求地址===》"+url);
        Request request = new Request
                .Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call,IOException e) {
                Log.e(TAG,"响应失败===》"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody=response.body().string();
                Log.i(TAG,"响应成功===》"+respBody);
                handler.post(()->{
                    try {
                        responseCallBack.success(respBody);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //ActivityUtils.showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * 表单提交数据
     * @param url 请求地址
     * @param formData 表单回调
     * @param responseCallBack 响应回调
     */
    public static void postAsyncFormData(String url, Map<String,String> formData, ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        FormBody.Builder builder = new FormBody.Builder();
        StringBuffer showData=new StringBuffer();
        for (String key:formData.keySet()){
            builder.add(key,formData.get(key));
            showData.append("   "+key+":"+formData.get(key));
        }
        FormBody formBody = builder
                .build();
        Request request = new Request
                .Builder()
                .addHeader("Cookie","token=")
                .url(url)
                .post(formBody)
                .build();

        Log.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数==>"+showData);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e) {
                Log.e(TAG,"响应失败===》"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(Call call,  Response response) throws IOException {
                String respBody=response.body().string();
                Log.i(TAG,"响应成功===》"+respBody);
                handler.post(()->{
                    try {
                        responseCallBack.success(respBody);
                    } catch (JSONException e) {
                        //ActivityUtils.showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }
    /**
     * json提交数据
     * @param url 请求地址
     * @param json json数据
     * @param responseCallBack 响应回调
     */
    public static void postAsyncJson(String url, String json, ResponseCallBack responseCallBack) {

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.
                Builder()
                .url(url)
                .addHeader("Cookie","token=")
                .post(requestBody)
                .build();

        Log.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数==>"+json);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call,  IOException e) {
                Log.e(TAG,"响应失败===》"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody=response.body().string();
                Log.i(TAG,"响应成功===》"+respBody);
                handler.post(()->{
                    try {
                        responseCallBack.success(respBody);
                    } catch (JSONException e) {
                        //ActivityUtils.showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * json提交数据
     * @param url 请求地址
     * @param json json数据
     * @param responseCallBack 响应回调
     */
    public static void putAsyncJson(String url, String json, ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.
                Builder()
                .url(url)
                .addHeader("Cookie","token=")
                .put(requestBody)
                .build();

        Log.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数==>"+json);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure( Call call,  IOException e) {
                Log.e(TAG,"响应失败===》"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse( Call call,  Response response) throws IOException {
                String respBody=response.body().string();
                Log.i(TAG,"响应成功===》"+respBody);
                handler.post(()->{
                    try {
                        responseCallBack.success(respBody);
                    } catch (JSONException e) {
                        //ActivityUtils.showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * json提交数据
     * @param url 请求地址
     * @param map 表单数据
     * @param file  File数据
     * @param responseCallBack 响应回调
     */
    public static void postAsyncFileWithFormdata(@NonNull String url, @NonNull final Map<String, Object> map , @NonNull File file, ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        // MediaType.parse() 里面是上传的文件类型。
        RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
        String filename = file.getName();

        // 参数分别为， 请求key ，文件名称 ， RequestBody
        requestBody.addFormDataPart("file", file.getName(), body);
        for (Map.Entry entry : map.entrySet()) {
            requestBody.addFormDataPart(valueOf(entry.getKey()), valueOf(entry.getValue()));
        }
        Request request = new Request.
                Builder()
                .url(url)
                .addHeader("Cookie","token=")
                .post(requestBody.build())
                .build();

        Log.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数==>");
        client.newBuilder().readTimeout(5000, TimeUnit.MILLISECONDS).build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call,  IOException e) {
                Log.e(TAG,"响应失败===》"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody=response.body().string();
                Log.i(TAG,"响应成功===》"+respBody);
                handler.post(()->{
                    try {
                        responseCallBack.success(respBody);
                    } catch (JSONException e) {
                        //ActivityUtils.showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * json请求数据
     * @param url 请求地址
     * @param responseCallBack 响应回调
     */
    public static void GetAsyncData(String url, ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.
                Builder()
                .url(url)
                .get()
                .build();

        Log.i(TAG,"开始发送请求：请求地址【"+url+"】,获取数据==>");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure( Call call,  IOException e) {
                Log.e(TAG,"响应失败===》"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(@NonNull Call call, Response response) throws IOException {
                String respBody=response.body().string();
                Log.i(TAG,"响应成功===》"+respBody);
                handler.post(()->{
                    try {
                        responseCallBack.success(respBody);
                    } catch (JSONException e) {
                        //ActivityUtils.showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }
    /**
     * json请求数据
     * @param url 请求地址
     */
    public static String GetSyncData(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.
                Builder()
                .url(url)
                .get()
                .build();

        Log.i(TAG,"开始发送请求：请求地址【"+url+"】,获取数据==>");
        Response response  = client.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();
    }
    /**
     * json提交数据
     * @param url 请求地址
     * @param formData 表单数据
     * @param responseCallBack 响应回调
     */
    public static void putAsyncForm(String url, Map<String,String> formData, ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        FormBody.Builder builder = new FormBody.Builder();
        StringBuffer showData=new StringBuffer();
        for (String key:formData.keySet()){
            builder.add(key,formData.get(key));
            showData.append("   "+key+":"+formData.get(key));
        }
        FormBody formBody = builder
                .build();
        Request request = new Request
                .Builder()
                .addHeader("Cookie","token=")
                .url(url)
                .put(formBody)
                .build();
        Log.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数==>"+showData);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call,IOException e) {
                Log.e(TAG,"响应失败===》"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody=response.body().string();
                Log.i(TAG,"响应成功===》"+respBody);
                handler.post(()->{
                    try {
                        responseCallBack.success(respBody);
                    } catch (JSONException e) {
                        //ActivityUtils.showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }
}

