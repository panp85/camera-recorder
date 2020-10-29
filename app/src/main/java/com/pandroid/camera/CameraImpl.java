package com.pandroid.camera;


import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.StatFs;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import com.pandroid.R;
import android.app.Activity;

import android.hardware.Camera.FaceDetectionListener;
import android.os.HandlerThread;

import com.pandroid.main.PermissionsActivity;




//#define  FILE_SIZE (60*5)

public class  CameraImpl implements MediaRecorder.OnErrorListener, SurfaceHolder.Callback 
		, FaceDetectionListener {

    //存放照片的文件夹
	public final static String TAG = "CameraImpl";
	private int FILE_SIZE = (20*3*1);
	public final static String	BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/";
    List<File> list = null;

	Activity mActivity;

    private SurfaceView mSurfaceView;
    private MediaRecorder mMediaRecorder;// 录制视频的类
    private SurfaceHolder mSurfaceHolder;
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

    private Thread mPoolThread;
	private Context mAppContext;
	private SharedPreferences mSharedPreferences;

	private boolean mHasCriticalPermissions;

	private static CameraImpl mCameraImpl;

	private CameraHandler mCameraHandler;
	private CameraFaceDetectionCallback mCallback;
	private Handler mMainHandler_ui;

	public static synchronized CameraImpl instance(Context context) {
        if (mCameraImpl == null) {
            mCameraImpl = new CameraImpl();
			mCameraImpl.mAppContext = context;
			mCameraImpl.init();


		    //mCameraImpl.initView();
        }
        
        return mCameraImpl;
    }
    protected void init() {
        HandlerThread ht = new HandlerThread("Camera Handler Thread");
        ht.start();
        mCameraHandler = new CameraHandler(ht.getLooper());
		this.mOnRecordFinishListener = recordFinishListener;
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext); 
    }

    private void initView() {
        mSurfaceHolder.addCallback(this); // holder加入回调接口
        mSurfaceHolder.setKeepScreenOn(true);
    }
	
	private boolean mIsLayoutInitializedAlready = false;


	public void setSurfaceView(SurfaceView sv, Activity a)
    {
        mActivity = a;
        mSurfaceView = sv;
        mSurfaceHolder = mSurfaceView.getHolder();// 取得holder
        initView();

/*
		mSurfaceView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                    int bottom, int oldLeft, int oldTop, int oldRight,
                    int oldBottom) {
                int width = right - left;
                int height = bottom - top;

                if (!mIsLayoutInitializedAlready) {
                    layoutPreview(1);
                }
            }
        });
        */
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
    public void initRecord() {
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
    public void startRecord() {
        
        isStarting = true;

        createRecordDir();
        try {
            initRecord();
            mTimeCount = 0;// 时间计数器重新赋值
            if(mTimer == null||timerTask == null)
            {
	            mTimer = new Timer();
	            timerTask = new TimerTask() {
	                @Override
	                public void run() {
	                    Log.i(TAG, "record ppt, in TimerTask, mTimeCount = " + mTimeCount);
	                    mTimeCount++;
	                    //mProgressBar.setProgress(mTimeCount);
	                    if (mTimeCount == mRecordMaxTime) {// 达到指定时间，停止拍摄
	                        mActivity.runOnUiThread(new Runnable() {
	                            @Override
	                            public void run() {
			                        if (mOnRecordFinishListener != null){
			                            mOnRecordFinishListener.onRecordFinish();
			                        }
	                                //stop();

	                            }
	                        });
                            resetRecord();
                            startRecord();
							/*
							Looper.prepare();
							new Thread(runnable).start();
						    Looper.loop();						    
						    */
						    mTimeCount = 0;
	                    }
	                }
	            };
            }
			
            mTimer.schedule(timerTask, 0, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止拍摄
     */
    public void stop() {
        stopRecord();
        releaseRecord();
        freeCameraResource();

    }

    private void resetRecord()
    {
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
     * 停止录制
     */
    public void stopRecord() {
        isStarting = false;
        if(timerTask != null)
            timerTask.cancel();
        if (mTimer != null)
            mTimer.cancel();
		
		resetRecord();
        
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
     * 获取目录下所有文件(按时间排序)
     *
     * @param path
     * @return
     */
    public static List<File> getFileSort(String path) {

        List<File> list = getFiles(path, new ArrayList<File>());

        if (list != null && list.size() > 0) {

            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    if (file.lastModified() < newFile.lastModified()) {
                        return -1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return 1;
                    }

                }
            });

        }

        return list;
    }
    /**
     *
     * 获取目录下所有文件
     *
     * @param realpath
     * @param files
     * @return
     */
    public static List<File> getFiles(String realpath, List<File> files) {
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles();
            for (File file : subfiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }
    private boolean loadAllFile_flag = false;
    private void loadAllFile(){
        if (!loadAllFile_flag) {
            list = getFileSort(BASE_PATH);
            loadAllFile_flag = true;
        }
    }

    private void file_delete_oldest(){
        File sdcardDir = Environment.getExternalStorageDirectory();
        StatFs sf = new  StatFs(sdcardDir.getPath());
        long  blockSize = sf.getBlockSize();
        long  blockCount = sf.getBlockCount();
        long  availCount = sf.getAvailableBlocks();
        loadAllFile();

        //if(false)
        if(availCount*blockCount/1024/1024 >= 600)
        {
            return;
        }
        Log.i(TAG, "camera ppt, in file_delete_oldest, file:");
        try {
/*            for (int i = 0; i < list.size() - 1; i++) {
                if(i >= list.size() - 3 || i < 3) {
                    Log.i(TAG, "camera ppt, in file_delete_oldest, file: " + list.get(i).getName());
                }
                //
            }
            */
            for (int i = 0; i < 2 && i < list.size() - 1; i++) {
                Log.i(TAG, "camera ppt, in file_delete_oldest, file: " + list.get(i).getName());
                list.get(i).delete();
                list.remove(i);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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
        file_delete_oldest();
        // 创建文件
        try {
            mVecordFile = new File(FileDir.getAbsolutePath() + "/" + Utils.getDateNumber() +".mp4");
            Log.d("Path:", mVecordFile.getAbsolutePath());
            list.add(mVecordFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    OnRecordFinishListener recordFinishListener = new OnRecordFinishListener() {
        @Override
        public void onRecordFinish() {
            Toast.makeText(mActivity, "录制完毕", Toast.LENGTH_SHORT).show();
        }
    };

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
    private int mCameraId = 0;
    public android.hardware.Camera openCamera(Handler uiMainH)
    {
        mMainHandler_ui = uiMainH;
        int CAMERA_HAL_API_VERSION_1_0 = 0x100;
        Log.i(TAG, "cameraImpl ppt, in openCamera, go in.\n");
        if (mCamera != null) {
            freeCameraResource();
        }
		try {
			try {
                Method openMethod = Class.forName("android.hardware.Camera").getMethod(
                        "openLegacy", int.class, int.class);
                mCamera = (android.hardware.Camera) openMethod.invoke(
                        null, mCameraId, CAMERA_HAL_API_VERSION_1_0);
            } catch (Exception e) {
                mCamera = android.hardware.Camera.open(mCameraId);
            }
            if (mCamera == null)
                return null;
            mCamera.setDisplayOrientation(270);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            parameters = mCamera.getParameters();// 获得相机参数

            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
            optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                    mSupportedPreviewSizes, 640, 480);

            parameters.setPreviewSize(optimalSize.width, optimalSize.height); // 设置预览图像大小
            //parameters.setPreviewSize(1280, 720); // 设置预览图像大小

            parameters.set("orientation", "portrait");
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) 
			{
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

            parameters.set("face-detection", "on");

            mCamera.setParameters(parameters);// 设置相机参数
            mCamera.startPreview();// 开始预览
            
		    Camera.CameraInfo info = new Camera.CameraInfo();	
			Camera.getCameraInfo(mCameraId, info);
            Log.e("camera", "camera ppt, in openCamera, camera info: " + info.facing + ", " + info.orientation);  

            mCamera.setPreviewCallback(new Camera.PreviewCallback(){
				public void onPreviewFrame(byte[] data, Camera camera) {
					if(mCallback != null)
					{
					    mCallback.processFace(data, camera);
					}
				}  
			});
        }catch (Exception io){
            io.printStackTrace();
        }

        return mCamera;
    }


	/*
	public void layoutPreview(float ratio) {

		FrameLayout.LayoutParams lp;
		float scaledTextureWidth, scaledTextureHeight;
		int rotation = CameraUtil.getDisplayRotation(mActivity);
		mScreenRatio = CameraUtil.determineRatio(ratio);
		Log.i(TAG, "photoui ppt camera, in layoutPreview, mScreenRatio = " + mScreenRatio + "; ratio = " + ratio + ", " + 
			CameraUtil.determinCloseRatio(ratio));
		if (mScreenRatio == CameraUtil.RATIO_16_9
				&& CameraUtil.determinCloseRatio(ratio) == CameraUtil.RATIO_4_3) {
			int l = (mTopMargin + mBottomMargin) * 4;
			int s = l * 9 / 16;
			switch (rotation) {
				case 90:
					lp = new FrameLayout.LayoutParams(l * 3 / 4, s);
					lp.setMargins(mTopMargin, 0, mBottomMargin, 0);
					scaledTextureWidth = l * 3 / 4;
					scaledTextureHeight = s;
					break;
				case 180:
					lp = new FrameLayout.LayoutParams(s, l * 3 / 4);
					lp.setMargins(0, mBottomMargin, 0, mTopMargin);
					scaledTextureWidth = s;
					scaledTextureHeight = l * 3 / 4;
					break;
				case 270:
					lp = new FrameLayout.LayoutParams(l * 3 / 4, s);
					lp.setMargins(mBottomMargin, 0, mTopMargin, 0);
					scaledTextureWidth = l * 3 / 4;
					scaledTextureHeight = s;
					break;
				default:
					lp = new FrameLayout.LayoutParams(s, l * 3 / 4);
					lp.setMargins(0, mTopMargin, 0, mBottomMargin);
					scaledTextureWidth = s;
					scaledTextureHeight = l * 3 / 4;
					break;
			}
		} else {
			float width = mMaxPreviewWidth, height = mMaxPreviewHeight;
			if (width == 0 || height == 0) return;
			if(mScreenRatio == CameraUtil.RATIO_4_3)
				height -=  (mTopMargin + mBottomMargin);
			if (mOrientationResize) {
				scaledTextureWidth = height * mAspectRatio;
				if (scaledTextureWidth > width) {
					scaledTextureWidth = width;
					scaledTextureHeight = scaledTextureWidth / mAspectRatio;
				} else {
					scaledTextureHeight = height;
				}
			} else {
				if (width > height) {
					if(Math.max(width, height * mAspectRatio) > width) {
						scaledTextureWidth = width;
						scaledTextureHeight = width / mAspectRatio;
					} else {
						scaledTextureWidth = height * mAspectRatio;
						scaledTextureHeight = height;
					}
				} else {
					if(Math.max(height, width * mAspectRatio) > height) {
						scaledTextureWidth = height / mAspectRatio;
						scaledTextureHeight = height;
					} else {
						scaledTextureWidth = width;
						scaledTextureHeight = width * mAspectRatio;
					}
				}
			}

			Log.v(TAG, "setTransformMatrix: scaledTextureWidth = " + scaledTextureWidth
					+ ", scaledTextureHeight = " + scaledTextureHeight);
			if (((rotation == 0 || rotation == 180) && scaledTextureWidth > scaledTextureHeight)
					|| ((rotation == 90 || rotation == 270)
						&& scaledTextureWidth < scaledTextureHeight)) {
				lp = new FrameLayout.LayoutParams((int) scaledTextureHeight,
						(int) scaledTextureWidth, Gravity.CENTER);
			} else {
				lp = new FrameLayout.LayoutParams((int) scaledTextureWidth,
						(int) scaledTextureHeight, Gravity.CENTER);
			}
			if(mScreenRatio == CameraUtil.RATIO_4_3) {
				lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
				lp.setMargins(0, mTopMargin, 0, mBottomMargin);
			}
		}

		if (mSurfaceTextureUncroppedWidth != scaledTextureWidth ||
				mSurfaceTextureUncroppedHeight != scaledTextureHeight) {
			mSurfaceTextureUncroppedWidth = scaledTextureWidth;
			mSurfaceTextureUncroppedHeight = scaledTextureHeight;
			if (mSurfaceTextureSizeListener != null) {
				mSurfaceTextureSizeListener.onSurfaceTextureSizeChanged(
						(int) mSurfaceTextureUncroppedWidth,
						(int) mSurfaceTextureUncroppedHeight);
				Log.i(TAG, "photoui ppt camera, mSurfaceTextureUncroppedWidth=" + mSurfaceTextureUncroppedWidth
						+ "mSurfaceTextureUncroppedHeight=" + mSurfaceTextureUncroppedHeight);
			}
		}

		mSurfaceView.setLayoutParams(lp);
		mRootView.requestLayout();
		if (mFaceView != null) {
			mFaceView.setLayoutParams(lp);
		}
		mIsLayoutInitializedAlready = true;
	}
*/
	
	@Override
	  public void surfaceCreated(SurfaceHolder holder) {

	  }
	  
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mCamera != null)
        {
	        
        }
    } 


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        freeCameraResource();
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
				Log.i(TAG, "MediaRecorder ppt, in onError, go to mr.reset.");
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


    public void openFace(CameraFaceDetectionCallback c)
    {
        mCallback =  c;
        setFaceDetectionListener(this);
    }
	
    private void setFaceDetectionListener(FaceDetectionListener listener) {
            mCamera.setFaceDetectionListener(listener);
			mCamera.startFaceDetection();
    }


	@Override
    public void onFaceDetection(
            final Camera.Face[] faces, Camera camera) {
        final android.hardware.Camera currentCamera = mCamera;
        Log.i(TAG, "cameraimpl ppt, in onFaceDetection, yes");
        mMainHandler_ui.post(new Runnable() {
            @Override
            public void run() {
                if ((currentCamera != null) && currentCamera.equals(mCamera)) {
                    mCallback.onFaceDetection(faces);
                }
            }
        });
    }

	private class CameraHandler extends Handler {
	    CameraHandler(Looper looper) {
            super(looper);
        }

		private void startFaceDetection() {
            mCamera.startFaceDetection();
        }

        private void stopFaceDetection() {
            mCamera.stopFaceDetection();
        }

		public void handleMessage(final Message msg) {
		    try {
                switch (msg.what) {
					case 1:
						
						break;
					default:
						Log.i(TAG, "cameraimpl ppt, in handleMessage, error message.");
                }
		    }catch (RuntimeException e) {
		    }
		}
	}

    /**
     * An interface which wraps
     * {@link android.hardware.Camera.FaceDetectionListener}.
     */
    public interface CameraFaceDetectionCallback {
        /**
         * Callback for face detection.
         *
         * @param faces   Recognized face in the preview.
         * @param camera  The camera which the preview image comes from.
         */
        public void onFaceDetection(Camera.Face[] faces);
		public void processFace(byte[] data, Camera camera);
    }
}

