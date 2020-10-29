package com.pandroid.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.io.DataOutputStream ;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pandroid.message.MessageTask;
import com.pandroid.socket.MessageReceiverCallback;


import org.json.JSONException;
import org.json.JSONObject;

/**
 */
public class SocketClientBase {
	private final String TAG = "SocketClientBase";
	private String socketName = "";
	private LocalSocket clientLocal;
	private Socket client_remote;
	private LocalSocketAddress address;
	private String server_ip;
	private int port;
	private boolean isConnected = false;
	private int connetTime = 1;
	private static int SOCKET_LOCAL = 0;
	private static int SOCKET_REMOTE = 1;
	private int socket_type;
	private int maxDateLen = 2048;
	public static final int REC_MESSAGE_PROCESS = 200;
	MessageReceiverCallback  rcb;
	DataOutputStream dateOutputStream;
	InputStream inputStream;

	private Handler mMessageHandler;
/*
	public SocketClient() {
		socket_type = SOCKET_LOCAL;
		clientLocal = new LocalSocket();

		address = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED);
		new ConnectSocketThread().start();
	}
*/
	public SocketClientBase(String sn, MessageReceiverCallback cb) {
		Log.i(TAG, "ppt, in SocketClientBase, local, socketName: " + sn);
		socket_type = SOCKET_LOCAL;
		clientLocal = new LocalSocket();
		isConnected = false;
		rcb = cb;
		socketName = sn;
		address = new LocalSocketAddress(sn, LocalSocketAddress.Namespace.RESERVED);
		new ConnectSocketThread().start();
	}

	public SocketClientBase(String ip, int port){
		isConnected = false;
		server_ip = ip;
		this.port = port;
		socket_type = SOCKET_REMOTE;
		socketName = server_ip + ":" + port;
		Log.i(TAG, "ppt, in SocketClientBase, remote, socketName: " + socketName);
		new ConnectSocketThread().start();

	}
	/*
	//public void setcallbackHandler(Handler h){
	public void setCallBackHandler(Handler h){
		mMessageHandler = h;
	}
	*/
	public String sendMsg(String msg) {
		long startMs = System.currentTimeMillis();
		if(!isConnected){
			return "";
		}
		while (!isConnected) {

			if(System.currentTimeMillis() - startMs > 3000){
				Log.e("SocketClient", "ppt, in sendMsg, connected timeout.");
				return "no Connected";
			}
			else{
				try {
					//Log.e("SocketClient", "socket ppt, in sendMsg, waiting conneted 100ms.");
					Thread.sleep(100);
				}catch (InterruptedException e) {
					e.printStackTrace();
					return "no Connected";
				}
			}
		}
		try {
			PrintWriter out;
			if(socket_type == SOCKET_LOCAL) {
				out = new PrintWriter(clientLocal.getOutputStream());
			}
			else{
				out = new PrintWriter(client_remote.getOutputStream());
			}
			out.println(msg);
			out.flush();
			return "";
//			return in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Nothing return";
	}

	private class ConnectSocketThread extends Thread {
		@Override
		public void run() {
			new ReceiveSocketThread().start();
			try{
				if(clientLocal.isConnected() || isConnected == true)
				{
					Log.i(TAG, "ppt, in ConnectSocketThread, close socket.");
					clientLocal.close();
				}
			}catch (Exception e){
				isConnected = false;
				e.printStackTrace();
			}
			
			while (true/*!isConnected && connetTime <= 10*/) {

				if(!clientLocal.isConnected() || isConnected == false) {
					isConnected = false;
					Log.i(TAG, "ppt, in ConnectSocketThread, connecting to " + socketName);
					try {
						if (socket_type == SOCKET_LOCAL) {

							clientLocal.connect(address);
							clientLocal.setSoTimeout(3000);
							inputStream = clientLocal.getInputStream();
							OutputStream os = clientLocal.getOutputStream();
							dateOutputStream = new DataOutputStream(os);
						} else if (socket_type == SOCKET_REMOTE) {
							client_remote = new Socket(server_ip, port);
							OutputStream os = client_remote.getOutputStream();
							dateOutputStream = new DataOutputStream(os);
						}
						Log.i(TAG, "ppt, in ConnectSocketThread, " + socketName + " is connected");
						isConnected = true;
					}catch (Exception e){
						connetTime++;
						isConnected = false;
						e.printStackTrace();
					}
				}
				/*else {
					if (socket_type == SOCKET_REMOTE) {
						try {
							client_remote.sendUrgentData(0xFF);
						}catch (Exception e){
							e.printStackTrace();
							isConnected = false;
						}
					}
					else if (socket_type == SOCKET_LOCAL && !clientLocal.isConnected()) {
						Log.e(TAG, "ppt, localsocket disconnect, reconnect....");
						isConnected = false;
					}
				}*/
				try {
					sleep(1000);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public DataOutputStream getDataOutputStream(){
		return dateOutputStream;
	}
	
    public boolean getConnected(){
    	return clientLocal.isConnected() && isConnected;
		//return isConnected;
		/*
		if(socket_type == SOCKET_LOCAL)
		{
			return isConnected;
		}
		else{
			return client_remote.isConnected();
		}
		*/
	}
/*
	private void receiveMessage(){
		Message message = Message.obtain();
		message.obj = buffer;
		message.what = REC_MESSAGE_PROCESS;
		mMessageHandler.sendMessage(message);
	}
*/
	private class ReceiveSocketThread extends Thread{
		@Override
		public void run() {
			while(true){
				try {
					if (!getConnected()) {
						Thread.sleep(1000);
						continue;
					}
					rcb.receiveMessage();
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
					
			}
			/*
			byte buffer[] = new byte[maxDateLen];

			int rec;
			int length;

			

			while (true) {
				try {
					if (!getConnected()) {
						Thread.sleep(1000);
						continue;
					}
					rec = inputStream.read(buffer, 0, maxDateLen);
					Log.i(TAG, "ppt, rec data from " + socketName + ", len: " + rec);
					if(rec == -1){
						isConnected = false;
						closeSocket();
					}
					if (rec <= 0) {
						Thread.sleep(10);
						continue;
					}
					
					processMessage(buffer);
				}catch (SocketTimeoutException e) {
					Log.i(TAG, "socket ppt, SocketTimeoutException...");
				}
				catch (IOException e) {
					//closeSocket();
					//isConnected = false;
					e.printStackTrace();
					//return;
				}catch (InterruptedException e) {
					e.printStackTrace();
					//return;
				}
			}
			*/
		}
	}
	
	public void closeSocket() {
		try {
			Log.i("SocketClient", "SocketClient ppt, in closeSocket, go to close.\n");
			if(socket_type == SOCKET_LOCAL){
				if(clientLocal != null){
				    clientLocal.close();
					isConnected = false;
				}
			}
			else{
				if(client_remote != null){
					client_remote.close();
				}
			}
			//client = null;
			//sendMsg("");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
