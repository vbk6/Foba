package com.fobae.android;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;

import com.anwios.alog.Logs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Response;


public class APIClient {

    private APIResultCallback callback;

    public void execute(APIRequestBuilder request, APIResultCallback callback) {
        this.callback = callback;

        new doAPITask().execute(request);
    }


    public class doAPITask extends AsyncTask<APIRequestBuilder, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(APIRequestBuilder... requests) {
            OkHttpClient client = new OkHttpClient();

            try {
                Response response = client.newCall(requests[0].buildRequest()).execute();
                try {
                    String s=response.body().string();
                    JSONObject result = new JSONObject(s);
                    return result;
                } catch (JSONException e) {
                    Logs.e(e.getMessage());
                    return null;
                }
            } catch (IOException e) {
                Logs.e(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (callback != null) {
                callback.onFinish(jsonObject);
            }
        }
    }


    public interface APIResultCallback {
        void onFinish(JSONObject object);
    }




}
