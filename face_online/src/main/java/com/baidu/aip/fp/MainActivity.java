///*
// * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
// */
//package com.baidu.aip.fp;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.webkit.WebChromeClient;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import com.baidu.aip.fp.utils.AndroidJSInterface;
//
//
//public class MainActivity extends Activity {
//
//    private WebView webview3;
//    protected Context mContext=MainActivity.this;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_web);
//
//
////        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Intent intent = new Intent(MainActivity.this, IdCardActivity.class);
////                startActivity(intent);
////
////                // 调转到活体识别界面
//////                Intent faceIntent = new Intent(MainActivity.this, FaceOnlineVerifyActivity.class);
//////                startActivity(faceIntent);
////                goLogin();
////            }
////        });
//        webview3 = (WebView) findViewById(R.id.web);
//        init();
////        goLogin();
//    }
//    String url="";
//    @SuppressLint({ "JavascriptInterface", "NewApi" })
//    private void init() {
//        registerBoradcastReceiver();
//        // TODO Auto-generated method stub
//        webview3.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//
//        webview3.addJavascriptInterface(new AndroidJSInterface(mContext), "android");
//        webview3.getSettings().setDisplayZoomControls(false);
//        webview3.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        webview3.getSettings().setSupportZoom(true);
//        webview3.getSettings().setDomStorageEnabled(true);
//        webview3.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        webview3.getSettings().setPluginState(WebSettings.PluginState.ON);
//        webview3.requestFocus();
//        webview3.getSettings().setUseWideViewPort(true);
//         webview3.getSettings().setLoadWithOverviewMode(true);
//        webview3.getSettings().setSupportZoom(true);
//        webview3.getSettings().setBuiltInZoomControls(true);
//        WebSettings webSettings = webview3.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setAllowFileAccess(true);
//        webSettings.setBuiltInZoomControls(true);
//        webview3.setWebChromeClient(new WebChromeClient());
//        url = "file:///android_asset/test.html";
//        webview3.loadUrl(url);
//        webview3.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                super.onPageStarted(view, url, favicon);
//             }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view,final String url) {
//                view.loadUrl(url);
//                return true;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//
//            }
//
//            @Override
//            public void onReceivedError(WebView view, int errorCode,
//                                        String description, String failingUrl) {
//                super.onReceivedError(view, errorCode, description, failingUrl);
//                view.loadUrl("www.dsadasd");
//
//
//            }
//        });
//    }
//
//    @Override
//    protected void onResume() {
//        // TODO Auto-generated method stub
//        super.onResume();
//    }
//    public void registerBoradcastReceiver(){
//        IntentFilter myIntentFilter = new IntentFilter();
//        myIntentFilter.addAction("androidcallgetinfo");
//        //注册广播
//        registerReceiver(mBroadcastReceiver, myIntentFilter);
//    }
//    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if(action.equals("androidcallgetinfo")){
//
//                Log.v("aaa","dasdasdsad");
//                webview3.loadUrl("javascript:androidcalljs()");
//            }
//        }
//
//    };
//}
