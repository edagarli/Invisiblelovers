package me.edagarli.invisiblelovers.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.OtherLoginListener;
import cn.bmob.v3.listener.UpdateListener;
import me.edagarli.invisiblelovers.BaseActivity;
import me.edagarli.invisiblelovers.R;
import me.edagarli.invisiblelovers.bean.QQBean;
import me.edagarli.invisiblelovers.bean.QQSourceBean;
import me.edagarli.invisiblelovers.bean.QQUserInfo;
import me.edagarli.invisiblelovers.bean.User;
import me.edagarli.invisiblelovers.utils.FastJsonTools;
import me.edagarli.invisiblelovers.utils.NetUtils;

public class ThirdPartyLoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_party_login);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.login)
    public void login() {
        Intent intent = new Intent(ThirdPartyLoginActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.register)
    public void register() {
        Intent intent = new Intent(ThirdPartyLoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

//    @OnClick(R.id.weibo)
//    public void weiBoLogin() {
//        progressDialog.show();
//        progressDialog.setContentView(R.layout.loading_progress);
//        BmobUser.weiboLogin(this, "720086881", "http://www.bmob.cn", new OtherLoginListener() {
//            @Override
//            public void onSuccess(JSONObject jsonObject) {
//                dismissDialog();
////                ShowToast("weibo登陆成功返回:" + jsonObject);
//                Log.i("login", "weibo登陆成功返回:" + jsonObject.toString());
////                Intent intent = new Intent(ThirdPartyLoginActivity.this, MainActivity.class);
////                intent.putExtra("json", jsonObject.toString());
////                intent.putExtra("from", "weibo");
////                startActivity(intent);
//            }
//
//            @Override
//            public void onFailure(int i, String s) {
//                dismissDialog();
//                ShowToast("第三方登陆失败：" + s);
//            }
//
//            @Override
//            public void onCancel() {
//                dismissDialog();
//            }
//        });
//    }
//
//    @OnClick(R.id.qq)
//    public void qqLogin() {
//        progressDialog.show();
//        progressDialog.setContentView(R.layout.loading_progress);
//        BmobUser.qqLogin(this, "222222", new OtherLoginListener() {
//            @Override
//            public void onSuccess(JSONObject userAuth) {
//                dismissDialog();
//                //    ShowToast("QQ登陆成功返回:" + userAuth.toString());
//                Log.i("login", "QQ登陆成功返回:" + userAuth.toString());
//                QQSourceBean qq = FastJsonTools.createJsonBean(userAuth.toString(), QQSourceBean.class);
//                getQQInfo(qq);
////                Intent intent = new Intent(ThirdPartyLoginActivity.this, MainActivity.class);
////                intent.putExtra("json", userAuth.toString());
////                intent.putExtra("from", "qq");
////                startActivity(intent);
//            }
//
//            @Override
//            public void onFailure(int code, String msg) {
//                dismissDialog();
//                ShowToast("第三方登陆失败：" + msg);
//            }
//
//
//            @Override
//            public void onCancel() {
//                dismissDialog();
//            }
//        });
//    }

    public void getQQInfo(final QQSourceBean qq) {
        new Thread() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("access_token", qq.qq.access_token);// 此为QQ登陆成功之后返回access_token
                params.put("openid", qq.qq.openid);
                params.put("oauth_consumer_key", "222222");// oauth_consumer_key为申请QQ登录成功后，分配给应用的appid
                params.put("format", "json");// 格式--非必填项
                String result = NetUtils.getRequest(
                        "https://graph.qq.com/user/get_user_info", params);
                Message msg = new Message();
                msg.obj = result;
                handler.sendMessage(msg);
            }
        }.start();
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            String result = (String) msg.obj;
            if (result != null) {
                QQUserInfo qqUserInfo = FastJsonTools.createJsonBean(result, QQUserInfo.class);
                BmobUser bmobUser = BmobUser.getCurrentUser(getApplicationContext());
                progressDialog.show();
                progressDialog.setContentView(R.layout.loading_progress);
                final User user = new User();
                if (qqUserInfo.gender.equals("男")) {
                    user.setSex(true);
                } else {
                    user.setSex(false);
                }
                user.setNiceName(qqUserInfo.nickname);
                user.setSignature("这个家伙很懒,什么也不说");
                user.setCount(0);
//                user.setAvatar(qqUserInfo.figureurl_1);
                user.setStatus(false);
                user.update(getApplicationContext(), bmobUser.getObjectId(), new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        dismissDialog();
                        userManager.bindInstallationForRegister(user.getUsername());
                        Intent intent = new Intent(ThirdPartyLoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        dismissDialog();
                        ShowToast("登录失败");
                    }
                });
            } else {
                ShowToast("登录失败~");
            }
        }

        ;
    };
}
