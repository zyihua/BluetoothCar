package com.example.myapplication2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

//蓝牙适配器
public class BlueToothController {
    public BluetoothAdapter mAdapter;
    public BlueToothController(){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    //一，判断当前设备是否支持蓝牙
    public boolean isSupportBlueTooth(){
        if(mAdapter!=null){
            return true;
        }else{
            return false;
        }
    }
    //二，判断蓝牙是否已经打开
    public boolean getBlueToothStatus(){
        if (mAdapter != null){
            return mAdapter.isEnabled();
        }
        return false;
    }
    //三，打开蓝牙
    public void turnOnBlueTooth(Activity activity, int requestCode){
        if(mAdapter != null && !mAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, requestCode);
        }
    }
    //四，关闭蓝牙
    public void turnOffBlueTooth() {
            mAdapter.disable();
    }
    //五，设置蓝牙的可见性
    public void enableVisibly(Context context){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //蓝牙可见时间300秒
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(discoverableIntent);
    }
    //六，查找设备
    public void findDevice(){
        assert (mAdapter != null);
        //如果正在搜索则取消搜索后再搜索
        if(mAdapter.isDiscovering()){
            mAdapter.cancelDiscovery();
        }
        mAdapter.startDiscovery();
    }
    //七，获取绑定设备
    public List<BluetoothDevice> getBondedDeviceList(){
        return new ArrayList<>(mAdapter.getBondedDevices());
    }

}
