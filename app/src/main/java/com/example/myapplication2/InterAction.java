package com.example.myapplication2;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
//用于实现人机交互功能的类
public class InterAction {
    private Context mContext;
    private Toast mToast;
    //构造函数传入上下文环境
    public InterAction(Context context){
        this.mContext = context;
    }
    //弹出提示框
    public void showToast(String text){
        if(mToast == null){
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
        }else{
            mToast.setText(text);
        }
        mToast.show();
    }
    //弹出提示对话框
    public void showDialog(String text){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle("提示");//设置对话框标题

        builder.setMessage(text);//设置对话框文本内容

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext, "确定啦", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext, "取消啦", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();
    }
}
