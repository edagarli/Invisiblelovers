package me.edagarli.invisiblelovers.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import me.edagarli.invisiblelovers.BaseActivity;
import me.edagarli.invisiblelovers.R;
import me.edagarli.invisiblelovers.adapter.AIContentAdapter;
import me.edagarli.invisiblelovers.adapter.BaseContentAdapter;
import me.edagarli.invisiblelovers.bean.QiangYu;
import me.edagarli.invisiblelovers.bean.User;
import me.edagarli.invisiblelovers.utils.ActivityUtil;
import me.edagarli.invisiblelovers.utils.Constant;
import me.edagarli.invisiblelovers.utils.LogUtils;

public class HotActivity extends BaseActivity {

    private int pageNum;
    private String lastItemTime;

    protected ArrayList<QiangYu> mListItems;
    private PullToRefreshListView mPullRefreshListView;
    private BaseContentAdapter<QiangYu> mAdapter;
    private ListView actualListView;

    private TextView networkTips;
    private ProgressBar progressbar;
    private boolean pullFromUser;
    public enum RefreshType{
        REFRESH,LOAD_MORE
    }
    private RefreshType mRefreshType = RefreshType.LOAD_MORE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        pageNum = 0;
        lastItemTime = getCurrentTime();

        mPullRefreshListView = (PullToRefreshListView)findViewById(R.id.pull_refresh_list);
        networkTips = (TextView)findViewById(R.id.networkTips);
        progressbar = (ProgressBar)findViewById(R.id.progressBar);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                String label = DateUtils.formatDateTime(HotActivity.this, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
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
        mAdapter = new AIContentAdapter(mContext, mListItems);
        actualListView.setAdapter(mAdapter);

        if(mListItems.size() == 0){
            fetchData();
        }
        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(HotActivity.this, CommentActivity.class);
                intent.putExtra("data", mListItems.get(position-1));
                startActivity(intent);
            }
        });
    }

    private String getCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String times = formatter.format(new Date(System.currentTimeMillis()));
        return times;
    }


    public void fetchData(){
        setState(LOADING);
        User user = BmobUser.getCurrentUser(mContext, User.class);
        BmobQuery<QiangYu> query = new BmobQuery<QiangYu>();
        query.addWhereRelatedTo("favorite", new BmobPointer(user));
        query.order("-createdAt");
        query.setLimit(Constant.NUMBERS_PER_PAGE);
        BmobDate date = new BmobDate(new Date(System.currentTimeMillis()));
        query.addWhereLessThan("createdAt", date);
        query.setSkip(Constant.NUMBERS_PER_PAGE*(pageNum++));
        query.include("author");
        query.findObjects(HotActivity.this, new FindListener<QiangYu>() {

            @Override
            public void onSuccess(List<QiangYu> list) {
                // TODO Auto-generated method stub
                LogUtils.i(TAG,"find success."+list.size());
                if(list.size()!=0&&list.get(list.size()-1)!=null){
                    if(mRefreshType==RefreshType.REFRESH){
                        mListItems.clear();
                    }
                    if(list.size()< Constant.NUMBERS_PER_PAGE){
                        ActivityUtil.show(HotActivity.this, "已加载完所有数据~");
                    }
                    mListItems.addAll(list);
                    mAdapter.notifyDataSetChanged();

                    LogUtils.i(TAG,"DD"+(mListItems.get(mListItems.size()-1)==null));
                    setState(LOADING_COMPLETED);
                    mPullRefreshListView.onRefreshComplete();
                }else{
                    ActivityUtil.show(HotActivity.this, "暂无更多数据~");
                    if(list.size()==0&&mListItems.size()==0){

                        networkTips.setText("暂无收藏。快去首页收藏几个把~");
                        setState(LOADING_FAILED);
                        pageNum--;
                        mPullRefreshListView.onRefreshComplete();

                        LogUtils.i(TAG,"SIZE:"+list.size()+"ssssize"+mListItems.size());
                        return;
                    }
                    pageNum--;
                    setState(LOADING_COMPLETED);
                    mPullRefreshListView.onRefreshComplete();
                }
            }

            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub
                LogUtils.i(TAG, "find failed." + arg1);
                pageNum--;
                setState(LOADING_FAILED);
                mPullRefreshListView.onRefreshComplete();
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hot, menu);
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
}
