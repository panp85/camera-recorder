package com.pandroid.luce;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.os.Bundle;

import com.pandroid.R;
import com.pandroid.lc.PhoneInfoThread;
import com.pandroid.socket.MessageReceiverCallback;
import com.pandroid.socket.SocketClientBase;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.pandroid.lc.CellGeneralInfo.NP_CELL_INFO_UPDATE;


public class CellSettingMainActivity extends AppCompatActivity implements MessageReceiverCallback{
    private static final String TAG = "CellSettingMainActivity";
    private PhoneInfoThread phoneInfoThread;
    private  int msgcount;
    public Handler mMainHandler;
	TextView mResult;
	private final String socketName = "set_cell";
	public SocketClientBase socketClient;

    EditText mccEdit;
    EditText mncEdit;
    EditText typeEdit;
    EditText cell1Edit;
    EditText cell2Edit;
    EditText cell3Edit;

	public static final int CMD_SET_SOCKET_DATA = 100;

    private boolean mHasCriticalPermissions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setcell);
        setView();
        mMainHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
            	case CMD_SET_SOCKET_DATA:
            	    try {
						Log.i(TAG, "ppt, in CellSettingMainActivity, will send cellinfo.");
						getDataOutputStream().writeInt(24);
						Log.i(TAG, "ppt, in CellSettingMainActivity, send cellinfo 1.");
						
                        getDataOutputStream().writeInt(1/*Integer.parseInt(mccEdit.getText().toString())*/);
                        getDataOutputStream().writeInt(Integer.parseInt(mncEdit.getText().toString()));
                        getDataOutputStream().writeInt(Integer.parseInt(typeEdit.getText().toString()));
                        getDataOutputStream().writeInt(Integer.parseInt(cell1Edit.getText().toString()));
                        getDataOutputStream().writeInt(Integer.parseInt(cell2Edit.getText().toString()));
						getDataOutputStream().writeInt(1/*Integer.parseInt(cell3Edit.getText().toString())*/);

                        getDataOutputStream().flush();
						Log.i(TAG, "ppt, in CellSettingMainActivity, send cellinfo ok.");
                    }catch(IOException e){
            	        e.printStackTrace();
                    }
					break;
				default:
					Log.i(TAG, "ppt, invalid message.");
					break;
            	}
        	}
        };
		socketClient = new SocketClientBase(socketName, this);
    }

	public DataOutputStream getDataOutputStream()
    {
    	return socketClient.getDataOutputStream();
    }

    public void setView(){
        mccEdit=(EditText)findViewById(R.id.mccvalue);
		mncEdit=(EditText)findViewById(R.id.mncvalue);
		typeEdit=(EditText)findViewById(R.id.typevalue);
		cell1Edit=(EditText)findViewById(R.id.cell1value);
		cell2Edit=(EditText)findViewById(R.id.cell2value);
		cell3Edit=(EditText)findViewById(R.id.cell3value);
        mResult=(TextView)findViewById(R.id.result);
        final Button sendButton=(Button)findViewById(R.id.button_send);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
	            switch (v.getId()){
	            case R.id.button_send:
	            	Message msg = mMainHandler.obtainMessage();
					msg.what = CMD_SET_SOCKET_DATA;
					mMainHandler.sendMessage(msg);
	                //(new CommandThread(mHandler, atcomand)).sendAt();
	                break;
	            default: 
	            	break;
            	}
            }
        });
    }

	public void receiveMessage(){
		
	}
	protected void onStop() {
        Log.i(TAG,"ppt, CellSettingMainActivity, onStop.\n");
        /*
        if(socketClient != null){
			socketClient.closeSocket();
		}
        */
        super.onStop();
    }

    protected void onDestroy(){
        Log.i(TAG,"ppt, CellSettingMainActivity, onStop.\n");
        if(socketClient != null){
            socketClient.closeSocket();
        }
        super.onDestroy();
    }
}
