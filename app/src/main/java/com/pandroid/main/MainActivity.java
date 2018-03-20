package com.pandroid.main;

import com.pandroid.message.MessageTask;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.view.MenuItem;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.hardware.Camera.Size;
import android.graphics.YuvImage;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Color;
import android.os.Looper;
import android.os.Handler;

import android.view.Menu;

import android.os.Message;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.pm.PackageManager;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.dinuscxj.progressbar.CircleProgressBar;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import android.view.LayoutInflater;

import com.pandroid.message.FightListPreferenceActivity;
import com.pandroid.R;
//#define  FILE_SIZE (60*5)


public class MainActivity extends AppCompatActivity 
    implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener{


    //存放照片的文件夹
    public final static String  BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/";
	public final static String TAG = "pandroid_APP";
	private int FILE_SIZE = (10);

    private SurfaceView mSurfaceView;
    private Button cameraBtn;
    private Button appBtn;
    private ToggleButton ftpBtn;
	private ImageView imageView;
    private ImageView tag_start;
    private AnimationDrawable anim;
    private LinearLayout lay_tool;
    private MediaRecorder mMediaRecorder;// 录制视频的类
    private SurfaceHolder mSurfaceHolder;
    private CircleProgressBar mProgressBar;
    private Camera mCamera;
    private Timer mTimer;// 计时器
    TimerTask timerTask;
    private boolean isOpenCamera = true;// 是否一开始就打开摄像头
//    private int mRecordMaxTime = 100;// 一次拍摄最长时间 10秒
    private int mRecordMaxTime = FILE_SIZE;// 一次拍摄最长时间 60秒

    private int mTimeCount;// 时间计数
    private File mVecordFile = null;// 文件

    private boolean isStarting = false;
    List<int[]> mFpsRange;
    private Camera.Size optimalSize;
    private Camera.Parameters parameters;
    private boolean isFlashLightOn = false;
    //摄像头默认是后置， 0：前置， 1：后置
    private int cameraPosition = 1;
    //视频存储的目录
    private String dirname;
    private int i = 0;

	private MessageTask mMessageTask;
    private Thread mPoolThread;
	private Context mAppContext;
	private SharedPreferences mSharedPreferences;

	View mRootView;


	private boolean mHasCriticalPermissions;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        checkPermissions();

        mAppContext = getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);

        setContentView(R.layout.activity_main);
		LayoutInflater inflater = getLayoutInflater();
		mRootView = inflater.inflate(R.layout.activity_main, null, false);
        initView();

		mSurfaceView = (SurfaceView)findViewById(R.id.preview_content);
        com.pandroid.camera.CameraImpl.instance(mAppContext).setSurfaceView(mSurfaceView, this);
		
         
	     mMessageTask = new MessageTask("messageTask");
		 mMessageTask.start();
         mMessageTask.setup_message();

