package com.daoyintech.facelib;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.idl.face.platform.FaceConfig;
import com.baidu.idl.face.platform.FaceEnvironment;
import com.baidu.idl.face.platform.FaceSDKManager;
import com.baidu.idl.face.platform.LivenessTypeEnum;
import com.baidu.idl.face.platform.listener.IInitCallback;
import com.baidu.idl.face.platform.model.ImageInfo;
import com.daoyintech.facelib.ui.FaceDetectActivity;
import com.daoyintech.facelib.ui.FaceLivenessActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 * Created by.
 */
public class FaceModel extends UniModule {

    private static final String TAG = "FaceModel";
    public static final int RESULT_OK = 20;
    public static List<LivenessTypeEnum> livenessList = new ArrayList<>();
    public final static int REQUEST_CODE = 100;
    public static ArrayList<String> base64ImageCropArray = new ArrayList<>();
    public static ArrayList<String> base64ImageSrcMapArray = new ArrayList<>();
    private UniJSCallback uniJSCallback;

    /**
     * 活体检测
     *
     * @param options
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void faceLiveness(JSONObject options, final UniJSCallback callback) {
        uniJSCallback = callback;
        String licenselId = options.getString("licenselId");
        String licenseFileName = options.getString("licenseFileName");
        setConfig();
        JSONArray livenessList = options.getJSONArray("livenessList");
        if (livenessList.size() < 2) {
            HashMap<String, Object> invokeData = new HashMap<>();
            invokeData.put("result", "fail");
            invokeData.put("message", "至少需要两个动作！");
            Log.d(TAG, "动作数量太少");
            callback.invoke(invokeData);
            return;
        }
        addActionLive(livenessList);
        FaceSDKManager.getInstance().initialize(this.mUniSDKInstance.getContext(), licenselId, licenseFileName, new IInitCallback() {
            @Override
            public void initSuccess() {
                Log.d(TAG, "initSuccess: 初始化成功");
                if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                    ((Activity) mUniSDKInstance.getContext()).startActivityForResult(new Intent(mUniSDKInstance.getContext(), FaceLivenessActivity.class), REQUEST_CODE);
                }
            }

            @Override
            public void initFailure(int i, String s) {
                HashMap<String, Object> invokeData = new HashMap<>();
                invokeData.put("result", "fail");
                invokeData.put("message", "初始化:" + s);
                callback.invoke(invokeData);
            }
        });
    }

    /**
     * 人脸采集
     *
     * @param options
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void faceDetect(JSONObject options, final UniJSCallback callback) {
        uniJSCallback = callback;
        String licenselId = options.getString("licenselId");
        String licenseFileName = options.getString("licenseFileName");
        setConfig();
        addActionLive(options.getJSONArray("livenessList"));
        FaceSDKManager.getInstance().initialize(this.mUniSDKInstance.getContext(), licenselId, licenseFileName, new IInitCallback() {
            @Override
            public void initSuccess() {
                if (FaceModel.this.mUniSDKInstance.getContext() instanceof Activity) {
                    Activity uniActivity = (Activity) FaceModel.this.mUniSDKInstance.getContext();
                    ((Activity) mUniSDKInstance.getContext()).startActivityForResult(new Intent(uniActivity, FaceDetectActivity.class), REQUEST_CODE);
                }
            }

            @Override
            public void initFailure(int i, String s) {

            }
        });
    }

    // 设置人脸识别参数
    private void setConfig() {
        FaceConfig config = FaceSDKManager.getInstance()
                .getFaceConfig();
        // 设置可检测的最小人脸阈值
        config.setMinFaceSize(FaceEnvironment.VALUE_MIN_FACE_SIZE);
        // 设置可检测到人脸的阈值
        config.setNotFaceValue(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
        // 设置模糊度阈值
        config.setBlurnessValue(FaceEnvironment.VALUE_BLURNESS);
        // 设置光照阈值（范围0-255）
        config.setBrightnessValue(FaceEnvironment.VALUE_BRIGHTNESS);
        // 设置遮挡阈值
        config.setOcclusionValue(FaceEnvironment.VALUE_OCCLUSION);
        // 设置人脸姿态角阈值
        config.setHeadPitchValue(FaceEnvironment.VALUE_HEAD_PITCH);
        config.setHeadYawValue(FaceEnvironment.VALUE_HEAD_YAW);
        // 设置闭眼阈值
        config.setEyeClosedValue(FaceEnvironment.VALUE_CLOSE_EYES);
        // 设置图片缓存数量
        config.setCacheImageNum(FaceEnvironment.VALUE_CACHE_IMAGE_NUM);
        // 设置活体动作，通过设置list，LivenessTypeEunm.Eye, LivenessTypeEunm.Mouth,
        // LivenessTypeEunm.HeadUp, LivenessTypeEunm.HeadDown, LivenessTypeEunm.HeadLeft,
        // LivenessTypeEunm.HeadRight, LivenessTypeEunm.HeadLeftOrRight
        config.setLivenessTypeList(livenessList);
        // 设置动作活体是否随机
        config.setLivenessRandom(false);
        // 设置开启提示音
        config.setSound(true);
        // 原图缩放系数
        config.setScale(FaceEnvironment.VALUE_SCALE);
        // 抠图高的设定，为了保证好的抠图效果，我们要求高宽比是4：3，所以会在内部进行计算，只需要传入高即可
        config.setCropHeight(FaceEnvironment.VALUE_CROP_HEIGHT);
        // 加密类型，0：Base64加密，上传时image_sec传false；1：百度加密文件加密，上传时image_sec传true
        config.setSecType(FaceEnvironment.VALUE_SEC_TYPE);
        FaceSDKManager.getInstance().setFaceConfig(config);
    }

    // 设置人脸识别动作
    private void addActionLive(JSONArray livenessList) {
        FaceModel.livenessList.clear();
        // 根据需求添加活体动作
        Object[] objects = livenessList.toArray();
        for (int i = 0; i < objects.length; i++) {
            LivenessTypeEnum livenessTypeEnum = LivenessTypeEnum.valueOf((String) objects[i]);
            FaceModel.livenessList.add(livenessTypeEnum);
        }
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            int currentLivenessCount = data.getIntExtra("currentLivenessCount", 0);
            HashMap<String, Object> invokeData = new HashMap<>();
            invokeData.put("base64ImageCropMap", base64ImageCropArray);
            invokeData.put("base64ImageSrcMap", base64ImageSrcMapArray);
            invokeData.put("currentLivenessCount", currentLivenessCount);
            invokeData.put("result", "ok");
            invokeData.put("message", "成功");
            System.out.println(invokeData);
            uniJSCallback.invoke(invokeData);
        } else {

        }
    }
}
