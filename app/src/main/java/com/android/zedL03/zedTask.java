package com.android.zedL03;

//import android.os.ServiceManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import java.lang.ref.WeakReference;
import android.util.Log;
import android.os.IBinder;

import android.os.RemoteException;

/**
 * Created by pc on 2017/11/6.
 */

public class ZedTask extends Handler {
	private static final String TAG = "zedApp";
	private static final java.lang.String DESCRIPTOR = "zedTask";
	private static final int GET_APP_MESSAGE = 1;
	private static final int SET_VIDEO_Q = 0x1000;
    public static native void native_connect(Object wt);
	public long mNativeZcb;

	void message_callback(Object ref, String cmd)
	{
	    ZedTask zt = (ZedTask)((WeakReference)ref).get();
        if (zt == null) {
            return;
        }
		//zt.sendMessage(cmd);
	}
	
    public void start()
    {
 /*
        System.loadLibrary("zedjni");
        native_connect(new WeakReference<ZedTask>(this));
 */
    }

    public void process(Parcel _reply)
    {
        int message_id = _reply.readInt();
		switch(message_id)
		{
		    case SET_VIDEO_Q:
				//set video quality
				break;
			default:
			    break;
		}
		
    }
/*
	public void getMessage() {
		  Log.i(TAG, "Client getMessage");
		  Parcel _data = Parcel.obtain();
		  Parcel _reply = Parcel.obtain();
		  IBinder b = ServiceManager.getService(DESCRIPTOR);
		   try {
			  _data.writeInterfaceToken(DESCRIPTOR);
			  b.transact(GET_APP_MESSAGE, _data, _reply, 0);
		      
			  //_reply.readException();

			  process(_reply);
			  
			  //_reply.readInt();
		  } catch (RemoteException e) {
			   // TODO Auto-generated catch block
			  e.printStackTrace();

		  } finally {
			  _reply.recycle();
			  _data.recycle();
		  }
	 }
*/
 	public ZedTask(Looper looper) {
        super(looper);
    }
	
     public void handleMessage(android.os.Message msg)  
    {  
        //process message
    }
};

