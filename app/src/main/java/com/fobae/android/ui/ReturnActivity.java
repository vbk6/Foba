package com.fobae.android.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anwios.alog.Logs;
import com.fobae.android.APIClient;
import com.fobae.android.APIRequestBuilder;
import com.fobae.android.DBHelper;
import com.fobae.android.NetworkUtil;
import com.fobae.android.R;
import com.fobae.android.adapters.ItemReturnAdapter;
import com.fobae.android.managers.ErrorHandler;

import org.json.JSONObject;


/**
 * UI for Item Partial Returns
 *
 * @author nooh.km
 */
public class  ReturnActivity extends AppCompatActivity {

    private ItemReturnAdapter adapter;
    private TextView textTotalAmount;
    private TextView textTitleReturn;

    // order id
    private String id;
    private String name;
    // order total amount
    private float amount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set order id
        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");

        // Return finish
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHelper dbHelper = new DBHelper(ReturnActivity.this);
                if (dbHelper.getReturnsQty(id)==0) {
                    Toast.makeText(ReturnActivity.this, "Enter valid quantity to return", Toast.LENGTH_SHORT).show();
                    return;

                }else if(dbHelper.getReturnsQty(id)==dbHelper.getOrderdQty(id)){
                    setAsFullReturn();
                } else {


                    if (!NetworkUtil.isNetworkAvailable(ReturnActivity.this)) {


                        dbHelper.updateOrderOffline(name, dbHelper.getReturnStatus(id));
                        Toast.makeText(ReturnActivity.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                        return;

                    }
                    APIRequestBuilder builder = new APIRequestBuilder()
                            .setType(APIRequestBuilder.Type.PUT)
                            .setMethod("/api/fobae/v1/partial_return")
                            .authenticate(ReturnActivity.this);
                    Cursor cr = new DBHelper(ReturnActivity.this).getReturnLines(id);

                    builder.addParameters("id", id);
                    builder.addParameters("order", getIntent().getStringExtra("name"));
                    while (cr.moveToNext()) {
                        builder.addParameters("p" + cr.getString(cr.getColumnIndex("product_id")), cr.getString(cr.getColumnIndex("returns")));
                    }
                    final ProgressDialog dialog = new ProgressDialog(view.getContext(), R.style.DialogTheme);
                    dialog.setMessage("Updating Order...");
                    dialog.setCancelable(false);
                    dialog.show();

                    new APIClient().execute(builder, new APIClient.APIResultCallback() {
                        @Override
                        public void onFinish(JSONObject object) {
                            dialog.dismiss();
                            if (!ErrorHandler.getInstance().handleError(ReturnActivity.this, object)) {

                                DBHelper dbHelper = new DBHelper(ReturnActivity.this);
                                dbHelper.updateOrder(name, dbHelper.getReturnStatus(id));

                                Toast.makeText(ReturnActivity.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                }
            }
        });


        //initialize ui elements

        amount = Float.parseFloat(getIntent().getStringExtra("amount"));

        textTotalAmount = (TextView) findViewById(R.id.textTotalAmount);

        textTitleReturn = (TextView) findViewById(R.id.textTitleReturn);


        textTitleReturn.setText(String.format(getString(R.string.return_title_order), getIntent().getStringExtra("name")));


        refreshTotalAmount();

        // Set the adapter
        ListView mListView = (ListView) findViewById(android.R.id.list);
        adapter = new ItemReturnAdapter(this, new DBHelper(this).getLines(id));
        ((AdapterView<ListAdapter>) mListView).setAdapter(adapter);


        //show dialog on click item and update returns
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int pos, long l) {

                final Dialog dialog = new Dialog(ReturnActivity.this, R.style.DialogTheme);
                dialog.setContentView(R.layout.dialog_return);
                dialog.setCancelable(false);
                Button buttonOk = (Button) dialog.findViewById(R.id.buttonOk);
                Button buttonCancel = (Button) dialog.findViewById(R.id.buttonCancel);
                final EditText editQty = (EditText) dialog.findViewById(R.id.editQty);
                buttonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        DBHelper dbHelper = new DBHelper(ReturnActivity.this);

                        if(editQty.getText().toString().trim().equals("")|| editQty.getText().length()>=5) {
                            Toast.makeText(ReturnActivity.this, "Enter a valid qty", Toast.LENGTH_SHORT).show();
                        }else{
                            int rqty = Integer.parseInt(editQty.getText().toString());
                            if (rqty > dbHelper.getOrderedQty((String) adapter.getItem(pos))) {
                                Toast.makeText(ReturnActivity.this, "Enter a valid qty", Toast.LENGTH_SHORT).show();
                            } else {
                                dbHelper.setReturns((String) adapter.getItem(pos), rqty);
                                adapter.setCursor(dbHelper.getLines(id));
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                                refreshTotalAmount();
                            }
                        }

                    }
                });
                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        }
                });
                dialog.show();
            }
        });

    }

    public void setAsFullReturn() {

        if(!NetworkUtil.isNetworkAvailable(ReturnActivity.this)){
            DBHelper dbHelper = new DBHelper(ReturnActivity.this);
            dbHelper.updateOrderOffline(name, "fr");
            Toast.makeText(ReturnActivity.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        APIRequestBuilder builder = new APIRequestBuilder()
                .setType(APIRequestBuilder.Type.PUT)
                .setMethod("/api/fobae/v1/full_return")
                .authenticate(this);;
        //Cursor cr = new DBHelper(OrderDetailsActivity.this).getLines(id);

        builder.addParameters("order",name);
        final ProgressDialog dialog = new ProgressDialog(ReturnActivity.this, R.style.DialogTheme);
        dialog.setMessage("Updating Order...");
        dialog.setCancelable(false);
        dialog.show();
        new APIClient().execute(builder, new APIClient.APIResultCallback() {
            @Override
            public void onFinish(JSONObject object) {
                dialog.cancel();

                if (!ErrorHandler.getInstance().handleError(ReturnActivity.this, object)) {
                    DBHelper dbHelper = new DBHelper(ReturnActivity.this);
                    dbHelper.updateOrder(name, "fr");
                    Toast.makeText(ReturnActivity.this, "Order marked as Full Return", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    /**
     * Refresh Total amount text
     */
    private void refreshTotalAmount() {
        DBHelper dbHelper = new DBHelper(ReturnActivity.this);
        textTotalAmount.setText(String.format(getString(R.string.return_title_amount), amount - dbHelper.getTotalReturnAmount(id)));
    }
}
