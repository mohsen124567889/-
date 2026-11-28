package com.mohsen.almusalli;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.webkit.*;
import android.view.*;
import java.util.Calendar;

public class MainActivity extends Activity {
    private WebView web;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(5,42,31));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(5,42,31));
        web = new WebView(this);
        web.setBackgroundColor(android.graphics.Color.rgb(5,25,20));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setDatabaseEnabled(true);
        web.getSettings().setGeolocationEnabled(true);
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        web.addJavascriptInterface(new NativeBridge(), "AlMusalli");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
        createChannel();
        requestRuntimePermissions();
        requestExactAlarmPermission();
    }
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel("prayer_alerts_v2", "تنبيهات الصلاة والمكالمة", NotificationManager.IMPORTANCE_HIGH);
            c.setDescription("مكالمة تذكير عند دخول وقت الصلاة");
            c.enableVibration(true);
            c.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmTone == null) alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            c.setSound(alarmTone, attrs);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }
    private void requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 41);
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 42);
    }
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                try { startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:"+getPackageName()))); } catch(Exception ignored) {}
            }
        }
        if (Build.VERSION.SDK_INT >= 34 && !getSystemService(NotificationManager.class).canUseFullScreenIntent()) {
            try { startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:"+getPackageName()))); } catch(Exception ignored) {}
        }
    }
    private static final int PICK_ADHAN_AUDIO = 73;
    private void pickAdhanAudio() {
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pick.setType("audio/*");
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        try { startActivityForResult(pick, PICK_ADHAN_AUDIO); } catch (Exception ignored) { }
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ADHAN_AUDIO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) { }
            getSharedPreferences("prayer_audio", MODE_PRIVATE).edit().putString("adhan_uri", uri.toString()).apply();
        }
    }
    public class NativeBridge {
        @JavascriptInterface public void schedulePrayer(String name, long whenMillis) {
            AlarmScheduler.schedule(MainActivity.this, name, whenMillis);
        }
        @JavascriptInterface public void cancelPrayer(String name) { AlarmScheduler.cancel(MainActivity.this, name); }
        @JavascriptInterface public void openAlarmSettings() { runOnUiThread(() -> requestExactAlarmPermission()); }
        @JavascriptInterface public void pickAdhanAudio() { runOnUiThread(() -> MainActivity.this.pickAdhanAudio()); }
        @JavascriptInterface public String platformVersion() { return "Android " + Build.VERSION.RELEASE; }
        @JavascriptInterface public void keepScreenOn(boolean enabled) {
            runOnUiThread(() -> { if(enabled) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); });
        }
    }
    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}