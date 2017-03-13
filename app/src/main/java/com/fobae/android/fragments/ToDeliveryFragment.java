package com.fobae.android.fragments;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fobae.android.APIClient;
import com.fobae.android.APIRequestBuilder;
import com.fobae.android.DBHelper;
import com.fobae.android.R;
import com.fobae.android.adapters.OrderListAdapter;
import com.fobae.android.managers.ErrorHandler;
import com.fobae.android.ui.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vishnubk on 7/4/16.
 */
public class ToDeliveryFragment extends Fragment implements MainActivity.OnListChangeListener {


    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private RecyclerView mListView;
    private OrderListAdapter adapter;
    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */

    public static ToDeliveryFragment newInstance(String param1, String param2) {
        ToDeliveryFragment fragment = new ToDeliveryFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ToDeliveryFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todelivery, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        DBHelper dbHelper = new DBHelper(getActivity());

        mListView = (RecyclerView) getView().findViewById(android.R.id.list);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Cursor cr = dbHelper.getOrdersToDeliver();
         Log.d("gg",cr.getCount()+"");
        if(cr.getCount()==0){
            mListView.setVisibility(View.GONE);
        }else{
            mListView.setVisibility(View.VISIBLE);
        }

        if (cr.getCount() > 0) {
            adapter = new OrderListAdapter(cr,this);
            mListView.setAdapter(adapter);
        } else {
           loadOrders();
        }
        // mAdapter=new OrderAdapter(ListDeliveryFragment.this, dbHelper.getOrders());
        //((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        //mListView.setOnItemClickListener(ListDeliveryFragment.this);


    }

    private void loadOrders() {

                         DBHelper dbHelper = new DBHelper(getActivity());
                         Cursor cr = dbHelper.getOrdersToDeliver();

                       Log.d("hjk",cr.getCount()+"kk");
                        if(cr.getCount()==0){

                            mListView.setVisibility(View.GONE);
                        }else{

                            mListView.setVisibility(View.VISIBLE);
                        }
                if (cr.getCount() > 0) {
                    mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    adapter = new OrderListAdapter(cr,this);
                    mListView.setAdapter(adapter);
                }else
                    mListView.setVisibility(View.GONE);



    }


    @Override
    public void onResume() {
        super.onResume();

        DBHelper dbHelper = new DBHelper(getActivity());

        // mListView = (RecyclerView) getView().findViewById(android.R.id.list);
        //mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Cursor cr = dbHelper.getOrdersToDeliver();

        if (cr.getCount() > 0) {
            adapter = new OrderListAdapter(cr,this);
            mListView.setAdapter(adapter);
        } else {
            loadOrders();
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }




    @Override
    public void onListChange() {
      loadOrders();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }
}
