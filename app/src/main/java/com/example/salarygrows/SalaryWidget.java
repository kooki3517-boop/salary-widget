package com.example.salarygrows;
import android.app.*;import android.appwidget.*;import android.content.*;import android.widget.*;import java.time.*;import java.text.*;import java.util.*;
public class SalaryWidget extends AppWidgetProvider {
 public static class Mini extends SalaryWidget {} public static class Wide extends SalaryWidget {} public static class Three extends SalaryWidget {} public static class Four extends SalaryWidget {} public static class Five extends SalaryWidget {}
 public static void refresh(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);for(Class<?> cl:new Class<?>[]{Mini.class,Wide.class,Three.class,Four.class,Five.class}){int[] ids=m.getAppWidgetIds(new ComponentName(c,cl));if(ids.length>0)render(c,m,ids,cl);}}
 static void render(Context c,AppWidgetManager m,int[] ids,Class<?> cl){SharedPreferences p=c.getSharedPreferences("salary",Context.MODE_PRIVATE);boolean transparent=p.getBoolean("transparent_bg",false);long target=p.getLong("salary_amount",p.getLong("net",2500000));LocalDateTime n=LocalDateTime.now();int days=YearMonth.from(n).lengthOfMonth();double f=((n.getDayOfMonth()-1)*86400d+n.getHour()*3600d+n.getMinute()*60d+n.getSecond())/(days*86400d);int layout=cl==Mini.class?R.layout.salary_widget_mini:cl==Wide.class?R.layout.salary_widget_wide:cl==Three.class?R.layout.salary_widget_three:cl==Four.class?R.layout.salary_widget_four:R.layout.salary_widget_five;for(int id:ids){RemoteViews v=new RemoteViews(c.getPackageName(),layout);v.setInt(R.id.widget_root,"setBackgroundResource",transparent?R.drawable.widget_bg_transparent:R.drawable.widget_bg);v.setTextViewText(R.id.title,"월급이 자라는 중..💵");v.setTextViewText(R.id.amount,NumberFormat.getIntegerInstance(Locale.KOREA).format((long)(target*f))+"원");v.setTextViewText(R.id.progress_text,cl==Mini.class?String.format(Locale.KOREA,"%.1f%%",f*100):String.format(Locale.KOREA,"달성률 %.2f%%",f*100));v.setProgressBar(R.id.progress,10000,(int)Math.round(f*10000),false);PendingIntent pi=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);v.setOnClickPendingIntent(R.id.widget_root,pi);m.updateAppWidget(id,v);}}
 static void scheduleMidnight(Context c){
  LocalDateTime tomorrow=LocalDate.now().plusDays(1).atStartOfDay();
  long millis=tomorrow.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()+1500;
  Intent i=new Intent(c,SalaryWidget.class).setAction("com.example.salarygrows.NEW_DAY");
  PendingIntent pi=PendingIntent.getBroadcast(c,317,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  if(am!=null)am.set(AlarmManager.RTC_WAKEUP,millis,pi);
 }
 @Override public void onReceive(Context c,Intent i){super.onReceive(c,i);if("com.example.salarygrows.NEW_DAY".equals(i.getAction())||Intent.ACTION_TIME_CHANGED.equals(i.getAction())||Intent.ACTION_TIMEZONE_CHANGED.equals(i.getAction())){refresh(c);}scheduleMidnight(c);}
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){render(c,m,ids,getClass());}
}