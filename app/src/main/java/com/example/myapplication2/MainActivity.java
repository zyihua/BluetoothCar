package com.example.myapplication2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.lang.String;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 0;
    private BlueToothController mController = new BlueToothController();//蓝牙适配器
    private DeviceAdapter mAdapter;//设备
    private InterAction mInterAction = new InterAction(this);//交互

    private List<BluetoothDevice> mDeviceList = new ArrayList<>();//存储查找出的设备的列表
    private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();//存储已绑定设备的列表
    private ListView mListView;//显示列表
    private Button mbutton;
    private Button mbutton2;
    private Button mbutton3;
    private Button mbutton4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//将指定的资源xml文件加载到对应的activity中

        initUI();
        Toolbar toolbar = findViewById(R.id.toolbar);//关联menu和toolbar
        setSupportActionBar(toolbar);//用toolbar控件代替actionbar控件
        mbutton = (Button) findViewById(R.id.button);
        mbutton.setOnTouchListener(new mbutton_OnTouchListener());
        mbutton2 = (Button) findViewById(R.id.button2);
        mbutton2.setOnTouchListener(new mbutton2_OnTouchListener());
        mbutton3 = (Button) findViewById(R.id.button3);
        mbutton3.setOnTouchListener(new mbutton3_OnTouchListener());
        mbutton4 = (Button) findViewById(R.id.button4);
        mbutton4.setOnTouchListener(new mbutton4_OnTouchListener());
        //实例化过滤器intentfilter
        IntentFilter filter = new IntentFilter();
        //添加想要监听的广播
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//状态改变
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);// 开始查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//结束查找
        filter.addAction(BluetoothDevice.ACTION_FOUND);//查找设备
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//蓝牙设备连接
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//断开连接
        //动态注册广播，广播跟随程序的生命周期
        registerReceiver(mReceiver, filter);
    }
    //安卓的广播采用观察者模式
    //我们要想使用BroadcastReceiver需要先定义个类继承BroadcastReceiver
    // 然后实现其public void onReceive(Context context, Intent intent)方法
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//从广播获取设备信息
            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED://蓝牙状态改变
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 1000);
                    switch (state) {
                        case BluetoothAdapter.STATE_ON:
                            mInterAction.showDialog("蓝牙已经打开");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            mInterAction.showDialog("蓝牙已经关闭");
                            break;
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED://当广播监听到蓝牙适配器开始查找设备时
                    //初始化数据列表
                    mInterAction.showToast("开始查找设备");
                    mDeviceList.clear();
                    mAdapter.notifyDataSetChanged();//更新
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED://当适配器查找设备结束时，一般为12s
                    mInterAction.showToast("设备扫描结束");
                    break;
                case BluetoothDevice.ACTION_FOUND://当适配器查找到设备时
                    //找到一个，添加一个
                    mInterAction.showToast("找到一个设备");
                    if(!mDeviceList.contains(device)){//去除查找到的重复的设备
                        mDeviceList.add(device);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    mInterAction.showDialog("蓝牙已连接"+device.getName());
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mInterAction.showDialog("已断开连接"+device.getName());
                    break;
            }
        }
    };

    //搜索完设备后要注销广播
    @Override
    protected void onDestroy(){
        //结束MainActivity的生命周期
        super.onDestroy();
        //动态注册的广播一定要取消注册
        unregisterReceiver(mReceiver);
        if (connectThread.msocket != null) {// 关闭连接socket
            try {
                connectThread.msocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void initUI(){
        //listview是一个以垂直方式在项目中显示视图的列表。是一种不能实现确定视图中的内容的适配器视图
        mListView = findViewById(R.id.device_list);//关联list和listView
        mAdapter = new DeviceAdapter(mDeviceList, this);
        mListView.setAdapter(mAdapter);//为ListView设置Adapter来绑定数据
        mListView.setOnItemClickListener(bindDeviceClick);//列表监听点击事件
        mListView.setOnItemClickListener(bindedDeviceClick);
    }

    //创建下拉列表
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //下拉菜单的实现方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.enable_visiblity://设置蓝牙可见
                if(mController.isSupportBlueTooth()){
                    mController.enableVisibly(this);
                    mInterAction.showToast("正在设置可见");
                }else{
                    mInterAction.showToast("设备不支持蓝牙");
                }
                break;
            case R.id.find_device://查找设备
                mAdapter.refresh(mDeviceList);
                mController.findDevice();
                mListView.setOnItemClickListener(bindDeviceClick);//点击listView
                break;
            case R.id.bonded_device://绑定设备
                mBondedDeviceList = mController.getBondedDeviceList();//获取已绑定设备列表
                mAdapter.refresh(mBondedDeviceList);
                mListView.setOnItemClickListener(bindedDeviceClick);
                break;
            case R.id.turnOnBlueTooth://打开蓝牙
                if(!mController.isSupportBlueTooth()){
                    mInterAction.showToast("设备不支持蓝牙");
                }else if(mController.getBlueToothStatus()){
                    mInterAction.showToast("蓝牙已经开启");
                }else{
                    mController.turnOnBlueTooth(this, REQUEST_CODE);
                }
                break;
            case R.id.turnOffBlueTooth://关闭蓝牙
                mController.turnOffBlueTooth();
                break;
            case R.id.disconnect://断开连接
                try{
                    connectThread.msocket.close();
                }catch (IOException e){

                }
                mInterAction.showToast("正在断开与小车的连接");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //实现点击列表中的设备进行绑定功能的函数，也是客户端入口
    private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
            if(mController.mAdapter.isDiscovering()){
                mController.mAdapter.cancelDiscovery();//连接之前需要先取消搜索
            }
            BluetoothDevice device = mDeviceList.get(i);
            device.createBond();//绑定设备
//            mInterAction.showDialog("确定要连接"+device.getName()+"吗");
//            new connectThread(device, connectThread.msocket).start();
//            Sends(1+10);
        }

    };
    //实现点击绑定列表中的设备进行连接的函数
    private AdapterView.OnItemClickListener bindedDeviceClick = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
            if(mController.mAdapter.isDiscovering()){
                mController.mAdapter.cancelDiscovery();//连接之前需要先取消搜索
            }
            BluetoothDevice device = mBondedDeviceList.get(i);
            mInterAction.showDialog("确定要连接"+device.getName()+"吗");
            new connectThread(device, connectThread.msocket).start();
            Sends(1+10);
        }

    };

    public void Sends(int v)
    {
        int i=0;
        int n=0;
        try{
            if(connectThread.msocket != null)
            {
                OutputStream os = connectThread.msocket.getOutputStream();   //蓝牙连接输出流
                os.write(v);
                Log.e("信息已经","发出");
            }
            else
            {
            }

        }catch(IOException e){
        }
    }

    //button的4个按键的监听实现方法
    class mbutton_OnTouchListener implements OnTouchListener//获取按下状态，发送命令，松开发送停止命令
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
            {
                Sends(0x02);//发送前进命令
            }
            if (event.getAction() == MotionEvent.ACTION_UP)//弹起
            {
                Sends(0x01);
            }
            //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
            return false;
        }
    }
    class mbutton2_OnTouchListener implements OnTouchListener//获取按下状态，发送命令，松开发送停止命令
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
            {

                Sends(0x03);//发送后退命令
            }
            if (event.getAction() == MotionEvent.ACTION_UP)//弹起
            {
                Sends(0x01);
            }
            //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
            return false;
        }
    }
    class mbutton3_OnTouchListener implements OnTouchListener//获取按下状态，发送命令，松开发送停止命令
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
            {

                Sends(0x04);//发送左转命令
            }
            if (event.getAction() == MotionEvent.ACTION_UP)//弹起
            {
                Sends(0x01);
            }
            //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
            return false;
        }
    }
    class mbutton4_OnTouchListener implements OnTouchListener//获取按下状态，发送命令，松开发送停止命令
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
            {

                Sends(0x05);//发送右转命令
            }
            if (event.getAction() == MotionEvent.ACTION_UP)//弹起
            {
                Sends(0x01);//发送停止命令
            }
            //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
            return false;
        }
    }

}
