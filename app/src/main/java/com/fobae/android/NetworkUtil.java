package com.fobae.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Class Description
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 27-05-2015
 */
public class NetworkUtil {
    private static final String TAG="NETWORK";
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
       /* if(activeNetworkInfo != null && (activeNetworkInfo.isConnected()){
            InetAddress ipAddr = null; //You can replace it with your name
            try {
                ipAddr = InetAddress.getByName("google.com");
            } catch (UnknownHostException e) {
                return false
            }
            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        }else{
            return false;
        }*/
        return activeNetworkInfo != null && (activeNetworkInfo.isConnected());
    }
}
