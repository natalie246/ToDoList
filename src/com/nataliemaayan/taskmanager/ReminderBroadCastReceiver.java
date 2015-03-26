package com.nataliemaayan.taskmanager;

import com.nataliemaayan.taskmanager.MainActivity;
import com.nataliemaayan.taskmanager.R;
import com.nataliemaayan.taskmanager.R.drawable;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

public class ReminderBroadCastReceiver extends BroadcastReceiver {


    public void	onReceive(Context context,	Intent intent)	{
                                System.out.println("BroadcastReceiver: onRecive()");


        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Get taskId & message
        int taskId = intent.getIntExtra("taskId", 0);
        String notificationText = intent.getStringExtra("taskMessage");

        Intent myIntent = new Intent(context, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

        Notification notification = new Notification(R.drawable.ic_launcher, "task2Do-"+notificationText , System.currentTimeMillis());
        notification.setLatestEventInfo( context,"Task2Do", notificationText, pendingIntent);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(null, taskId, notification); //0 is id

        // Vibrate the mobile phone
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }
}
