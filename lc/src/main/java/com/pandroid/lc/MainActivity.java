package com.pandroid.lc;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.pandroid.lc.CellGeneralInfo.NP_CELL_INFO_UPDATE;

public class MainActivity extends AppCompatActivity {
    private PhoneInfoThread phoneInfoThread;
    private  int msgcount;
    public Handler mMainHandler;
    // for current
    private List<CellGeneralInfo> mCellInfoList;
    private CellnfoRecycleViewAdapter myRecycleViewAdapter;
    private RecyclerView recyclerView;
    //for history
    private List<CellGeneralInfo> mHistoryServerCellList;
    private CellnfoRecycleViewAdapter historyRecycleViewAdapter;
    private RecyclerView historyrecyclerView;

    private boolean mHasCriticalPermissions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.pandroid.lc.R.layout.activity_main_lc);

        mCellInfoList = new ArrayList<CellGeneralInfo>();
        recyclerView = (RecyclerView)findViewById(R.id.rcvcell);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        myRecycleViewAdapter  = new CellnfoRecycleViewAdapter(MainActivity.this,mCellInfoList);
        recyclerView.setAdapter(myRecycleViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //
        mHistoryServerCellList = new ArrayList<CellGeneralInfo>();
        historyrecyclerView = (RecyclerView)findViewById(R.id.historyrcv);
        LinearLayoutManager historylayoutManager = new LinearLayoutManager(this);
        historylayoutManager.setOrientation(OrientationHelper.VERTICAL);
        historyrecyclerView.setLayoutManager(historylayoutManager);
        historyRecycleViewAdapter  = new CellnfoRecycleViewAdapter(MainActivity.this,mHistoryServerCellList);
        historyrecyclerView.setAdapter(historyRecycleViewAdapter);
        historyrecyclerView.setItemAnimator(new DefaultItemAnimator());

        msgcount = 0;
        InitProcessThread();
    }

    private void InitProcessThread() {
        mMainHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.what == NP_CELL_INFO_UPDATE)
                {
                    msgcount++;
                    Bundle bundle = msg.getData();
                    myRecycleViewAdapter.notifyDataSetChanged();
                    historyRecycleViewAdapter.notifyDataSetChanged();
                    TextView tvTime = (TextView)findViewById(R.id.tvTimeleaps);
                    tvTime.setText("Time:" + msgcount);
                    TextView tvAllCellInfo = (TextView)findViewById(R.id.tvCellCount);
                    tvAllCellInfo.setText("("+mHistoryServerCellList.size()+")");

                    TextView tvDeviceId = (TextView)findViewById(R.id.tvDeviceId);
                    tvDeviceId.setText("DeviceId:" + phoneInfoThread.deviceId);

                    TextView tvRatType = (TextView)findViewById(R.id.tvRatType);
                    tvRatType.setText("RatType:"+ phoneInfoThread.ratType);

                    TextView tvMnc = (TextView)findViewById(R.id.tMnc);
                    tvMnc.setText("Mnc:"+phoneInfoThread.mnc);

                    TextView tvMcc = (TextView)findViewById(R.id.tvMcc);
                    tvMcc.setText("Mcc:"+phoneInfoThread.mcc);

                    TextView tvOperatorName = (TextView)findViewById(R.id.tvOperaterName);
                    tvOperatorName.setText("Operator:"+phoneInfoThread.operaterName);

                    TextView tvImsi = (TextView)findViewById(R.id.tvImsi);
                    tvImsi.setText("Imsi:"+phoneInfoThread.Imsi);

                    TextView tvLine1Number = (TextView)findViewById(R.id.tvLine1Number);
                    tvLine1Number.setText("LN:"+phoneInfoThread.line1Number);

                    TextView tvSerialNum = (TextView)findViewById(R.id.tvSerialNum);
                    tvSerialNum.setText("SN:"+phoneInfoThread.serialNumber);

                    TextView tvModel = (TextView)findViewById(R.id.tvModel);
                    tvModel.setText("Model:" + phoneInfoThread.phoneModel);

                    TextView tvSoftwareVersion = (TextView)findViewById(R.id.tvSoftware);
                    tvSoftwareVersion.setText("Version:" + phoneInfoThread.deviceSoftwareVersion);

                }
                super.handleMessage(msg);
            }
        };

        //phoneInfoThread = new PhoneInfoThread(MainActivity.this);
        phoneInfoThread = new PhoneInfoThread(MainActivity.this, mCellInfoList, mHistoryServerCellList, mMainHandler);
        phoneInfoThread.start();
    }
}
