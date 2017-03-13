package com.fobae.android.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.anwios.alog.Logs;
import com.fobae.android.DBHelper;
import com.fobae.android.R;
import com.fobae.android.fragments.BarcodeFragment;
import com.fobae.android.fragments.ListDeliveryFragment;
import com.fobae.android.fragments.ToDeliveryFragment;
import com.fobae.android.managers.UserManager;
import com.fobae.android.views.SlidingTabLayout;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

/**
 * Main Screen with tab Orders,Map,Barcode and a Menu
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private SlidingTabLayout tabLayout;

    private String[] tabs = {"List", "Map","To Deliver","Barcode"};
    private SupportMapFragment mapFragment;

    private ListDeliveryFragment listDeliveryFragment;
    private ToDeliveryFragment toDeliveryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        viewPager.setAdapter(new TSViewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setViewPager(viewPager);
        tabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));


        mapFragment = new SupportMapFragment();
        mapFragment.getMapAsync(this);


        listDeliveryFragment = new ListDeliveryFragment();
        toDeliveryFragment =new ToDeliveryFragment();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logs.db(new DBHelper(this).getOrders(), "id", "name", "status", "sync");
        Logs.db(new DBHelper(this).getLines("575"),"id","order_id","product");
        Logs.db(new DBHelper(this).getOrderlines(), "id", "order_id", "product", "product_id", "amount", "qty", "returns");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
            builder.setMessage("Do you want to clear orders?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DBHelper dbHelper = new DBHelper(MainActivity.this);

                           int id= viewPager.getCurrentItem();

                               if(id==0) {

                                   if (listDeliveryFragment != null) {
                                       dbHelper.clearOrders();
                                       ((OnListChangeListener) listDeliveryFragment).onListChange();
                                   }
                               }else if(id==2){
                                   if (toDeliveryFragment != null) {
                                       ((OnListChangeListener) toDeliveryFragment).onListChange();
                                   }
                               }
                        }
                    })
                    .setCancelable(false)
                    .setNegativeButton("No", null)
                    .show().show();

            return true;
        } else if (id == R.id.action_logout) {
            new DBHelper(this).clearOrders();
            UserManager.getInstance().logoutUser(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (id == R.id.action_details) {
            final Dialog dialog = new Dialog(this, R.style.DialogTheme);
            dialog.setContentView(R.layout.dialog_details);
            dialog.setCancelable(false);
            TextView textTotal = (TextView) dialog.findViewById(R.id.textTotal);
            TextView textYetToDeliver = (TextView) dialog.findViewById(R.id.textYetToDeliver);
            TextView textDelivered = (TextView) dialog.findViewById(R.id.textDelivered);
            TextView textCOH = (TextView) dialog.findViewById(R.id.textCOH);

            TextView textReturned = (TextView) dialog.findViewById(R.id.textReturned);
            TextView textPReturned = (TextView) dialog.findViewById(R.id.textPReturned);
            TextView textCNA = (TextView) dialog.findViewById(R.id.textCNA);
            TextView textDND = (TextView) dialog.findViewById(R.id.textDND);

            DBHelper dbHelper = new DBHelper(this);
            textTotal.setText(String.format(getString(R.string.dialog_details_total), dbHelper.getTotalOrders()));

            int yetToDeliver= dbHelper.getTotalOrders()
                    - dbHelper.getDeliveredOrders()
                    - dbHelper.getReturnedOrders()
                    - dbHelper.getPROrders()
                    - dbHelper.getCNAOrders()
                    - dbHelper.getDNDOrders();
            textYetToDeliver.setText(String.format(getString(R.string.dialog_details_yet),yetToDeliver));
            textDelivered.setText(String.format(getString(R.string.dialog_details_done), dbHelper.getDeliveredOrders()));
            textCOH.setText(String.format(getString(R.string.dialog_details_coh), dbHelper.getTotalCOH()));

            textReturned.setText(String.format(getString(R.string.dialog_details_return), dbHelper.getReturnedOrders()));
            textPReturned.setText(String.format(getString(R.string.dialog_details_preturn), dbHelper.getPROrders()));
            textCNA.setText(String.format(getString(R.string.dialog_details_cna), dbHelper.getCNAOrders()));
            textDND.setText(String.format(getString(R.string.dialog_details_dnd), dbHelper.getDNDOrders()));


            Button buttonOk = (Button) dialog.findViewById(R.id.buttonOk);
            buttonOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }

            });
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(37.4, -122.1))
                .radius(1000);

        Circle circle = googleMap.addCircle(circleOptions);
    }


    private class TSViewPagerAdapter extends FragmentStatePagerAdapter {
        public TSViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return listDeliveryFragment;
                case 1:
                    return mapFragment;
                case 2:
                    return toDeliveryFragment;
                case 3:
                    return new BarcodeFragment();

                default:
                    return mapFragment;
            }

        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }
    }


    public interface OnListChangeListener {
        public void onListChange();
    }
}