package com.pandroid.socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pandroid.R;

public class ASocketActivity extends Activity {
	private SocketClient socketClient = null;
	private static String INFO;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_socket);
		Log.i("ASocketActivity", "Sleep waiting for service socket start");
		socketClient = new SocketClient();
	}

	/**
	 
	 */
	public void sendName(View view) {
		String name = ((TextView) this.findViewById(R.id.editText1)).getText().toString();
		if (name == "") {
			Toast.makeText(this, getResources().getString(R.string.nameNull), Toast.LENGTH_SHORT).show();
			return;
		}
		String sex = ((Spinner) this.findViewById(R.id.spinner1)).getSelectedItem().toString();
		INFO = sex+ "." + name ;
		new SocketThread(handler).start();
	}

	private class SocketThread extends Thread {
		private Handler handler;

		public SocketThread(Handler handler) {
			super();
			this.handler = handler;
		}

		@Override
		public void run() {
			Log.i("ASocketActivity", "SocketThead wait for result.");
			String result = getSocketClient().sendMsg(INFO);
			Log.i("ASocketActivity", "Socket reslut:" + result);
			Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putString("result", result);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String result = msg.getData().getString("result");
			Log.i("ASocketActivity", "Handler result:" + result);
			((TextView) ASocketActivity.this.findViewById(R.id.tvResult)).setText(result);
		};
	};

	@Override
	protected void onDestroy() {
		if (socketClient != null) {
			getSocketClient().closeSocket();
		}
		super.onDestroy();
	}

	public SocketClient getSocketClient() {
		if (socketClient == null) {
			socketClient = new SocketClient();
		}
		return socketClient;
	}

}