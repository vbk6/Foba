package com.fobae.android.managers;

import android.accounts.AccountManager;
import android.content.Context;

/**
 * UserManager handles all user operations, its singleton (cant create object),
 * use  UserManager.getInstance()
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 17-02-2016
 */
public class UserManager {
    private static final String USER_PREF ="user_pref";
    private static final String USER_NAME ="user_name";
    private static final String USER_KEY ="user_key";
    private static final UserManager INSTANCE =new UserManager();

    public static UserManager getInstance() {
        return INSTANCE;
    }

    public static boolean isUserLoggedIn(Context context){
        return !context.getSharedPreferences(USER_PREF,Context.MODE_PRIVATE).getString(USER_NAME,"").equals("");
    }

    public  void setUser(Context context,String username,String key){
        context.getSharedPreferences(USER_PREF,Context.MODE_PRIVATE).edit()
                .putString(USER_NAME,username)
                .putString(USER_KEY,key)
                .commit();
    }

    public  String getUsername(Context context){
        return context.getSharedPreferences(USER_PREF,Context.MODE_PRIVATE).getString(USER_NAME,"");
    }
    public  String getUserKey(Context context){
        return context.getSharedPreferences(USER_PREF,Context.MODE_PRIVATE).getString(USER_KEY,"");
    }
    public  void logoutUser(Context context){
        if(AccountManager.get(context).getAccountsByType("fobae.com").length >0) {
            AccountManager.get(context).removeAccount(AccountManager.get(context).getAccountsByType("fobae.com")[0], null, null);
        }
        context.getSharedPreferences(USER_PREF,Context.MODE_PRIVATE).edit()
                .clear().commit();
    }
}
