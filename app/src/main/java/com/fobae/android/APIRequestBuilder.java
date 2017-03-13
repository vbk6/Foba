package com.fobae.android;

import android.content.Context;

import com.anwios.alog.Logs;
import com.fobae.android.managers.UserManager;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;


public class APIRequestBuilder {

    private static final String BASE_URL = "http://odoo.mypits.org:7059";
//    private final APIRequestBuilder builder = new APIRequestBuilder();

    public enum Type {GET, POST, PUT}

    private Type type;
    private String method;
    private FormBody.Builder body = new FormBody.Builder();
    private boolean auth = false;
    private String username;
    private String userKey;

    public APIRequestBuilder setType(Type type) {
        this.type = type;
        return this;
    }


    public APIRequestBuilder setMethod(String method) {
        this.method = method;
        return this;
    }


    public APIRequestBuilder addParameters(String key, String value) {
        this.body.add(key, value);
        return this;
    }

    public APIRequestBuilder authenticate(Context context) {
        auth = true;
        username = UserManager.getInstance().getUsername(context);
        userKey = UserManager.getInstance().getUserKey(context);
        return this;
    }

    public Request buildRequest() {
        //RequestBody body=
        Request.Builder builder = new Request.Builder();
        builder.addHeader("API-Key", "fsdfsdfsdgdfgsdgsdg");
        if (auth) {
            builder.addHeader("API-User", username);
            builder.addHeader("API-User-Key", userKey);
        }
        builder.url(BASE_URL + method);
        if (type == Type.POST) {
            builder.post(body.build());
        } else if (type == Type.PUT) {
            builder.put(body.build());
        }
        return builder.build();
    }

}
