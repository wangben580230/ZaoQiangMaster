//package com.baidu.aip.fp.utils;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
//
//public class BoradCast_Faceonline extends BroadcastReceiver {
//
//
//    public BoradCast_Faceonline() {
//        Log.v("ceshi", "go_login_myBroadCast");
//    }
//    String FaceValue="";
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        // TODO Auto-generated method stub
//        FaceValue=intent.getStringExtra("faceValue");
//        Log.v("ceshi", "onReceive"+FaceValue);
////        Intent it=new Intent(context,MainActivity.class);
////         it.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
////				| Intent.FLAG_ACTIVITY_NEW_TASK);
////         it.putExtra("faceValue",FaceValue);
////        context.startActivity(it);
//    }
//
//}