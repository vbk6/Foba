package com.fobae.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.anwios.alog.Logs;
import com.fobae.android.DBHelper;
import com.fobae.android.R;
import com.fobae.android.managers.UserManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.setTag("AndFob");
        setContentView(R.layout.activity_splash);

        Logs.db(new DBHelper(this).getOrders(),"id","name");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                if (UserManager.getInstance().isUserLoggedIn(SplashActivity.this)) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
