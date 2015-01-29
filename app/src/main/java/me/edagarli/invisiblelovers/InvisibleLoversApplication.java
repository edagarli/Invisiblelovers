package me.edagarli.invisiblelovers;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.easemob.EMCallBack;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.umeng.fb.push.FeedbackPush;

import java.io.File;
import java.util.Map;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobUser;
import me.edagarli.invisiblelovers.bean.QiangYu;
import me.edagarli.invisiblelovers.domain.User;
import me.edagarli.invisiblelovers.utils.ActivityManagerUtils;

/**
 * Created by jhp-android on 14-11-19.
 */
public class InvisibleLoversApplication extends Application{

    public static InvisibleLoversApplication mInstance;

    // login user name
    public final String PREF_USERNAME = "username";

    public static String TAG;
    public static Context applicationContext;

    public static String currentUserNick = "";
    public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();
    private QiangYu currentQiangYu = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        applicationContext = this;
        hxSDKHelper.onInit(applicationContext);
        TAG = this.getClass().getSimpleName();
        FeedbackPush.getInstance(this).init(true);
        AppPush.getInstance(this).init();
        initImageLoader();
    }

    public static InvisibleLoversApplication getInstance() {
        return mInstance;
    }

    /**
     * 退出登录,清空缓存数据
     */
    public void logout() {
        BmobUserManager.getInstance(getApplicationContext()).logout();
    }

    public QiangYu getCurrentQiangYu() {
        return currentQiangYu;
    }

    public void setCurrentQiangYu(QiangYu currentQiangYu) {
        this.currentQiangYu = currentQiangYu;
    }


    public void addActivity(Activity ac){
        ActivityManagerUtils.getInstance().addActivity(ac);
    }


    public me.edagarli.invisiblelovers.bean.User getCurrentUser() {
        me.edagarli.invisiblelovers.bean.User user = BmobUser.getCurrentUser(mInstance, me.edagarli.invisiblelovers.bean.User.class);
        if(user!=null){
            return user;
        }
        return null;
    }

    /**
     * 获取内存中好友user list
     *
     * @return
     */
    public Map<String, User> getContactList() {
        return hxSDKHelper.getContactList();
    }

    /**
     * 设置好友user list到内存中
     *
     * @param contactList
     */
    public void setContactList(Map<String, User> contactList) {
        hxSDKHelper.setContactList(contactList);
    }

    /**
     * 获取当前登陆用户名
     *
     * @return
     */
    public String getUserName() {
        return hxSDKHelper.getHXId();
    }

    /**
     * 获取密码
     *
     * @return
     */
    public String getPassword() {
        return hxSDKHelper.getPassword();
    }

    /**
     * 设置用户名
     *
     */
    public void setUserName(String username) {
        hxSDKHelper.setHXId(username);
    }

    /**
     * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
     * 内部的自动登录需要的密码，已经加密存储了
     *
     * @param pwd
     */
    public void setPassword(String pwd) {
        hxSDKHelper.setPassword(pwd);
    }

    /**
     * 退出登录,清空数据
     */
    public void logout(final EMCallBack emCallBack) {
        // 先调用sdk logout，在清理app中自己的数据
        hxSDKHelper.logout(emCallBack);
    }

    /**
     * 初始化imageLoader
     */
    public void initImageLoader(){
        File cacheDir = StorageUtils.getCacheDirectory(getApplicationContext());
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCache(new LruMemoryCache(5*1024*1024))
                .memoryCacheSize(10*1024*1024)
                .discCache(new UnlimitedDiscCache(cacheDir))
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);
    }

    public DisplayImageOptions getOptions(int drawableId){
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(drawableId)
                .showImageForEmptyUri(drawableId)
                .showImageOnFail(drawableId)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public Activity getTopActivity(){
        return ActivityManagerUtils.getInstance().getTopActivity();
    }
}

