package com.example.arne.translogistics_device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Arne on 30-03-2018.
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private SendDataActivity mActivity;

    private ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       SendDataActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
      //  this.peers = peers;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
           //     mActivity.setIsWifiP2pEnabled(true);
              //  Toast.makeText(mActivity,"wifi direc is turned on", Toast.LENGTH_LONG).show();
            } else {
             //   mActivity.setIsWifiP2pEnabled(false);
                Toast.makeText(mActivity,"wifi direc is turned off", Toast.LENGTH_LONG).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            Toast.makeText(mActivity,"inside peers changed intent", Toast.LENGTH_SHORT).show();
            if (mManager != null) {
                Toast.makeText(mActivity,"Peers changed", Toast.LENGTH_LONG).show();
                mManager.requestPeers(mChannel, peerListListener);

            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    private PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Toast.makeText(mActivity,"List Found", Toast.LENGTH_LONG).show();
            List<String> deviceNames = new ArrayList<>();
            Collection<WifiP2pDevice> refreshedPeers =  peerList.getDeviceList();
            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);
                Toast.makeText(mActivity,"Inside if", Toast.LENGTH_LONG).show();
                // If an AdapterView is backed by this data, notify it
                // of the change. For instance, if you have a ListView of
                // available peers, trigger an update.
                // ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
               // mActivity.getArrayAdapter().clear();
               // mActivity.getArrayAdapter().addAll(peers);
               // mActivity.getArrayAdapter().notifyDataSetChanged();
                // Perform any other updates needed based on the new list of
                // peers connected to the Wi-Fi P2P network.
            }

            if (peers.size() == 0) {
                Log.d("SendDataActivity", "No devices found");
                Toast.makeText(mActivity,"No peers Found", Toast.LENGTH_LONG).show();
                return;
            }
        }
    };



}
