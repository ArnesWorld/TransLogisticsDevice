package com.example.arne.translogistics_device;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arne.translogistics_device.Model.DataRecording;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class SendDataActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";
    private static final int REQUEST_BLUETOOTH = 100;
    private static final int DISCOVERY_REQUEST = 300;
    private static final int LOCATION_REQUEST_CODE = 50;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private MyDeviceAdapter adapter;
    private HashMap<String, BluetoothDevice> bluetoothDeviceHashMap;
    private Handler mHandler;
    public Set<BluetoothDevice> pairedDevices;

    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;

    private ListView listViewDevices;
    private ImageButton btnRefresh;
    private ConnectedThread comThread;
    private DataRecording dataRecording;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);

        dataRecording = (DataRecording) getIntent().getSerializableExtra("datarecording");
        Toast.makeText(getApplicationContext(),"Datarexcording id: " + dataRecording.getId() + "package: " + dataRecording.pack.getCompany(), Toast.LENGTH_LONG).show();
        listViewDevices = findViewById(R.id.listViewDevices);
        btnRefresh = findViewById(R.id.btnRefresh);
        // Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDeviceHashMap = new HashMap<>();
        adapter = new MyDeviceAdapter(this, R.layout.device_list_item);
        listViewDevices.setAdapter(adapter);

        findBlueToothDevices();

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.clear();
                findBlueToothDevices();
            }
        });


        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MessageConstants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                       // txtDisplayMsg.setText(readMessage);
                        break;
                    case MessageConstants.MESSAGE_WRITE:
                        break;
                    case MessageConstants.MESSAGE_CONNECTSUCCES:
                        TextView txtDeviceName = findViewById(R.id.txtDeviceName);
                        txtDeviceName.setTextColor(Color.parseColor("#42f44b"));
                        break;
                    case MessageConstants.MESSAGE_DATASENDSUCCES:
                        Toast.makeText(getApplicationContext(), "Data sent succesfully", Toast.LENGTH_SHORT).show();

                }
            }
        };

        setDiscoverability();
    }

    private void setDiscoverability(){
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, DISCOVERY_REQUEST);
    }

    public void connect(BluetoothDevice device){
        if(bluetoothAdapter.getBondedDevices().contains(device)) {
            try {
                BluetoothDevice realDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
                BluetoothSocket socket = realDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                comThread = new ConnectedThread(socket);
                comThread.start();
            } catch (Exception e) {

            }
        }
        else{
            try {
                Thread connThread = new ConnectThread(device);
                connThread.start();

            } catch (Exception e) {

            }
        }
    }

    public void sendData(){
        if(comThread != null){
            try {
                byte[] objBytes = dataRecording.serialize();
                comThread.write(objBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void findBlueToothDevices() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissin granted", Toast.LENGTH_SHORT).show();

            if(bluetoothAdapter == null){

            }
            if(!bluetoothAdapter.isEnabled()){
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_BLUETOOTH);
            }
            else{
                startDiscovery();
            }

        }else { // Else we ask for for it
            String[] permissionRequest = {Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissionRequest, LOCATION_REQUEST_CODE);
            Toast.makeText(getApplicationContext(), "Permissin denied", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RESULT_OK){
            startDiscovery();
        }
        if (requestCode ==  DISCOVERY_REQUEST ){
            if(resultCode == RESULT_CANCELED) {
                Log.d("Main","Discovery aborted by user");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Toast.makeText(getApplicationContext(), "Discovery finished", Toast.LENGTH_SHORT).show();
            }

            else if(action.equals(BluetoothDevice.ACTION_FOUND)){

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                adapter.add(device);
                bluetoothDeviceHashMap.put(deviceName, device);
            }
        }
    };

    private void startDiscovery(){
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        boolean discoverytSuccess = bluetoothAdapter.startDiscovery();
        if(discoverytSuccess){
            Toast.makeText(getApplicationContext(), "Discovery has succesfully startet", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Discovery incountered an error", Toast.LENGTH_SHORT).show();
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("ArneIsAwsome!", uuid);
            } catch (IOException e) {
                Log.e("MAInActivity", "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    comThread = new ConnectedThread(socket);
                    comThread.start();
                } catch (IOException e) {
                    Log.e("MainActivity", "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    // manageMyConnectedSocket(socket);
                    Toast.makeText(getApplicationContext(), "Connection has been established", Toast.LENGTH_SHORT).show();

                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e("MainActivity", "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e("MainActivity", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            // bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                comThread = new ConnectedThread(mmSocket);
                comThread.start();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("MainActivity", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //  manageMyConnectedSocket(mmSocket);
            pairedDevices.add(mmDevice);
          Message msg = mHandler.obtainMessage(MessageConstants.MESSAGE_CONNECTSUCCES);
          msg.sendToTarget();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("", "Could not close the client socket", e);
            }
        }
    }

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
        public static final int MESSAGE_CONNECTSUCCES = 3;
        public static final int MESSAGE_DATASENDSUCCES = 4;

        // ... (Add other message types here as needed.)
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
            Message msg = mHandler.obtainMessage(MessageConstants.MESSAGE_DATASENDSUCCES);
            msg.sendToTarget();
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

}
