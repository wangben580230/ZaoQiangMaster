/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.fp.utils.CustomDialog;
import com.baidu.aip.fp.utils.FileUtil;
import com.baidu.aip.fp.utils.WeiboDialogUtils;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class IdCardActivity extends Activity {

    public static final int REQUEST_CODE_FACEONLINE = 800;
    public static final int REQUEST_CODE_CAMERA = 102;
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSIONS_REQUEST_CAMERA = 800;
    private static final int PERMISSIONS_EXTERNAL_STORAGE = 801;

    private TextView tipTv;
    private String username = "";
    private String idnumber = "";
    private AlertDialog.Builder alertDialog;
    private boolean mannulInput = false;
    private Dialog mWeiboDialog;
    private  String FaceValue="70";
    String FaceUrl="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard_layout);
        FaceValue=getIntent().getStringExtra("faceValue");
        FaceUrl=getIntent().getStringExtra("faceUrl");
        if(TextUtils.isEmpty(FaceValue)){
            FaceValue="70";
        }
        if (mWeiboDialog!=null&&mWeiboDialog.isShowing()){
            mWeiboDialog.dismiss();
        }
        mWeiboDialog = WeiboDialogUtils.createLoadingDialog(this, "人脸识别初始化");
        setTitle("人脸识别");
        alertDialog = new AlertDialog.Builder(this);
        findView();
        // 初始化OCR SDK 使用的license是aip.license，名字不能修改
        splashHandler.sendEmptyMessageDelayed(2,1000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(IdCardActivity.this,
                    new String[] {Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
            return;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    Handler splashHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what==1){
                AccessToken accessToken = OCR.getInstance().getAccessToken();
                if (accessToken == null || TextUtils.isEmpty(accessToken.getAccessToken())) {
                    initOCRSDK();
                    Toast.makeText(IdCardActivity.this, "OCR token 正在拉取，请稍后再试 ", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Intent intent = new Intent(IdCardActivity.this, CameraActivity.class);
                    // 设置临时存储
                    intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                            FileUtil.getSaveFile(getApplication()).getAbsolutePath());

                    // 调用拍摄身份证正面的activity
                    intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
                    intent.putExtra(CameraActivity.KEY_NATIVE_TOKEN, OCR.getInstance().getLicense());
                    intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE, true);
                    startActivityForResult(intent, REQUEST_CODE_CAMERA);
                }
            }else if(msg.what==2){
                initOCRSDK();
            }
            super.handleMessage(msg);
        }
    };
    private void findView() {

        tipTv = (TextView) findViewById(R.id.tip_tv);
        tipTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                splashHandler.sendEmptyMessage(1);
            }
        });
    }

    private void initOCRSDK() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
                WeiboDialogUtils.closeDialog(mWeiboDialog);
                splashHandler.sendEmptyMessageDelayed(1,0);
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
                WeiboDialogUtils.closeDialog(mWeiboDialog);
                displayToastTip(error.getMessage());
