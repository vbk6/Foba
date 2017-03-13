package com.fobae.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.fobae.android.R;
import com.fobae.android.fragments.ListDeliveryFragment;

/**
 * Adapter Class for Return Items List
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 09-09-2015
 */
public class ItemReturnAdapter extends BaseAdapter {
    private Context context;
    private Cursor cursor;


    public ItemReturnAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int i) {
        cursor.moveToPosition(i);
        return cursor.getString(cursor.getColumnIndex("id"));
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        cursor.moveToPosition(i);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_return, null);
        ((TextView) v.findViewById(R.id.textProduct)).setText(cursor.getString(cursor.getColumnIndex("product")));
        ((TextView) v.findViewById(R.id.textPrice)).setText(String.format(context.getString(R.string.unit_price),cursor.getString(cursor.getColumnIndex("amount"))));
        ((TextView) v.findViewById(R.id.textOrderQty)).setText(String.format(context.getString(R.string.ordered_qty),cursor.getString(cursor.getColumnIndex("qty"))));
        ((TextView) v.findViewById(R.id.textReturnQty)).setText(String.format(context.getString(R.string.return_qty),cursor.getString(cursor.getColumnIndex("returns"))));
        return v;
    }
}
