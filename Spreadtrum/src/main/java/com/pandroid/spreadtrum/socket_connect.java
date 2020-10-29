package com.pandroid.spreadtrum;

/**
 * Created by qingliangjianb on 18-5-30.
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.Buffer;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class socket_connect {

    private final String SOCKET_NAME = "/dev/socket_test0";
    private LocalSocket client;
    private LocalSocketAddress address;
    private boolean isConnected = false;
    private int connetTime = 1;

    private Handler              handler;
    private BufferedOutputStream bos;

    public socket_connect( Handler handler,  BufferedOutputStream bos ) {
        client = new LocalSocket(LocalSocket.SOCKET_SEQPACKET);
        address = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT);

        this.handler = handler;
        this.bos     = bos;

        new ConnectSocketThread().start();
    }

    public String sendMsg(String msg) {
        if (!isConnected) {
            return "Connect fail";
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());
            out.println(msg);
            out.flush();
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Nothing return";
    }


    private class ConnectSocketThread extends Thread {

        //FileOutputStream stream = openFileOutput("cellinfo.data");

        @Override
        public void run() {

            while(true){
                Log.i("SocketClient","ppt while start");
                //connect
                while (!isConnected){

                    try {
                        sleep(1000);
                        Log.i("SocketClient","Try to connect socket;ConnectTime:"+connetTime);
                        client.connect(address);
                        isConnected = true;
                    } catch (Exception e) {
                        connetTime++;
                        isConnected = false;
                        Log.i("SocketClient","Connect fail:" +  e.toString());
                    }
                }

                try {

                    client.setReceiveBufferSize( 4*1024*1024 );

                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    InputStream is = client.getInputStream();
                    byte[] buffer = new byte[4*1024*1024];

                    int recv_len = 0;

                    Log.i("SocketClient", "Start Received xml...\n");

                    while(  ( recv_len = is.read( buffer, 0, 2*1024*1024 ) ) > 0 ) {

                        String content = new String(buffer);
                        content.trim();

                        Log.d("SocketClient", "received content len " + recv_len);
                        Log.i("SocketClient", "received content: \n" +  content );
                        bos.write(buffer, 0, recv_len);

                        //Message msg = new Message();
                        //Bundle bundle = new Bundle();
                        //bundle.putString("xml", content);
                        //msg.setData(bundle);
                        //handler.sendMessage(msg);

                    }

                } catch ( Exception e ){
                    Log.e("SocketClient", "received error" + e );
                }finally {
                    Log.i("SocketClient", "ppt, ok" );
                    //isConnected = false;
                    connetTime = 0;
                }
            }

        }
    }

    public void closeSocket() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}