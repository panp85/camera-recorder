package com.pandroid.message;

//import android.os.ServiceManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.preference.PreferenceManager;
import android.util.Log;
import android.os.IBinder;

import android.os.RemoteException;
import android.os.HandlerThread;
import android.view.View;

import com.pandroid.socket.SocketClient;

import org.json.JSONException;
import org.json.JSONObject;

import static android.R.id.input;

/**
 * Created by pp on 2018/3/6.
 */

public class MessageTask extends HandlerThread
        implements SharedPreferences.OnSharedPreferenceChangeListener{
	private static final String TAG = "Message Task";
	private static final int LOCAL_CLIENT_TYPE_APPMAIN = 0;

    private static final int SEND_SOCKET_MESSAGE_LOCAL = 0;

	private static final int SEND_SOCKET_MESSAGE_REMOTE = 100;
    private static final int HEART_EVENT = 1;

	private static final int MESSAGE_TYPE = 0;


	private static final java.lang.String DESCRIPTOR = "Message Task";
	private static final int GET_APP_MESSAGE = 1;
	private static final int SET_VIDEO_Q = 0x1000;
 //   public native void native_connect(Object wt);
	public long mNativeZcb;
	private Handler mThreadHandler;
	private SocketClient mSocketClient_local = null;
	private SocketClient mSocketClient_remote = null;

    private Timer timer;
    private TimerTask task;
    Context context;

    public MessageTask(Context context, String name) {
        super(name);
        this.context = context;
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        //init();
    }
    private void start_RemoteClinet(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String server = sp.getString("server_address", "192.168.1.1:12345");
        Log.i(TAG, "socket ppt, in setup_message, server: " + server);
        String[] elements = server.split(":");
        mSocketClient_remote = new SocketClient(elements[0], Integer.parseInt(elements[1]));
    }

	public void setup_message()
	{
		mSocketClient_local = new SocketClient();
        start_RemoteClinet();

		mThreadHandler = new Handler(getLooper())
		{
			@Override
			public void handleMessage(Message msg)
			{
				Log.i("MessageTask", "heart ppt, msg.what: " + msg.what);

                switch(msg.what){
                    case SEND_SOCKET_MESSAGE_LOCAL:
                        mSocketClient_local.sendMsg(new String((byte[]) msg.obj));
                        break;
					case SEND_SOCKET_MESSAGE_REMOTE:
                        mSocketClient_remote.sendMsg(new String((byte[]) msg.obj));
                        break;
/*
                    case HEART_EVENT:
                        Log.i("MessageTask", "heart ppt, handleMessage HEART_EVENT");
                        try {
                            JSONObject message_json = new JSONObject();
                            message_json.put("messagetype", "heart");
                            mThreadHandler.sendMessage(encode(message_json));
                        }


                        timer.schedule(task, 3000);
                        break;
                        */
                }
			}
		};

        setAppType();

        task = new TimerTask() {
            @Override
            public void run() {
                Log.i("MessageTask", "heart ppt, go in");

 /*               Message message = Message.obtain();
                message.what = HEART_EVENT;
                message.obj = "";
                Log.i("MessageTask", "heart ppt, sendMessage HEART_EVENT");
                mThreadHandler.sendMessage(message);*/
                JSONObject message_json = new JSONObject();
                try {
                    message_json.put("messagetype", "heart");
                }
                catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
                //mThreadHandler.sendMessage(encode(message_json));
                mSocketClient_local.sendMsg(new String((byte[])encode(message_json).obj));
				mSocketClient_remote.sendMsg(new String((byte[])encode(message_json).obj));
            }
        };
        timer = new Timer();
        timer.schedule(task, 3000, 3000);//3秒后执行TimeTask的run方法
	}

	public void setAppType()
	{
		try{
			byte buffer[];
            JSONObject message_json = new JSONObject();
            message_json.put("messagetype", "setapptype");
            message_json.put("apptype", LOCAL_CLIENT_TYPE_APPMAIN);
            Message message = encode(message_json);
			message.what = SEND_SOCKET_MESSAGE_LOCAL;
            mThreadHandler.sendMessage(message);
			message.what = SEND_SOCKET_MESSAGE_REMOTE;

	    }catch (JSONException ex) {
			throw new RuntimeException(ex);
		}
	}

	private  Message encode(JSONObject message_json){
        byte buffer[];
        Log.i("MessageTask", "message ppt, in setAppType, json data length: " + message_json.toString().length());
        buffer = new byte[2+message_json.toString().length()+1];
        //String l1 = String.valueOf((((short)message_json.toString().length())>>8));
        buffer[0] = (byte)(((short)(message_json.toString().length()+1)>>8) & 0xff);
        //String l2 = String.valueOf((((short)message_json.toString().length()) & 0xff));
        buffer[1] = (byte)((((short)(message_json.toString().length()+1)) & 0xff));
        Log.i("MessageTask", "message ppt, in setAppType, buffer: " +
                buffer[0] + ", " + buffer[1]);

        System.arraycopy(message_json.toString().getBytes(), 0, buffer, 2, message_json.toString().length());

        Message message = Message.obtain();
        message.obj = buffer;
        
        return message;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "ppt SETTING CHANGED: key = " + key);
        if ("server_address".equals(key) ) {
            if(mSocketClient_remote != null) {
                mSocketClient_remote.closeSocket();
            }
            start_RemoteClinet();
        }
    }
/*
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
        {
            //System.loadLibrary("zedJni");
        }
        Log.i(TAG, "zedTask ppt, in start.\n");
        
        //native_connect(new WeakReference<ZedTask>(this));
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
    public void closeSocket()
    {
        if (mSocketClient_local != null) {
            mSocketClient_local.closeSocket();
        }
        if (mSocketClient_remote != null) {
            mSocketClient_remote.closeSocket();
        }
    }



};

