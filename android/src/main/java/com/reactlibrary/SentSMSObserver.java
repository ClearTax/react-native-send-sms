package com.reactlibrary;

import android.content.Context;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.net.Uri;
import android.database.Cursor;
import android.os.Looper;

import com.facebook.react.bridge.ReadableMap;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SentSMSObserver extends ContentObserver {

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Uri uri = Uri.parse("content://sms/");

    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_TYPE = "type";
    private static final String[] PROJECTION = { COLUMN_ADDRESS, COLUMN_TYPE };
    private static final int MESSAGE_TYPE_ALL = 0;
    private static final int MESSAGE_TYPE_INBOX = 1;
    private static final int MESSAGE_TYPE_SENT = 2;
    private static final int MESSAGE_TYPE_DRAFT = 3;
    private static final int MESSAGE_TYPE_OUTBOX = 4;
    private static final int MESSAGE_TYPE_FAILED = 5; //failed outgoing messages
    private static final int MESSAGE_TYPE_QUEUED = 6; //queued to send later

    private ReactNativeSendSmsModule module;
    private ContentResolver resolver = null;
    private List<String> successTypes;
    private Map<String, Integer> types;
    private Timer sentSMSStatusWaitTimer = null;
    private String recipientAddress = null;

    class RemindTask extends TimerTask {
        public void run() {
            messageError("Times up");
            stop();
            sentSMSStatusWaitTimer.cancel(); //Terminate the timer thread
        }
    }

    public SentSMSObserver(Context context, ReactNativeSendSmsModule module, Map<String, String> options) {
        super(handler);

        types = new HashMap<>();
        types.put("all", MESSAGE_TYPE_ALL);
        types.put("inbox", MESSAGE_TYPE_INBOX);
        types.put("sent", MESSAGE_TYPE_SENT);
        types.put("draft", MESSAGE_TYPE_DRAFT);
        types.put("outbox", MESSAGE_TYPE_OUTBOX);
        types.put("failed", MESSAGE_TYPE_FAILED);
        types.put("queued", MESSAGE_TYPE_QUEUED);

        this.successTypes = Arrays.asList("sent", "queued", "outbox");
        this.module = module;
        this.resolver = context.getContentResolver();
        this.sentSMSStatusWaitTimer = new Timer();
        this.recipientAddress = options.get("phone_number");
    }

    public void start() {
        if (resolver != null) {
            resolver.registerContentObserver(uri, true, this);
            sentSMSStatusWaitTimer.schedule(new RemindTask(), 3000);
        }
        else {
            throw new IllegalStateException("Current SmsSendObserver instance is invalid");
        }
    }

    public void stop() {
        if (resolver != null) {
            resolver.unregisterContentObserver(this);
        }
    }

    private void messageSuccess() {
        module.sendCallback(false, "");
        stop();
    }

    private void messageError(String errMsg) {
        module.sendCallback(true, errMsg);
        stop();
    }

    @Override
    public void onChange(boolean selfChange) {

        Cursor cursor = null;

        try {

            cursor = resolver.query(uri, PROJECTION, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                if(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS) ) == this.recipientAddress) {
                    //loop through provided success types
                    boolean wasSuccess = false;

                    final int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                    System.out.println("onChange() type: " + type);

                    for (int i = 0; i < successTypes.size(); i++) {
                        if (type == types.get(successTypes.get(i))) {
                            wasSuccess = true;
                            break;
                        }
                    }
                    if (wasSuccess) {
                        messageSuccess();
                    } else {
                        messageError("Failed info from content observer");
                    }
                }
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}