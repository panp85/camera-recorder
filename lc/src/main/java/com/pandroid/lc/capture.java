package com.pandroid.lc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class capture extends Service {
    public capture() {
        //getLaunchIntentForPackage();
        //getPackageManager().getInt
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                Log.i("ppt", "onStartCommand......................");
                //创建Intent对象，action为ELITOR_CLOCK，附加信息为字符串“你该打酱油了”
                Intent intent = new Intent("ELITOR_CLOCK");
                intent.putExtra("msg","capture");

                //定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
                //也就是发送了action 为"ELITOR_CLOCK"的intent
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(),0,intent,0);

                //AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
                AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

                //设置闹钟从当前时间开始，每隔5s执行一次PendingIntent对象pi，注意第一个参数与第二个参数的关系
                // 5秒后通过PendingIntent pi对象发送广播
                //am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),5*1000,pi);
                //am.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+5*1000,pi);
                do {
                    System.out.println("ppt, Service is running......");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);
            }
        }.start();
        flags = START_FLAG_RETRY;
        return super.onStartCommand(intent, flags, startId);
    }



}
