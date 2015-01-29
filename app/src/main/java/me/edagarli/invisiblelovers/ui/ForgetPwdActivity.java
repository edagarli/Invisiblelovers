package me.edagarli.invisiblelovers.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.EmailVerifyListener;
import cn.bmob.v3.listener.ResetPasswordListener;
import me.edagarli.invisiblelovers.BaseActivity;
import me.edagarli.invisiblelovers.R;
import me.edagarli.invisiblelovers.utils.CommonUtils;

public class ForgetPwdActivity extends BaseActivity {

    @InjectView(R.id.email)
    EditText emailEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.okay)
    public void okay(){

        final String email =emailEt.getText().toString();

        if(TextUtils.isEmpty(email)){
            ShowToast(R.string.toast_email_null);
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

        BmobUser.resetPassword(this, email, new ResetPasswordListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                dismissDialog();
                ShowToast("重置密码请求成功，请到" + email + "邮箱进行密码重置操作。");
            }

            @Override
            public void onFailure(int code, String e) {
                // TODO Auto-generated method stub
                dismissDialog();
                ShowToast("重置密码失败:" + e);
            }
        });
    }
}
