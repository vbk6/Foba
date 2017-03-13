package com.fobae.android.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fobae.android.APIClient;
import com.fobae.android.APIRequestBuilder;
import com.fobae.android.DBHelper;
import com.fobae.android.NetworkUtil;
import com.fobae.android.R;
import com.fobae.android.managers.ErrorHandler;
import com.fobae.android.models.Order;

import org.json.JSONObject;

/**
 * Screen with Order details , and actions like call user, set as delivered etc.
 */
public class OrderDetailsActivity extends AppCompatActivity {

    private Order order;
    private String id;

    //UI Elements
    TextView textName;
    TextView textAddressLine1;
    TextView textAddressLine2;
    TextView textAddressCity;
    TextView textAddressSC;
    TextView textAddressZip;
    TextView textPayment;
    TextView textStatus;
    TextView textAmount;
    TextView textShipping;

    Button buttonMobile;
    Button buttonPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set Contents and Toolbar
        setContentView(R.layout.activity_order_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get Order
        id = getIntent().getStringExtra("id");
        order = new DBHelper(this).getOrder(id);
        if (order == null) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            finish();
        }

        //set Title
        setTitle(order.getName());

        //Referencing UI elements
        textName = (TextView) findViewById(R.id.textName);
        textAddressLine1 = (TextView) findViewById(R.id.textAddressLine1);
        textAddressLine2 = (TextView) findViewById(R.id.textAddressLine2);
        textAddressCity = (TextView) findViewById(R.id.textAddressCity);
        textAddressSC = (TextView) findViewById(R.id.textAddressSC);
        textAddressZip = (TextView) findViewById(R.id.textAddressZip);
        textPayment = (TextView) findViewById(R.id.textPayment);
        textStatus = (TextView) findViewById(R.id.textStatus);
        textAmount = (TextView) findViewById(R.id.textAmount);
        textShipping = (TextView) findViewById(R.id.textShipping);


        buttonMobile = (Button) findViewById(R.id.buttonMobile);
        buttonPhone = (Button) findViewById(R.id.buttonPhone);

        //Set Contents
        textName.setText(order.getCustomer());
        textAddressLine1.setText(order.getStreet());
        textAddressLine2.setText(order.getStreet2());
        textAddressCity.setText(order.getCity());
        textAddressSC.setText(String.format(getString(R.string.address_sc), order.getState(), order.getCountry()));
        textAddressZip.setText(String.format(getString(R.string.address_zip), order.getZip()));
        textPayment.setText(order.getPayment());

        textAmount.setText("Total: "+new DBHelper(this).getTotalAmount(id)+"AED");
        textShipping.setText("Shipping: "+new DBHelper(this).getShippingAmount(id)+"AED");

