// ReactNativeSendSmsModule.java

package com.reactlibrary;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

public class ReactNativeSendSmsModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public ReactNativeSendSmsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ReactNativeSendSms";
    }

    private void sendSMSUtil(SmsManager smsManager, ReadableArray toAddresses, String textMessage, Promise promise){
        if(toAddresses.size() == 0){
            promise.resolve(null);
            return;
        }
        try {
            PendingIntent sentIntent = PendingIntent.getBroadcast(getReactApplicationContext(), 0, new Intent("SENDING_SMS"),0);
            for(int i=0; i< toAddresses.size(); i++ ){
                String phoneNumber = toAddresses.getString(i);
                smsManager.sendTextMessage(phoneNumber, null, textMessage, sentIntent, null);
            }
            promise.resolve("SMS Sent successfully");
        }catch(RuntimeException error){
            promise.reject(error.getMessage());
            return;
        }
    }


    @ReactMethod
    public void sendSMSDefault(ReadableArray toAddresses, String messageText, Promise promise){
        sendSMSUtil(SmsManager.getDefault(), toAddresses, messageText, promise);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @ReactMethod
    public void sendSMS(ReadableArray toAddress, int subscriptionId, String messageText, Promise promise){
        sendSMSUtil(SmsManager.getSmsManagerForSubscriptionId(subscriptionId), toAddress, messageText, promise);
    }

    @TargetApi(Build.VERSION_CODES.P)
    @ReactMethod
    public void getActiveSubscriptionInfo(Promise promise){
        try{
            SubscriptionManager subscriptionManager = (SubscriptionManager) reactContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getAccessibleSubscriptionInfoList();
            WritableArray subscriptionArray = Arguments.createArray();
            if(subscriptionInfoList != null && subscriptionInfoList.size() > 0){
                for(int index=0; index< subscriptionInfoList.size(); index++){
                    WritableMap subscriptionInfo = Arguments.createMap();
                    subscriptionInfo.putString("phone_number", subscriptionInfoList.get(index).getNumber());
                    subscriptionInfo.putInt("subscription_id", subscriptionInfoList.get(index).getSubscriptionId());
                    subscriptionInfo.putString("carrier_name", (String) subscriptionInfoList.get(index).getCarrierName());
                    subscriptionArray.pushMap(subscriptionInfo);
                }
            }
            promise.resolve(subscriptionArray);
        }catch(RuntimeException error){
            promise.reject("Error while getting subscription info", error);
        }
    }
}
