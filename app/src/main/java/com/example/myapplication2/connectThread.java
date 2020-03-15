package com.example.myapplication2;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class connectThread extends Thread {
    private BluetoothDevice mdevice;
    private String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static BluetoothSocket msocket;//蓝牙socket
    public connectThread(BluetoothDevice device, BluetoothSocket socket){
        this.mdevice = device;
        this.msocket = socket;
    }

    public void run(){

        try{
            msocket = mdevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            msocket.connect();
            Log.e("socket","已连接");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
