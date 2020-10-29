package com.pandroid.message;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;

import android.net.ConnectivityManager;

import android.net.wifi.WifiManager;
import android.content.Context;
import com.pandroid.R;

import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.pandroid.JNIInterface;

/** �����ϵ�ʹ�÷�ʽ����API level 11֮ǰ����δ����fragmentʱ��ʹ�÷�ʽ������������ѧϰһЩpreferences�Ļ��������ص�
 * XML�﷨*/
public class FightListPreferenceActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    static final String TAG = "ListPreference";
    ListPreference lp_wifi;
	ListPreference lp_resolution;
	ListPreference lp_otg;
	private WifiManager mWifiManager;
	private ConnectivityManager mCm;

	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.system_options);

		lp_resolution=(ListPreference)findPreference("selected_resolution_option");
	    lp_resolution.setOnPreferenceChangeListener(this); 

		lp_wifi=(ListPreference)findPreference("selected_wifi_option");
		lp_wifi.setOnPreferenceChangeListener(this); 

		lp_otg=(ListPreference)findPreference("otg_select");
		lp_otg.setOnPreferenceChangeListener(this);

        String res = JNIInterface.getOtgfromJNI();
        Log.i(TAG, "ppt, otg: " + res);
		if(res.substring(0,1).equals("0")){
			lp_otg.setSummary("host");
			lp_otg.setValue("0");
        }else{
            lp_otg.setSummary("device");
            lp_otg.setValue("1");
        }
		mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		mCm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	
/*		Preference p = findPreference("flight_option_preference");
		showInfo("summary: " + p.getSummary());
		showInfo("title: " + p.getTitle());
		
		ListPreference lp = (ListPreference)findPreference("selected_flight_sort_option");
		showInfo("lp = " + lp);
		showInfo("entry = " + lp.getEntry());
		showInfo("value = " + lp.getValue());*/
	}

	private void startTethering(boolean enable)
    {
		if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && !Settings.System.canWrite(this)) {
            //ToastUtil.longTips("请在该设置页面勾选，才可以使用路况提醒功能");
            Uri selfPackageUri = Uri.parse("package:"
                    + getApplicationContext().getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    selfPackageUri);
            startActivity(intent);
        }

        try {
            WifiConfiguration netConfig=new WifiConfiguration();

            //netConfig.SSID=wifiBean.getApname();
            netConfig.SSID="123456";
            //netConfig.preSharedKey=wifiBean.getAppasswd();
            netConfig.preSharedKey="654321";
            Log.d(TAG, "WifiPresenter：createAp----->netConfig.SSID:"
                    +netConfig.SSID+",netConfig.preSharedKey:"+netConfig.preSharedKey+",isOpen="+true);
 //           netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
 //           netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
 //           netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            if (true) {
 //               netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }else {
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            }
     //       netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
      //      netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
    //        netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
     //       netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            if(false) {
                Field field = mCm.getClass().getDeclaredField("TETHERING_WIFI");
                field.setAccessible(true);
                int mTETHERING_WIFI = (int) field.get(mCm);

                Field iConnMgrField = mCm.getClass().getDeclaredField("mService");
                iConnMgrField.setAccessible(true);
                Object iConnMgr = iConnMgrField.get(mCm);
                Class<?> iConnMgrClass = Class.forName(iConnMgr.getClass().getName());
                Method mstartTethering = iConnMgrClass.getMethod("startTethering", int.class, ResultReceiver.class, boolean.class);
                mstartTethering.invoke(iConnMgr, mTETHERING_WIFI, /*new ResultReceiver(handler) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    super.onReceiveResult(resultCode, resultData);
                }
            }*/null, enable);

            }
            else
            {
                Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
                method.invoke(mWifiManager, netConfig, enable);
            }
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
  /*  private static final class OnStartTetheringCallback extends
            ConnectivityManager.OnStartTetheringCallback {

        OnStartTetheringCallback() {
        }

        @Override
        public void onTetheringStarted() {
        }

        @Override
        public void onTetheringFailed() {
        }
    }
*/

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
		 if(preference instanceof ListPreference){ 
	        if(preference == lp_wifi)
	        {
	            Log.i(TAG, "ppt SETTING, wifi = " + (String)newValue);

				Intent intent = new Intent("android.intent.action.L03WifiBROADCAST");
				intent.putExtra("msg", (String)newValue);
				sendBroadcast(intent);
/*
				int r = Integer.valueOf((String)newValue).intValue();
				int wifiState = mWifiManager.getWifiState();
				if(r == 2)
				{
				    //if((wifiState == WifiManager.WIFI_STATE_ENABLING) || (wifiState == WifiManager.WIFI_STATE_ENABLED))
				    {
				        mWifiManager.setWifiEnabled(false);
				    }
					//mCm.stopTethering(ConnectivityManager.TETHERING_WIFI);
                    startTethering(false);
					//mWifiManager.setWifiApEnabled(null, true);
				}
				else if(r == 1)
				{
				    //if((wifiState == WifiManager.WIFI_STATE_ENABLING) || (wifiState == WifiManager.WIFI_STATE_ENABLED))
				    {
				        mWifiManager.setWifiEnabled(false);
				    }
					//mCm.startTethering(ConnectivityManager.TETHERING_WIFI, true, null, null);
                    startTethering(true);
				}
				else if(r == 0)
				{

					//mCm.stopTethering(ConnectivityManager.TETHERING_WIFI);
                    startTethering(false);
					if(!((wifiState == WifiManager.WIFI_STATE_ENABLING) || (wifiState == WifiManager.WIFI_STATE_ENABLED)))
				    {
				        mWifiManager.setWifiEnabled(true);
				    }
				}
*/
	        }
			else if(preference == lp_resolution)
			{
			    Log.i(TAG, "ppt SETTING, resolution = " + (String)newValue);
			}

			else if(preference == lp_otg)
			{
			    Log.i(TAG, "otg = " + (String)newValue);
			    if(newValue.equals("0")){
			        lp_otg.setSummary("host");
                }
			    else{
                    lp_otg.setSummary("device");
                }
				Log.i(TAG, "return: " + JNIInterface.setOtg2JNI((String)newValue));
			}
		 } 
	     return true; 
	 } 
/*
	private void showInfo(String s){
		Log.d(MainActivity.TAG + getLocalClassName(),s);
	}*/
}
