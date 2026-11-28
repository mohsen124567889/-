package com.mohsen.almusalli;

import android.app.*;
import android.content.*;
import android.os.Build;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String name=intent.getStringExtra("prayer");
        if(name==null) name="الصلاة";
        Intent full=new Intent(context,AlarmFullScreenActivity.class).putExtra("prayer",name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent fullPi=PendingIntent.getActivity(context, name.hashCode()+1000, full, PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(context,"prayer_alerts"):new Notification.Builder(context);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("حان وقت صلاة "+name)
          .setContentText("حيّ على الصلاة — تقبّل الله طاعتكم")
          .setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_MAX)
          .setVisibility(Notification.VISIBILITY_PUBLIC).setAutoCancel(true)
          .setFullScreenIntent(fullPi,true).setContentIntent(fullPi)
          .addAction(android.R.drawable.ic_menu_view,"استجابة",fullPi);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(name.hashCode(),b.build());
        long next=System.currentTimeMillis()+86400000L;
        AlarmScheduler.schedule(context,name,next);
    }
}
