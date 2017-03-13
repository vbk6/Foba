package com.fobae.android.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.net.Authenticator;

/**
 * Class Description
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 11-03-2016
 */
public class AuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private FobaeAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new FobaeAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}