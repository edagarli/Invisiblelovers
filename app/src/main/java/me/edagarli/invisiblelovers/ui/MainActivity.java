package me.edagarli.invisiblelovers.ui;


import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chatuidemo.activity.ChatActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.message.PushAgent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;
import me.edagarli.invisiblelovers.BaseActivity;
import me.edagarli.invisiblelovers.InvisibleLoversApplication;
import me.edagarli.invisiblelovers.R;
import me.edagarli.invisiblelovers.adapter.AIContentAdapter;
import me.edagarli.invisiblelovers.bean.QiangYu;
import me.edagarli.invisiblelovers.db.DatabaseUtil;
import me.edagarli.invisiblelovers.utils.ActivityUtil;
import me.edagarli.invisiblelovers.utils.Constant;
import me.edagarli.invisiblelovers.utils.LogUtils;


public class MainActivity extends BaseActivity{

    private DrawerLayout mDrawerLayout;
    private View mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private boolean drawerArrowColor;

    @InjectView(R.id.vPager)
    protected ViewPager viewPager;//页卡内容

    @InjectView(R.id.cursor)
    protected ImageView imageView;// 动画图片

    @InjectView(R.id.text1)
    protected TextView textView1;

    @InjectView(R.id.text2)
    protected TextView textView2;

    protected List<View> views;// Tab页面列表

    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private View view1, view2;//各个页卡
    private AIContentAdapter mAdapter;

    //    @InjectView(R.id.listview)
    protected ListView mListView, mListViewTwo;

    //    @InjectView(R.id.swipe_container)
//    protected SwipeRefreshLayout swipeLayout, swipeLayoutTwo;
    private ListView actualListView;
    private PullToRefreshListView mPullRefreshListView;
    private TextView networkTips;
    private ProgressBar progressbar;


    private ListView actualListViewTwo;
    private PullToRefreshListView mPullRefreshListViewTwo;
    private TextView networkTipsTwo;
    private ProgressBar progressbarTwo;
    private ArrayList<QiangYu> mListItemsTwo;
    private AIContentAdapter mAdapterTwo;


    private boolean pullFromUser;
    public enum RefreshType{
        REFRESH,LOAD_MORE
    }
    private int currentIndex ;
    private int pageNum;
    private String lastItemTime;//当前列表结尾的条目的创建时间，
    private RefreshType mRefreshType = RefreshType.LOAD_MORE;
    private ArrayList<QiangYu> mListItems;

    FeedbackAgent fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        ButterKnife.inject(this);

        MobclickAgent.setDebugMode(true);
        MobclickAgent.updateOnlineConfig(this);

        InitImageView();
        InitTextView();
        InitViewPager();

//        mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
//                getData()));

//        swipeLayout.setOnRefreshListener(this);
//        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (View) findViewById(R.id.navdrawer);

        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                drawerArrow, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();


