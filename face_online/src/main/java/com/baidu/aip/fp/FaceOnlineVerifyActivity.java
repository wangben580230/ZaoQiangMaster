/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.fp.exception.FaceException;
import com.baidu.aip.fp.model.AccessToken;

import java.io.File;

/**
 * 在线检测活体和公安核实
 */

public class FaceOnlineVerifyActivity extends Activity implements View.OnClickListener{

    public static final int OFFLINE_FACE_LIVENESS_REQUEST = 100;

    private String username;
    private String idnumber;

    private TextView resultTipTV;
    private TextView onlineFacelivenessTipTV;
    private TextView scoreTV;
    private ImageView avatarIv;
    private Button retBtn;
    private String filePath;
    private boolean policeVerifyFinish = false;
    private boolean waitAccesstoken = true;
    private AlertDialog.Builder alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_online_check);

        alertDialog = new AlertDialog.Builder(this);
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            idnumber = intent.getStringExtra("idnumber");
        }

        resultTipTV = (TextView) findViewById(R.id.result_tip_tv);
        onlineFacelivenessTipTV = (TextView) findViewById(R.id.online_faceliveness_tip_tv);
        scoreTV = (TextView) findViewById(R.id.score_tv);
        avatarIv = (ImageView) findViewById(R.id.avatar_iv);
        retBtn = (Button) findViewById(R.id.retry_btn);
        retBtn.setOnClickListener(this);


        initAccessToken();
        // 打开离线活体检测
        Intent faceLivenessintent = new Intent(this, OfflineFaceLivenessActivity.class);
        startActivityForResult(faceLivenessintent, OFFLINE_FACE_LIVENESS_REQUEST);
    }

    @Override
    public void onClick(View v) {
        if (v == retBtn) {
            if (TextUtils.isEmpty(filePath)) {
                finish();
                return;
            }
//            if (TextUtils.isEmpty(APIService.getInstance().getAccessToken())) {
//                initAccessToken();
//            } else {
//                policeVerify(filePath);
//            }
        }
    }
    public static String faceonline_img="";
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 检测完成后 开始在线活体检测和公安核实
        if (requestCode == OFFLINE_FACE_LIVENESS_REQUEST && data != null) {

            filePath = data.getStringExtra("bestimage_path");
            Log.v("aaa","bestimage_path222>>>>"+filePath);
            if (TextUtils.isEmpty(filePath)) {
                Toast.makeText(this, "活体检测失败", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
//            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//            avatarIv.setImageBitmap(bitmap);
//            policeVerify(filePath);
//            Goupload("http://192.168.2.233:8080/HandGovernment/servlet/face",idcard,filePath,"75");
            faceonline_img=filePath;


            //返回到上一个页面
            Intent intent = new Intent();
            intent.putExtra("faceonline", filePath);
            Log.v("aaa","bestimage_path1111>>>>"+filePath);
            setResult(IdCardActivity.REQUEST_CODE_FACEONLINE, intent);
            finish();
        } else {
            finish();
        }
    }

    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title)
                        .setMessage("正常检测...")
                        .show();
            }
        });
    }
    // 在线活体检测和公安核实需要使用该token，为了防止ak、sk泄露，建议在线活体检测和公安接口在您的服务端请求
    private void initAccessToken() {
        APIService.getInstance().init(getApplicationContext());

        displayTip(resultTipTV, "加载中");
        APIService.getInstance().init(getApplicationContext());
        APIService.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                if (result != null && !TextUtils.isEmpty(result.getAccessToken())) {
                    waitAccesstoken = false;
//                    policeVerify(filePath);
                }else if (result != null) {
                    displayTip(resultTipTV, "在线活体token获取失败");
                    retBtn.setVisibility(View.VISIBLE);
                } else {
                    displayTip(resultTipTV, "在线活体token获取失败");
                    retBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(FaceException error) {
                // TODO 错误处理
                displayTip(resultTipTV, "在线活体token获取失败");
                retBtn.setVisibility(View.VISIBLE);
            }
        }, Config.apiKey, Config.secretKey);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Toast.makeText(FaceOnlineVerifyActivity.this,"取消扫描",Toast.LENGTH_SHORT);
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    private void delete() {
        File file = new File(filePath);
        if (file.exists() ) {
            file.delete();
        }
    }

    private void displayTip(final TextView textView, final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (textView != null) {
                    textView.setText(tip);
                }
            }
        });
    }


}

