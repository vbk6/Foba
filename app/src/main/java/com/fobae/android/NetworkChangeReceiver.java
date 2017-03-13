package com.fobae.android;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Class Description
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 14-03-2016
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(NetworkUtil.isNetworkAvailable(context)){
            //context.startService(new Intent(context,SyncService.class));
            context.startService(new Intent(context,SyncService.class));
            //ContentResolver.requestSync(AccountManager.get(context).getAccountsByType("fobae.com")[0],"com.fobae.android.provider",new Bundle());
        }
    }

}
