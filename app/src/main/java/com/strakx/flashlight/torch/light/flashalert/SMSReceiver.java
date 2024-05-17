package com.strakx.flashlight.torch.light.flashalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (smsMessages != null && smsMessages.length > 0) {
                String messageBody = smsMessages[0].getMessageBody();
                // Trigger notification or desired action for incoming message
                // Example: showNotification(context, "New SMS", messageBody);
            }
        }
    }
}
