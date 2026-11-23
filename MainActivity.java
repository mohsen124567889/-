package com.musalli.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView web;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(7,19,15));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(7,19,15));
        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new NativeBridge(), "MusalliNative");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
        requestPermissionsIfNeeded();
        PrayerAlarmReceiver.createChannel(this);
    }
    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 77);
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                try { startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()))); }
                catch (Exception ignored) { }
            }
        }
    }
    public class NativeBridge {
        @JavascriptInterface public void schedule(String json) {
            runOnUiThread(() -> {
                try {
                    JSONArray arr = new JSONArray(json);
                    getSharedPreferences("musalli", MODE_PRIVATE).edit().putString("prayers", arr.toString()).apply();
                    PrayerAlarmReceiver.scheduleAll(MainActivity.this, arr);
                    Toast.makeText(MainActivity.this, "تم تحديث تنبيهات الصلاة", Toast.LENGTH_SHORT).show();
                } catch (Exception e) { Toast.makeText(MainActivity.this, "تعذر حفظ المواقيت", Toast.LENGTH_SHORT).show(); }
            });
        }
        @JavascriptInterface public void openAlarmSettings() {
            runOnUiThread(() -> { if (Build.VERSION.SDK_INT >= 31) startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()))); });
        }
        @JavascriptInterface public String platform() { return "android"; }
    }
    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
