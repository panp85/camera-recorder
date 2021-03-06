package com.pandroid.main;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
//import org.codeaurora.snapcam.R;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

/**
 * Activity that shows permissions request dialogs and handles lack of critical permissions.
 */
public class PermissionsActivity extends Activity {
    private static final String TAG = "PermissionsActivity";

    private static int PERMISSION_REQUEST_CODE = 1;

    private int mIndexPermissionRequestCamera;
    private int mIndexPermissionRequestMicrophone;
    private int mIndexPermissionRequestLocation;
    private int mIndexPermissionRequestStorageWrite;
    private int mIndexPermissionRequestStorageRead;
    private int mIndexPermissionRequestReadPhone;
    private boolean mShouldRequestCameraPermission;
    private boolean mShouldRequestMicrophonePermission;
    private boolean mShouldRequestLocationPermission;
    private boolean mShouldRequestStoragePermission;
    private boolean mShouldRequestReadPhonePermission;
    private int mNumPermissionsToRequest;
    private boolean mFlagHasCameraPermission;
    private boolean mFlagHasMicrophonePermission;
    private boolean mFlagHasStoragePermission;
    private boolean mCriticalPermissionDenied;
    private Intent mIntent;
    private boolean mIsReturnResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntent = getIntent();
        mIsReturnResult = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mCriticalPermissionDenied && !mIsReturnResult) {
            mNumPermissionsToRequest = 0;
            checkPermissions();
        } else {
            mCriticalPermissionDenied = false;
        }
    }

    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mNumPermissionsToRequest++;
            mShouldRequestCameraPermission = true;
        } else {
            mFlagHasCameraPermission = true;
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            mNumPermissionsToRequest++;
            mShouldRequestMicrophonePermission = true;
        } else {
            mFlagHasMicrophonePermission = true;
        }

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            mNumPermissionsToRequest = mNumPermissionsToRequest + 2;
            mShouldRequestStoragePermission = true;
        } else {
            mFlagHasStoragePermission = true;
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            mNumPermissionsToRequest++;
            mShouldRequestLocationPermission = true;
        }

        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            mNumPermissionsToRequest++;
            mShouldRequestReadPhonePermission = true;
        }

        if (mNumPermissionsToRequest != 0) {
            buildPermissionsRequest();
        } else {
            handlePermissionsSuccess();
        }
    }

    private void buildPermissionsRequest() {
        String[] permissionsToRequest = new String[mNumPermissionsToRequest];
        int permissionsRequestIndex = 0;

        if (mShouldRequestCameraPermission) {
            permissionsToRequest[permissionsRequestIndex] = Manifest.permission.CAMERA;
            mIndexPermissionRequestCamera = permissionsRequestIndex;
            permissionsRequestIndex++;
        }
        if (mShouldRequestMicrophonePermission) {
            permissionsToRequest[permissionsRequestIndex] = Manifest.permission.RECORD_AUDIO;
            mIndexPermissionRequestMicrophone = permissionsRequestIndex;
            permissionsRequestIndex++;
        }
        if (mShouldRequestStoragePermission) {
            permissionsToRequest[permissionsRequestIndex] =
                    Manifest.permission.WRITE_EXTERNAL_STORAGE;
            mIndexPermissionRequestStorageWrite = permissionsRequestIndex;
            permissionsRequestIndex++;
            permissionsToRequest[permissionsRequestIndex] =
                    Manifest.permission.READ_EXTERNAL_STORAGE;
            mIndexPermissionRequestStorageRead = permissionsRequestIndex;
            permissionsRequestIndex++;
        }
        if (mShouldRequestLocationPermission) {
            permissionsToRequest[permissionsRequestIndex] =
                    Manifest.permission.ACCESS_COARSE_LOCATION;
            mIndexPermissionRequestLocation = permissionsRequestIndex;
            permissionsRequestIndex++;
        }
        if (mShouldRequestReadPhonePermission) {
            permissionsToRequest[permissionsRequestIndex] =
                    Manifest.permission.READ_PHONE_STATE;
            mIndexPermissionRequestReadPhone = permissionsRequestIndex;
            permissionsRequestIndex++;
        }
        requestPermissions(permissionsToRequest, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
		Exception e = new Exception("onRequestPermissionsResult ppt");
		e.printStackTrace();

        if (mShouldRequestCameraPermission) {
            if ((grantResults.length >= mIndexPermissionRequestCamera + 1) &&
                (grantResults[mIndexPermissionRequestCamera] ==
                        PackageManager.PERMISSION_GRANTED)) {
                mFlagHasCameraPermission = true;
            } else {
                mCriticalPermissionDenied = true;
            }
        }
        if (mShouldRequestMicrophonePermission) {
            if ((grantResults.length >= mIndexPermissionRequestMicrophone + 1) &&
                (grantResults[mIndexPermissionRequestMicrophone] ==
                        PackageManager.PERMISSION_GRANTED)) {
                mFlagHasMicrophonePermission = true;
            } else {
                mCriticalPermissionDenied = true;
            }
        }
        if (mShouldRequestStoragePermission) {
            if ((grantResults.length >= mIndexPermissionRequestStorageRead + 1) &&
                (grantResults[mIndexPermissionRequestStorageWrite] ==
                        PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[mIndexPermissionRequestStorageRead] ==
                            PackageManager.PERMISSION_GRANTED)) {
                mFlagHasStoragePermission = true;
            } else {
                mCriticalPermissionDenied = true;
            }
        }

        if (mShouldRequestLocationPermission) {
            if ((grantResults.length >= mIndexPermissionRequestLocation + 1) &&
                (grantResults[mIndexPermissionRequestLocation] ==
                        PackageManager.PERMISSION_GRANTED)) {
                // Do nothing
            } else {
                // Do nothing
            }
        }
        if (mShouldRequestReadPhonePermission) {
            if ((grantResults.length >= mIndexPermissionRequestReadPhone + 1) &&
                    (grantResults[mIndexPermissionRequestReadPhone] ==
                            PackageManager.PERMISSION_GRANTED)) {
                // Do nothing
            } else {
                // Do nothing
            }
        }

        if (mFlagHasCameraPermission && mFlagHasMicrophonePermission &&
                mFlagHasStoragePermission) {
            handlePermissionsSuccess();
        } else if (mCriticalPermissionDenied) {
            handlePermissionsFailure();
        }
    }

    private void handlePermissionsSuccess() {
        if (mIntent != null) {
            setRequestPermissionShow();
            mIsReturnResult = true;
            mIntent.setClass(this, MainActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(mIntent);
            finish();
        } else {
            mIsReturnResult = false;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void handlePermissionsFailure() {
        new AlertDialog.Builder(this).setTitle("failed")
                .setMessage("不允许")
                .setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            finish();
                        }
                        return true;
                    }
                })
                .setPositiveButton("返回",
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void setRequestPermissionShow() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRequestShown = prefs.getBoolean("request_permission_p", false);
        if (!isRequestShown) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("request_permission_p", true);
            editor.apply();
        }
    }
}

