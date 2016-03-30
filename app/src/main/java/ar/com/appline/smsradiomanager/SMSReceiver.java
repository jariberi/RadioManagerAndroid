package ar.com.appline.smsradiomanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;

public class SMSReceiver extends BroadcastReceiver {
    public SMSReceiver() {
        Log.i("APPLINE-DEBUG","appline-debug");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] messages = (Object[]) bundle.get("pdus");
        SmsMessage[] sms = new SmsMessage[messages.length];
        //Create messages for each incoming PDU
        for (int n = 0; n < messages.length; n++) {
            sms[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
        }
        for (SmsMessage msg : sms) {
            abortBroadcast();
            Toast.makeText(context, "Nuevo mensaje recibido", Toast.LENGTH_SHORT).show();
            try {
                ApiRestAccess put = ApiRestUtil.putSMS(new Date(System.currentTimeMillis()),msg.getOriginatingAddress(), msg.getMessageBody());
                put.execute();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

