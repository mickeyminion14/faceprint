package com.orah.faceprint;

import androidx.appcompat.app.AppCompatActivity;
import me.aflak.bluetooth.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {
    private boolean found = false;
    private Button btn_sendData;
    private BluetoothDevice foundDevice = null;
    private BluetoothConnector.BluetoothSocketWrapper wrapper = null;
    private TextView textView;
    private BluetoothAdapter mBluetoothAdapter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("MENIME", "Started Discovery");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("MENIME", "Finished Discovery");
                if(found) {
                    BluetoothConnector bc = new BluetoothConnector(foundDevice, true, mBluetoothAdapter, null);
                    try {
                        wrapper = bc.connect();
                        Log.d("MENIME","CONNECTED TO TARGET DEVICE");
                        Toast.makeText(context, "connected to "+foundDevice.getName(), Toast.LENGTH_SHORT).show();
                        btn_sendData.setVisibility(View.VISIBLE);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }


                    found = false;

                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                Toast.makeText(context, device.getName().toString(), Toast.LENGTH_SHORT).show();

                if(device.getName().toString().equalsIgnoreCase("faceprint")) {
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d("MENIME","INTENDED DEVICE FOUND");
                    found = true;
                    foundDevice = device;

//                    ConnectThread ct = new ConnectThread(device ,mBluetoothAdapter);
                }
                Log.d("MENIME", device.getName());


            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        try {
            wrapper.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        textView = (TextView) findViewById(R.id.textview);
        btn_sendData=findViewById(R.id.btn_reconnect);
        btn_sendData.setVisibility(View.GONE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device Not Supported", Toast.LENGTH_SHORT).show();
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 0);
            }

            IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);

            mBluetoothAdapter.startDiscovery();

        }
        btn_sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject o=new JSONObject();
                try {
                    o.accumulate("username","test");
                    wrapper.getOutputStream().write(o.toString().getBytes(Charset.forName("UTF-8")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
