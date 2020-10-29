package com.pandroid.lc;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.List;

import static com.pandroid.lc.CellGeneralInfo.NP_CELL_INFO_UPDATE;

/**
 * Created by pc on 2018/4/16.
 */

public class PhoneInfoThread extends Thread{

    public Handler mMainHandler;
    private List<CellGeneralInfo> mCellInfoList;
    private List<CellGeneralInfo> mHistoryServerCellList;

    private Context context;
    public String deviceId;
    public String deviceSoftwareVersion;
    public String Imsi;
    public String Imei;
    public String line1Number;
    public String serialNumber;
    public String operaterName;
    public String operaterId;
    public int mnc;
    public int mcc;
    public int datastate;
    public int ratType= TelephonyManager.NETWORK_TYPE_UNKNOWN;
    public int cellcount;
    public int phoneDatastate;
    public String phoneModel;
    public int timecount;

    public PhoneInfoThread(Context context)
    {
        this.context = context;
        timecount = 0;
    }
    public PhoneInfoThread(Context context, List<CellGeneralInfo> cellInfoList, List<CellGeneralInfo> historyServerCellList, Handler mainHandler)
    {
        mHistoryServerCellList = historyServerCellList;
        mMainHandler = mainHandler;
        mCellInfoList = cellInfoList;
        this.context = context;
        timecount = 0;
    }

