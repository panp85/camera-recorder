package com.pandroid.spreadtrum;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedOutputStream;

/**
 * Created by pc on 2018/5/14.
 */

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    CommandThread mCmdThread;
    Handler mHandler;
    socket_connect     socket_xml = null;
    TextView mAtResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(com.pandroid.spreadtrum.R.layout.layout_spr);
        //setToolbarTabLayout();
        //initCoordinatorAndTabLayout();
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.e("TAG", "result:" + msg.getData().getString("result"));
                mAtResult.setText(msg.getData().getString("result"));
                mAtResult.postInvalidate();


                if(msg.getData().getString("result").indexOf("COPS:") != -1){

                }
            }
        };

        try{

            BufferedOutputStream bos = new BufferedOutputStream(openFileOutput( "output.xml",MODE_PRIVATE));

            socket_xml = new socket_connect(mHandler, bos);


        }catch (Exception e){

        }
        //mCmdThread = new CommandThread();
        setView();
    }
    public void setView(){
        final EditText atEdit=(EditText)findViewById(R.id.atcommand);
        mAtResult=(TextView)findViewById(R.id.result);
        final Button sendButton=(Button)findViewById(R.id.button_send);

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String atcomand=atEdit.getText().toString();
                Log.i("At","At command：" + atcomand);
                (new CommandThread(mHandler, atcomand)).sendAt();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

        }
    }

    @Override
    protected void onDestroy() {
        //mIsFirst = false;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