        if (order.getPhone().equals("false") && order.getMobile().equals("false")) {
            findViewById(R.id.textCall).setVisibility(View.GONE);
            buttonPhone.setVisibility(View.GONE);
            buttonMobile.setVisibility(View.GONE);
        } else {
            if (!order.getPhone().equals("false")) {
                buttonPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + order.getPhone()));
                        if (ActivityCompat.checkSelfPermission(OrderDetailsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(OrderDetailsActivity.this, "", Toast.LENGTH_SHORT).show();
                            String[] permissions = {Intent.ACTION_CALL};
                            ActivityCompat.requestPermissions(OrderDetailsActivity.this, permissions, 1);
                        } else {
                            startActivity(intent);
                        }
                    }
                });
            } else {
                buttonPhone.setVisibility(View.GONE);
            }

            if (!order.getMobile().equals("false")) {

                buttonMobile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + order.getMobile()));
                        if (ActivityCompat.checkSelfPermission(OrderDetailsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(OrderDetailsActivity.this, "", Toast.LENGTH_SHORT).show();
                            String[] permissions = {Intent.ACTION_CALL};
                            ActivityCompat.requestPermissions(OrderDetailsActivity.this, permissions, 1);
                        } else {
                            startActivity(intent);
                        }
                    }
                });
            } else {
                buttonMobile.setVisibility(View.GONE);
            }

        }


        String status=order.getStatus();


        if (status.equals("done") || status.equals("ft") || status.equals("pr") || status.equals("cna")|| status.equals("dnd")) {
            textStatus.setTextColor(getResources().getColor(R.color.textColorOK));
            textStatus.setBackground(getResources().getDrawable(R.drawable.circle_delivey));
        }else{
            textStatus.setTextColor(getResources().getColor(R.color.textColorFail));
            textStatus.setBackground(getResources().getDrawable(R.drawable.circle_not_delivey));
        }


        if (!order.getPayment().equals("Pay by Cash")) {
            textPayment.setTextColor(getResources().getColor(R.color.textColorFail));
            textPayment.setBackground(getResources().getDrawable(R.drawable.circle_not_delivey));
        }


        hideDeliveryActions();

        if (status.equals("done")) {
            status = "Delivered";
        } else if (status.equals("fr")) {
            status = "Full Returned";
        } else if (status.equals("pr")) {
            status = "Partial Returned";
        } else if (status.equals("cna")) {
            status = "Customer Unavailable";
        } else if (status.equals("da")) {
            status = "Not Delivered";
        }else if (status.equals("dnd")) {
            status = "Driver not delivered";
        }else {
            findViewById(R.id.btnSetAsDA).setVisibility(View.VISIBLE);
            findViewById(R.id.textmarkAS).setVisibility(View.VISIBLE);
            status = "Driver Not Accepted";
        }

        textStatus.setText(status);


    }

    private void hideDeliveryActions() {
        if (!order.getStatus().equals("da")||order.getStatus().equals("done") || order.getStatus().equals("fr")
                || order.getStatus().equals("pr") || order.getStatus().equals("cna")
                || order.getStatus().equals("dnd")) {
            findViewById(R.id.textmarkAS).setVisibility(View.GONE);
            findViewById(R.id.btnSetAsCNA).setVisibility(View.GONE);
            findViewById(R.id.btnSetAsFullReturn).setVisibility(View.GONE);
            findViewById(R.id.btnSetAsDelivered).setVisibility(View.GONE);
            findViewById(R.id.btnSetAsDND).setVisibility(View.GONE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        Toast.makeText(this, "Requesting permission completed.", Toast.LENGTH_SHORT).show();
    }


    public void setAsDA(View v) {


        if(!NetworkUtil.isNetworkAvailable(OrderDetailsActivity.this)){
            DBHelper dbHelper = new DBHelper(OrderDetailsActivity.this);
            dbHelper.updateOrderOffline(order.getName(), "da");
            Toast.makeText(OrderDetailsActivity.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
            textStatus.setText("Not Delivered");
            showActions();
            return;
        }


        final DBHelper dbHelper = new DBHelper(this);
        APIRequestBuilder builder = new APIRequestBuilder()
                .setType(APIRequestBuilder.Type.PUT)
                .setMethod("/api/fobae/v1/update_order")
                .addParameters("order", order.getName())
                .addParameters("cna", "3")
                .authenticate(this);

        final ProgressDialog dialog = new ProgressDialog(this, R.style.DialogTheme);
        dialog.setMessage("Updating Order...");
        dialog.setCancelable(false);
        dialog.show();
        new APIClient().execute(builder, new APIClient.APIResultCallback() {
            @Override
            public void onFinish(JSONObject object) {
                dialog.cancel();
                if (!ErrorHandler.getInstance().handleError(OrderDetailsActivity.this, object)) {
                    dbHelper.updateOrder(order.getName(), "da");
                    Toast.makeText(OrderDetailsActivity.this, "Order marked as Driver Accepted", Toast.LENGTH_SHORT).show();
                    textStatus.setText("Not Delivered");
                    showActions();
                }

            }
        });

    }


    public void setAsCNA(View v) {


        if(!NetworkUtil.isNetworkAvailable(OrderDetailsActivity.this)){
            DBHelper dbHelper = new DBHelper(OrderDetailsActivity.this);
            dbHelper.updateOrderOffline(order.getName(), "cna");
            Toast.makeText(OrderDetailsActivity.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
            hideDeliveryActions();
            finish();
            return;
        }

        Log.d("cvm","jj");
        final DBHelper dbHelper = new DBHelper(this);
        APIRequestBuilder builder = new APIRequestBuilder()
                .setType(APIRequestBuilder.Type.PUT)
                .setMethod("/api/fobae/v1/update_order")
                .addParameters("order", order.getName())
                .addParameters("cna", "1")
                .authenticate(this);



        final ProgressDialog dialog = new ProgressDialog(this, R.style.DialogTheme);
        dialog.setMessage("Updating Order...");
        dialog.setCancelable(false);
        dialog.show();
        new APIClient().execute(builder, new APIClient.APIResultCallback() {
            @Override
            public void onFinish(JSONObject object) {
                dialog.cancel();
                if (!ErrorHandler.getInstance().handleError(OrderDetailsActivity.this, object)) {
                    dbHelper.updateOrder(order.getName(),"cna");
                    Toast.makeText(OrderDetailsActivity.this, "Order marked as Customer not Available", Toast.LENGTH_SHORT).show();
                    hideDeliveryActions();
                    finish();
                }

            }
        });
    }


    public void setAsDND(View v) {


        if(!NetworkUtil.isNetworkAvailable(OrderDetailsActivity.this)){
            DBHelper dbHelper = new DBHelper(OrderDetailsActivity.this);
            dbHelper.updateOrderOffline(order.getName(), "dnd");
            Toast.makeText(OrderDetailsActivity.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
            hideDeliveryActions();
            finish();
            return;
        }

            Log.d("cvb","jj");
        final DBHelper dbHelper = new DBHelper(this);
        APIRequestBuilder builder = new APIRequestBuilder()
                .setType(APIRequestBuilder.Type.PUT)
                .setMethod("/api/fobae/v1/update_order")
                .addParameters("order", order.getName())
                .addParameters("cna", "2")
                .authenticate(this);

        final ProgressDialog dialog = new ProgressDialog(this, R.style.DialogTheme);
        dialog.setMessage("Updating Order...");
        dialog.setCancelable(false);
        dialog.show();
        new APIClient().execute(builder, new APIClient.APIResultCallback() {
            @Override
            public void onFinish(JSONObject object) {
                dialog.cancel();
                if (!ErrorHandler.getInstance().handleError(OrderDetailsActivity.this, object)) {
                    dbHelper.updateOrder(order.getName(),"dnd");
                    Toast.makeText(OrderDetailsActivity.this, "Order marked as Driver Not Delivered", Toast.LENGTH_SHORT).show();
                    hideDeliveryActions();
                    finish();
                }

            }
        });
    }

    public void setAsFullReturn(View v) {
        if(!NetworkUtil.isNetworkAvailable(OrderDetailsActivity.this)){
            DBHelper dbHelper = new DBHelper(OrderDetailsActivity.this);
            dbHelper.updateOrderOffline(order.getName(), "fr");
            Toast.makeText(OrderDetailsActivity.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
            hideDeliveryActions();
            finish();
            return;
        }
        APIRequestBuilder builder = new APIRequestBuilder()
                .setType(APIRequestBuilder.Type.PUT)
                .setMethod("/api/fobae/v1/full_return")
                .authenticate(this);;
        //Cursor cr = new DBHelper(OrderDetailsActivity.this).getLines(id);

        builder.addParameters("order",order.getName());
        final ProgressDialog dialog = new ProgressDialog(OrderDetailsActivity.this, R.style.DialogTheme);
        dialog.setMessage("Updating Order...");
        dialog.setCancelable(false);
        dialog.show();
        new APIClient().execute(builder, new APIClient.APIResultCallback() {
            @Override
            public void onFinish(JSONObject object) {
                dialog.cancel();

                if (!ErrorHandler.getInstance().handleError(OrderDetailsActivity.this, object)) {
                    DBHelper dbHelper = new DBHelper(OrderDetailsActivity.this);
                    dbHelper.updateOrder(order.getName(), "fr");
                    Toast.makeText(OrderDetailsActivity.this, "Order marked as Full Return", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    public void setAsDelivered(View v) {

        if(!NetworkUtil.isNetworkAvailable(OrderDetailsActivity.this)){
            DBHelper dbHelper = new DBHelper(OrderDetailsActivity.this);
            dbHelper.updateOrderOffline(order.getName(), "done");
            Toast.makeText(OrderDetailsActivity.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
            hideDeliveryActions();
            finish();
            return;
        }

        final DBHelper dbHelper = new DBHelper(this);
        APIRequestBuilder builder = new APIRequestBuilder()
                .setType(APIRequestBuilder.Type.PUT)
                .setMethod("/api/fobae/v1/update_order")
                .addParameters("order", order.getName())
                .authenticate(this);;

        final ProgressDialog dialog = new ProgressDialog(this, R.style.DialogTheme);
        dialog.setMessage("Updating Order...");
        dialog.setCancelable(false);
        dialog.show();

        new APIClient().execute(builder, new APIClient.APIResultCallback() {
            @Override
            public void onFinish(JSONObject object) {
                dialog.cancel();
                if (!ErrorHandler.getInstance().handleError(OrderDetailsActivity.this, object)) {
                    dbHelper.updateOrder(order.getName(),"done");
                    Toast.makeText(OrderDetailsActivity.this, "Item Delivered", Toast.LENGTH_SHORT).show();
                    hideDeliveryActions();
                    finish();
                }
            }
        });
    }



    private void showActions() {
        findViewById(R.id.textmarkAS).setVisibility(View.VISIBLE);
        findViewById(R.id.btnSetAsDA).setVisibility(View.GONE);
        findViewById(R.id.btnSetAsCNA).setVisibility(View.VISIBLE);
        findViewById(R.id.btnSetAsFullReturn).setVisibility(View.VISIBLE);
        findViewById(R.id.btnSetAsDelivered).setVisibility(View.VISIBLE);
        findViewById(R.id.btnSetAsDND).setVisibility(View.VISIBLE);
    }

}
