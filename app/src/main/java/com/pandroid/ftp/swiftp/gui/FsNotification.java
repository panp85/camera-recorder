/*
Copyright 2011-2013 Pieter Pareit

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pandroid.ftp.swiftp.gui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import net.vrallev.android.cat.Cat;

import java.net.InetAddress;

import com.pandroid.ftp.swiftp.FsService;
import com.pandroid.ftp.swiftp.FsSettings;
import com.pandroid.R;

public class FsNotification extends BroadcastReceiver {

    private final int NOTIFICATION_ID = 7890;

    @Override
    public void onReceive(Context context, Intent intent) {
        Cat.d("onReceive broadcast: " + intent.getAction());
        switch (intent.getAction()) {
            case FsService.ACTION_STARTED:
                setupNotification(context);
                break;
            case FsService.ACTION_STOPPED:
                clearNotification(context);
                break;
        }
    }

    private void setupNotification(Context context) {
        Cat.d("Setting up the notification");
        // Get NotificationManager reference
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);

        // get ip address
        InetAddress address = FsService.getLocalInetAddress();
        if (address == null) {
            Cat.w("Unable to retrieve the local ip address");
            return;
        }
        String iptext = "ftp://" + address.getHostAddress() + ":"
                + FsSettings.getPortNumber() + "/";

        // Instantiate a Notification
        int icon = R.mipmap.notification;
        CharSequence tickerText = String.format(context.getString(R.string.notification_server_starting), iptext);
        long when = System.currentTimeMillis();

        // Define Notification's message and Intent
        CharSequence contentTitle = context.getString(R.string.notification_title);
        CharSequence contentText = String.format(context.getString(R.string.notification_text), iptext);

        Intent notificationIntent = new Intent(context, MainActivity_Ftp.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        int stopIcon = android.R.drawable.ic_menu_close_clear_cancel;
        CharSequence stopText = context.getString(R.string.notification_stop_text);
        Intent stopIntent = new Intent(FsService.ACTION_STOP_FTPSERVER);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0,
                stopIntent, PendingIntent.FLAG_ONE_SHOT);

        int preferenceIcon = android.R.drawable.ic_menu_preferences;
        CharSequence preferenceText = context.getString(R.string.notif_settings_text);
        Intent preferenceIntent = new Intent(context, MainActivity_Ftp.class);
        preferenceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent preferencePendingIntent = PendingIntent.getActivity(context, 0, preferenceIntent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setWhen(when)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(stopIcon, stopText, stopPendingIntent)
                .addAction(preferenceIcon, preferenceText, preferencePendingIntent)
                .setShowWhen(false)
                .build();

        // Pass Notification to NotificationManager
        nm.notify(NOTIFICATION_ID, notification);

        Cat.d("Notification setup done");
    }


    private void clearNotification(Context context) {
        Cat.d("Clearing the notifications");
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);
        nm.cancelAll();
        Cat.d("Cleared notification");
    }
}
