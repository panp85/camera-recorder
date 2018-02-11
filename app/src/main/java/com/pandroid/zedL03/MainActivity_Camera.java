package com.pandroid.zedL03;

import com.android.zedL03.ZedTask;

import android.Manifest;
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
import android.view.MenuItem;

import android.os.Message;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.pm.PackageManager;

import com.dinuscxj.progressbar.CircleProgressBar;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import com.pandroid.R;

import com.pandroid.main.PermissionsActivity;

//#define  FILE_SIZE (60*5)


public class MainActivity_Camera extends AppCompatActivity implements View.OnClickListener
        , SurfaceHolder.Callback, MediaRecorder.OnErrorListener{


    //存放照片的文件夹
    public final static String  BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/";
	public final static String TAG = "ZED_APP";
	private int FILE_SIZE = (10);

    private SurfaceView mSurfaceView;
    private ImageView startBtn;
    private ImageView lightBtn;
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
    private OnRecordFinishListener mOnRecordFinishListener;// 录制完成回调接口
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

	private ZedTask zt;
    private Thread mPoolThread;
	private Context mAppContext;
	private SharedPreferences mSharedPreferences;


	private boolean mHasCriticalPermissions;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkPermissions() || !mHasCriticalPermissions) {
            Log.v(TAG, "onCreate: Missing critical permissions.");
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        
		
        setContentView(R.layout.activity_main_camera);
        //getSupportActionBar().hide();
        initView();

		mAppContext = getApplicationContext();
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
		

        mPoolThread = new Thread()  
        {  
            @Override  
            public void run()  
            {
                Looper.prepare();
                zt = new ZedTask(Looper.myLooper());
				zt.start();
                Looper.loop();  
            }  
        };  
        mPoolThread.start();  
		
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
        mProgressBar = (CircleProgressBar)findViewById(R.id.progress);

        lightBtn = (ImageView) findViewById(R.id.lightBtn);
        tag_start = (ImageView) findViewById(R.id.tag_start);
		imageView = (ImageView) findViewById(R.id.mytest);
        anim = (AnimationDrawable)tag_start.getDrawable();
        anim.setOneShot(false); // 设置是否重复播放
        lay_tool = (LinearLayout) findViewById(R.id.lay_tool);
        lightBtn.setOnClickListener(this);
        findViewById(R.id.exitBtn).setOnClickListener(this);
        findViewById(R.id.switchCamera).setOnClickListener(this);

        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();// 取得holder
        mSurfaceHolder.addCallback(this); // holder加入回调接口
        mSurfaceHolder.setKeepScreenOn(true);

        startBtn = (ImageView) findViewById(R.id.startBtn);
        startBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN :
                        if(isStarting){
 //                           stopRecord();
                        }else {
 //                           startRecord(recordFinishListener);
                        }
//                        Log.i("ACTION", "DOWN");
                        break;
                    case MotionEvent.ACTION_UP:
						if(isStarting)
						{
						     stopRecord();
							 if (mOnRecordFinishListener != null){
                                mOnRecordFinishListener.onRecordFinish();
                             }
						}
						else
						{
						     startRecord(recordFinishListener);
						}
						break;
 /*                   
                        if(mTimeCount < 30){
//                            Utils.toast("不能少于3秒！");
                            Toast.makeText(MainActivity.this, "不能少于3秒！", Toast.LENGTH_SHORT).show();
                            stopRecord();
                        } else {
                            stopRecord();
                            if (mOnRecordFinishListener != null){
                                mOnRecordFinishListener.onRecordFinish();
                            }
                        }
//                        Log.i("ACTION", "UP");
                        break;
*/                        
                }
                return true;
            }
        });
    }

    /**
     * 释放摄像头资源
     */
    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 录制前，初始化
     */
    private void initRecord() {
        try {

            if(mMediaRecorder == null){
                mMediaRecorder = new MediaRecorder();

            }
            if(mCamera != null){
                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);
            }

            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT );
			//mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC );
			
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 视频源

            // Use the same size for recording profile.
            CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
			//CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
			
            mProfile.videoFrameWidth = optimalSize.width;
            mProfile.videoFrameHeight = optimalSize.height;

			mProfile.videoFrameWidth = 1280;
			mProfile.videoFrameHeight = 720;
			

            //mMediaRecorder.setProfile(mProfile);


			
			//mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			

			mMediaRecorder.setOutputFormat(mProfile.fileFormat);
	       // mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
	       // mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
	       // mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
	        //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
	        //mMediaRecorder.setVideoEncoder(mProfile.videoCodec);
	        mMediaRecorder.setVideoEncoder(5);
		   
            //该设置是为了抽取视频的某些帧，真正录视频的时候，不要设置该参数
