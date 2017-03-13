package com.fobae.android.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ListDeliveryFragment extends Fragment implements MainActivity.OnListChangeListener {




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

    public static ListDeliveryFragment newInstance(String param1, String param2) {
        ListDeliveryFragment fragment = new ListDeliveryFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListDeliveryFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listdelivery, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        DBHelper dbHelper = new DBHelper(getActivity());

        mListView = (RecyclerView) getView().findViewById(android.R.id.list);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Cursor cr = dbHelper.getOrders();

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


        final ProgressDialog dialog = new ProgressDialog(getActivity(), R.style.DialogTheme);
        dialog.setMessage("Loading orders...");
        dialog.setCancelable(false);
        dialog.show();
      //  getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        APIRequestBuilder builder = new APIRequestBuilder()
                .setType(APIRequestBuilder.Type.GET)
                .setMethod("/api/fobae/v1/get_orders")
                .authenticate(getActivity());

        new APIClient().execute(builder, new APIClient.APIResultCallback() {

            @Override
            public void onFinish(JSONObject object) {


                dialog.cancel();

                if (object != null) {

                    if (!ErrorHandler.getInstance().handleError(getActivity(), object)) {
                        DBHelper dbHelper = new DBHelper(getActivity());
                        try {
                            dbHelper.insertOrders(object.getJSONArray("orders"));
                            Cursor cr = dbHelper.getOrders();
                            if (cr.getCount() == 0) {
                                mListView.setVisibility(View.GONE);
                            } else {
                                mListView.setVisibility(View.VISIBLE);
                            }
                            mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            adapter = new OrderListAdapter(cr,ListDeliveryFragment.this);
                            mListView.setAdapter(adapter);

                        } catch (JSONException e) {
                        }
                    }
                }

            }

        });

       // getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }


    @Override
    public void onResume() {
        super.onResume();

        DBHelper dbHelper = new DBHelper(getActivity());

       // mListView = (RecyclerView) getView().findViewById(android.R.id.list);
        //mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Cursor cr = dbHelper.getOrders();

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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
