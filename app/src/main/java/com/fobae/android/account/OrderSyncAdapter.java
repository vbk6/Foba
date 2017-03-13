package com.fobae.android.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.anwios.alog.Logs;
import com.fobae.android.APIClient;
import com.fobae.android.APIRequestBuilder;
import com.fobae.android.DBHelper;
import com.fobae.android.R;
import com.fobae.android.managers.UserManager;
import com.fobae.android.ui.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Class Description
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 11-03-2016
 */
public class OrderSyncAdapter extends AbstractThreadedSyncAdapter {

    private final AccountManager mAccountManager;


    public OrderSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        try {
            Logs.d("Hell2");
            String authToken = mAccountManager.blockingGetAuthToken(account, "Full access", true);


            final DBHelper dbHelper = new DBHelper(getContext());
            JSONObject ordersToSync = new JSONObject();
            JSONArray delivered = new JSONArray();
            Cursor c = dbHelper.getDeliverdOrdersToSync();
            while (c.moveToNext()) {
                delivered.put(c.getString(c.getColumnIndex("name")));
            }
            c = dbHelper.getCNAOrdersToSync();
            JSONArray cna = new JSONArray();
            while (c.moveToNext()) {
                cna.put(c.getString(c.getColumnIndex("name")));
            }
            c = dbHelper.getReturnOrdersToSync();
            JSONArray fr = new JSONArray();
            while (c.moveToNext()) {
                cna.put(c.getString(c.getColumnIndex("name")));
            }
            c = dbHelper.getPReturnOrdersToSync();
            JSONArray pr = new JSONArray();
            while (c.moveToNext()) {
                JSONObject order = new JSONObject();
                JSONObject products = new JSONObject();

                try {
                    order.put("name", c.getString(c.getColumnIndex("name")));
                    Cursor cr = dbHelper.getReturnLines(c.getString(c.getColumnIndex("id")));
                    products.put("p" + cr.getString(cr.getColumnIndex("product_id")), cr.getString(cr.getColumnIndex("returns")));
                    order.put("products", products);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cna.put(c.getString(c.getColumnIndex("name")));
            }
            try {
                ordersToSync.put("delivered", delivered);
                ordersToSync.put("return", fr);
                ordersToSync.put("cna", cna);
                ordersToSync.put("partial_return", pr);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            APIRequestBuilder builder = new APIRequestBuilder()
                    .setType(APIRequestBuilder.Type.PUT)
                    .setMethod("/api/fobae/v1/update_orders")
                    .authenticate(getContext());
            builder.addParameters("data", ordersToSync.toString());
            new APIClient().execute(builder, new APIClient.APIResultCallback() {

                        @Override
                        public void onFinish(JSONObject object) {
                            Logs.d("Loading Finished ");
                            try {
                                if (object == null) {
                                    Toast.makeText(getContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (object.has("needToLogin") && object.getBoolean("needToLogin")) {
                                        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                        Toast.makeText(getContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                                        Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                                        PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(), 0, loginIntent, 0);
                                        // notification.
                                        //notification.setLatestEventInfo(getContext(), "Fobae.com","Error to login Fobae.com", loginIntent);

                                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext());
                                        UserManager.getInstance().logoutUser(getContext());
                                        mBuilder.setContentText("Error to login Fobae.com")
                                                .setContentTitle("Fobae.com")
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setContentIntent(resultPendingIntent);
                                        notificationManager.notify(0, mBuilder.build());
                                    } else {
                                        if (object.has("error") && object.getBoolean("error")) {
                                            Toast.makeText(getContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                                        } else {
                                            dbHelper.syncFinished();
                                        }

                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );

            //mAccountManager.getAuthToken(account,"Full access", true);
            Logs.d("Hello " + authToken);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.mipmap.ic_launcher, "Error to login Fobae.com", System.currentTimeMillis());

            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(), 0, loginIntent, 0);
            // notification.
            //notification.setLatestEventInfo(getContext(), "Fobae.com","Error to login Fobae.com", loginIntent);
            notificationManager.notify(9999, notification);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext());

            mBuilder.setContentText("Error to login Fobae.com")
                    .setContentTitle("Fobae.com")
                    .setContentIntent(resultPendingIntent);
            notificationManager.notify(999, mBuilder.build());
            //e.printStackTrace();
        }
    }
}

