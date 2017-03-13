package com.fobae.android;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.anwios.alog.Logs;
import com.fobae.android.managers.UserManager;
import com.fobae.android.ui.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class Description
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 14-03-2016
 */
public class SyncService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent)  {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started, Updating Orders", Toast.LENGTH_LONG).show();
        boolean isDelivered = false;
        boolean isCNA = false;
        boolean isDND= false;
        boolean isFR = false;
        boolean isPR = false;
        boolean isDA = false;

        final DBHelper dbHelper = new DBHelper(this);
        JSONObject ordersToSync = new JSONObject();
        JSONArray delivered = new JSONArray();
        Cursor c = dbHelper.getDeliverdOrdersToSync();
        while (c.moveToNext()) {
            isDelivered = true;
            delivered.put(c.getString(c.getColumnIndex("name")));
        }
        c = dbHelper.getCNAOrdersToSync();
        JSONArray cna = new JSONArray();
        while (c.moveToNext()) {
            isCNA = true;
            cna.put(c.getString(c.getColumnIndex("name")));
        }

        c = dbHelper.getDNDOrdersToSync();
        JSONArray dnd = new JSONArray();
        while (c.moveToNext()) {
            isDND= true;
            dnd.put(c.getString(c.getColumnIndex("name")));
        }

        c = dbHelper.getDAOrdersToSync();
        JSONArray da = new JSONArray();
        while (c.moveToNext()) {
            isDA= true;
            da.put(c.getString(c.getColumnIndex("name")));
        }


        c = dbHelper.getReturnOrdersToSync();
        JSONArray fr = new JSONArray();
        while (c.moveToNext()) {
            isFR = true;
            fr.put(c.getString(c.getColumnIndex("name")));
        }
        c = dbHelper.getPReturnOrdersToSync();
        JSONArray pr = new JSONArray();
        while (c.moveToNext()) {
            isPR = true;
            JSONObject order = new JSONObject();
            JSONObject products = new JSONObject();

            try {
                order.put("name", c.getString(c.getColumnIndex("name")));
                Cursor cr = dbHelper.getReturnLines(c.getString(c.getColumnIndex("id")));
                while(cr.moveToNext()) {
                    products.put("p" + cr.getString(cr.getColumnIndex("product_id")), cr.getString(cr.getColumnIndex("returns")));
                }
                order.put("products", products);
                pr.put(order);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cna.put(c.getString(c.getColumnIndex("name")));
        }
        try {


            if (!isDelivered && !isFR && !isCNA && !isPR && !isDA && !isDND)
                return 0;

            if (isDelivered)
                ordersToSync.put("delivered", delivered);
            if (isFR)
                ordersToSync.put("return", fr);
            if (isCNA)
                ordersToSync.put("cna", cna);
            if (isDND)
                ordersToSync.put("dnd", dnd);
            if (isDA)
                ordersToSync.put("da", da);
            if (isPR)
                ordersToSync.put("partial_return", pr);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Logs.d("KKKOOOooooui");
        Logs.d(ordersToSync.toString());

        APIRequestBuilder builder = new APIRequestBuilder()
                .setType(APIRequestBuilder.Type.PUT)
                .setMethod("/api/fobae/v1/update_orders")
                .authenticate(this);
        builder.addParameters("data", ordersToSync.toString());
        new APIClient().execute(builder, new APIClient.APIResultCallback() {

                    @Override
                    public void onFinish(JSONObject object) {
                        try {
                            if (object == null) {
                                Toast.makeText(SyncService.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                            } else {
                                if (object.has("needToLogin") && object.getBoolean("needToLogin")) {
                                    UserManager.getInstance().logoutUser(SyncService.this);
                                    Toast.makeText(SyncService.this, object.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    if (object.has("error") && object.getBoolean("error")) {
                                        Toast.makeText(SyncService.this, object.getString("message"), Toast.LENGTH_SHORT).show();
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


        return super.

                onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}
