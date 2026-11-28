package com.mohsen.almusalli;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmFullScreenActivity extends Activity {
    private static final long CALL_TIMEOUT_MS = 60_000L;
    private final Handler handler = new Handler();
    private Ringtone ringtone;
    private TextToSpeech tts;
    private TextView clock;
    private TextView status;
    private boolean finished = false;
    private String prayer = "الصلاة";

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            if (clock != null && !finished) {
                clock.setText(new SimpleDateFormat("hh:mm:ss a", new Locale("ar")).format(new Date()));
                handler.postDelayed(this, 1000L);
            }
        }
    };
    private final Runnable timeout = () -> closeCall();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (Build.VERSION.SDK_INT >= 27) setShowWhenLocked(true);
        if (Build.VERSION.SDK_INT >= 27) setTurnScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        prayer = getIntent().getStringExtra("prayer");
        if (prayer == null || prayer.trim().isEmpty()) prayer = "الصلاة";
        String action = getIntent().getStringExtra("call_action");

        buildCallScreen();
        if ("answer".equals(action)) {
            markPrayerCompleted();
            showAnswered();
            return;
        }
        if ("ignore".equals(action)) {
            closeCall();
            return;
        }

        startAlarmTone();
        clockTick.run();
        handler.postDelayed(timeout, CALL_TIMEOUT_MS);
    }

    private void buildCallScreen() {
        final int bg = Color.rgb(5, 29, 22);
        final int panel = Color.rgb(15, 53, 40);
        final int gold = Color.rgb(218, 181, 96);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 34, 28, 34);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(16, 61, 44), bg}));

        TextView brand = new TextView(this);
        brand.setText("۞  المُصلّي");
        brand.setTextColor(gold);
        brand.setTextSize(22);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setGravity(Gravity.CENTER);

        TextView avatar = new TextView(this);
        avatar.setText("۞");
        avatar.setTextColor(gold);
        avatar.setTextSize(58);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(panel);
        avatarBg.setStroke(2, gold);
        avatar.setBackground(avatarBg);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(132, 132);
        avatarParams.setMargins(0, 42, 0, 22);
        avatar.setLayoutParams(avatarParams);

        TextView caller = new TextView(this);
        caller.setText("المُصلّي");
        caller.setTextColor(Color.WHITE);
        caller.setTextSize(30);
        caller.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        caller.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("تنبيه الصلاة • " + prayer);
        subtitle.setTextColor(gold);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 0);

        TextView title = new TextView(this);
        title.setText("حان وقت صلاة " + prayer);
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 32, 0, 6);

        clock = new TextView(this);
        clock.setTextColor(Color.rgb(207, 220, 210));
        clock.setTextSize(19);
        clock.setGravity(Gravity.CENTER);
        clock.setPadding(0, 6, 0, 8);

        status = new TextView(this);
        status.setText("يرنّ المُصلّي لتذكيرك بوقت الصلاة");
        status.setTextColor(Color.rgb(177, 197, 184));
        status.setTextSize(14);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, 32);

        Button answer = new Button(this);
        answer.setText("ردّ  •  تمّت الصلاة");
        answer.setTextColor(Color.rgb(5, 35, 24));
        answer.setTextSize(17);
        answer.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        answer.setBackground(roundDrawable(Color.rgb(76, 181, 133), 28));
        Button ignore = new Button(this);
        ignore.setText("إغلاق / تجاهل");
        ignore.setTextColor(Color.WHITE);
        ignore.setTextSize(16);
        ignore.setBackground(roundDrawable(Color.rgb(65, 83, 74), 28));

        root.addView(brand);
        root.addView(avatar);
        root.addView(caller);
        root.addView(subtitle);
        root.addView(title);
        root.addView(clock);
        root.addView(status);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, -2);
        buttonParams.setMargins(0, 8, 0, 10);
        root.addView(answer, buttonParams);
        root.addView(ignore, buttonParams);
        setContentView(root);

        answer.setOnClickListener(v -> {
            markPrayerCompleted();
            stopAlarmTone();
            showAnswered();
        });
        ignore.setOnClickListener(v -> closeCall());
    }

    private GradientDrawable roundDrawable(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private void startAlarmTone() {
        try {
            String chosen = getSharedPreferences("prayer_audio", MODE_PRIVATE).getString("adhan_uri", null);
            Uri sound = chosen == null ? null : Uri.parse(chosen);
            if (sound == null) sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (sound == null) sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, sound);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    ringtone.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
                }
                if (Build.VERSION.SDK_INT >= 28) ringtone.setLooping(true);
                ringtone.play();
            }
        } catch (Exception ignored) { }
    }

    private void markPrayerCompleted() {
        String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        getSharedPreferences("prayer_completed", MODE_PRIVATE).edit()
                .putBoolean(day + "_" + prayer, true)
                .putLong(day + "_" + prayer + "_at", System.currentTimeMillis())
                .apply();
    }

    private void showAnswered() {
        if (finished) return;
        stopAlarmTone();
        handler.removeCallbacks(timeout);
        handler.removeCallbacks(clockTick);
        if (status != null) status.setText("جزاك الله خيرًا • تم تسجيل استجابتك");
        if (tts == null) {
            tts = new TextToSpeech(this, result -> {
                if (result == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("ar"));
                    tts.speak("تقبل الله منا ومنكم صالح الأعمال", TextToSpeech.QUEUE_FLUSH, null, "answered");
                }
            });
        } else {
            tts.speak("تقبل الله منا ومنكم صالح الأعمال", TextToSpeech.QUEUE_FLUSH, null, "answered");
        }
        handler.postDelayed(this::closeCall, 2200L);
    }

    private void stopAlarmTone() {
        try { if (ringtone != null && ringtone.isPlaying()) ringtone.stop(); } catch (Exception ignored) { }
    }

    private void closeCall() {
        if (finished) return;
        finished = true;
        stopAlarmTone();
        handler.removeCallbacks(timeout);
        handler.removeCallbacks(clockTick);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(prayer.hashCode());
        finishAndRemoveTask();
    }

    @Override protected void onDestroy() {
        stopAlarmTone();
        handler.removeCallbacks(timeout);
        handler.removeCallbacks(clockTick);
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}