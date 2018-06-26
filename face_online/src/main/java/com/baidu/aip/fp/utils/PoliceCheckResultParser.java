/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.aip.fp.exception.FaceException;
import com.baidu.aip.fp.model.LivenessVsIdcardResult;
import com.baidu.ocr.sdk.exception.OCRError;

import android.util.Log;

public class PoliceCheckResultParser implements Parser<LivenessVsIdcardResult> {

    @Override
    public LivenessVsIdcardResult parse(String json) throws FaceException {

        Log.i("PoliceCheckResultParser", "LivenessVsIdcardResult->" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                FaceException error = new FaceException(jsonObject.optInt("error_code"),
                        jsonObject.optString("error_msg"));
                throw error;
            }

            LivenessVsIdcardResult result = new LivenessVsIdcardResult();
            result.setLogId(jsonObject.optLong("log_id"));
            result.setJsonRes(json);

            result.setScore(jsonObject.optDouble("result"));
            result.setIdcardImage(jsonObject.optString("matting_image"));

            JSONObject extInfoObject = jsonObject.optJSONObject("ext_info");
            if (extInfoObject != null) {
                double faceliveness =  extInfoObject.optDouble("faceliveness");
                result.setFaceliveness(faceliveness);
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            FaceException error = new FaceException(FaceException.ErrorCode.JSON_PARSE_ERROR,
                    "Json parse error:" + json, e);
            throw error;
        }
    }
}
