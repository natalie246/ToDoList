package com.nataliemaayan.taskmanager;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;

public class ManageTaskReceiver extends BroadcastReceiver 
{
    public void	onReceive(Context context,	Intent intent)	
    {
    	System.out.println("BroadcastReceiver: onRecive()");

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Get taskId & message
        int taskId = intent.getIntExtra("taskId", 0);
        String notificationText = intent.getStringExtra("taskMessage");

        Intent myIntent = new Intent(context, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

        @SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.ic_launcher, "Task Manager- "+notificationText , System.currentTimeMillis());
        notification.setLatestEventInfo( context,"Task Manager", notificationText, pendingIntent);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(null, taskId, notification); //0 is id
        
        Toast.makeText(context, "Task Manager: " + notificationText, Toast.LENGTH_LONG).show(); 
        
        // Vibrate the mobile phone
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }
}
