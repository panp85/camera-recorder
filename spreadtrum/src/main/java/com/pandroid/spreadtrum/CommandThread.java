package com.pandroid.spreadtrum;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.sprd.engineermode.utils.EngineerModeNative;

/**
 * Created by pc on 2018/5/14.
 */

public class CommandThread extends Thread {
    Handler mHandler;
    String mCommand;

    public CommandThread(Handler h, String s){
        mHandler = h;
        mCommand = s;
    }
    public void run() {
        String result = EngineerModeNative.native_sendATCmd(0, mCommand);
        Message message = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("result", result);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public void sendAt(){
        start();
    }
}