        setUpUmengFeedback();
    }

    /**
     * @InjectView(R.id.hot) protected View hotView;
     * @InjectView(R.id.apply) protected View applyView;
     * @InjectView(R.id.feedback) protected View feedbackView;
     * @InjectView(R.id.share) protected View sharaeView;
     * @InjectView(R.id.setting) protected View settingView;
     */

    @OnClick(R.id.main)
    public void main() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @OnClick(R.id.hot)
    public void hot() {
        Intent intent = new Intent(MainActivity.this, HotActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.apply)
    public void apply() {
        Intent intent = new Intent(MainActivity.this, ApplyActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.feedback)
    public void feedback() {
//        Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
//        startActivity(intent);
        fb.startFeedbackActivity();
    }

    private void setUpUmengFeedback() {
        fb = new FeedbackAgent(this);
        // check if the app developer has replied to the feedback or not.
        fb.sync();
        fb.openFeedbackPush();
        fb.setWelcomeInfo("~~亲,感谢你来吐槽~~");
        PushAgent.getInstance(this).enable();
    }

    @OnClick(R.id.mine)
    public void mine(){
//        Intent intent = new Intent(MainActivity.this,MineActivity.class);
//        startActivity(intent);
          startActivity(new Intent(MainActivity.this, ChatActivity.class).putExtra("userId","虚拟恋人"));
    }

    @OnClick(R.id.share)
    public void share() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_description) + "\n" +
                "App下载地址: https://play.google.com/store/apps/details?id=" +
                getPackageName());
        startActivity(Intent.createChooser(share,
                getString(R.string.app_name)));
    }

    @OnClick(R.id.setting)
    public void setting() {
        Intent intent = new Intent(MainActivity.this, com.easemob.chatuidemo.activity.MainActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.exit)
    public void exit() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("正在退出登陆..");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        InvisibleLoversApplication.getInstance().logout(new EMCallBack() {

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        // 重新显示登陆页面
                        InvisibleLoversApplication.getInstance().logout();
                        MainActivity.this.finish();
                        startActivity(new Intent(MainActivity.this, ThirdPartyLoginActivity.class));

                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        ;

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

//    public void onRefresh() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                swipeLayout.setRefreshing(false);
//            }
//        }, 5000);
//    }

    private String getCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String times = formatter.format(new Date(System.currentTimeMillis()));
        return times;
    }

    private void InitViewPager() {
        views = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();
        view1 = inflater.inflate(R.layout.lay1, null);
        view2 = inflater.inflate(R.layout.lay2, null);
        views.add(view1);
        views.add(view2);
        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());

        mPullRefreshListView = (PullToRefreshListView)view1
                .findViewById(R.id.pull_refresh_list);
        networkTips = (TextView)view1.findViewById(R.id.networkTips);
        progressbar = (ProgressBar)view1.findViewById(R.id.progressBar);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                String label = DateUtils.formatDateTime(MainActivity.this, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
                pullFromUser = true;
                mRefreshType = RefreshType.REFRESH;
                pageNum = 0;
                lastItemTime = getCurrentTime();
                fetchData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                mRefreshType = RefreshType.LOAD_MORE;
                fetchData();
            }
        });
        mPullRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                // TODO Auto-generated method stub

            }
        });

        actualListView = mPullRefreshListView.getRefreshableView();
        mListItems = new ArrayList<QiangYu>();
        mAdapter = new AIContentAdapter(MainActivity.this, mListItems);
        actualListView.setAdapter(mAdapter);
        if(mListItems.size() == 0){
            fetchData();
        }
        mPullRefreshListView.setState(PullToRefreshBase.State.RELEASE_TO_REFRESH, true);
        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
//				MyApplication.getInstance().setCurrentQiangYu(mListItems.get(position-1));
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CommentActivity.class);
                intent.putExtra("data", mListItems.get(position-1));
                startActivity(intent);
            }
        });

       //--------
        mPullRefreshListViewTwo = (PullToRefreshListView)view2
                .findViewById(R.id.pull_refresh_list_two);
        networkTipsTwo = (TextView)view2.findViewById(R.id.networkTips_two);
        progressbarTwo = (ProgressBar)view2.findViewById(R.id.progressBar_two);
        mPullRefreshListViewTwo.setMode(PullToRefreshBase.Mode.BOTH);
        mPullRefreshListViewTwo.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                String label = DateUtils.formatDateTime(MainActivity.this, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                mPullRefreshListViewTwo.setMode(PullToRefreshBase.Mode.BOTH);
                pullFromUser = true;
                mRefreshType = RefreshType.REFRESH;
                pageNum = 0;
                lastItemTime = getCurrentTime();
                fetchDataTwo();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                mRefreshType = RefreshType.LOAD_MORE;
                fetchDataTwo();
            }
        });
        mPullRefreshListViewTwo.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                // TODO Auto-generated method stub

            }
        });

        actualListViewTwo = mPullRefreshListViewTwo.getRefreshableView();
        mListItemsTwo = new ArrayList<QiangYu>();
        mAdapterTwo = new AIContentAdapter(MainActivity.this, mListItemsTwo);
        actualListViewTwo.setAdapter(mAdapterTwo);
        if(mListItemsTwo.size() == 0){
            fetchDataTwo();
        }
        mPullRefreshListViewTwo.setState(PullToRefreshBase.State.RELEASE_TO_REFRESH, true);
        actualListViewTwo.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
