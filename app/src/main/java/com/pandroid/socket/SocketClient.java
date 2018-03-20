package com.pandroid.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	private LocalSocket client;
	private LocalSocketAddress address;
	private boolean isConnected = false;
	private int connetTime = 1;

	public SocketClient() {
		client = new LocalSocket();
		address = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED);
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
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream());
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
			while (!isConnected && connetTime <= 10) {
				try {
					sleep(1000);
					Log.i("SocketClient","Try to connect socket;ConnectTime:"+connetTime);
					client.connect(address);
					isConnected = true;

				} catch (Exception e) {
					connetTime++;
					isConnected = false;
					Log.i("SocketClient","Connect fail");
				}
			}
		}
	}

	/**

	 */
	public void closeSocket() {
		try {
			Log.i("SocketClient", "SocketClient ppt, in closeSocket, go to close.\n");

			client.close();
			//client = null;
			//sendMsg("");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
