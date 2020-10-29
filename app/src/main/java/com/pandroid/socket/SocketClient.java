package com.pandroid.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pandroid.message.MessageTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 */
public class SocketClient {
	private final String TAG = "SocketClient";
	private final String SOCKET_NAME = "zed_task";
	private LocalSocket client_local;
	private Socket client_remote;
	private LocalSocketAddress address;
	private String server_ip;
	private int port;
	private boolean isConnected = false;
	private int connetTime = 1;
	private static int SOCKET_LOCAL = 0;
	private static int SOCKET_REMOTE = 1;
	private int socket_type;

	InputStream inputStream;

	private Handler mMessageHandler;

	public SocketClient() {
		socket_type = SOCKET_LOCAL;
		client_local = new LocalSocket();

		address = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED);
		new ConnectSocketThread().start();
	}

	public SocketClient(String sn) {
		socket_type = SOCKET_LOCAL;
		client_local = new LocalSocket();

		address = new LocalSocketAddress(sn, LocalSocketAddress.Namespace.RESERVED);
		new ConnectSocketThread().start();
	}

	public SocketClient(String ip, int port){

			server_ip = ip;
			this.port = port;
			socket_type = SOCKET_REMOTE;
			new ConnectSocketThread().start();

	}

	public void setcallbackHandler(Handler h){
		mMessageHandler = h;
	}

	public String sendMsg(String msg) {
		long startMs = System.currentTimeMillis();
		if(!isConnected){
			return "";
		}
		while (!isConnected) {

			if(System.currentTimeMillis() - startMs > 3000){
				Log.e("SocketClient", "socket ppt, in sendMsg, waiting conneted timeout.");
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
			//return "Connect fail";
		}
		try {
			PrintWriter out;
			if(socket_type == SOCKET_LOCAL) {
				out = new PrintWriter(client_local.getOutputStream());
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

	/**

	 */
	private class ConnectSocketThread extends Thread {
		@Override
		public void run() {
			new ReceiveSocketThread().start();
			while (true/*!isConnected && connetTime <= 10*/) {

				if(!isConnected) {
					Log.i("SocketClient", "Try to connect socket;ConnectTime:" + connetTime);
					try {
						if (socket_type == SOCKET_LOCAL) {
							if(client_local.isConnected())
							{
								client_local.close();
							}

							client_local = new LocalSocket();


							client_local.connect(address);
							client_local.setSoTimeout(3000);

							inputStream = client_local.getInputStream();
						} else if (socket_type == SOCKET_REMOTE) {
							Log.i(TAG, "socket ppt, in ConnectSocketThread, connect to " + server_ip + ": " + port);

							client_remote = new Socket(server_ip, port);
							inputStream = client_remote.getInputStream();
							Log.i(TAG, "socket ppt, in ConnectSocketThread, SOCKET_REMOTE connect ok");
						}

						isConnected = true;

					}catch (Exception e){
						connetTime++;
						isConnected = false;
						e.printStackTrace();
						Log.i("SocketClient", "socket ppt, " + ((socket_type == SOCKET_LOCAL)?"SOCKET_LOCAL":"SOCKET_REMOTE")  + " Connect failed");
					}
				}
				else {
					if (socket_type == SOCKET_REMOTE) {
						try {
							client_remote.sendUrgentData(0xFF);
						}catch (Exception e){
							Log.e(TAG, "socket ppt, tcp socket disconnect, reconnect....");
							isConnected = false;
						}
					}
					else if (socket_type == SOCKET_LOCAL && !client_local.isConnected()) {
						Log.e(TAG, "socket ppt, localsocket disconnect, reconnect....");
						isConnected = false;
					}
				}
				try {
					sleep(3000);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}

    private boolean getConnected(){
		return isConnected;
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

	private void decode_message(byte[] buffer){
		Message message = Message.obtain();
		message.obj = buffer;
		message.what = MessageTask.REC_SOCKET_MESSAGE_PROCESS;
		mMessageHandler.sendMessage(message);
	}

	private class ReceiveSocketThread extends Thread{
		@Override
		public void run() {
			byte buffer[] = new byte[4 * 1024];

			int rec;
			int rec_old = 0;
			int length;

				byte[] data_length = new byte[2];
				byte[] data ;
				while (true) {
					try {
						if (!getConnected()) {
							Thread.sleep(1000);
							continue;
						}
						rec = inputStream.read(data_length, rec_old, 2 - rec_old);
						Log.i(TAG, "socket ppt, rec data header: " + rec);
						if(rec == -1){
							isConnected = false;
							closeSocket();
						}

						if (rec <= 0) {

							Thread.sleep(1000);
							rec_old = 0;
							continue;
						}
						if (rec + rec_old < 2) {
							Log.i(TAG, "socket ppt, rec data head: " + rec);
							rec_old = 1;
							continue;
						}
						length = data_length[0] << 8 | data_length[1];
						data = new byte[length];
						rec = inputStream.read(data);
						if (rec < length) {
							Log.e(TAG, "socket ppt, rec data length less than length[head].");
						}
						decode_message(data);
					}catch (SocketTimeoutException e) {
						Log.i(TAG, "socket ppt, SocketTimeoutException...");
					}
					catch (IOException e) {
//						closeSocket();
//						isConnected = false;

						e.printStackTrace();

						//return;
					}catch (InterruptedException e) {
						e.printStackTrace();
						//return;
					}
				}


		}
	}
	/**

	 */
	public void closeSocket() {
		try {
			Log.i("SocketClient", "SocketClient ppt, in closeSocket, go to close.\n");
			if(socket_type == SOCKET_LOCAL){
				if(client_local != null){
				    client_local.close();
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
