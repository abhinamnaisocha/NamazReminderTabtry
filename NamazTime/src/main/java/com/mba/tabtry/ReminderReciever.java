package com.mba.tabtry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Muhammad Bilal on 18/02/2016.
 */
public class ReminderReciever extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle=intent.getExtras();
        bundle.getString("prayer");
        Intent scheduledIntent = new Intent(context,ReminderActivity.class);
        scheduledIntent.putExtras(bundle);
        scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(scheduledIntent);
    }
}
