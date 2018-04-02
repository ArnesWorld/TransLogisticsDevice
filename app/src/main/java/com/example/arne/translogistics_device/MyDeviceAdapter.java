package com.example.arne.translogistics_device;

import android.bluetooth.BluetoothDevice;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Arne on 31-03-2018.
 */

public class MyDeviceAdapter extends ArrayAdapter<BluetoothDevice> {

    private SendDataActivity context;

    public MyDeviceAdapter(@NonNull SendDataActivity context, int resource) {
        super(context, resource);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // return super.getView(position, convertView, parent);
        ViewHolder viewHolder;
        BluetoothDevice device = getItem(position);
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.device_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            viewHolder.setConnectSendBtnListener(device);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(device.getName() != null){
            viewHolder.txtDeviceName.setText(device.getName());
        }
        else{viewHolder.txtDeviceName.setText("Undefined device");}
        if(context.bluetoothAdapter.getBondedDevices().contains(device)){
            viewHolder.btnConnectSend.setText("Send");
        }

        return convertView;
    }

    private class ViewHolder{
        TextView txtDeviceName;
        Button btnConnectSend;
        public ViewHolder(View v){
            txtDeviceName = v.findViewById(R.id.txtDeviceName);
            btnConnectSend = v.findViewById(R.id.btnConnectSend);

        }

        public void setConnectSendBtnListener(final BluetoothDevice device){
            btnConnectSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(context.bluetoothAdapter.getBondedDevices().contains(device)) {
                        context.sendData(device);
                    }
                    else{context.connect(device);}


                }
            });
        }
    }



}
