package me.edagarli.invisiblelovers.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.SaveListener;
import me.edagarli.invisiblelovers.BaseActivity;
import me.edagarli.invisiblelovers.InvisibleLoversApplication;
import me.edagarli.invisiblelovers.R;
import me.edagarli.invisiblelovers.config.BmobConstants;
import me.edagarli.invisiblelovers.db.UserDao;
import me.edagarli.invisiblelovers.domain.User;
import me.edagarli.invisiblelovers.utils.CommonUtils;
import me.edagarli.invisiblelovers.utils.Constant;
import me.edagarli.invisiblelovers.utils.MD5Util;

public class LoginActivity extends BaseActivity {


    @InjectView(R.id.email)
    EditText emailEt;

    @InjectView(R.id.password)
    EditText passwordEt;

    private MyBroadcastReceiver receiver = new MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.inject(this);
        //注册退出广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BmobConstants.ACTION_REGISTER_SUCCESS_FINISH);
        registerReceiver(receiver, filter);
    }

    @OnClick(R.id.login)
    public void loginBtn() {
        boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
        if (!isNetConnected) {
            ShowToast(R.string.network_tips);
            return;
        }
        login();
    }

    @OnClick(R.id.forgetpwd)
    public void forgetpwd() {
        Intent intent = new Intent(LoginActivity.this, ForgetPwdActivity.class);
        startActivity(intent);
    }


    private void login() {
        final String email = emailEt.getText().toString();
        final String password = passwordEt.getText().toString();
        if (TextUtils.isEmpty(email)) {
            ShowToast(R.string.toast_email_null);
            return;
        }

        if (!emailFormat(email)) {
            ShowToast(R.string.toast_email_invalid);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            ShowToast(R.string.toast_pwd_null);
            return;
        }

        closeInput();
        progressDialog.show();
        progressDialog.setContentView(R.layout.loading_progress);

        userManager.login(email, password, new SaveListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        progressDialog.setMessage("登录中...");
                        // 调用sdk登陆方法登陆聊天服务器
                        EMChatManager.getInstance().login(email.substring(0,email.indexOf("@")), password, new EMCallBack() {

                            @Override
                            public void onSuccess() {

                                // 登陆成功，保存用户名密码
                                InvisibleLoversApplication.getInstance().setUserName(email);
                                InvisibleLoversApplication.getInstance().setPassword(password);

                                try {
                                    // ** 第一次登录或者之前logout后，加载所有本地群和回话
                                    // ** manually load all local groups and
                                    // conversations in case we are auto login
                                    EMGroupManager.getInstance().loadAllGroups();
                                    EMChatManager.getInstance().loadAllConversations();

                                    // demo中简单的处理成每次登陆都去获取好友username，开发者自己根据情况而定
                                    List<String> usernames = EMContactManager.getInstance().getContactUserNames();
                                    EMLog.d("roster", "contacts size: " + usernames.size());
                                    Map<String, User> userlist = new HashMap<String, User>();
                                    for (String username : usernames) {
                                        User user = new User();
                                        user.setUsername(username);
                                        setUserHearder(username, user);
                                        userlist.put(username, user);
                                    }
                                    // 添加user"申请与通知"
                                    User newFriends = new User();
                                    newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
                                    newFriends.setNick("申请与通知");
                                    newFriends.setHeader("");
                                    userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
                                    // 添加"群聊"
                                    User groupUser = new User();
                                    groupUser.setUsername(Constant.GROUP_USERNAME);
                                    groupUser.setNick("群聊");
                                    groupUser.setHeader("");
                                    userlist.put(Constant.GROUP_USERNAME, groupUser);

                                    // 存入内存
                                    InvisibleLoversApplication.getInstance().setContactList(userlist);
                                    // 存入db
                                    UserDao dao = new UserDao(LoginActivity.this);
                                    List<User> users = new ArrayList<User>(userlist.values());
                                    dao.saveContactList(users);

                                    // 获取群聊列表(群聊里只有groupid和groupname等简单信息，不包含members),sdk会把群组存入到内存和db中
                                    EMGroupManager.getInstance().getGroupsFromServer();

                                    progressDialog.dismiss();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    LoginActivity.this.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onProgress(int progress, String status) {

                            }

                            @Override
                            public void onError(final int code, final String message) {

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                        ShowToast("登录失败: " + message);

                                    }
                                });
                            }
                        });
                    }
                }
                );
            };

                    @Override
                    public void onFailure(int i, String s) {
                        BmobLog.i(s);
                        progressDialog.dismiss();
                        if (i == 205) {
                            ShowToast("没有找到此邮件的用户");
                        } else if (i == 101) {
                            ShowToast("邮件或密码不正确");
                        } else {
                            ShowToast(s);
                        }
                    }
                });
            }

            public class MyBroadcastReceiver extends BroadcastReceiver {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && BmobConstants.ACTION_REGISTER_SUCCESS_FINISH.equals(intent.getAction())) {
                        finish();
                    }
                }

            }

            public static boolean emailFormat(String email) {
                boolean tag = true;
                final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                final Pattern pattern = Pattern.compile(pattern1);
                final Matcher mat = pattern.matcher(email);
                if (!mat.find()) {
                    tag = false;
                }
                return tag;
            }

            /**
             * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
             *
             * @param username
             * @param user
             */
            protected void setUserHearder(String username, User user) {
                String headerName = null;
                if (!TextUtils.isEmpty(user.getNick())) {
                    headerName = user.getNick();
                } else {
                    headerName = user.getUsername();
                }
                if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
                    user.setHeader("");
                } else if (Character.isDigit(headerName.charAt(0))) {
                    user.setHeader("#");
                } else {
                    user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1).toUpperCase());
                    char header = user.getHeader().toLowerCase().charAt(0);
                    if (header < 'a' || header > 'z') {
                        user.setHeader("#");
                    }
                }
            }
        }
