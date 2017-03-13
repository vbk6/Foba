package com.fobae.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.anwios.alog.Logs;
import com.fobae.android.models.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class Description
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 18-02-2016
 */
public class DBHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "fobae_db";
    private static final int DB_VERSION = 6;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE orders " +
                "(id integer primary key, name text,customer text," +
                "phone text,mobile text,amount REAL, payment text,status text," +
                "street text,street2 text,city text,state text,country text,zip text,sync integer,shipping text)"
        );
        sqLiteDatabase.execSQL("CREATE TABLE orders_lines " +
                        "(id integer primary key, order_id integer,product text,product_id integer," +
                        "amount REAL,qty integer,returns integer)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE  IF EXISTS orders");
        sqLiteDatabase.execSQL("DROP TABLE  IF EXISTS orders_lines");
        onCreate(sqLiteDatabase);
    }

    public void clearOrders() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from orders");
        db.execSQL("delete from orders_lines");
    }

    public void insertOrders(JSONArray array) throws JSONException {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from orders");
        db.execSQL("delete from orders_lines");
        for (int i = 0; i < array.length(); i++) {
            JSONObject order = array.getJSONObject(i);
            ContentValues contentValues = new ContentValues();
            String id = order.getString("id");
            contentValues.put("id", id);
            contentValues.put("name", order.getString("order"));
            contentValues.put("customer", order.getString("customer"));
            contentValues.put("phone", order.getString("phone"));
            contentValues.put("mobile", order.getString("mobile"));
            contentValues.put("amount", order.getString("amount"));
            contentValues.put("payment", order.getString("payment"));
            contentValues.put("status", order.getString("status"));
            contentValues.put("street", order.getString("street"));
            contentValues.put("street2", order.getString("street2"));
            contentValues.put("city", order.getString("city"));
            contentValues.put("state", order.getString("state"));
            contentValues.put("country", order.getString("country"));
            contentValues.put("zip", order.getString("zip"));
            contentValues.put("shipping", order.getString("shipping"));
            db.insert("orders", null, contentValues);

            //insert products
            JSONArray items = order.getJSONArray("items");
            for (int j = 0; j < items.length(); j++) {
                JSONObject item = items.getJSONObject(j);
                ContentValues itemtValues = new ContentValues();
                itemtValues.put("order_id", id);
                itemtValues.put("product", item.getString("product"));
                itemtValues.put("qty", item.getString("qty"));
                itemtValues.put("amount", item.getString("price"));
                itemtValues.put("product_id", item.getString("product_id"));
                itemtValues.put("returns", 0);
                db.insert("orders_lines", null, itemtValues);
            }

        }
    }


    public void updateOrder(String name,String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update orders set status='"+status+"', sync=0 where name='" + name+"'");
    }

    public int getOrderId(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select id from orders where name='" + name + "'", null);
        if (c.moveToNext()) {
            return c.getInt(c.getColumnIndex("id"));
        } else {
            return 0;
        }
    }


    public Cursor getOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from orders", null);
    }
    public Cursor getOrderlines(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from orders_lines",null);

    }


    public Cursor getOrdersToDeliver() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from orders where status='da'", null);
    }

    public Cursor getDeliverdOrdersToSync() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select name from orders where sync=1 and status='done'", null);
    }
    public Cursor getCNAOrdersToSync() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select name from orders where sync=1 and status='cna'", null);
    }
    public Cursor getReturnOrdersToSync() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select name from orders where sync=1 and status='fr'", null);
    }
    public Cursor getPReturnOrdersToSync() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select id,name from orders where sync=1 and status='pr'", null);
    }
    public Cursor getDAOrdersToSync() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select id,name from orders where sync=1 and status='da'", null);
    }
    public Cursor getDNDOrdersToSync() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select id,name from orders where sync=1 and status='dnd'", null);
    }



    public Cursor getLines(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from orders_lines where order_id=" + id, null);
    }

    public String getReturnStatus(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String status="fr";

        if(db.rawQuery("select * from orders_lines where returns != qty and order_id=" + id, null).moveToNext()){
            status="pr";
        }

        return status;
    }


    public Cursor getReturnLines(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from orders_lines where order_id=" + id+" and returns>0", null);
    }

    public void setReturns(String id, int returnQty) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update orders_lines set returns= '" + returnQty + "' where id=" + id);
    }

    public int getOrderedQty(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select qty from orders_lines where id=" + id, null);
        if (c.moveToNext())
            return c.getInt(0);
        else
            return 0;
    }


    public String getTotalAmountProducts(String id) {
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor c = db.rawQuery("select sum((qty-returns)*amount) as sum from orders_lines where order_id=" + id, null);
        if (c.moveToNext()){
            String v=c.getString(c.getColumnIndex("sum"));
            Log.d("bk",v+"");
            return c.getFloat(0)+"";}
        else
            return "0";
    }
    public float getTotalCOH() {
        SQLiteDatabase db = this.getReadableDatabase();
        float productCost=0;
        float shipCost=0;
        Cursor c = db.rawQuery("select sum((qty-returns)*amount) from orders_lines where order_id in (select id from orders where payment != 'Pay by Cash' and (status='done' or status='pr' ))", null);
        if (c.moveToNext())
             productCost= c.getFloat(0);
        c = db.rawQuery("select sum(shipping) from orders where payment != 'Pay by Cash' and (status='done' or status='pr' )", null);
        if (c.moveToNext())
            shipCost= c.getFloat(0);

        return productCost+shipCost;
    }

    public float getTotalReturnAmount(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select sum(returns*amount) from orders_lines where order_id=" + id, null);
        if (c.moveToNext()) {
            float f = (float) c.getDouble(0);
            return f;
        } else
            return 0;
    }


    public Order getOrder(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cr = db.rawQuery("select * from orders where id='" + id + "'", null);
        if (cr.moveToNext()) {
            Order order = new Order();
            order.setId(cr.getString(cr.getColumnIndex("id")));
            order.setName(cr.getString(cr.getColumnIndex("name")));
            order.setCustomer(cr.getString(cr.getColumnIndex("customer")));
            order.setPhone(cr.getString(cr.getColumnIndex("phone")));
            order.setMobile(cr.getString(cr.getColumnIndex("mobile")));
            order.setAmount(cr.getString(cr.getColumnIndex("amount")));
            order.setPayment(cr.getString(cr.getColumnIndex("payment")));
            order.setStatus(cr.getString(cr.getColumnIndex("status")));
            order.setStreet(cr.getString(cr.getColumnIndex("street")));
            order.setStreet2(cr.getString(cr.getColumnIndex("street2")));
            order.setCity(cr.getString(cr.getColumnIndex("city")));
            order.setState(cr.getString(cr.getColumnIndex("state")));
            order.setCountry(cr.getString(cr.getColumnIndex("country")));
            order.setZip(cr.getString(cr.getColumnIndex("zip")));
            return order;
        }
        return null;
    }


    public int getTotalOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select count(id) from orders", null);
        if (c.moveToNext())
            return c.getInt(0);
        else
            return 0;
    }

    public int getDeliveredOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select count(id) from orders where status='done'", null);
        if (c.moveToNext())
            return c.getInt(0);
        else
            return 0;
    }


    public int getReturnedOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select count(id) from orders where status='fr'", null);
        if (c.moveToNext())
            return c.getInt(0);
        else
            return 0;
    }

    public int getPROrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select count(id) from orders where status='pr'", null);
        if (c.moveToNext())
            return c.getInt(0);
        else
            return 0;
    }
    public int getCNAOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select count(id) from orders where status='cna'", null);
        if (c.moveToNext())
            return c.getInt(0);
        else
            return 0;
    }
    public int getDNDOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select count(id) from orders where status='dnd'", null);
        if (c.moveToNext())
            return c.getInt(0);
        else
            return 0;
    }




    public void updateOrderOffline(String name, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update orders set status='"+status+"', sync=1 where name='"+name+"'");

    }

    public void syncFinished() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update orders set sync=0 where 1");
    }

    public String getShippingAmount(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select shipping from orders where id=" + id, null);
        if (c.moveToNext())
            return c.getString(0);
        else
            return "0";
    }

    public Float getTotalAmount(String id) {
        String amd=getShippingAmount(id);
        String toal=getTotalAmountProducts(id);
        Log.d("vbk", toal + "ll");

        return Float.parseFloat(getTotalAmountProducts(id))+Float.parseFloat(getShippingAmount(id));
    }


    public int getReturnsQty(String order){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cr=db.rawQuery("select sum(returns) from orders_lines where order_id=" + order, null);
        if(cr.moveToNext()){
                return cr.getInt(0);
        }
        return 0;
    }
    public int getOrderdQty(String order){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cr=db.rawQuery("select sum(qty) from orders_lines where order_id="+order,null);
        if(cr.moveToNext()){
            return cr.getInt(0);
        }
        return 0;
    }

}
