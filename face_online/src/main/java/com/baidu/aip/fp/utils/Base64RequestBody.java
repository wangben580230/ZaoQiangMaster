/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Map;

import android.util.Base64;
import android.util.Log;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class Base64RequestBody extends RequestBody {

    private static final MediaType CONTENT_TYPE =
            MediaType.parse("application/x-www-form-urlencoded");

    private static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";

    private FileBase64Encoder encoder = new FileBase64Encoder();

    private Map<String, File> fileMap;
    private Map<String, String> stringParams;
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public void setFileParams(Map<String, File> files) {
        fileMap = files;
    }

    public void setStringParams(Map<String, String> params) {
        this.stringParams = params;
    }

    @Override
    public MediaType contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        int count = 0;
        StringBuilder sb = new StringBuilder();

        boolean debug = true;

        if (stringParams != null) {
            for (String key : stringParams.keySet()) {
                if (count++ > 0) {
                    sink.writeByte('&');
                    if (debug) {
                        sb.append("&");
                    }

                }
                key = Util.canonicalize(key, FORM_ENCODE_SET, false, false);
                sink.writeUtf8(key);
                sink.writeByte('=');
                sink.writeUtf8(Util.canonicalize(stringParams.get(key), FORM_ENCODE_SET, false, false));

                if (debug) {
                    sb.append(key);
                    sb.append('=');
                    sb.append(Util.canonicalize(stringParams.get(key), FORM_ENCODE_SET, false, false));
                }
            }
        }

     //   byte[] encoded;
        if (fileMap != null && fileMap.size() > 0) {
            if (count++ > 0) {
                sink.writeByte('&');

                sb.append("&");
            }
            String key = Util.canonicalize(this.key, FORM_ENCODE_SET, false, false);
            sink.writeUtf8(key);
            sink.writeByte('=');

            sb.append(key);
            sb.append('=');

            int num = 0;
            for (String k : fileMap.keySet()) {
                num++;
                encoder.setInputFile(fileMap.get(k));

                File file = fileMap.get(k);

                byte[] buf = readFile(file);

                byte[] base = Base64.encode(buf, Base64.NO_WRAP);

                sink.writeUtf8(Util.canonicalize(new String(base), FORM_ENCODE_SET, false, false));
                sb.append(Util.canonicalize(new String(base), FORM_ENCODE_SET, false, false));

                if (num < fileMap.size()) {
                    sink.writeByte(',');
                    sink.flush();

                    sb.append(",");
                }
            }
        }

        sink.close();
    }

    public static byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength) {
                throw new IOException("File size >= 2 GB");
            }
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
}
