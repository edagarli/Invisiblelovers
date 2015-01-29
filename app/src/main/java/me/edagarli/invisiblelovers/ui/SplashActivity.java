package me.edagarli.invisiblelovers.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import cn.bmob.v3.Bmob;
import me.edagarli.invisiblelovers.BaseActivity;
import me.edagarli.invisiblelovers.R;
import me.edagarli.invisiblelovers.config.Config;

public class SplashActivity extends BaseActivity {

    private static final int GO_HOME = 100;
    private static final int GO_LOGIN = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Bmob.initialize(this, Config.applicationId);
        if (userManager.getCurrentUser() != null) {
            mHandler.sendEmptyMessageDelayed(GO_HOME, 2000);
        } else {
            mHandler.sendEmptyMessageDelayed(GO_LOGIN, 2000);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_HOME:
                    startAnimActivity(MainActivity.class);
                    finish();
                    break;
                case GO_LOGIN:
                    startAnimActivity(ThirdPartyLoginActivity.class);
                    finish();
                    break;
            }
        }
    };
}
