/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pandroid.camera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.hardware.Camera.Face;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Path;
import android.view.SurfaceView;
import android.view.Surface;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.graphics.YuvImage;
import android.graphics.ImageFormat;
import android.widget.ImageView;
import android.graphics.Rect;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.graphics.Bitmap.CompressFormat;



import android.app.Activity;

import com.pandroid.R;
import org.codeaurora.camera.ExtendedFace;

public class FaceView extends View
    implements CameraImpl.CameraFaceDetectionCallback
   /* 
    FocusIndicator, Rotatable,
    PhotoUI.SurfaceTextureSizeChangedListener
    */
    {
    protected static final String TAG = "CAM FaceView";
    protected final boolean LOGV = false;
    // The value for android.hardware.Camera.setDisplayOrientation.
    protected int mDisplayOrientation;
    // The orientation compensation for the face indicator to make it look
    // correctly in all device orientations. Ex: if the value is 90, the
    // indicator should be rotated 90 degrees counter-clockwise.
    protected int mOrientation;
    protected boolean mMirror;
    protected boolean mPause;
    protected Matrix mMatrix = new Matrix();
    protected RectF mRect = new RectF();
    // As face detection can be flaky, we add a layer of filtering on top of it
    // to avoid rapid changes in state (eg, flickering between has faces and
    // not having faces)
    private Face[] mFaces;
    private Face[] mPendingFaces;
    protected int mColor;
    protected final int mFocusingColor;
    private final int mFocusedColor;
    private final int mFailColor;
    protected Paint mPaint;
    protected volatile boolean mBlocked;

    protected int mUncroppedWidth = 600;
    protected int mUncroppedHeight = 600;

    private final int smile_threashold_no_smile = 30;
    private final int smile_threashold_small_smile = 60;
    private final int blink_threshold = 60;

    protected static final int MSG_SWITCH_FACES = 1;
    protected static final int SWITCH_DELAY = 70;
    private int mDisplayRotation = 0;
    protected boolean mStateSwitchPending = false;


        private ImageView mImageView;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SWITCH_FACES:
                mStateSwitchPending = false;
                mFaces = mPendingFaces;
                invalidate();
                break;
            }
        }
    };

	

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
		Log.i(TAG, "faceview ppt, set attrs....");
        Resources res = getResources();
       /* mFocusingColor = res.getColor(R.color.face_detect_start);
        mFocusedColor = res.getColor(R.color.face_detect_success);
        mFailColor = res.getColor(R.color.face_detect_fail);
        */
		mFocusingColor = 0xff00ff;
        mFocusedColor = 0xff00ff;
        mFailColor = 0xff00ff;
        mColor = mFocusingColor;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(res.getDimension(R.dimen.face_circle_stroke));
        mPaint.setDither(true);
       // mPaint.setColor(Color.WHITE);//setColor(0xFFFFFF00);
        mPaint.setColor(Color.RED);//setColor(0xFFFFFF00);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mImageView = (ImageView)getRootView().findViewById(R.id.face_view);
    }


    public void onSurfaceTextureSizeChanged(int uncroppedWidth, int uncroppedHeight) {
        mUncroppedWidth = uncroppedWidth;
        mUncroppedHeight = uncroppedHeight;
    }

    public void setFaces(Face[] faces) {
        if (LOGV) Log.v(TAG, "Num of faces=" + faces.length);
        if (mPause) return;
		setVisibility(View.VISIBLE);
		//((SurfaceView)findViewById(R.id.preview_content)).setVisibility(View.INVISIBLE);
        if (mFaces != null) {
            if ((faces.length > 0 && mFaces.length == 0)
                    || (faces.length == 0 && mFaces.length > 0)) {
                mPendingFaces = faces;
                if (!mStateSwitchPending) {
                    mStateSwitchPending = true;
                    mHandler.sendEmptyMessageDelayed(MSG_SWITCH_FACES, SWITCH_DELAY);
                }
                return;
            }
        }
        if (mStateSwitchPending) {
            mStateSwitchPending = false;
            mHandler.removeMessages(MSG_SWITCH_FACES);
        }
		
        mFaces = faces;
        if (!mBlocked && (mFaces != null) && (mFaces.length > 0)) {
            invalidate();
        }

    }

    public void setDisplayOrientation(int orientation) {
        mDisplayOrientation = orientation;
        if (LOGV) Log.v(TAG, "mDisplayOrientation=" + orientation);
    }


    public void setOrientation(int orientation, boolean animation) {
        mOrientation = orientation;
        invalidate();
    }

    public void setMirror(boolean mirror) {
        mMirror = mirror;
        if (LOGV) Log.v(TAG, "mMirror=" + mirror);
    }

    public boolean faceExists() {
        return (mFaces != null && mFaces.length > 0);
    }


    public void showStart() {
        mColor = mFocusingColor;
        invalidate();
    }

    // Ignore the parameter. No autofocus animation for face detection.

    public void showSuccess(boolean timeout) {
        mColor = mFocusedColor;
        invalidate();
    }

    // Ignore the parameter. No autofocus animation for face detection.

    public void showFail(boolean timeout) {
        mColor = mFailColor;
        invalidate();
    }


    public void clear() {
        // Face indicator is displayed during preview. Do not clear the
        // drawable.
        mColor = mFocusingColor;
        mFaces = null;
        invalidate();
    }

    public void pause() {
        mPause = true;
    }

    public void resume() {
        mPause = false;
    }

    public void setBlockDraw(boolean block) {
        mBlocked = block;
    }

    public void setDisplayRotation(int orientation) {
        mDisplayRotation = orientation;
    }

	public int getDisplayRotation() {
	    Context context = getContext();
		Activity activity;
		if(context instanceof Activity)
		{
		    activity = (Activity)context;
		}
		else
		{
		    return -1;
		}
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		switch (rotation) {
			case Surface.ROTATION_0: return 0;
			case Surface.ROTATION_90: return 90;
			case Surface.ROTATION_180: return 180;
			case Surface.ROTATION_270: return 270;
		}
		return 1;
	}

	
    @Override
    protected void onDraw(Canvas canvas) {
        //Log.i(TAG, "faceview ppt, cancas: " + canvas.getWidth() + ", " + canvas.getHeight());
        //Log.i(TAG, "faceview ppt, in onDraw, mBlocked = " + mBlocked);
        Log.i(TAG, "faceview ppt, getDisplayRotation: " + getDisplayRotation());
		mPaint.setColor(Color.RED);
        if (!mBlocked && (mFaces != null) && (mFaces.length > 0)) {
            int rw, rh;
            rw = mUncroppedWidth;
            rh = mUncroppedHeight;
            // Prepare the matrix.
            if (((rh > rw) && ((mDisplayOrientation == 0) || (mDisplayOrientation == 180)))
                    || ((rw > rh) && ((mDisplayOrientation == 90) || (mDisplayOrientation == 270)))) {
                int temp = rw;
                rw = rh;
                rh = temp;
            }
					mOrientation = 270;
            mDisplayOrientation = 270;
            com.pandroid.camera.util.CameraUtil.prepareMatrix(mMatrix, mMirror, mDisplayOrientation, rw, rh);
            int dx = (getWidth() - rw) / 2;
            int dy = (getHeight() - rh) / 2;
			Log.i(TAG, "faceview ppt, in onDraw, rw, rh, getWidth, getHeight = " + rw + ", " + rh + ", " + getWidth() + ", " + getHeight() +
				", mOrientation = " + mOrientation);

            // Focus indicator is directional. Rotate the matrix and the canvas
            // so it looks correctly in all orientations.
            canvas.save();
			canvas.drawRGB(0, 0, 255);  
            mMatrix.postRotate(mOrientation); // postRotate is clockwise
            canvas.rotate(-mOrientation); // rotate is counter-clockwise (for canvas)
            for (int i = 0; i < mFaces.length; i++) {
                // Filter out false positives.
                Log.i(TAG, "faceview ppt, in onDraw, mFaces[i].score = " + mFaces[i].score + ", mFaces[i].rect = " + 
                		mFaces[i].rect.width() + "," + mFaces[i].rect.height() + ", p: " + 
                		mFaces[i].rect.left + ", " + mFaces[i].rect.top + ", "  + mFaces[i].rect.right + ", " + mFaces[i].rect.bottom);
               // if (mFaces[i].score < 50) continue;

                // Transform the coordinates.
                
                mRect.set(mFaces[i].rect);
                //if (LOGV) CameraUtil.dumpRect(mRect, "Original rect");
                mMatrix.mapRect(mRect);
                //if (LOGV) CameraUtil.dumpRect(mRect, "Transformed rect");
                
                mPaint.setColor(Color.RED);
                //mRect.offset(dx, dy);
                
                canvas.drawOval(mRect, mPaint);

				

/*
                if (mFaces[i] instanceof ExtendedFace) {
                    ExtendedFace face = (ExtendedFace)mFaces[i];
                    float[] point = new float[4];
                    int delta_x = mFaces[i].rect.width() / 12;
                    int delta_y = mFaces[i].rect.height() / 12;
                    Log.e(TAG, "blink: (" + face.getLeftEyeBlinkDegree()+ ", " +
                        face.getRightEyeBlinkDegree() + ")");
                    if (face.leftEye != null) {
                        if ((mDisplayRotation == 0) ||
                                (mDisplayRotation == 180)) {
                            point[0] = face.leftEye.x;
                            point[1] = face.leftEye.y - delta_y / 2;
                            point[2] = face.leftEye.x;
                            point[3] = face.leftEye.y + delta_y / 2;
                        } else {
                            point[0] = face.leftEye.x - delta_x / 2;
                            point[1] = face.leftEye.y;
                            point[2] = face.leftEye.x + delta_x / 2;
                            point[3] = face.leftEye.y;

                        }
                        mMatrix.mapPoints (point);
                        if (face.getLeftEyeBlinkDegree() >= blink_threshold) {
                            canvas.drawLine(point[0]+ dx, point[1]+ dy,
                                point[2]+ dx, point[3]+ dy, mPaint);
                        }
                    }
                    if (face.rightEye != null) {
                        if ((mDisplayRotation == 0) ||
                                (mDisplayRotation == 180)) {
                            point[0] = face.rightEye.x;
                            point[1] = face.rightEye.y - delta_y / 2;
                            point[2] = face.rightEye.x;
                            point[3] = face.rightEye.y + delta_y / 2;
                        } else {
                            point[0] = face.rightEye.x - delta_x / 2;
                            point[1] = face.rightEye.y;
                            point[2] = face.rightEye.x + delta_x / 2;
                            point[3] = face.rightEye.y;
                        }
                        mMatrix.mapPoints (point);
                        if (face.getRightEyeBlinkDegree() >= blink_threshold) {
                            //Add offset to the points if the rect has an offset
                            canvas.drawLine(point[0] + dx, point[1] + dy,
                                point[2] +dx, point[3] +dy, mPaint);
                        }
                    }

                    if (face.getLeftRightGazeDegree() != 0
                        || face.getTopBottomGazeDegree() != 0 ) {

                        double length =
                            Math.sqrt((face.leftEye.x - face.rightEye.x) *
                                (face.leftEye.x - face.rightEye.x) +
                                (face.leftEye.y - face.rightEye.y) *
                                (face.leftEye.y - face.rightEye.y)) / 2.0;
                        double nGazeYaw = -face.getLeftRightGazeDegree();
                        double nGazePitch = -face.getTopBottomGazeDegree();
                        float gazeRollX =
                            (float)((-Math.sin(nGazeYaw/180.0*Math.PI) *
                                Math.cos(-face.getRollDirection()/
                                    180.0*Math.PI) +
                                Math.sin(nGazePitch/180.0*Math.PI) *
                                Math.cos(nGazeYaw/180.0*Math.PI) *
                                Math.sin(-face.getRollDirection()/
                                    180.0*Math.PI)) *
                                (-length) + 0.5);
                        float gazeRollY =
                            (float)((Math.sin(-nGazeYaw/180.0*Math.PI) *
                                Math.sin(-face.getRollDirection()/
                                    180.0*Math.PI)-
                                Math.sin(nGazePitch/180.0*Math.PI) *
                                Math.cos(nGazeYaw/180.0*Math.PI) *
                                Math.cos(-face.getRollDirection()/
                                    180.0*Math.PI)) *
                                (-length) + 0.5);

                        if (face.getLeftEyeBlinkDegree() < blink_threshold) {
                            if ((mDisplayRotation == 90) ||
                                    (mDisplayRotation == 270)) {
                                point[0] = face.leftEye.x;
                                point[1] = face.leftEye.y;
                                point[2] = face.leftEye.x + gazeRollX;
                                point[3] = face.leftEye.y + gazeRollY;
                            } else {
                                point[0] = face.leftEye.x;
                                point[1] = face.leftEye.y;
                                point[2] = face.leftEye.x + gazeRollY;
                                point[3] = face.leftEye.y + gazeRollX;
                            }
                            mMatrix.mapPoints (point);
                            canvas.drawLine(point[0] +dx, point[1] + dy,
                                point[2] + dx, point[3] +dy, mPaint);
                        }

                        if (face.getRightEyeBlinkDegree() < blink_threshold) {
                            if ((mDisplayRotation == 90) ||
                                    (mDisplayRotation == 270)) {
                                point[0] = face.rightEye.x;
                                point[1] = face.rightEye.y;
                                point[2] = face.rightEye.x + gazeRollX;
                                point[3] = face.rightEye.y + gazeRollY;
                            } else {
                                point[0] = face.rightEye.x;
                                point[1] = face.rightEye.y;
                                point[2] = face.rightEye.x + gazeRollY;
                                point[3] = face.rightEye.y + gazeRollX;

                            }
                            mMatrix.mapPoints (point);
                            canvas.drawLine(point[0] + dx, point[1] + dy,
                                point[2] + dx, point[3] + dy, mPaint);
                        }
                    }

                    if (face.mouth != null) {
                        Log.e(TAG, "smile: " + face.getSmileDegree() + "," +
                            face.getSmileScore());
                        if (face.getSmileDegree() < smile_threashold_no_smile) {
                            point[0] = face.mouth.x + dx - delta_x;
                            point[1] = face.mouth.y;
                            point[2] = face.mouth.x + dx + delta_x;
                            point[3] = face.mouth.y;

                            Matrix faceMatrix = new Matrix(mMatrix);
                            faceMatrix.preRotate(face.getRollDirection(),
                                    face.mouth.x, face.mouth.y);
                            faceMatrix.mapPoints(point);
                            canvas.drawLine(point[0] + dx, point[1] + dy,
                                point[2] + dx, point[3] + dy, mPaint);
                        } else if (face.getSmileDegree() <
                            smile_threashold_small_smile) {
                            int rotation_mouth = 360 - mDisplayRotation;
                            mRect.set(face.mouth.x-delta_x,
                                face.mouth.y-delta_y, face.mouth.x+delta_x,
                                face.mouth.y+delta_y);
                            mMatrix.mapRect(mRect);
                            mRect.offset(dx, dy);
                            canvas.drawArc(mRect, rotation_mouth,
                                    180, true, mPaint);
                        } else {
                            mRect.set(face.mouth.x-delta_x,
                                face.mouth.y-delta_y, face.mouth.x+delta_x,
                                face.mouth.y+delta_y);
                            mMatrix.mapRect(mRect);
                            mRect.offset(dx, dy);
                            canvas.drawOval(mRect, mPaint);
                        }
                    }
                }
				*/
            }
		    canvas.restore();
        }
        super.onDraw(canvas);
    }

	@Override
    public void onFaceDetection(Face[] faces) {
   /*     Exception e = new Exception("face panpan test, in PhotoUI.java onFaceDetection, go in.\n");
          e.printStackTrace();
     */
         
         setFaces(faces);
    }

    public void displayFace(Bitmap bmp)
    {
        Log.i(TAG, "face ppt, displayFace");
		if(mImageView == null)
		{
		    mImageView = (ImageView)getRootView().findViewById(R.id.face_view);
		}
        mImageView.setVisibility(View.VISIBLE);
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        //Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
		Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0,0, bmp.getWidth(),  bmp.getHeight(), matrix, true);

        mImageView.setImageBitmap(nbmp2);
    }
	
    static int i = 0;
	public void processFace(byte[] data, Camera camera)
    {
            if(mFaces == null)
       	    {
       	        return;
       	    }
          	if(mFaces.length > 0){
				
				 Size size = camera.getParameters().getPreviewSize();		   
				 try{
					 YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);	
					 if((image!=null) && ((i++)%10 == 0)){  
							ByteArrayOutputStream stream = new ByteArrayOutputStream();  
							image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream); 
                            BitmapFactory.Options newOpts = new BitmapFactory.Options(); 
							newOpts.inSampleSize = 1;
						   final Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size(), newOpts);
							
							//**********************************
							Runnable runnable2 = new Runnable() {
				                public void run() {
									try{
									
										Log.i(TAG, "face ppt, face: " + mFaces[0].rect.left +
										", " + mFaces[0].rect.top + ", " + mFaces[0].rect.right + ", " 
										+ mFaces[0].rect.bottom
											+ ", " + mFaces[0].rect.width() + ", " + mFaces[0].rect.height());

                                       RectF r = new RectF((float)(mFaces[0].rect.left), 
											(float)(mFaces[0].rect.top),
                                            (float)(mFaces[0].rect.right),
                                            (float)(mFaces[0].rect.bottom));
									   Matrix matrix = new Matrix();
									   matrix.reset();
									   // matrix.postRotate(270);
									   matrix.postScale(size.width / 2000f, size.height / 2000f);
                                       matrix.postTranslate(size.width / 2f, size.height / 2f);
									   matrix.mapRect(r);

									   Log.i(TAG, "face panpan test, crop: " + r.left + ", " + r.top + ", " + r.right + ", " + r.bottom +
											", " + r.width() + ", " + r.height());
									/*	
										Matrix m ＝ new Matrix();
										m.reset();
										m.preScale(/1600.0F * size.width, /1200.0F * size.height); 
										m.mapRect(mUI.mFaceView.mFaces[0].rect);
										*/
									    Bitmap croppedImage = Bitmap.createBitmap((int)r.width(), (int)r.height(),
					                        Bitmap.Config.RGB_565);
						                Canvas canvasCrop = new Canvas(croppedImage);
										Rect dstRect = new Rect(0, 0, (int)r.width(), (int)r.height());
						                canvasCrop.drawBitmap(bmp, new Rect((int)r.left, (int)r.top, (int)r.right, (int)r.bottom), dstRect, null);


										OutputStream outputStream = null;
										File file = null;

										/*
										file = new File("/sdcard/test/raw" + i + ".jpg");
										outputStream = new FileOutputStream(file);
										bmp.compress(CompressFormat.JPEG, 75, outputStream);
										outputStream.close();
										*/
										i++;
                                        displayFace(croppedImage);

										file = new File("/sdcard/test/crop" + i + ".jpg");
										outputStream = new FileOutputStream(file);
										croppedImage.compress(CompressFormat.JPEG, 75, outputStream);
										outputStream.close();


										//croppedImage.recycle();
										//bmp.recycle();
										if(stream != null)
										    stream.close(); 
									}catch(Exception ex){	
				                        Log.e("Sys","face ppt, Error:"+ex.getMessage());  
									}
									
				                }
			     
				            };								
							runnable2.run();			                             	  
					}  
				 }catch(Exception ex){	
				     Log.e("Sys","Error:"+ex.getMessage());  
			     }  
			}		
    }
	
}
