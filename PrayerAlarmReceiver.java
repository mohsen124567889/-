package com.musalli.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL = "prayer_alerts";
    public static void createChannel(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "تنبيهات الصلاة", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("تنبيهات دخول أوقات الصلاة"); ch.enableVibration(true); ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }
    public static void scheduleAll(Context c, JSONArray arr) {
        AlarmManager am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        for (int i=0;i<arr.length();i++) {
            try {
                JSONObject p=arr.getJSONObject(i); int id=p.optInt("id",i+1); if (!p.optBoolean("enabled", true)) { cancel(c, am, id); continue; }
                int hour=p.getInt("hour"), minute=p.getInt("minute");
                Calendar at=Calendar.getInstance(); at.set(Calendar.HOUR_OF_DAY,hour); at.set(Calendar.MINUTE,minute); at.set(Calendar.SECOND,0); at.set(Calendar.MILLISECOND,0);
                if (at.getTimeInMillis() <= System.currentTimeMillis()) at.add(Calendar.DAY_OF_YEAR,1);
                scheduleOne(c, am, id, p.optString("name","الصلاة"), hour, minute, at.getTimeInMillis());
            } catch(Exception ignored) {}
        }
    }
    static void cancel(Context c, AlarmManager am, int id) {
        Intent intent=new Intent(c,PrayerAlarmReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(c,id,intent,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);
        if(pi!=null){ am.cancel(pi); pi.cancel(); }
    }
    static void scheduleOne(Context c, AlarmManager am, int id, String name, int hour, int minute, long when) {
        Intent intent=new Intent(c,PrayerAlarmReceiver.class); intent.putExtra("id",id); intent.putExtra("name",name); intent.putExtra("hour",hour); intent.putExtra("minute",minute);
        PendingIntent pi=PendingIntent.getBroadcast(c,id,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        try {
            if (Build.VERSION.SDK_INT >= 23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
            else am.setExact(AlarmManager.RTC_WAKEUP,when,pi);
        } catch(SecurityException e) { am.set(AlarmManager.RTC_WAKEUP,when,pi); }
    }
    @Override public void onReceive(Context c, Intent in) {
        int id=in.getIntExtra("id",1), hour=in.getIntExtra("hour",0), minute=in.getIntExtra("minute",0);
        String name=in.getStringExtra("name"); if(name==null) name="الصلاة";
        createChannel(c);
        Intent call=new Intent(c,CallActivity.class); call.putExtra("name",name); call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent full=PendingIntent.getActivity(c,id+100,call,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT>=26 ? new Notification.Builder(c,CHANNEL) : new Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("حان وقت صلاة "+name).setContentText("تقبّل الله طاعتكم").setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_MAX).setVisibility(Notification.VISIBILITY_PUBLIC).setAutoCancel(true).setFullScreenIntent(full,true).setContentIntent(full);
        ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id,b.build());
        Calendar next=Calendar.getInstance(); next.set(Calendar.HOUR_OF_DAY,hour); next.set(Calendar.MINUTE,minute); next.set(Calendar.SECOND,0); next.set(Calendar.MILLISECOND,0); next.add(Calendar.DAY_OF_YEAR,1);
        scheduleOne(c,(AlarmManager)c.getSystemService(Context.ALARM_SERVICE),id,name,hour,minute,next.getTimeInMillis());
    }
}