//                displayTip("调用失败，请重新尝试");
            }
        }, getApplicationContext(),Config.apiKey,Config.secretKey);
    }



    // 身份证识别成功后跳转到人脸离线活体检测
    private void jumpToOnlineVerify() {

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(IdCardActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(idnumber)) {
            Toast.makeText(IdCardActivity.this, "身份证不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO 身份证号码合法校验，长度，字母数字

        // 调转到活体识别界面
        Intent faceIntent = new Intent(IdCardActivity.this, FaceOnlineVerifyActivity.class);
        faceIntent.putExtra("username", username);
        faceIntent.putExtra("idnumber", idnumber);
        startActivityForResult(faceIntent, REQUEST_CODE_FACEONLINE);
    }
    String sidcardExpirydate,sidcardsigndate,sidcardbirthday,sidcaeraddress,sidcardgender,sidcardethnic;

    /**
     * 识别身份证
     *
     * @param idCardSide
     * @param filePath
     */
  public static  String sdcard_file;
    private void recIDCard(final String idCardSide, final String filePath) {
//        displayTip("识别中...");
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        param.setIdCardSide(idCardSide);
        param.setDetectDirection(true);
        OCR.getInstance().recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                if (result != null) {
                    WeiboDialogUtils.closeDialog(mWeiboDialog);
                    SharedPreferences 	preferences= getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    Word idnumberWord = result.getIdNumber();
                    Word nameWord = result.getName();
                    Word idcardExpirydate=result.getExpiryDate();
                    Word idcardsigndate=result.getSignDate();
                    String idcardside=result.getIdCardSide();
                    Word idcardbirthday=result.getBirthday();
                    Word idcaeraddress=result.getAddress();
                    Word idcardgender= result.getGender();
                    Word idcardethnic=result.getEthnic();
                    if (idnumberWord != null) {
                        idnumber = idnumberWord.getWords();
                        editor.putString("idcardnumber", idnumber);
                    }
                    if (nameWord != null) {
                        username = nameWord.getWords();
                        editor.putString("idcardusername", username);
                    }
                    if (idcardsigndate != null) {
                        sidcardsigndate = idcardsigndate.getWords();
                        editor.putString("idcardsigndate", sidcardsigndate);
                    }
                    if (idcardExpirydate != null) {
                         sidcardExpirydate = idcardExpirydate.getWords();
                        editor.putString("idcardExpirydate", sidcardExpirydate);
                    }
                    if (idcardside != null) {

                        editor.putString("idcardside", idcardside);
                    }
                    if (idcardbirthday != null) {
                        sidcardbirthday = idcardbirthday.getWords();
                        editor.putString("idcardbirthday", sidcardbirthday);
                    }
                    if (idcaeraddress != null) {
                        sidcaeraddress = idcaeraddress.getWords();
                        editor.putString("idcaeraddress", sidcaeraddress);
                    }
                    if (idcardgender != null) {
                        sidcardgender = idcardgender.getWords();
                        editor.putString("idcardgender", sidcardgender);
                    }
                    if (idcardethnic != null) {
                        sidcardethnic = idcardethnic.getWords();
                        editor.putString("idcardethnic", sidcardethnic);
                    }
                    editor.commit();

                    if(idCardSide.equals(IDCardParams.ID_CARD_SIDE_FRONT)){

                        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(idnumber) ){
//                            alertText("识别结果", "身份证识别出错");
                            showUpdateDialog("人脸识别失败","身份证识别出错");
                        }else{

                            Intent intent = new Intent(IdCardActivity.this, CameraActivity.class);
                            // 设置临时存储
                            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                                    FileUtil.getSaveFile(getApplication()).getAbsolutePath());

                            // 调用拍摄身份证反面的activity
                            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
                            intent.putExtra(CameraActivity.KEY_NATIVE_TOKEN, OCR.getInstance().getLicense());
                            intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE, true);
                            startActivityForResult(intent, REQUEST_CODE_CAMERA);
                        }



                    }else{
//                        Bitmap bmp = BitmapFactory.decodeFile(filePath);
//                        img2.setImageBitmap(bmp);
                        Log.v("aaa",filePath+"/n"+sdcard_file);
                        Log.v("aaa",sidcardsigndate+"/n"+sidcardExpirydate);
                        if (TextUtils.isEmpty(sidcardsigndate) || TextUtils.isEmpty(sidcardExpirydate) ){
//                            alertText("识别结果", "身份证识别出错");
                            showUpdateDialog("人脸识别失败","身份证识别出错");
                        }else{
                            jumpToOnlineVerify();
                        }
                    }
                    Log.v("aaa","num:"+idnumber+"name"+username+"data"+sidcardsigndate);
//                    Bitmap bmp = BitmapFactory.decodeFile(copy(file));
//                    img.setImageBitmap(bmp);

//                    alertText("识别结果", "idnumber->" + idnumber + " name->" + username+"sign"+result.getSignDate()+"re"+result.getExpiryDate());
                }
