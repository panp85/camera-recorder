package com.android.message.swiftp.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

import net.vrallev.android.cat.Cat;

import java.net.InetAddress;

import com.pandroid.ftp.swiftp.FsService;
import com.pandroid.ftp.swiftp.FsSettings;
import com.pandroid.R;

@RequiresApi(api = Build.VERSION_CODES.N)
public class FsTileService extends TileService {

    @Override
    public void onClick() {
        if(getQsTile().getState() == Tile.STATE_INACTIVE)
            sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
        else if(getQsTile().getState() == Tile.STATE_ACTIVE)
            sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
    }

    @Override
    public void onStartListening() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FsService.ACTION_STARTED);
        intentFilter.addAction(FsService.ACTION_STOPPED);
        intentFilter.addAction(FsService.ACTION_FAILEDTOSTART);
        registerReceiver(mFsActionsReceiver, intentFilter);
        updateTileState();
    }

    @Override
    public void onStopListening() {
        unregisterReceiver(mFsActionsReceiver);
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (FsService.isRunning()) {
            tile.setState(Tile.STATE_ACTIVE);
            // Fill in the FTP server address
            InetAddress address = FsService.getLocalInetAddress();
            if (address == null) {
                Cat.v("Unable to retrieve wifi ip address");
                tile.setLabel(getString(R.string.swiftp_name));
                return;
            }
            tile.setLabel(address.getHostAddress() + ":" + FsSettings.getPortNumber());
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel(getString(R.string.swiftp_name));
        }
        tile.updateTile();
    }

    BroadcastReceiver mFsActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTileState();
        }
    };
}
