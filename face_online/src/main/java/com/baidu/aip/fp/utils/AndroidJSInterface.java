package com.baidu.aip.fp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;



import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AndroidJSInterface {

	private Context mContext;
	private String[] imageUrls;
	List<String> str;

	public AndroidJSInterface(Context context) {
		mContext = context;
	}

	@JavascriptInterface
	public void FaceOnline(String FaceValue) {
		Intent intent = new Intent("faceonline");
		intent.putExtra("faceValue",FaceValue);
		mContext.sendBroadcast(intent);
	}


	@JavascriptInterface
	public String getIdcardInfo() {

		SharedPreferences preferences = mContext.getSharedPreferences("user",
				Context.MODE_PRIVATE);
		String idcardusername = preferences.getString("idcardusername", "");
		String idcardnumber = preferences.getString("idcardnumber", "");
		String idcardsigndate = preferences.getString("idcardsigndate", "");
		String idcardExpirydate = preferences.getString("idcardExpirydate", "");
		String idcardside = preferences.getString("idcardside", "");
		String idcardbirthday = preferences.getString("idcardbirthday", "");
		String idcaeraddress = preferences.getString("idcaeraddress", "");
		String idcardgender = preferences.getString("idcardgender", "");
		String idcardethnic = preferences.getString("idcardethnic", "");
		String JS_UserInfo = null ;
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("idcardusername", idcardusername);
			jsonObject.put("idcardnumber", idcardnumber);
			jsonObject.put("idcardsigndate", idcardsigndate);
			jsonObject.put("idcardExpirydate", idcardExpirydate);
			jsonObject.put("idcardside", idcardside);
			jsonObject.put("idcardbirthday",idcardbirthday );
			jsonObject.put("idcaeraddress",idcaeraddress );
			jsonObject.put("idcardgender", idcardgender);
			jsonObject.put("idcardethnic", idcardethnic);
			JS_UserInfo = jsonObject.toString();
		} catch (JSONException e) {
			Log.i("OTH", "JS获取用户信息错误");
		}
		return JS_UserInfo ;

	}


}
