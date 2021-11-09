package com.reactlibrary;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.Collections;
import java.util.List;

public class ReactNativeSendSmsModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private static String LOG_TAG = "CLEAR_PRO_ANDROID_DEBUG";
    private Promise sendSmsPromise = null;
    private SentSMSObserver smsContentObserver = null;

    public ReactNativeSendSmsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ReactNativeSendSms";
    }

    public void sendCallback(Boolean isErrorState, String errorMsg){
        if(this.sendSmsPromise != null){
            if(isErrorState){
                this.sendSmsPromise.reject(errorMsg, new Exception(errorMsg));
            }else{
                this.sendSmsPromise.resolve("SMS Sent successfully");
            }
        }
    }

    private void sendSingleSMSUtil(SmsManager smsManager, String toAddress, String textMessage, Promise promise) {
        try {
            PendingIntent sentIntent = PendingIntent.getBroadcast(getReactApplicationContext(), 0, new Intent("SENDING_SMS"), 0);
            this.smsContentObserver = new SentSMSObserver(reactContext, this, Collections.singletonMap("phone_number", toAddress));
            this.smsContentObserver.start();
            this.sendSmsPromise = promise;
            smsManager.sendTextMessage(toAddress, null, textMessage, sentIntent, null);
        } catch (RuntimeException error) {
            promise.reject(error.getMessage(), error);
            return;
        }
    }

    private void sendSMSUtil(SmsManager smsManager, ReadableArray toAddresses, String textMessage,Boolean withSentFeedback, Promise promise) {
        if (toAddresses.size() == 0) {
            promise.resolve(null);
            return;
        }
        try {
            PendingIntent sentIntent = PendingIntent.getBroadcast(getReactApplicationContext(), 0, new Intent("SENDING_SMS"), 0);
            for (int i = 0; i < toAddresses.size(); i++) {
                String phoneNumber = toAddresses.getString(i);
                smsManager.sendTextMessage(phoneNumber, null, textMessage, sentIntent, null);
            }
            promise.resolve("SMS Sent successfully");
        } catch (RuntimeException error) {
            promise.reject(error.getMessage(), error);
            return;
        }
    }


    @ReactMethod
    public void sendBulkSMSDefault(ReadableArray toAddresses, String messageText, Boolean withSentFeedback,Promise promise) {
        sendSMSUtil(SmsManager.getDefault(), toAddresses, messageText,withSentFeedback, promise);
    }

    @ReactMethod
    public void sendSingleSMSDefault(String toAddress, String messageText, Promise promise) {
        sendSingleSMSUtil(SmsManager.getDefault(), toAddress, messageText, promise);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @ReactMethod
    public void sendBulkSMS(ReadableArray toAddresses, int subscriptionId, String messageText, Boolean withSentFeedback, Promise promise) {
        sendSMSUtil(SmsManager.getSmsManagerForSubscriptionId(subscriptionId), toAddresses, messageText, withSentFeedback, promise);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @ReactMethod
    public void sendSingleSMS(String toAddress, int subscriptionId, String messageText,  Promise promise) {
        sendSingleSMSUtil(SmsManager.getSmsManagerForSubscriptionId(subscriptionId), toAddress, messageText,  promise);
    }

    @TargetApi(Build.VERSION_CODES.P)
    @ReactMethod
    public void getActiveSubscriptionInfo(Promise promise) {
        try {
            SubscriptionManager subscriptionManager = (SubscriptionManager) reactContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            WritableArray subscriptionArray = Arguments.createArray();
            if(subscriptionInfoList != null && subscriptionInfoList.size() > 0){
                for(int index=0; index< subscriptionInfoList.size(); index++){
                    WritableMap subscriptionInfo = Arguments.createMap();
                    subscriptionInfo.putString("phoneNumber", subscriptionInfoList.get(index).getNumber());
                    subscriptionInfo.putInt("subscriptionId", subscriptionInfoList.get(index).getSubscriptionId());
                    subscriptionInfo.putString("carrierName", (String) subscriptionInfoList.get(index).getCarrierName());
                    subscriptionInfo.putInt("simSlotIndex", subscriptionInfoList.get(index).getSimSlotIndex());
                    subscriptionArray.pushMap(subscriptionInfo);
                }
            }
            promise.resolve(subscriptionArray);
        }catch(RuntimeException error){
            promise.reject("Error while getting subscription info", error);
        }
    }
}
