package com.pandroid.lc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.ALARM_SERVICE;

public class LcReceiver extends BroadcastReceiver
{
    void startCaptureService(Context context) {
        Intent intent = new Intent(context, capture.class);
        context.startService(intent);
    }
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // TODO Auto-generated method stub
        Log.i("ppt", "ppt receiver onclock......................");

        String msg = intent.getStringExtra("msg");
        //Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
        startCaptureService(context);
        //创建Intent对象，action为ELITOR_CLOCK，附加信息为字符串“你该打酱油了”
        Intent intent1 = new Intent("ELITOR_CLOCK");
        intent.putExtra("msg","capture");

        //定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
        //也就是发送了action 为"ELITOR_CLOCK"的intent
        PendingIntent pi = PendingIntent.getBroadcast(context.getApplicationContext(),0,intent1,0);

        //AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
        AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);

        //设置闹钟从当前时间开始，每隔5s执行一次PendingIntent对象pi，注意第一个参数与第二个参数的关系
        // 5秒后通过PendingIntent pi对象发送广播
        //am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),5*1000,pi);
        //am.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+30*1000,pi);
        try {
            Thread.sleep(3000);
            Log.e("ppt", "ppt receiver after sleep");
        }catch(Exception e) {

        }
    }
}