package me.edagarli.invisiblelovers.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;
import me.edagarli.invisiblelovers.BaseActivity;
import me.edagarli.invisiblelovers.R;
import me.edagarli.invisiblelovers.bean.QiangYu;
import me.edagarli.invisiblelovers.bean.User;
import me.edagarli.invisiblelovers.utils.ActivityUtil;
import me.edagarli.invisiblelovers.utils.CacheUtils;
import me.edagarli.invisiblelovers.utils.LogUtils;
import me.edagarli.invisiblelovers.view.ActionBar;

public class ApplyActivity extends BaseActivity implements View.OnClickListener {

    @InjectView(R.id.edit_content)
    EditText contentEt;

    @InjectView(R.id.actionbar_edit)
    ActionBar actionbar;

    String dateTime;

    @InjectView(R.id.open_pic)
    ImageView albumPic;

    @InjectView(R.id.take_pic)
    ImageView takePic;

    @InjectView(R.id.open_layout)
    LinearLayout openLayout;

    @InjectView(R.id.take_layout)
    LinearLayout takeLayout;

    private static final int REQUEST_CODE_ALBUM = 1;
    private static final int REQUEST_CODE_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_apply);
        ButterKnife.inject(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        openLayout.setOnClickListener(this);
        takeLayout.setOnClickListener(this);

        actionbar.setTitle("申请成为恋人");
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAction(new ActionBar.Action() {

            @Override
            public void performAction(View view) {
                // TODO Auto-generated method stub
                finish();
            }

            @Override
            public int getDrawable() {
                // TODO Auto-generated method stub
                return R.drawable.ic_launcher;
            }
        });

        actionbar.addAction(new ActionBar.Action() {

            @Override
            public void performAction(View view) {
                // TODO Auto-generated method stub
                String commitContent = contentEt.getText().toString().trim();
                if (TextUtils.isEmpty(commitContent)) {
                    ActivityUtil.show(ApplyActivity.this, "内容不能为空");
                    return;
                }
                if (targeturl == null) {
                    ActivityUtil.show(ApplyActivity.this, "请上传图片");
                } else {
                    publish(commitContent);
                }
            }

            @Override
            public int getDrawable() {
                // TODO Auto-generated method stub
                return R.drawable.btn_comment_publish;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.apply, menu);
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

    /*
	 * 发表带图片
	 */
    private void publish(final String commitContent){

        final BmobFile figureFile = new BmobFile(new File(targeturl));
        figureFile.upload(getApplicationContext(), new UploadFileListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                LogUtils.i(TAG, "上传文件成功。" + figureFile.getFileUrl());
                publishWithoutFigure(commitContent, figureFile);

            }

            @Override
            public void onProgress(Integer arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailure(int arg0, String arg1) {
                // TODO Auto-generated method stub
                LogUtils.i(TAG, "上传文件失败。"+arg1);
            }
        });

    }

    private void publishWithoutFigure(final String commitContent,
                                      final BmobFile figureFile) {
        User user = BmobUser.getCurrentUser(getApplicationContext(), User.class);

        final QiangYu qiangYu = new QiangYu();
        qiangYu.setAuthor(user);
        qiangYu.setContent(commitContent);
        if(figureFile!=null){
            qiangYu.setContentfigureurl(figureFile);
        }
        qiangYu.setLove(0);
        qiangYu.setHate(0);
        qiangYu.setShare(0);
        qiangYu.setComment(0);
        qiangYu.setPass(true);
        qiangYu.save(getApplicationContext(), new SaveListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                ActivityUtil.show(ApplyActivity.this, "发表成功！");
                LogUtils.i(TAG,"创建成功。");
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(int arg0, String arg1) {
                // TODO Auto-generated method stub
                ActivityUtil.show(ApplyActivity.this, "发表失败！yg"+arg1);
                LogUtils.i(TAG,"创建失败。"+arg1);
            }
        });
    }

    String targeturl = null;
    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.i(TAG,"get album:");
        if(resultCode == RESULT_OK){
            switch (requestCode) {
                case REQUEST_CODE_ALBUM:
                    String fileName = null;
                    if(data!=null){
                        Uri originalUri = data.getData();
                        ContentResolver cr = getContentResolver();
                        Cursor cursor = cr.query(originalUri, null, null, null, null);
                        if(cursor.moveToFirst()){
                            do{
                                fileName= cursor.getString(cursor.getColumnIndex("_data"));
                                LogUtils.i(TAG,"get album:"+fileName);
                            }while (cursor.moveToNext());
                        }
                        Bitmap bitmap = compressImageFromFile(fileName);
                        targeturl = saveToSdCard(bitmap);
                        albumPic.setBackgroundDrawable(new BitmapDrawable(bitmap));
                        takeLayout.setVisibility(View.GONE);
                    }
                    break;
                case REQUEST_CODE_CAMERA:
                    String files =CacheUtils.getCacheDirectory(getApplicationContext(), true, "pic") + dateTime;
                    File file = new File(files);
                    if(file.exists()){
                        Bitmap bitmap = compressImageFromFile(files);
                        targeturl = saveToSdCard(bitmap);
                        takePic.setBackgroundDrawable(new BitmapDrawable(bitmap));
                        openLayout.setVisibility(View.GONE);
                    }else{

                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
//		case R.id.commit_edit:
//			String commitContent = content.getText().toString().trim();
//			if(TextUtils.isEmpty(commitContent)){
//				ActivityUtil.show(this, "内容不能为空");
//				return;
//			}
//			if(targeturl == null){
//				publishWithoutFigure(commitContent, null);
//			}else{
//				publish(commitContent);
//			}
//			break;
            case R.id.open_layout:
                Date date1 = new Date(System.currentTimeMillis());
                dateTime = date1.getTime() + "";
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_CODE_ALBUM);
                break;
            case R.id.take_layout:
                Date date = new Date(System.currentTimeMillis());
                dateTime = date.getTime() + "";
                File f = new File(CacheUtils.getCacheDirectory(getApplicationContext(), true, "pic") + dateTime);
                if (f.exists()) {
                    f.delete();
                }
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri uri = Uri.fromFile(f);
                Log.e("uri", uri + "");

                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(camera, REQUEST_CODE_CAMERA);
                break;
            default:
                break;
        }
    }

    private Bitmap compressImageFromFile(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 800f;//
        float ww = 480f;//
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置采样率

        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;//该模式是默认的,可不设
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收

        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
//		return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
        //其实是无效的,大家尽管尝试
        return bitmap;
    }

    public String saveToSdCard(Bitmap bitmap){
        String files =CacheUtils.getCacheDirectory(getApplicationContext(), true, "pic") + dateTime+"_11";
        File file=new File(files);
        try {
            FileOutputStream out=new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)){
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LogUtils.i(TAG, file.getAbsolutePath());
        return file.getAbsolutePath();
    }
}
