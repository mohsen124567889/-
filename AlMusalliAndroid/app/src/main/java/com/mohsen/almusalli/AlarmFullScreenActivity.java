package com.mohsen.almusalli;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.*;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class AlarmFullScreenActivity extends Activity {
    private TextToSpeech tts;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if(android.os.Build.VERSION.SDK_INT>=27) setShowWhenLocked(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        String prayer=getIntent().getStringExtra("prayer");
        if(prayer==null) prayer="الصلاة";
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setGravity(Gravity.CENTER); root.setPadding(32,48,32,48); root.setBackgroundColor(Color.rgb(5,42,31));
        TextView mark=new TextView(this); mark.setText("۞"); mark.setTextSize(58); mark.setTextColor(Color.rgb(218,181,96)); mark.setGravity(Gravity.CENTER);
        TextView title=new TextView(this); title.setText("حان وقت صلاة "+prayer); title.setTextSize(28); title.setTextColor(Color.WHITE); title.setTypeface(null,Typeface.BOLD); title.setGravity(Gravity.CENTER);
        TextView sub=new TextView(this); sub.setText("الصلاة خيرٌ من النوم والعمل"); sub.setTextSize(17); sub.setTextColor(Color.rgb(218,205,166)); sub.setGravity(Gravity.CENTER);
        Button answer=new Button(this); answer.setText("استجابة — تقبّل الله منا ومنكم صالح الأعمال");
        Button close=new Button(this); close.setText("إغلاق التنبيه");
        root.addView(mark); root.addView(title); root.addView(sub);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,38,0,12); root.addView(answer,p); root.addView(close);
        setContentView(root);
        tts=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS){tts.setLanguage(new Locale("ar"));}}, "com.google.android.tts");
        answer.setOnClickListener(v->{if(tts!=null) tts.speak("حان وقت الصلاة المحددة، تقبل الله منا ومنكم صالح الأعمال",TextToSpeech.QUEUE_FLUSH,null,"prayer"); finish();});
        close.setOnClickListener(v->finish());
    }
    @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
