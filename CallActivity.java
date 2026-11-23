package com.musalli.app;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

public class CallActivity extends Activity {
    private Ringtone ringtone; private TextToSpeech tts;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags( android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON );
        String name=getIntent().getStringExtra("name"); if(name==null) name="الصلاة";
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setGravity(Gravity.CENTER); root.setPadding(32,50,32,40); root.setBackgroundColor(0xFF07130F);
        TextView brand=new TextView(this); brand.setText("المُصلّي"); brand.setTextColor(0xFFD7B96E); brand.setTextSize(22); brand.setGravity(Gravity.CENTER);
        TextView title=new TextView(this); title.setText("حان وقت صلاة\n"+name); title.setTextColor(-1); title.setTextSize(34); title.setGravity(Gravity.CENTER); title.setPadding(0,70,0,18);
        TextView sub=new TextView(this); sub.setText("حيّ على الصلاة • حيّ على الفلاح"); sub.setTextColor(0xFFB9C9C0); sub.setTextSize(16); sub.setGravity(Gravity.CENTER);
        Button answer=new Button(this); answer.setText("الاستماع إلى التنبيه"); answer.setTextSize(17); answer.setAllCaps(false); answer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1E9B69)); answer.setTextColor(-1);
        Button end=new Button(this); end.setText("إنهاء"); end.setAllCaps(false); end.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF26352F)); end.setTextColor(-1);
        root.addView(brand); root.addView(title); root.addView(sub); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,150); p.setMargins(0,90,0,14); root.addView(answer,p); root.addView(end,new LinearLayout.LayoutParams(-1,120)); setContentView(root);
        try { Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); if(uri==null) uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); ringtone=RingtoneManager.getRingtone(this,uri); if(ringtone!=null) ringtone.play(); } catch(Exception ignored){}
        answer.setOnClickListener(v->{ stopRing(); answer.setEnabled(false); tts=new TextToSpeech(this,status->{ if(status==TextToSpeech.SUCCESS){ tts.setLanguage(new Locale("ar")); tts.speak("حان وقت الصلاة المحددة، تقبل الله منا ومنكم صالح الأعمال",TextToSpeech.QUEUE_FLUSH,null,"prayer_phrase"); } }); });
        end.setOnClickListener(v->{ stopRing(); if(tts!=null) tts.stop(); finish(); });
    }
    private void stopRing(){ try{if(ringtone!=null&&ringtone.isPlaying())ringtone.stop();}catch(Exception ignored){} }
    @Override protected void onDestroy(){stopRing();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
