package com.mohsen.almusalli;

import android.app.*;
import android.content.*;
import android.os.Build;

public final class AlarmScheduler {
    static PendingIntent pending(Context c, String name) {
        Intent i=new Intent(c, PrayerAlarmReceiver.class).putExtra("prayer", name);
        int flags=PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0);
        return PendingIntent.getBroadcast(c, name.hashCode(), i, flags);
    }
    public static void schedule(Context c, String name, long when) {
        if (when <= System.currentTimeMillis()) return;
        c.getSharedPreferences("prayer_alarms",0).edit().putLong(name,when).apply();
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi=pending(c,name);
        if (Build.VERSION.SDK_INT>=23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
        else am.setExact(AlarmManager.RTC_WAKEUP,when,pi);
    }
    public static void cancel(Context c,String name) {
        ((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).cancel(pending(c,name));
        c.getSharedPreferences("prayer_alarms",0).edit().remove(name).apply();
    }
    public static void restore(Context c) {
        for(String name:c.getSharedPreferences("prayer_alarms",0).getAll().keySet()) {
            long t=c.getSharedPreferences("prayer_alarms",0).getLong(name,0);
            if(t<=System.currentTimeMillis()) t+=86400000L;
            schedule(c,name,t);
        }
    }
}