//                displayTip("");
            }

            @Override
            public void onError(OCRError error) {
                WeiboDialogUtils.closeDialog(mWeiboDialog);
//                alertText("识别结果", "身份证识别出错");
                showUpdateDialog("人脸识别失败","身份证识别出错");

//                displayTip("");
            }
        });
    }

   /* private void alertText(final String title, final String message) {

        alertDialog.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 身份证识别成功后跳转到人脸离线活体检测
//                        jumpToOnlineVerify();
                        dialog.dismiss();
                        finish();
                    }
                }).show();
    }*/
    String idcard_img="";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    Log.v("aaa","Activity.RESULT_OK:"+Activity.RESULT_OK+"resultCode"+resultCode+"RESULT_CANCELED"+RESULT_CANCELED);
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                String filePath = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
                if (!TextUtils.isEmpty(contentType)) {
                    mWeiboDialog = WeiboDialogUtils.createLoadingDialog(this, "正在识别身份证");
                    if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                        sdcard_file=copy(filePath);
                        recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
                    } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                        recIDCard(IDCardParams.ID_CARD_SIDE_BACK, filePath);
                    }
                }
            }else{
                finish();
            }
        }else if(resultCode==Activity.RESULT_CANCELED){
            finish();
        }else if(requestCode == REQUEST_CODE_FACEONLINE){
            if (data != null) {
                String faceonline = data.getStringExtra("faceonline");
                Log.v("aaa","faceonline"+faceonline);
                mWeiboDialog = WeiboDialogUtils.createLoadingDialog(this, "正在人脸核身");
                Goupload(FaceUrl+"/HandGovernment/servlet/face", sdcard_file, faceonline, FaceValue);
            }
        }
    }
    public void Goupload (String uploadHost, String img1,String img2,String str){
        RequestParams params = new RequestParams();
        params.addBodyParameter("idcard",new File( img1));
        params.addBodyParameter("face",new File( img2));
        params.addBodyParameter("similarity_degree",str);
        uploadMethod(params,uploadHost);
    }
    //    localhost:8080/HandGovernment/servlet/face参数 idcard ，face
//    @RequestParam("idcard") MultipartFile file1,@RequestParam("face") MultipartFile file2,
    boolean is_update_ok=false;
    public  void uploadMethod(final RequestParams params, final String uploadHost) {

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST, uploadHost, params,new RequestCallBack<String>() {
            @Override
            public void onStart() {
                //上传開始
//                alertText("人脸识别中","");
            }
            @Override
            public void onLoading(long total, long current,boolean isUploading) {
                //上传中
            }
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //上传成功，这里面的返回值，就是server返回的数据
                //使用 String result = responseInfo.result 获取返回值
                try {
                    WeiboDialogUtils.closeDialog(mWeiboDialog);
                    JSONObject jsonObject = new JSONObject(responseInfo.result);
                    String success = jsonObject.getString("success");
                    Log.v("AAA","上传成功"+responseInfo.result);
                    Log.v("aaa","success"+success);
                    if (success.equals("true")){
                        is_update_ok=true;

//                        alertText("人脸识别成功","");
                        showUpdateDialog("人脸识别成功","");

                    }else{
                        is_update_ok=false;
//                        alertText("人脸识别失败","");
                        showUpdateDialog("人脸识别失败","");
//                        displayTip("人脸识别失败,点击重新识别");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            @Override
            public void onFailure(HttpException error, String msg) {
                //上传失败
                is_update_ok=false;
                WeiboDialogUtils.closeDialog(mWeiboDialog);
                Log.v("AAA","上传失败"+msg.toString());
//                alertText("人脸识别失败","请查看网络");
                showUpdateDialog("人脸识别失败","");}
        });
    }
    public String copy(String FileName) {
// 获取文件名称
        String res = FileName.substring(FileName.lastIndexOf("/") + 1);
// sdcard下保存文件的目录
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/idcard/";
        Log.v("aaa","zou l ma ");
// 文件路径
        String path = dir + res;
        try {
            // 读取文件
            Bitmap bmp = BitmapFactory.decodeFile(FileName);
            File file = new File(dir);
            // 判断目录是否存在
            if (!file.exists())
                // 创建目录
                file.mkdirs();
            file = new File(path);
            // 创建新文件
            file.createNewFile();
            // 创建一个输出流
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            // 在数据缓存中数据满了之后刷新该缓存，
            fos.flush();
            // 关闭流
            fos.close();
            // 返回该文件的最新路径
            return path;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
        private void displayTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tipTv.setText(tip);
            }
        });
    }

    private void displayToastTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(IdCardActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (is_update_ok==true){
            Intent mIntent = new Intent("androidcallgetinfo");
            //发送广播
            sendBroadcast(mIntent);
        }
    }

    private void showUpdateDialog(String title,String msg) {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);


        builder.setMessage(title);
        builder.setTitle(msg);
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 设置你的操作事项
                        finish();

                    }
                });
        builder.create().show();

    }
}
