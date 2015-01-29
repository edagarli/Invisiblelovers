package me.edagarli.invisiblelovers;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.util.EasyUtils;
import com.umeng.analytics.MobclickAgent;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.util.BmobLog;
import me.edagarli.invisiblelovers.utils.CommonUtils;
import me.edagarli.invisiblelovers.utils.Constant;
import me.edagarli.invisiblelovers.utils.Sputil;


/**
 */
public class BaseActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static String TAG;
    private static final int notifiId = 11;
    protected NotificationManager notificationManager;
    public static final int MSG_ERROR = -1;
    protected ProgressDialog progressDialog;
    protected ActionBar mActionBar;
    LayoutInflater inflater;
    Toast mToast;
    protected InvisibleLoversApplication mMyApplication;
    protected Sputil sputil;
    protected Resources mResources;
    protected Context mContext;

    protected BmobUserManager userManager;
    protected BmobChatManager manager;

    /**
     * default BroadcastReceiver receiver route the onReceive to the
     * BaseActivity::onBroadcastReceive so , the subclass activity only need to
     * do override the method onBroadcastReceive, and call
     * registerBroadcastReceiver to register the acttion
     */
//    private BaseActivityReceiver mReceiver;
    public void closeInput() {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }


    public void dismissDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void startAnimActivity(Class<?> cla) {
        this.startActivity(new Intent(this, cla));
    }

    public void ShowToast(final String text) {
        if (!TextUtils.isEmpty(text)) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (mToast == null) {
                        mToast = Toast.makeText(getApplicationContext(), text,
                                Toast.LENGTH_LONG);
                    } else {
                        mToast.setText(text);
                    }
                    mToast.show();
                }
            });

        }
    }

    public void ShowToast(final int resId) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mToast == null) {
                    mToast = Toast.makeText(BaseActivity.this.getApplicationContext(), resId,
                            Toast.LENGTH_LONG);
                } else {
                    mToast.setText(resId);
                }
                mToast.show();
            }
        });
    }

    protected void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("正在联网...");
        progressDialog.setMessage("请稍后...");
        // progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void goBack(View view) {

        finish();
    }

    public void onRefresh(View view) {
        // finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initProgressDialog();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        userManager = BmobUserManager.getInstance(this);
        manager = BmobChatManager.getInstance(this);
        // 添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
        initConfigure();
    }

    private void initConfigure() {
        mContext = this;
        if (null == mMyApplication) {
            mMyApplication = InvisibleLoversApplication.getInstance();
        }
        mMyApplication.addActivity(this);
        if (null == sputil) {
            sputil = new Sputil(this, Constant.PRE_NAME);
        }
        sputil.getInstance().registerOnSharedPreferenceChangeListener(this);
        mResources = getResources();
    }

    /**
     * 打Log
     * ShowLog
     *
     * @return void
     * @throws
     */
    public void ShowLog(String msg) {
        BmobLog.i(msg);
    }

    protected InvisibleLoversApplication getAppContext() {
        InvisibleLoversApplication ac = (InvisibleLoversApplication) getApplication();
        return ac;
    }

    @Override
    protected void onDestroy() {
        // 结束Activity&从堆栈中移除
        AppManager.getAppManager().finishActivity(this);
//        if (mReceiver != null)
//            unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    /**
     * 当应用在前台时，如果当前消息不是属于当前会话，在状态栏提示一下
     * 如果不需要，注释掉即可
     *
     * @param message
     */
    protected void notifyNewMessage(EMMessage message) {
        //如果是设置了不提醒只显示数目的群组(这个是app里保存这个数据的，demo里不做判断)
        //以及设置了setShowNotificationInbackgroup:false(设为false后，后台时sdk也发送广播)
        if (!EasyUtils.isAppRunningForeground(this)) {
            return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getApplicationInfo().icon)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true);

        String ticker = CommonUtils.getMessageDigest(message, this);
        if (message.getType() == EMMessage.Type.TXT)
            ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
        //设置状态栏提示
        mBuilder.setTicker(message.getFrom() + ": " + ticker);

        Notification notification = mBuilder.build();
        notificationManager.notify(notifiId, notification);
        notificationManager.cancel(notifiId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onresume时，取消notification显示
        EMChatManager.getInstance().activityResumed();
        // umeng
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // umeng
        MobclickAgent.onPause(this);
    }


    //    /**
//     * register you custom actions
//     */
//    protected void registerBroadcastReceiver(String action) {
//        if (mReceiver == null)
//            mReceiver = new BaseActivityReceiver();
//        registerReceiver(mReceiver, new IntentFilter(action));
//    }
//
//    /**
//     * default handle for receiver
//     *
//     * @param context
//     * @param intent
//     */
//    protected void onBroadcastReceive(Context context, Intent intent) {
////        Log.d(this.getClass().getName(), "onBroadcastReceive no implementatio!");
//    }
//
//    private class BaseActivityReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            BaseActivity.this.onBroadcastReceive(context, intent);
//        }
//    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        // TODO Auto-generated method stub
        //可用于监听设置参数，然后作出响应
    }

    /**
     * 返回
     *
     * @param view
     */
    public void back(View view) {
        finish();
    }
}

