package com.fobae.android.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.anwios.alog.Logs;
import com.fobae.android.managers.UserManager;
import com.fobae.android.ui.LoginActivity;

/**
 * Class Description
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 11-03-2016
 */
public class FobaeAuthenticator  extends AbstractAccountAuthenticator {

    private Context context;

    public FobaeAuthenticator(Context context) {
        super(context);
        this.context=context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Logs.d(accountType);
        final Bundle bundle = new Bundle();
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.ARG_AUTH_TYPE, authTokenType);
        //intent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;

    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String s, Bundle options) throws NetworkErrorException {
//        final Bundle result = new Bundle();
//        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
//        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
//        result.putString(AccountManager.KEY_AUTHTOKEN, "testtt");
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, "fobae.com");
        intent.putExtra(LoginActivity.ARG_AUTH_TYPE, "Full access");
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, "test");

        //intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        //intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, account.type);
       // intent.putExtra(LoginActivity.ARG_AUTH_TYPE, "Full access");


        final Bundle bundle = new Bundle();
        //bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        //bundle.putParcelable(AccountManager.KEY_ERROR_CODE, intent);
        bundle.putString(AccountManager.KEY_AUTHTOKEN, UserManager.getInstance().getUserKey(context));
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        return bundle;
    }



    @Override
    public Bundle getAccountCredentialsForCloning(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        return super.getAccountCredentialsForCloning(response, account);
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return "Driver access to fobae.com";
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }

}