/*
		try {
            Thread.sleep(1000);
        }catch (InterruptedException e) {
            return;
        }
        */
        PreferenceManager.getDefaultSharedPreferences(mAppContext).registerOnSharedPreferenceChangeListener(this);	
		

    }
	 protected void onStart(Bundle savedInstanceState) {
	     
	 }
	 
	 @Override	
	 protected void onResume() {  
	   
	   super.onResume();
	 } 


	 @Override	
	 protected void onPause() {  
	   PreferenceManager.getDefaultSharedPreferences(mAppContext).unregisterOnSharedPreferenceChangeListener(this);
	   super.onPause();  
	 }	

	 private boolean checkPermissions() {
        boolean requestPermission = false;

        if (checkSelfPermission(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
            mHasCriticalPermissions = true;
        } else {
            mHasCriticalPermissions = false;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRequestShown = prefs.getBoolean("request_permission_p", false);
        if(!isRequestShown || !mHasCriticalPermissions) {
            Log.v(TAG, "Request permission");
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("request_permission_p", true);
            editor.apply();
            requestPermission = true;
       }
        return requestPermission;
    }
    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
/*            if (msg.what == HIDE_ACTION_BAR) {
                removeMessages(HIDE_ACTION_BAR);
                CameraActivity.this.setSystemBarsVisibility(false);
            }else if ( msg.what == SWITCH_SAVE_PATH ) {
                mCurrentModule.onSwitchSavePath();
            }
*/
         }
    }
    private void initView() {
        cameraBtn = (Button) findViewById(R.id.button_camera);
        cameraBtn.setOnClickListener(this);

        ftpBtn = (ToggleButton) findViewById(R.id.ftp_server_switch);
        //ftpBtn.setText("ftp server状态:close,    点击打开");
        ftpBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked){
                    ftpBtn.setChecked(true);
                    ftpBtn.setBackgroundColor(getResources().getColor(R.color.green));
                    onClickSelect();
                    //Toast.makeText(MainActivity.this, "ftp server已打开", Toast.LENGTH_SHORT).show();
                }else{
                    ftpBtn.setChecked(false);
                    ftpBtn.setBackgroundColor(getResources().getColor(R.color.red));
                    sendBroadcast(new Intent(com.pandroid.ftp.swiftp.FsService.ACTION_STOP_FTPSERVER));
                    Toast.makeText(MainActivity.this, "ftp server已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

        appBtn = (Button) findViewById(R.id.going);
        appBtn.setOnClickListener(this);
    }

    public void onClickSelect(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("后台打开", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Log.i(TAG, "app ppt, in onClickSimple, back.");
				sendBroadcast(new Intent(com.pandroid.ftp.swiftp.FsService.ACTION_START_FTPSERVER));
                Toast.makeText(MainActivity.this, "ftp server已打开", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("打开activity", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
              //取消
                Log.i(TAG, "app ppt, in onClickSimple, open activity.");
                Intent intent=new Intent();
                if(intent!=null)   {
                    intent.setClass(MainActivity.this, com.pandroid.ftp.swiftp.gui.MainActivity_Ftp.class); //设置跳转的Activity
                    startActivity(intent);
                }
            }
        });
         builder.create().show();
     }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_camera:
                Log.i(TAG, "app ppt, in onClick, button_camera");
               // Intent intent = this.getPackageManager().("com.pandroid.message.MainActivity_Camera");
                Intent intent=new Intent();
                if(intent!=null)   {
                    intent.setClass(this, com.pandroid.message.MainActivity_Camera.class); //设置跳转的Activity
                    startActivity(intent);
                }
                else	{
                    Toast.makeText(this, "该功能未开放，敬请期待", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.going:
                Log.i(TAG, "app ppt, in onClick, going");
				com.pandroid.camera.CameraImpl.instance(mAppContext).openCamera(new Handler(Looper.getMainLooper()));
                com.pandroid.camera.FaceView faceview = (com.pandroid.camera.FaceView)findViewById(R.id.face_paint);
				com.pandroid.camera.CameraImpl.instance(mAppContext).openFace(faceview);
                //com.pandroid.camera.CameraImpl.instance(mAppContext).startRecord();
                appBtn.setText("后台应用正在运行.....");
                appBtn.setBackgroundColor(getResources().getColor(R.color.green));
				mRootView.requestLayout();
                break;

        }
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_setting, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_setting1) {
			Intent i0 = new Intent(getApplicationContext(),FightListPreferenceActivity.class);
			startActivityForResult(i0, 1000);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean show = super.onPrepareOptionsMenu(menu);
		if (!show)
			return show;

		return true;
	}

	public void onSharedPreferenceChanged(SharedPreferences         		sharedPreferences, String key) {
	   Log.i(TAG, "ppt SETTING CHANGED: key = " + key);
	   if ("selected_resolution_option".equals(key) || "selected_wifi_option".equals(key)) {
	       
	   }
	}

    protected void onStop() {
        Log.i(TAG, "pandroid ppt, in OnStop, mMessageTask = " + mMessageTask);
        if (mMessageTask != null) {
            mMessageTask.closeSocket();
        }
        super.onStop();
    }

    protected void onDestroy() {
        Log.i(TAG, "pandroid ppt, in onDestroy, mMessageTask = " + mMessageTask);
        if (mMessageTask != null) {
            mMessageTask.closeSocket();
        }

        super.onDestroy();
    }
}

