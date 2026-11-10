package com.example.salarygrows;
import android.content.*;import android.widget.*;import java.time.*;import java.time.temporal.*;
public class SalaryFeatures {
 static double hours(LocalTime a,LocalTime b,int rest){int m=(int)Duration.between(a,b).toMinutes();if(m<=0)m+=1440;return Math.max(0,m-rest)/60.0;}
 static double monthWorkHours(SharedPreferences p,CheckBox[] days,String start,String end,int rest){try{YearMonth ym=YearMonth.now();double daily=hours(LocalTime.parse(start),LocalTime.parse(end),rest);double total=0;for(int d=1;d<=ym.lengthOfMonth();d++){int day=ym.atDay(d).getDayOfWeek().getValue()-1;if(days[day].isChecked())total+=daily;}return total;}catch(Exception e){return 0;}}
 static LocalDate clamp(YearMonth ym,int day){return ym.atDay(day==0?ym.lengthOfMonth():Math.min(day,ym.lengthOfMonth()));}
 static LocalDate[] period(SharedPreferences p,LocalDate date){
  int startDay=Math.max(0,Math.min(31,p.getInt("period_start_day",1)));
  int endDay=Math.max(0,Math.min(31,p.getInt("period_end_day",0)));
  YearMonth ym=YearMonth.from(date);
  LocalDate start=clamp(ym,startDay);
  if(start.isAfter(date)){ym=ym.minusMonths(1);start=clamp(ym,startDay);}
  LocalDate end=clamp(ym,endDay);
  if(!end.isAfter(start))end=clamp(ym.plusMonths(1),endDay);
  if(date.isAfter(end)){
   ym=ym.plusMonths(1);start=clamp(ym,startDay);
   end=clamp(ym,endDay);
   if(!end.isAfter(start))end=clamp(ym.plusMonths(1),endDay);
  }
  return new LocalDate[]{start,end};
 }
 public static double fraction(SharedPreferences p,LocalDateTime now){LocalDate[] dates=period(p,now.toLocalDate());LocalDateTime start=dates[0].atStartOfDay();LocalDateTime finish=dates[1].plusDays(1).atStartOfDay();return Math.max(0,Math.min(1,(double)Duration.between(start,now).toMillis()/Duration.between(start,finish).toMillis()));}
 static boolean isPeriodEnd(SharedPreferences p,LocalDate date){return period(p,date)[1].equals(date);}
 static long daysUntilPeriodEnd(SharedPreferences p,LocalDate date){return ChronoUnit.DAYS.between(date,period(p,date)[1]);}
 static String periodLabel(SharedPreferences p,LocalDate date){LocalDate[] a=period(p,date);return a[0].getMonthValue()+"월 "+a[0].getDayOfMonth()+"일 ~ "+a[1].getMonthValue()+"월 "+a[1].getDayOfMonth()+"일";}
 static LocalDate payDate(SharedPreferences p,YearMonth month){int d=Math.max(1,Math.min(31,p.getInt("payday",10)));LocalDate date=month.atDay(Math.min(d,month.lengthOfMonth()));int rule=p.getInt("holiday_rule",0);if(rule!=0){while(date.getDayOfWeek()==DayOfWeek.SATURDAY||date.getDayOfWeek()==DayOfWeek.SUNDAY)date=date.plusDays(rule==1?-1:1);}return date;}
 static LocalDate nextPay(SharedPreferences p,LocalDate today){LocalDate date=payDate(p,YearMonth.from(today));if(date.isBefore(today))date=payDate(p,YearMonth.from(today).plusMonths(1));return date;}
 static String dday(SharedPreferences p,LocalDate today){long days=ChronoUnit.DAYS.between(today,nextPay(p,today));return days==0?"월급날 D-DAY":"다음 월급날 D-"+days;}
}