    public void run()
    {
        while (true) {
            try {
                timecount++;
                Message message = new Message();
                message.what = NP_CELL_INFO_UPDATE;
                getCellInfo();
                Bundle bundle = new Bundle();
                bundle.putString("deviceId", deviceId);
                message.setData(bundle);
                mMainHandler.sendMessage(message);
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getCellInfo()
    {
        TelephonyManager phoneManager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        operaterName = phoneManager.getNetworkOperatorName();
        operaterId = phoneManager.getNetworkOperator();
        if(operaterId == null){
            Log.i("cellinfo", "lc ppt, in getCellInfo, operaterId null.\n");
            return;
        }
        //mnc = Integer.parseInt(operaterId.substring(0, 3));
        //mcc = Integer.parseInt(operaterId.substring(3));
        phoneDatastate = phoneManager.getDataState();
        deviceId = phoneManager.getDeviceId();
        Imei = phoneManager.getSimSerialNumber();
        Imsi = phoneManager.getSubscriberId();
        line1Number = phoneManager.getLine1Number();
        serialNumber = phoneManager.getSimSerialNumber();
        deviceSoftwareVersion = android.os.Build.VERSION.RELEASE;
        phoneModel = android.os.Build.MODEL;
        ratType = phoneManager.getNetworkType();

        mCellInfoList.clear();
        try
        {
            List<CellInfo> allCellinfo;
            allCellinfo = phoneManager.getAllCellInfo();

            if (allCellinfo != null)
            {
                cellcount = allCellinfo.size();
                Log.e("cell", "ppt cellInfo,2 cellcount:" + cellcount);
                for(CellInfo cellInfo:allCellinfo)
                {
                    CellGeneralInfo newCellInfo = new CellGeneralInfo();
                    newCellInfo.type = 0;
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        newCellInfo.CId = cellInfoGsm.getCellIdentity().getCid();
                        newCellInfo.signalStrength = cellInfoGsm.getCellSignalStrength().getDbm();
                        newCellInfo.asulevel = cellInfoGsm.getCellSignalStrength().getAsuLevel();
                        newCellInfo.lac = cellInfoGsm.getCellIdentity().getLac();
                        newCellInfo.RatType = TelephonyManager.NETWORK_TYPE_GSM;
                        //Log.e("cell", "ppt cellInfoGsm arfcn:" + cellInfoGsm.getCellIdentity().getArfcn());
                        if (cellInfoGsm.isRegistered()) {
                            newCellInfo.type = 1;
                        }
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                        newCellInfo.CId = cellInfoWcdma.getCellIdentity().getCid();
                        newCellInfo.psc = cellInfoWcdma.getCellIdentity().getPsc();
                        newCellInfo.lac = cellInfoWcdma.getCellIdentity().getLac();
                        newCellInfo.signalStrength = cellInfoWcdma.getCellSignalStrength().getDbm();
                        newCellInfo.asulevel = cellInfoWcdma.getCellSignalStrength().getAsuLevel();
                        newCellInfo.RatType = TelephonyManager.NETWORK_TYPE_UMTS;
                        Log.e("cell", "ppt cellInfoWcdma arfcn:" + cellInfoWcdma.getCellIdentity().getUarfcn());
                        if (cellInfoWcdma.isRegistered()) {
                            newCellInfo.type = 1;
                        }
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                        newCellInfo.CId = cellInfoLte.getCellIdentity().getCi();
                        newCellInfo.pci = cellInfoLte.getCellIdentity().getPci();
                        newCellInfo.tac = cellInfoLte.getCellIdentity().getTac();
                        newCellInfo.signalStrength = cellInfoLte.getCellSignalStrength().getDbm();
                        newCellInfo.asulevel = cellInfoLte.getCellSignalStrength().getAsuLevel();
                        newCellInfo.RatType = TelephonyManager.NETWORK_TYPE_LTE;
                        Log.e("cell", "ppt cellInfoLte arfcn."/* + cellInfoLte.getCellIdentity().getEarfcn()*/);
                        if (cellInfoLte.isRegistered()) {
                            newCellInfo.type = 1;
                        }
                    }
                    else if (cellInfo instanceof CellInfoCdma) {
                        CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                        /*newCellInfo.CId = cellInfoCdma.getCellIdentity().getCid();
                        newCellInfo.pci = cellInfoCdma.getCellIdentity().getPci();
                        newCellInfo.tac = cellInfoCdma.getCellIdentity().getTac();*/
                        newCellInfo.signalStrength = cellInfoCdma.getCellSignalStrength().getDbm();
                        newCellInfo.asulevel = cellInfoCdma.getCellSignalStrength().getAsuLevel();
                        Log.e("cell", "ppt cellInfoCdma NETWORK_TYPE_CDMA.");
                        newCellInfo.RatType = TelephonyManager.NETWORK_TYPE_CDMA;
                        if (cellInfoCdma.isRegistered()) {
                            newCellInfo.type = 1;
                        }
                    }
                    else {
                        Log.i("cell", "ppt cellInfo unkown.\n");
                    }
                    //Log.i("cell", "ppt cellInfo:" + newCellInfo);
                    mCellInfoList.add(newCellInfo);
                    if(newCellInfo.type == 1)
                    {
                        int flag = 0;
                        for (CellGeneralInfo serverCellInfo:mHistoryServerCellList)
                        {
                            if ((newCellInfo.CId == serverCellInfo.CId) && (newCellInfo.RatType == serverCellInfo.RatType))
                            {
                                flag = 1;
                                break;
                            }
                        }
                        if(flag == 0)
                        {
                            mHistoryServerCellList.add(newCellInfo);
                        }
                        //delete first one if more than 5
                        if(mHistoryServerCellList.size() > 5) {
                            mHistoryServerCellList.remove(0);
                        }
                    }

                }
                List<NeighboringCellInfo> infos = phoneManager.getNeighboringCellInfo();
                for (NeighboringCellInfo info1 : infos) { // 根据邻区总数进行循环

                    Log.e("cell","ppt cellInfo LAC : " + info1.getLac()); // 取出当前邻区的LAC

                    Log.e("cell", "ppt cellInfo CID : " + info1.getCid()); // 取出当前邻区的CID

                    Log.e("cell","ppt cellInfo BSSS : " + (-113 + 2 * info1.getRssi()) + "\n"); // 获取邻区基站信号强度

                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            //for older devices
            /*
            GsmCellLocation location = (GsmCellLocation) phoneManager.getCellLocation();
            CellGeneralInfo newCellInfo = new CellGeneralInfo();
            newCellInfo.type = 1;
            newCellInfo.CId = location.getCid();
            newCellInfo.tac = location.getLac();
            newCellInfo.psc = location.getPsc();
            */
        }
    }

}
