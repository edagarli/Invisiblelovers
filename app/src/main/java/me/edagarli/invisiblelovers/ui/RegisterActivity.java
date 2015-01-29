package me.edagarli.invisiblelovers.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;
import me.edagarli.invisiblelovers.BaseActivity;
import me.edagarli.invisiblelovers.InvisibleLoversApplication;
import me.edagarli.invisiblelovers.R;
import me.edagarli.invisiblelovers.bean.User;
import me.edagarli.invisiblelovers.config.BmobConstants;
import me.edagarli.invisiblelovers.utils.CommonUtils;
import me.edagarli.invisiblelovers.utils.MD5Util;

public class RegisterActivity extends BaseActivity {

    @InjectView(R.id.email)
    EditText emailEt;

    @InjectView(R.id.password)
    EditText passwordEt;

    @InjectView(R.id.nicename)
    EditText niceNameEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        ButterKnife.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.register, menu);
        return true;
    }


    @OnClick(R.id.register)
    public void register(){
        String email = emailEt.getText().toString();
        String password=passwordEt.getText().toString();
        String niceName=niceNameEt.getText().toString();

        if(TextUtils.isEmpty(email)){
            ShowToast(R.string.toast_email_null);
            return;
        }

        if(!emailFormat(email)){
            ShowToast(R.string.toast_email_invalid);
            return;
        }

        if(TextUtils.isEmpty(password)){
            ShowToast(R.string.toast_pwd_null);
            return;
        }

        if(TextUtils.isEmpty(niceName)){
            ShowToast(R.string.toast_niceName_null);
            return;
        }

        boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
        if(!isNetConnected){
            ShowToast(R.string.network_tips);
            return;
        }

        closeInput();
        progressDialog.show();
        progressDialog.setContentView(R.layout.loading_progress);

        final User bu = new User();
        bu.setUsername(email);
        bu.setPassword(password);
        bu.setEmail(email);
        bu.setNiceName(niceName);
        bu.setSex(true);
        bu.setStatus(false);
        bu.setSignature("这个家伙很懒,什么也不说");
        bu.setCount(0);
        bu.signUp(this, new SaveListener() {
            @Override
            public void onSuccess() {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            EMChatManager.getInstance().createAccountOnServer(bu.getUsername().substring(0, bu.getUsername().indexOf("@")), bu.getPassword());
//                            EMChatManager.getInstance().updateCurrentUserNick(bu.getNiceName());
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (!RegisterActivity.this.isFinishing())
                                        progressDialog.dismiss();
                                    InvisibleLoversApplication.getInstance().setUserName(bu.getUsername().substring(0, bu.getUsername().indexOf("@")));
                                    InvisibleLoversApplication.currentUserNick =bu.getNiceName();
                                    ShowToast("注册成功");
                                    // 将设备与username进行绑定
                                    userManager.bindInstallationForRegister(bu.getUsername().substring(0, bu.getUsername().indexOf("@")));
                                    //发广播通知登陆页面退出
                                    sendBroadcast(new Intent(BmobConstants.ACTION_REGISTER_SUCCESS_FINISH));
                                    // 启动主页
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } catch (final EaseMobException e) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (!RegisterActivity.this.isFinishing())
                                        progressDialog.dismiss();
                                    int errorCode = e.getErrorCode();
                                    if (errorCode == EMError.NONETWORK_ERROR) {
                                        Toast.makeText(getApplicationContext(), "网络异常，请检查网络！", Toast.LENGTH_SHORT).show();
                                    } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                                        Toast.makeText(getApplicationContext(), "用户已存在！", Toast.LENGTH_SHORT).show();
                                    } else if (errorCode == EMError.UNAUTHORIZED) {
                                        Toast.makeText(getApplicationContext(), "注册失败，无权限！", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }}).start();
            }
            @Override
            public void onFailure(int code, String msg) {
                BmobLog.i(msg);
                progressDialog.dismiss();
                if(code==202){
                    ShowToast("用户名已经存在");
                }else if(code==203){
                    ShowToast("邮箱已经存在");
                }else{
                    ShowToast(msg);
                }
            }
        });
    }

    public static boolean emailFormat(String email)
    {
        boolean tag = true;
        final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        final Pattern pattern = Pattern.compile(pattern1);
        final Matcher mat = pattern.matcher(email);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }
}
