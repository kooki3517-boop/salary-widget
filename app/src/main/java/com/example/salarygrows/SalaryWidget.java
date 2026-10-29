package com.example.salarygrows;
import android.app.*;import android.appwidget.*;import android.content.*;import android.widget.*;import java.time.*;import java.text.*;import java.util.*;import android.graphics.*;import android.util.TypedValue;import android.view.View;
public class SalaryWidget extends AppWidgetProvider {
 public static class Mini extends SalaryWidget {} public static class Wide extends SalaryWidget {} public static class Three extends SalaryWidget {} public static class Four extends SalaryWidget {} public static class Five extends SalaryWidget {}
 public static void refresh(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);for(Class<?> cl:new Class<?>[]{Mini.class,Wide.class,Three.class,Four.class,Five.class}){int[] ids=m.getAppWidgetIds(new ComponentName(c,cl));if(ids.length>0)render(c,m,ids,cl);}}
 static void render(Context c,AppWidgetManager m,int[] ids,Class<?> cl){SharedPreferences p=c.getSharedPreferences("salary",Context.MODE_PRIVATE);boolean transparent=p.getBoolean("transparent_bg",false);int mode=p.getInt("display_mode",0);long target=mode==2?p.getLong("net_salary_amount",p.getLong("net",2200000)):p.getLong("salary_amount",p.getLong("net",2500000));LocalDateTime n=LocalDateTime.now();int days=YearMonth.from(n).lengthOfMonth();double f=((n.getDayOfMonth()-1)*86400d+n.getHour()*3600d+n.getMinute()*60d+n.getSecond())/(days*86400d);int layout=cl==Mini.class?R.layout.salary_widget_mini:cl==Wide.class?R.layout.salary_widget_wide:cl==Three.class?R.layout.salary_widget_three:cl==Four.class?R.layout.salary_widget_four:R.layout.salary_widget_five;for(int id:ids){RemoteViews v=new RemoteViews(c.getPackageName(),layout);if(transparent){v.setInt(R.id.widget_root,"setBackgroundResource",0);v.setInt(R.id.widget_root,"setBackgroundColor",Color.TRANSPARENT);}else{v.setInt(R.id.widget_root,"setBackgroundResource",R.drawable.widget_bg);}v.setTextViewText(R.id.title,"월급이 자라는 중..💵");String amount=(mode==1?"세전 ":mode==2?"세후 ":"")+NumberFormat.getIntegerInstance(Locale.KOREA).format((long)(target*f))+"원";
 v.setTextViewText(R.id.amount,amount);
 boolean lastDay=n.getDayOfMonth()==days;
 v.setViewVisibility(R.id.amount,lastDay?View.INVISIBLE:View.VISIBLE);
 v.setViewVisibility(R.id.rainbow_amount,lastDay?View.VISIBLE:View.GONE);
 if(lastDay){int sp=cl==Mini.class?17:cl==Wide.class?20:cl==Three.class?21:cl==Four.class?23:24;v.setImageViewBitmap(R.id.rainbow_amount,rainbowText(c,amount,sp));}
 v.setViewVisibility(R.id.progress,lastDay || n.getDayOfMonth()>=days-4?View.GONE:View.VISIBLE);v.setViewVisibility(R.id.progress_lastfive,lastDay || n.getDayOfMonth()>=days-4?View.VISIBLE:View.GONE);v.setTextViewText(R.id.progress_text,cl==Mini.class?String.format(Locale.KOREA,"%.1f%%",f*100):String.format(Locale.KOREA,"달성률 %.2f%%",f*100));v.setProgressBar(R.id.progress,10000,(int)Math.round(f*10000),false);v.setProgressBar(R.id.progress_lastfive,10000,(int)Math.round(f*10000),false);PendingIntent pi=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);v.setOnClickPendingIntent(R.id.widget_root,pi);m.updateAppWidget(id,v);}}
 static Bitmap rainbowText(Context c,String value,int sp){
  float density=c.getResources().getDisplayMetrics().scaledDensity;
  Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.SUBPIXEL_TEXT_FLAG);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(sp*density);
  int width=Math.max(1,(int)Math.ceil(p.measureText(value))+4);int height=Math.max(1,(int)Math.ceil(sp*density*1.55));
  Bitmap b=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);
  int[] colors={0xFFFF4545,0xFFFF9E3C,0xFFFFE34D,0xFF55D66D,0xFF52A6FF,0xFF6A65D9,0xFFC36FE9};
  p.setShader(new LinearGradient(0,0,width,0,colors,null,Shader.TileMode.CLAMP));
  canvas.drawText(value,1,height/2f-(p.ascent()+p.descent())/2f,p);return b;
 }
 static void scheduleMidnight(Context c){
  LocalDateTime tomorrow=LocalDate.now().plusDays(1).atStartOfDay();
  long millis=tomorrow.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()+1500;
  Intent i=new Intent(c,Mini.class).setAction("com.example.salarygrows.NEW_DAY");
  PendingIntent pi=PendingIntent.getBroadcast(c,317,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  if(am!=null)am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,millis,pi);
 }
 @Override public void onReceive(Context c,Intent i){super.onReceive(c,i);if("com.example.salarygrows.NEW_DAY".equals(i.getAction())||Intent.ACTION_TIME_CHANGED.equals(i.getAction())||Intent.ACTION_TIMEZONE_CHANGED.equals(i.getAction())){refresh(c);}scheduleMidnight(c);}
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){render(c,m,ids,getClass());scheduleMidnight(c);}
}