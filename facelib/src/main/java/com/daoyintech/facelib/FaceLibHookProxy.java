package com.daoyintech.facelib;

import android.app.Application;

import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.common.WXException;

import io.dcloud.feature.uniapp.UniAppHookProxy;

/**
 * Created by.
 */
public class FaceLibHookProxy implements UniAppHookProxy {
    @Override
    public void onSubProcessCreate(Application application) {

    }

    @Override
    public void onCreate(Application application) {
        try {
            WXSDKEngine.registerModule("DD-FaceLib", FaceModel.class);
        } catch (WXException e) {
            e.printStackTrace();
        }
    }
}
