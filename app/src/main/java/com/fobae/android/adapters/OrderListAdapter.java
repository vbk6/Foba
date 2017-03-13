package com.fobae.android.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anwios.alog.Logs;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.fobae.android.APIClient;
import com.fobae.android.APIRequestBuilder;
import com.fobae.android.DBHelper;
import com.fobae.android.NetworkUtil;
import com.fobae.android.R;
import com.fobae.android.managers.ErrorHandler;
import com.fobae.android.ui.MainActivity;
import com.fobae.android.ui.OrderDetailsActivity;
import com.fobae.android.ui.ReturnActivity;

import org.json.JSONObject;

/**
 * Adapter class to fill Order List
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 24-02-2016
 */
public class OrderListAdapter extends RecyclerSwipeAdapter<OrderListAdapter.OrderItemHolder> {

    private Cursor cursor;
    private Fragment fragment;

    public OrderListAdapter(Cursor cursor,Fragment fragment) {
        this.cursor = cursor;
        this.fragment=fragment;
    }

    @Override
    public OrderListAdapter.OrderItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final OrderListAdapter.OrderItemHolder holder, final int position) {


        cursor.moveToPosition(position);
        String status = cursor.getString(cursor.getColumnIndex("status"));

        if (status.equals("done") || status.equals("fr")
                || status.equals("pr") || status.equals("cna")
                || status.equals("dnd")
                || !status.equals("da")) {
            holder.btnReturn.setVisibility(View.GONE);
            holder.btnDelivery.setVisibility(View.GONE);
        } else {
            holder.btnReturn.setVisibility(View.VISIBLE);
            holder.btnDelivery.setVisibility(View.VISIBLE);
        }

        holder.btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeItem(position);
                cursor.moveToPosition(position);
                Intent intent = new Intent(view.getContext(), ReturnActivity.class);
                intent.putExtra("id", cursor.getString(cursor.getColumnIndex("id")));
                intent.putExtra("name", cursor.getString(cursor.getColumnIndex("name")));
                intent.putExtra("amount", cursor.getString(cursor.getColumnIndex("amount")));
                view.getContext().startActivity(intent);
            }
        });

        holder.btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeItem(position);
                Context context = view.getContext();
                cursor.moveToPosition(position);
                Intent intent = new Intent(context, OrderDetailsActivity.class);
                intent.putExtra("id", cursor.getString(cursor.getColumnIndex("id")));
                context.startActivity(intent);
            }
        });
        holder.btnDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                closeItem(position);
                cursor.moveToPosition(position);
                final DBHelper dbHelper = new DBHelper(view.getContext());
                final String name = cursor.getString(cursor.getColumnIndex("name"));

                if (!NetworkUtil.isNetworkAvailable(view.getContext())) {
                    dbHelper.updateOrderOffline(name, "done");
                    Toast.makeText(view.getContext(), "Order updated successfully", Toast.LENGTH_SHORT).show();
                    ((MainActivity.OnListChangeListener)fragment).onListChange();
                    return;
                }

                APIRequestBuilder builder = new APIRequestBuilder()
                        .setType(APIRequestBuilder.Type.PUT)
                        .setMethod("/api/fobae/v1/update_order")
                        .addParameters("order", name)
                        .authenticate(view.getContext());


                final ProgressDialog dialog = new ProgressDialog(view.getContext(), R.style.DialogTheme);
                dialog.setMessage("Updating Order...");
                dialog.setCancelable(false);
                dialog.show();
                new APIClient().execute(builder, new APIClient.APIResultCallback() {
                    @Override
                    public void onFinish(JSONObject object) {
                        if (!ErrorHandler.getInstance().handleError((Activity) view.getContext(), object)) {
                            dbHelper.updateOrder(name, "done");
                            cursor = dbHelper.getOrders();
                            notifyDataSetChanged();
                            Toast.makeText(view.getContext(), "Item Delivered", Toast.LENGTH_SHORT).show();
                            dialog.cancel();

                        }
                    }
                });
            }
        });


        holder.textCustomer.setText(cursor.getString(cursor.getColumnIndex("customer")));


        String street = cursor.getString(cursor.getColumnIndex("street"));
        String street2 = cursor.getString(cursor.getColumnIndex("street2"));
        String city = cursor.getString(cursor.getColumnIndex("city"));

        holder.textAddress.setText(street + "," + street2 + "," + city + "...");


        String statusDelivered = cursor.getString(cursor.getColumnIndex("status"));
        Log.d("bnm", statusDelivered + "");


        if (status.equals("done") || status.equals("ft") || status.equals("pr") || status.equals("cna")|| status.equals("dnd")) {
            holder.textStatus.setTextColor(holder.textStatus.getContext().getResources().getColor(R.color.textColorOK));
            holder.textStatus.setBackground(holder.textStatus.getContext().getResources().getDrawable(R.drawable.circle_delivey));
        } else {
            holder.textStatus.setTextColor(holder.textStatus.getContext().getResources().getColor(R.color.textColorFail));
            holder.textStatus.setBackground(holder.textStatus.getContext().getResources().getDrawable(R.drawable.circle_not_delivey));

        }
        if (status.equals("done")) {
            status = "Delivered";
        } else if (status.equals("fr")) {
            status = "Full Returned";
        } else if (status.equals("pr")) {
            status = "Partial Returned";
        } else if (status.equals("cna")) {
            status = "Customer Unavailable";
        } else if (status.equals("dnd")) {
            status = "Driver not delivered";
        }else if (status.equals("da")) {
            status = "Not Delivered";
        }else {
            status = "Driver Not Accepted";
        }

        holder.textStatus.setText("Shipping: " + status);

        String payment = cursor.getString(cursor.getColumnIndex("payment"));
        holder.textPayment.setText("Payment : " + payment);
        if (!payment.equals("Pay by Cash")) {
            holder.textPayment.setTextColor(holder.textPayment.getContext().getResources().getColor(R.color.textColorFail));
            holder.textPayment.setBackground(holder.textPayment.getContext().getResources().getDrawable(R.drawable.circle_not_delivey));
        } else {
            holder.textPayment.setTextColor(holder.textPayment.getContext().getResources().getColor(R.color.textColorOK));
            holder.textPayment.setBackground(holder.textPayment.getContext().getResources().getDrawable(R.drawable.circle_delivey));
        }

        if ( cursor.getInt(cursor.getColumnIndex("sync")) == 1) {
            holder.imgSync.setVisibility(View.VISIBLE);
        } else {
            holder.imgSync.setVisibility(View.GONE);
        }
        mItemManger.bindView(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    class OrderItemHolder extends RecyclerView.ViewHolder {
        public TextView textCustomer;
        public TextView textAddress;

        public TextView textStatus;
        public TextView textPayment;


        public ImageButton btnReturn;
        public ImageButton btnDetails;
        public ImageButton btnDelivery;

        public ImageView imgSync;

        public OrderItemHolder(View itemView) {
            super(itemView);

            textCustomer = (TextView) itemView.findViewById(R.id.textCustomer);
            textAddress = (TextView) itemView.findViewById(R.id.textAddress);
            textStatus = (TextView) itemView.findViewById(R.id.textStatus);
            textPayment = (TextView) itemView.findViewById(R.id.textPayment);


            btnReturn = (ImageButton) itemView.findViewById(R.id.item_return);
            btnDetails = (ImageButton) itemView.findViewById(R.id.item_details);
            btnDelivery = (ImageButton) itemView.findViewById(R.id.item_delivery);


            imgSync = (ImageView) itemView.findViewById(R.id.imgSync);

        }
    }
}
