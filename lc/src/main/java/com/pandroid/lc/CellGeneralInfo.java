package com.pandroid.lc;

import android.telephony.TelephonyManager;

/**
 * Created by pc on 2018/4/16.
 */

public class CellGeneralInfo {
    public static final int NP_CELL_INFO_UPDATE = 1001;

    public int type = -1;
    public int CId = -1;
    public int lac = -1;
    public int tac = -1;
    public int psc = -1;
    public int pci = -1;
    public int RatType= TelephonyManager.NETWORK_TYPE_UNKNOWN;
    public int signalStrength = -1;
    public int asulevel = -1;

    @Override
    public String toString() {
        return new String("CellGeneralInfo: \n" + "type: " + type +
                ", CId: " + CId +
                ", lac: " + lac +
                ", tac: " + tac +
                ", psc: " + psc +
                ", pci: " + pci +
                ", RatType: " + RatType +
                ", signalStrength: " + signalStrength +
                ", asulevel: " + asulevel);
    }
}