//				MyApplication.getInstance().setCurrentQiangYu(mListItems.get(position-1));
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CommentActivity.class);
                intent.putExtra("data", mListItemsTwo.get(position-1));
                startActivity(intent);
            }
        });
    }


    public void fetchDataTwo(){
        setStateTwo(LOADING);
        BmobQuery<QiangYu> query = new BmobQuery<QiangYu>();
        query.order("-createdAt");
//		query.setCachePolicy(CachePolicy.NETWORK_ONLY);
        query.setLimit(Constant.NUMBERS_PER_PAGE);
        BmobDate date = new BmobDate(new Date(System.currentTimeMillis()));
        query.addWhereLessThan("createdAt", date);
        LogUtils.i(TAG, "SIZE:" + Constant.NUMBERS_PER_PAGE * pageNum);
        query.setSkip(Constant.NUMBERS_PER_PAGE*(pageNum++));
        LogUtils.i(TAG,"SIZE:"+Constant.NUMBERS_PER_PAGE*pageNum);
        query.include("author");
        query.findObjects(MainActivity.this, new FindListener<QiangYu>() {

            @Override
            public void onSuccess(List<QiangYu> list) {
                // TODO Auto-generated method stub
                LogUtils.i(TAG,"find success."+list.size());
                if(list.size()!=0&&list.get(list.size()-1)!=null){
                    if(mRefreshType==RefreshType.REFRESH){
                        mListItemsTwo.clear();
                    }
                    if(list.size()<Constant.NUMBERS_PER_PAGE){
                        LogUtils.i(TAG,"已加载完所有数据~");
                    }
                    if(InvisibleLoversApplication.getInstance().getCurrentUser()!=null){
                        list = DatabaseUtil.getInstance(getAppContext()).setFav(list);
                    }
                    mListItemsTwo.addAll(list);
                    mAdapterTwo.notifyDataSetChanged();

                    setStateTwo(LOADING_COMPLETED);
                    mPullRefreshListViewTwo.onRefreshComplete();
                }else{
                    ActivityUtil.show(MainActivity.this, "暂无更多数据~");
                    pageNum--;
                    setStateTwo(LOADING_COMPLETED);
                    mPullRefreshListViewTwo.onRefreshComplete();
                }
            }

            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub
                LogUtils.i(TAG,"find failed."+arg1);
                pageNum--;
                setStateTwo(LOADING_FAILED);
                mPullRefreshListViewTwo.onRefreshComplete();
            }
        });
    }

    public void fetchData(){
        setState(LOADING);
        BmobQuery<QiangYu> query = new BmobQuery<QiangYu>();
        query.order("-love,-createdAt");
//		query.setCachePolicy(CachePolicy.NETWORK_ONLY);
        query.setLimit(Constant.NUMBERS_PER_PAGE);
        BmobDate date = new BmobDate(new Date(System.currentTimeMillis()));
        query.addWhereLessThan("createdAt", date);
        LogUtils.i(TAG, "SIZE:" + Constant.NUMBERS_PER_PAGE * pageNum);
        query.setSkip(Constant.NUMBERS_PER_PAGE*(pageNum++));
        LogUtils.i(TAG,"SIZE:"+Constant.NUMBERS_PER_PAGE*pageNum);
        query.include("author");
        query.findObjects(MainActivity.this, new FindListener<QiangYu>() {

            @Override
            public void onSuccess(List<QiangYu> list) {
                // TODO Auto-generated method stub
                LogUtils.i(TAG,"find success."+list.size());
                if(list.size()!=0&&list.get(list.size()-1)!=null){
                    if(mRefreshType==RefreshType.REFRESH){
                        mListItems.clear();
                    }
                    if(list.size()<Constant.NUMBERS_PER_PAGE){
                        LogUtils.i(TAG,"已加载完所有数据~");
                    }
                    if(InvisibleLoversApplication.getInstance().getCurrentUser()!=null){
                        list = DatabaseUtil.getInstance(getAppContext()).setFav(list);
                    }
                    mListItems.addAll(list);
                    mAdapter.notifyDataSetChanged();

                    setState(LOADING_COMPLETED);
                    mPullRefreshListView.onRefreshComplete();
                }else{
                    ActivityUtil.show(MainActivity.this, "暂无更多数据~");
                    pageNum--;
                    setState(LOADING_COMPLETED);
                    mPullRefreshListView.onRefreshComplete();
                }
            }

            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub
                LogUtils.i(TAG,"find failed."+arg1);
                pageNum--;
                setState(LOADING_FAILED);
                mPullRefreshListView.onRefreshComplete();
            }
        });
    }

    /**
     * 初始化头标
     */

    private void InitTextView() {
        textView1.setOnClickListener(new MyOnClickListener(0));
        textView2.setOnClickListener(new MyOnClickListener(1));
    }

    /**
     * 2      * 初始化动画
     * 3
     */

    private void InitImageView() {
        imageView = (ImageView) findViewById(R.id.cursor);
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView.setImageMatrix(matrix);// 设置动画初始位置
    }

    /**
     * 头标点击监听 3
     */
    private class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            viewPager.setCurrentItem(index);
        }

    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量

        public void onPageScrollStateChanged(int arg0) {


        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        public void onPageSelected(int arg0) {
            Animation animation = new TranslateAnimation(one * currIndex, one * arg0, 0, 0);
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            imageView.startAnimation(animation);
//            Toast.makeText(MainActivity.this, "您选择了" + viewPager.getCurrentItem() + "页卡", Toast.LENGTH_SHORT).show();
        }

    }

    private static long firstTime;
    /**
     * 连续按两次返回键就退出
     */
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (firstTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            ShowToast("再按一次退出程序");
        }
        firstTime = System.currentTimeMillis();
    }


    private static final int LOADING = 1;
    private static final int LOADING_COMPLETED = 2;
    private static final int LOADING_FAILED =3;
    private static final int NORMAL = 4;
    public void setState(int state){
        switch (state) {
            case LOADING:
                if(mListItems.size() == 0){
                    mPullRefreshListView.setVisibility(View.GONE);
                    progressbar.setVisibility(View.VISIBLE);
                }
                networkTips.setVisibility(View.GONE);

                break;
            case LOADING_COMPLETED:
                networkTips.setVisibility(View.GONE);
                progressbar.setVisibility(View.GONE);

                mPullRefreshListView.setVisibility(View.VISIBLE);
                mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);


                break;
            case LOADING_FAILED:
                if(mListItems.size()==0){
                    mPullRefreshListView.setVisibility(View.VISIBLE);
                    mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    networkTips.setVisibility(View.VISIBLE);
                }
                progressbar.setVisibility(View.GONE);
                break;
            case NORMAL:

                break;
            default:
                break;
        }
    }

    public void setStateTwo(int state){
        switch (state) {
            case LOADING:
                if(mListItemsTwo.size() == 0){
                    mPullRefreshListViewTwo.setVisibility(View.GONE);
                    progressbarTwo.setVisibility(View.VISIBLE);
                }
                networkTipsTwo.setVisibility(View.GONE);

                break;
            case LOADING_COMPLETED:
                networkTipsTwo.setVisibility(View.GONE);
                progressbarTwo.setVisibility(View.GONE);

                mPullRefreshListViewTwo.setVisibility(View.VISIBLE);
                mPullRefreshListViewTwo.setMode(PullToRefreshBase.Mode.BOTH);


                break;
            case LOADING_FAILED:
                if(mListItemsTwo.size()==0){
                    mPullRefreshListViewTwo.setVisibility(View.VISIBLE);
                    mPullRefreshListViewTwo.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    networkTipsTwo.setVisibility(View.VISIBLE);
                }
                progressbarTwo.setVisibility(View.GONE);
                break;
            case NORMAL:

                break;
            default:
                break;
        }
    }
}

