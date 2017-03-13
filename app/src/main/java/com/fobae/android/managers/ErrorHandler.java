package com.fobae.android.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.fobae.android.ui.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class Description
 *
 * @author Nooh KVM
 * @version 1.0
 * @since 29-02-2016
 */
public class ErrorHandler {

    private static final ErrorHandler INSTANCE = new ErrorHandler();

    public static ErrorHandler getInstance() {
        return INSTANCE;
    }


    public boolean handleError(Activity activity, JSONObject object) {
        try {
            if (object == null) {
                Toast.makeText(activity, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                if (object.has("needToLogin") && object.getBoolean("needToLogin")) {
                    UserManager.getInstance().logoutUser(activity);
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                    activity.finish();
                    return true;
                } else if (object.has("error") && object.getBoolean("error")) {
                    Toast.makeText(activity, object.getString("message"), Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        } catch (JSONException e) {
            Toast.makeText(activity, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

}
