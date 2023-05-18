package com.example.faceidentification.net;

import org.json.JSONException;

import okhttp3.Response;

/**
 * 自定义回调
 */
public interface ResponseCallBack {
    Response body = null;

    void success(String json) throws JSONException;

    void error(String json);
}
