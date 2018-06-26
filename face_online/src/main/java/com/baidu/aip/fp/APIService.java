/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp;

import java.io.File;

import com.baidu.aip.fp.model.AccessToken;
import com.baidu.aip.fp.model.DynamicParams;
import com.baidu.aip.fp.model.LivenessVsIdcardResult;
import com.baidu.aip.fp.utils.HttpUtil;
import com.baidu.aip.fp.utils.OnlineLivenessResultParser;
import com.baidu.aip.fp.utils.PoliceCheckResultParser;

import android.content.Context;

/**
 * Created by wangtianfei01 on 17/7/13.
 */

public class APIService {


    // 获取在线活体检测和公安接口使用的token， client_id为ak， client_secret为sk，为了安全起见，请把ak，sk放在自己的服务端去获取token
    private static final String ACCESS_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";

    // 在线活体接口
    private static final String ONLINE_LIVENESS_URL = "https://aip.baidubce.com/rest/2.0/face/v2/faceverify";
    // 公安接口
    private static final String LIVENESS_VS_IDCARD_URL = "https://aip.baidubce.com/rest/2.0/face/v2/person/verify";

    private String accessToken;

    private APIService() {

    }

    private static volatile APIService instance;

    public static APIService getInstance() {
        if (instance == null) {
            synchronized (APIService.class) {
                if (instance == null) {
                    instance = new APIService();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        HttpUtil.getInstance().init();
    }

    /**
     * 设置accessToken 如何获取 accessToken 详情见:
     *
     * @param accessToken accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 明文aksk获取token
     *
     * @param listener 回调
     * @param ak       apiKey
     * @param sk       secretKey
     */
    public void initAccessTokenWithAkSk(final OnResultListener<AccessToken> listener, String ak,
                                        String sk) {

        StringBuilder sb = new StringBuilder();
        sb.append("client_id=").append(ak);
        sb.append("&client_secret=").append(sk);
        sb.append("&grant_type=client_credentials");
        HttpUtil.getInstance().getAccessToken(listener, ACCESS_TOKEN_URL, sb.toString());

    }

    /**
     * 在线活体检测接口，放在视频伪造，传活体得的图片，
     * @param listener
     * @param file
     */
    public void onlineLiveness(OnResultListener listener, File file) {
        DynamicParams params = new DynamicParams();
        params.putParam("face_fields", "qualities,faceliveness");
        params.putFile("image", file);
        OnlineLivenessResultParser parser = new OnlineLivenessResultParser();
        HttpUtil.getInstance().post(urlAppendCommonParams(ONLINE_LIVENESS_URL), "image", params, parser, listener);
    }

    public void policeVerify(String name, String idnumber, String file, OnResultListener<LivenessVsIdcardResult>
            listener) {
        DynamicParams params = new DynamicParams();
        params.putParam("name", name);
        params.putParam("id_card_number", idnumber);
        params.putParam("ext_fields", "qualities,faceliveness");
        params.putParam("quality", "use");
        params.putParam("faceliveness", "use");
        params.putFile("image", new File(file));


        PoliceCheckResultParser parser = new PoliceCheckResultParser();

        HttpUtil.getInstance().post(urlAppendCommonParams(LIVENESS_VS_IDCARD_URL), "image", params, parser, listener);
    }

    /**
     * URL append access token，sdkversion，aipdevid
     *
     * @param url
     * @return
     */
    private String urlAppendCommonParams(String url) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?access_token=").append(accessToken);

        return sb.toString();
    }

}
