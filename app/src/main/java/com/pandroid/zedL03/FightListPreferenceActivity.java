package com.pandroid.zedL03;

import android.os.Bundle;
import android.preference.PreferenceActivity;


import com.pandroid.R;

/** �����ϵ�ʹ�÷�ʽ����API level 11֮ǰ����δ����fragmentʱ��ʹ�÷�ʽ������������ѧϰһЩpreferences�Ļ��������ص�
 * XML�﷨*/
public class FightListPreferenceActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.resolution_options);
/*		Preference p = findPreference("flight_option_preference");
		showInfo("summary: " + p.getSummary());
		showInfo("title: " + p.getTitle());
		
		ListPreference lp = (ListPreference)findPreference("selected_flight_sort_option");
		showInfo("lp = " + lp);
		showInfo("entry = " + lp.getEntry());
		showInfo("value = " + lp.getValue());*/
	}
	
/*
	private void showInfo(String s){
		Log.d(MainActivity.TAG + getLocalClassName(),s);
	}*/
}