//            mMediaRecorder.setCaptureRate(mFpsRange.get(0)[0]);//获取最小的每一秒录制的帧数
			mMediaRecorder.setCaptureRate(20);//获取最小的每一秒录制的帧数
			mMediaRecorder.setVideoFrameRate(20);

			//String key = mAppContext.getString(R.string.resolution_option_preference);
			String value = mSharedPreferences.getString("selected_resolution_option", "3");
			int r = Integer.valueOf(value).intValue();
			if(r == 0)
			{
			    Log.i(TAG, "ppt, set resolution to 480x320,200k/s");
				mMediaRecorder.setVideoSize(480, 320);
				mMediaRecorder.setVideoEncodingBitRate(200*1024);
			}
			else if(r == 1)
			{
			    Log.i(TAG, "ppt, set resolution to 640x480,512k/s");
				mMediaRecorder.setVideoSize(640, 480);
				mMediaRecorder.setVideoEncodingBitRate(512*1024);
			}
			else if(r == 2)
			{
			    Log.i(TAG, "ppt, set resolution to 1280x720,1M/s");
				//mMediaRecorder.setVideoSize(640, 480);
				mMediaRecorder.setVideoSize(1280, 720);
				mMediaRecorder.setVideoEncodingBitRate(1024*1024);
			}
				


            mMediaRecorder.setAudioEncodingBitRate(50*1024);
            mMediaRecorder.setAudioChannels(1);
            mMediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);
            mMediaRecorder.setAudioEncoder(mProfile.audioCodec);
			

			//mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());

            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            releaseRecord();
        }
    }

    private void switchCamera(){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for(int i = 0; i < cameraCount; i++ ) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if(cameraPosition == 1) {
                //现在是后置，变更为前置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        mCamera.setDisplayOrientation(90);
                        mCamera.setPreviewDisplay(mSurfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mCamera.setParameters(parameters);// 设置相机参数
                    mCamera.startPreview();//开始预览
                    cameraPosition = 0;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        mCamera.setDisplayOrientation(90);
                        mCamera.setPreviewDisplay(mSurfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mCamera.setParameters(parameters);// 设置相机参数
                    mCamera.startPreview();//开始预览
                    cameraPosition = 1;
                    break;
                }
            }

        }
    }

    /**
     * 开始录制视频
     */
    public void startRecord(final OnRecordFinishListener onRecordFinishListener) {
        this.mOnRecordFinishListener = onRecordFinishListener;
        isStarting = true;
        lay_tool.setVisibility(View.INVISIBLE);
        tag_start.setVisibility(View.VISIBLE);
        anim.start();
        createRecordDir();
        try {
            initRecord();
            mTimeCount = 0;// 时间计数器重新赋值
            mTimer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    mTimeCount++;
                    //mProgressBar.setProgress(mTimeCount);
                    if (mTimeCount == mRecordMaxTime) {// 达到指定时间，停止拍摄
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
		                        if (mOnRecordFinishListener != null){
		                            mOnRecordFinishListener.onRecordFinish();
		                        }
                                //stop();
								stopRecord();
								startRecord(recordFinishListener);
                            }
                        });
						//Looper.prepare();

                        
					    //Looper.loop();
					    

                    }
                }
            };
            mTimer.schedule(timerTask, 0, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    /**
     * 停止拍摄
     */
    public void stop() {
        stopRecord();
        releaseRecord();
        freeCameraResource();

    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        mProgressBar.setProgress(0);
        isStarting = false;
        tag_start.setVisibility(View.GONE);
        anim.stop();
        lay_tool.setVisibility(View.VISIBLE);
        if(timerTask != null)
            timerTask.cancel();
        if (mTimer != null)
            mTimer.cancel();
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放资源
     */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setPreviewDisplay(null);
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
    }

    /**
     * 创建目录与文件
     */
    private void createRecordDir() {
       // dirname = String.valueOf(System.currentTimeMillis()) +  String.valueOf( new Random().nextInt(1000));
        //File FileDir = new File(BASE_PATH + dirname);
        File FileDir = new File(BASE_PATH);
        if (!FileDir.exists()) {
            FileDir.mkdirs();
        }
        // 创建文件
        try {
            mVecordFile = new File(FileDir.getAbsolutePath() + "/" + Utils.getDateNumber() +".mp4");
            Log.d("Path:", mVecordFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    OnRecordFinishListener recordFinishListener = new OnRecordFinishListener() {
        @Override
        public void onRecordFinish() {
            Toast.makeText(MainActivity_Camera.this, "拍摄完毕", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onBackPressed() {
        stop();
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.exitBtn:
                stop();
                finish();
                break;
            case R.id.lightBtn:
                flashLightToggle();
                break;
            case R.id.switchCamera:
                switchCamera();
                break;
        }
    }


    private void flashLightToggle(){
        try {
            if(isFlashLightOn){
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                isFlashLightOn = false;
            }else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                isFlashLightOn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void permissions_request()
    {
        String[] permissionsToRequest = new String[3];
		permissionsToRequest[0] = Manifest.permission.CAMERA;

		permissionsToRequest[1] = Manifest.permission.RECORD_AUDIO;
		permissionsToRequest[2] = Manifest.permission.READ_EXTERNAL_STORAGE;
		requestPermissions(permissionsToRequest, 1);
    }
	
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
	    

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            freeCameraResource();
        }
		
		int CAMERA_HAL_API_VERSION_1_0 = 0x100;

        try {
     //       mCamera = Camera.open();
			try {
                Method openMethod = Class.forName("android.hardware.Camera").getMethod(
                        "openLegacy", int.class, int.class);
                mCamera = (android.hardware.Camera) openMethod.invoke(
                        null, 0, CAMERA_HAL_API_VERSION_1_0);
            } catch (Exception e) {
                mCamera = android.hardware.Camera.open(0);
            }
            if (mCamera == null)
                return;
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            parameters = mCamera.getParameters();// 获得相机参数

            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
            optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                    mSupportedPreviewSizes, height, width);

            //parameters.setPreviewSize(optimalSize.width, optimalSize.height); // 设置预览图像大小
            parameters.setPreviewSize(1280, 720); // 设置预览图像大小

            parameters.set("orientation", "portrait");
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            mFpsRange =  parameters.getSupportedPreviewFpsRange();

			
			Log.d("camera", "range:" + mFpsRange.size());
	        for(int j=0; j<mFpsRange.size(); j++) {  
	            int[] r = mFpsRange.get(j);  
	            for(int k = 0; k < r.length; k++) {  
	                Log.e("camera", "panpan test, camera supported fps: " + r[k]);  
	            }  
	        }
			//parameters.setPreviewFormat (ImageFormat.RGB_565);
			parameters.setPreviewFrameRate(20);
			parameters.setPreviewFpsRange(20000, 20000);

            mCamera.setParameters(parameters);// 设置相机参数
            mCamera.startPreview();// 开始预览


        }catch (Exception io){
            io.printStackTrace();
        }
	    mCamera.setPreviewCallback(new Camera.PreviewCallback(){
				public void onPreviewFrame(byte[] data, Camera camera) {
					 Size size = camera.getParameters().getPreviewSize();		   
					 try{
						 Log.e("camera", "panpan test, in onPreviewFrame, size: " + size.width + ", " + size.height);
						 YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);	
						 if(image!=null){  
								ByteArrayOutputStream stream = new ByteArrayOutputStream();  
								image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream); 
	                            BitmapFactory.Options newOpts = new BitmapFactory.Options(); 
								newOpts.inSampleSize = 1;
							   final Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size(), newOpts);
								//**********************
								//因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
								Runnable runnable1 = new Runnable() {
					                public void run() {
					                    rotateMyBitmap(bmp);
					                }
					            };
								  
								//**********************************
								Runnable runnable2 = new Runnable() {
					                public void run() {
										if((i++)%20 == 0)
					                        saveBmp(bmp);
										
					                }
					            };
									
								runnable1.run();
								new Thread(runnable2).start();
	                             
							 stream.close();  
						}  
					 }catch(Exception ex){	
					  Log.e("Sys","Error:"+ex.getMessage());  
				  }  
				}	
			});

    }
	private void saveBmp(Bitmap bitmap) {  
        if (bitmap == null)  
            return;  
        // 位图大小  
        int nBmpWidth = bitmap.getWidth();  
        int nBmpHeight = bitmap.getHeight();  

        // 图像数据大小  
        int bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4);  
        try {  
            // 存储文件名  
            String filename = "/sdcard/test" + i + ".bmp";  
            File file = new File(filename);  
            if (!file.exists()) {  
                file.createNewFile();  
            }  
            FileOutputStream fileos = new FileOutputStream(filename);  
            // bmp文件头  
            int bfType = 0x4d42;  
            long bfSize = 14 + 40 + bufferSize;  
            int bfReserved1 = 0;  
            int bfReserved2 = 0;  
            long bfOffBits = 14 + 40;  
            // 保存bmp文件头  
            writeWord(fileos, bfType);  
            writeDword(fileos, bfSize);  
            writeWord(fileos, bfReserved1);  
            writeWord(fileos, bfReserved2);  
            writeDword(fileos, bfOffBits);  
            // bmp信息头  
            long biSize = 40L;  
            long biWidth = nBmpWidth;  
            long biHeight = nBmpHeight;  
            int biPlanes = 1;  
            int biBitCount = 24;  
            long biCompression = 0L;  
            long biSizeImage = 0L;  
            long biXpelsPerMeter = 0L;  
            long biYPelsPerMeter = 0L;  
            long biClrUsed = 0L;  
            long biClrImportant = 0L;  
            // 保存bmp信息头  
            writeDword(fileos, biSize);  
            writeLong(fileos, biWidth);  
            writeLong(fileos, biHeight);  
            writeWord(fileos, biPlanes);  
            writeWord(fileos, biBitCount);  
            writeDword(fileos, biCompression);  
            writeDword(fileos, biSizeImage);  
            writeLong(fileos, biXpelsPerMeter);  
            writeLong(fileos, biYPelsPerMeter);  
            writeDword(fileos, biClrUsed);  
            writeDword(fileos, biClrImportant);  
            // 像素扫描  
            byte bmpData[] = new byte[bufferSize];  
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);  
            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)  
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += 3) {  
                    int clr = bitmap.getPixel(wRow, nCol);  
                    bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);  
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);  
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);  
                }  
  
            fileos.write(bmpData);  
            fileos.flush();  
            fileos.close();  
  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    protected void writeWord(FileOutputStream stream, int value) throws IOException {  
        byte[] b = new byte[2];  
        b[0] = (byte) (value & 0xff);  
        b[1] = (byte) (value >> 8 & 0xff);  
        stream.write(b);  
    }  
  
    protected void writeDword(FileOutputStream stream, long value) throws IOException {  
        byte[] b = new byte[4];  
        b[0] = (byte) (value & 0xff);  
        b[1] = (byte) (value >> 8 & 0xff);  
        b[2] = (byte) (value >> 16 & 0xff);  
        b[3] = (byte) (value >> 24 & 0xff);  
        stream.write(b);  
    }  
  
    protected void writeLong(FileOutputStream stream, long value) throws IOException {  
        byte[] b = new byte[4];  
        b[0] = (byte) (value & 0xff);  
        b[1] = (byte) (value >> 8 & 0xff);  
        b[2] = (byte) (value >> 16 & 0xff);  
        b[3] = (byte) (value >> 24 & 0xff);  
        stream.write(b);  
    }  
	
	public void rotateMyBitmap(Bitmap bmp){
	  //*****旋转一下
		Matrix matrix = new Matrix();
		 matrix.postRotate(270);

	//	 Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);

		Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0,0, bmp.getWidth()*3/5,  bmp.getHeight(), matrix, true);

		//*******显示一下
		imageView.setImageBitmap(nbmp2);

	};

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        freeCameraResource();
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录制完成回调接口
     */
    public interface OnRecordFinishListener {
        void onRecordFinish();
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

}

