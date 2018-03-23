package com.pandroid.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 */
public class SocketClient {
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

	public SocketClient() {
		socket_type = SOCKET_LOCAL;
		client_local = new LocalSocket();
		address = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED);
		new ConnectSocketThread().start();
	}

	public SocketClient(String ip, int port){

			server_ip = ip;
			this.port = port;
			socket_type = SOCKET_REMOTE;
			new ConnectSocketThread().start();

	}


	public String sendMsg(String msg) {
		long startMs = System.currentTimeMillis();
		while (!isConnected) {

			if(System.currentTimeMillis() - startMs > 3000){
				Log.e("SocketClient", "socket ppt, in sendMsg, waiting conneted timeout.");
				return "no Connected";
			}
			else{
				try {
					Log.e("SocketClient", "socket ppt, in sendMsg, waiting conneted 100ms.");
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
			while (!isConnected /*&& connetTime <= 10*/) {
				try {
					sleep(1000);
					Log.i("SocketClient","Try to connect socket;ConnectTime:"+connetTime);
					if(socket_type == SOCKET_LOCAL){
					    client_local.connect(address);
					}
					else if (socket_type == SOCKET_REMOTE){
						client_remote = new Socket(server_ip, port);
					}
					isConnected = true;

				} catch (Exception e) {
					connetTime++;
					isConnected = false;
					Log.i("SocketClient","Connect fail");
				}
			}
		}
	}

    private boolean getConnected(){
		if(socket_type == SOCKET_LOCAL)
		{
			return isConnected;
		}
		else{
			return client_remote.isConnected();
		}
	}

	private void encode_message(byte[] buffer){

	}

	private class ReceiveSocketThread extends Thread{
		@Override
		public void run() {
			byte buffer[] = new byte[4 * 1024];
			InputStream inputStream;
			int rec;
			try {
				if (socket_type == SOCKET_LOCAL) {
					inputStream = client_local.getInputStream();
				} else {
					inputStream = client_remote.getInputStream();
				}

				while (getConnected() == true) {
					rec = inputStream.read(buffer);
					if(rec != -1)
					{
						encode_message(buffer);
					}
				}
			}catch (IOException e) {
				e.printStackTrace();
				return;
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
