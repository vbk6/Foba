package com.fobae.android.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fobae.android.APIClient;
import com.fobae.android.APIRequestBuilder;
import com.fobae.android.DBHelper;
import com.fobae.android.NetworkUtil;
import com.fobae.android.R;
import com.fobae.android.managers.ErrorHandler;
import com.fobae.android.models.Order;
import com.fobae.android.ui.OrderDetailsActivity;
import com.google.zxing.Result;
import com.google.zxing.common.StringUtils;

import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BarcodeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BarcodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarcodeFragment extends Fragment implements ZXingScannerView.ResultHandler {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private  ZXingScannerView scannerView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BarcodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BarcodeFragment newInstance(String param1, String param2) {
        BarcodeFragment fragment = new BarcodeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public BarcodeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        scannerView=new ZXingScannerView(getActivity());
        return scannerView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();

    }

    @Override
    public void handleResult(Result result) {

        DBHelper dbHelper = new DBHelper(getActivity());
        int id=dbHelper.getOrderId(result.getText());


        if(id>0) {
            Order order=dbHelper.getOrder(String.valueOf(id));
            if(order.getStatus().equals("da")){
                Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
                intent.putExtra("id", String.valueOf(id));
                getActivity().startActivity(intent);
            }

            else{
                setAsDA(order);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 100);
                scannerView.startCamera();
            }


        }else {
            Toast.makeText(getActivity(), "Invalid Order :" + result.getText(), Toast.LENGTH_SHORT).show();

            scannerView.startCamera();

        }
    }



    public void setAsDA(final Order order) {


        if(!NetworkUtil.isNetworkAvailable(getContext())){

            //Check already returned
            if(order.getStatus().equals("fr")||order.getStatus().equals("pr")){
                Toast.makeText(getContext(), "Order already returned", Toast.LENGTH_SHORT).show();
                return;
            }

            DBHelper dbHelper = new DBHelper(getContext());
            dbHelper.updateOrderOffline(order.getName(), "da");
            Toast.makeText(getContext(), "Order marked as Driver Accepted", Toast.LENGTH_SHORT).show();
            return;
        }


        final DBHelper dbHelper = new DBHelper(getContext());
        APIRequestBuilder builder = new APIRequestBuilder()
                .setType(APIRequestBuilder.Type.PUT)
                .setMethod("/api/fobae/v1/update_order")
                .addParameters("order", order.getName())
                .addParameters("cna", "3")
                .authenticate(getContext());

        final ProgressDialog dialog = new ProgressDialog(getContext(), R.style.DialogTheme);
        dialog.setMessage("Updating Order...");
        dialog.setCancelable(false);
        dialog.show();
        new APIClient().execute(builder, new APIClient.APIResultCallback() {
            @Override
            public void onFinish(JSONObject object) {
                dialog.cancel();
                if (!ErrorHandler.getInstance().handleError(getActivity(), object)) {
                    dbHelper.updateOrder(order.getName(),"da");
                    Toast.makeText(getContext(), "Order marked as Driver Accepted", Toast.LENGTH_SHORT).show();
                }

            }
        });
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
        public void onFragmentInteraction(Uri uri);
    }

}
