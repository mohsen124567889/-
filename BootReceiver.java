package com.musalli.app;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.json.JSONArray;
public class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c, Intent i) {
  try { String raw=c.getSharedPreferences("musalli",Context.MODE_PRIVATE).getString("prayers","[]"); PrayerAlarmReceiver.scheduleAll(c,new JSONArray(raw)); } catch(Exception ignored) {}
 }
}
