package com.mohsen.almusalli;

import android.app.*;
import android.content.*;
import android.os.Build;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    private PendingIntent screenIntent(Context context, String prayer, String action, int requestCode) {
        Intent i = new Intent(context, AlarmFullScreenActivity.class)
                .putExtra("prayer", prayer)
                .putExtra("call_action", action)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getActivity(context, requestCode, i, flags);
    }

    @Override public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("prayer");
        if (name == null) name = "الصلاة";

        PendingIntent fullPi = screenIntent(context, name, null, name.hashCode() + 1000);
        PendingIntent answerPi = screenIntent(context, name, "answer", name.hashCode() + 2000);
        PendingIntent ignorePi = screenIntent(context, name, "ignore", name.hashCode() + 3000);

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, "prayer_alerts_v2")
                : new Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("المُصلّي")
                .setContentText("حان وقت صلاة " + name)
                .setSubText("الوقت المحدد للصلاة")
                .setCategory(Notification.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_MAX)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(fullPi, true)
                .setContentIntent(fullPi)
                .addAction(android.R.drawable.ic_menu_view, "ردّ", answerPi)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إغلاق", ignorePi);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(name.hashCode(), builder.build());

        try {
            context.startActivity(new Intent(context, AlarmFullScreenActivity.class)
                    .putExtra("prayer", name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } catch (Exception ignored) { }

        // Keep the next reminder scheduled; the app refreshes exact prayer times whenever it updates the timetable.
        AlarmScheduler.schedule(context, name, System.currentTimeMillis() + 24L * 60L * 60L * 1000L);
    }
}