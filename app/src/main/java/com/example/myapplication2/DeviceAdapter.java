package com.example.myapplication2;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {
    private List<BluetoothDevice> mData;
    private Context mContext;

    public DeviceAdapter(List<BluetoothDevice> data, Context context){
        mData = data;
        mContext = context.getApplicationContext();
    }

    public int getCount(){
        return mData.size();
    }
    public Object getItem(int i){
        return mData.get(i);
    }
    public long getItemId(int i){
        return i;
    }
    public View getView(int i, View view, ViewGroup viewGroup){
        View itemView = view;
        //复用View，优化性能
        if(itemView == null){
            itemView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2,viewGroup,false);
        }

        TextView line1 =  itemView.findViewById(android.R.id.text1);
        TextView line2 =  itemView.findViewById(android.R.id.text2);

        //获取对应的蓝牙设备
        BluetoothDevice device = (BluetoothDevice) getItem(i);
        //显示名称
        line1.setText(device.getName());
        //显示地址
        line2.setText(device.getAddress());

        return itemView;
    }
    //刷新数据，每传入一个数据，告诉Adapter数据改变
    public void refresh(List<BluetoothDevice> data){
        mData = data;
        notifyDataSetChanged();
    }
}
