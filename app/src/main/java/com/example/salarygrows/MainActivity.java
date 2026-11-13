package com.example.salarygrows;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.graphics.drawable.Drawable;import android.graphics.drawable.GradientDrawable;import android.view.*;import android.widget.*;import android.text.InputType;import android.text.TextWatcher;import android.text.Editable;import java.time.*;import java.time.temporal.*;import android.net.Uri;import java.io.*;import org.json.*;import java.text.*;import java.util.*;
public class MainActivity extends Activity {
 Button saveButton; boolean saveSucceeded=false; static final int SOFT_TEXT=0xffFFFFFF; static final int BG=0xff192334; static final int FIELD=0xff2D3C52; static final int LINE=0xff46576C; ProgressBar appProgress; TextView insuranceNetPreview;
 SharedPreferences prefs; EditText salary,netSalary,payday; Spinner displayMode; TextView preview,previewPercent;  Switch pensionOn,healthOn,employmentOn; CheckBox applyInsuranceNet; TextView insuranceResult; Spinner themeChoice,previewSize; SeekBar opacityBar; TextView opacityLabel; LinearLayout widgetPreview; Handler handler=new Handler(); Runnable ticker; Spinner payType,holidayRule; EditText annualSalary,hourlyRate,weeklyHours; LinearLayout workDaysRow; CheckBox[] workDays=new CheckBox[7]; TextView hourlyEstimate; TextView periodStartButton,periodEndButton; int periodStartDay,periodEndDay; CheckBox ddayWidgetOn,notifyPay,notifyProgress; TextView ddayText,periodText;
 int dp(int v){return (int)(v*getResources().getDisplayMetrics().density);} TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextColor(SOFT_TEXT);t.setTextSize(size);return t;}
 void add(LinearLayout l,View v){LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(-1,-2);a.bottomMargin=dp(14);l.addView(v,a);} EditText field(long n){EditText e=new EditText(this);e.setText(String.valueOf(n));e.setTextColor(SOFT_TEXT);e.setInputType(InputType.TYPE_CLASS_NUMBER);e.setSingleLine(true);styleField(e);return e;}
 GradientDrawable roundedBox(int color,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(12));d.setStroke(dp(1),stroke);return d;}
 void styleField(EditText e){e.setBackground(roundedBox(FIELD,LINE));e.setPadding(dp(12),dp(11),dp(12),dp(11));}
 void styleSpinner(Spinner sp){sp.setBackground(roundedBox(FIELD,LINE));sp.setPadding(dp(12),dp(8),dp(10),dp(8));}
 long number(EditText e){return Long.parseLong(e.getText().toString().replace(",", "").trim());}
 void commas(EditText e){e.setText(NumberFormat.getIntegerInstance(Locale.KOREA).format(number(e)));e.setSelection(e.length());e.addTextChangedListener(new TextWatcher(){boolean busy=false;public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){if(busy)return;String digits=s.toString().replaceAll("[^0-9]", "");if(digits.isEmpty())return;try{String formatted=NumberFormat.getIntegerInstance(Locale.KOREA).format(Long.parseLong(digits));if(!formatted.equals(s.toString())){busy=true;e.setText(formatted);e.setSelection(e.length());busy=false;}}catch(Exception ex){busy=false;}}public void afterTextChanged(Editable x){}});}


 TextView label(String s){TextView t=text(s,14);t.setTextColor(SOFT_TEXT);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
 void row(LinearLayout parent,String name,View control){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setOrientation(LinearLayout.HORIZONTAL);TextView title=label(name);r.addView(title,new LinearLayout.LayoutParams(0,dp(44),.95f));r.addView(control,new LinearLayout.LayoutParams(0,-2,1.65f));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);parent.addView(r,lp);}
 void section(LinearLayout l,String name){TextView t=text(name,16);t.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);t.setTextColor(SOFT_TEXT);t.setPadding(0,dp(13),0,dp(8));add(l,t);}
 void sectionDividerBefore(LinearLayout parent,View anchor,int topSpace,int bottomSpace){
  if(anchor==null)return;
  View divider=new View(this);divider.setBackgroundColor(LINE);
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));
  lp.topMargin=dp(topSpace);lp.bottomMargin=dp(bottomSpace);
  parent.addView(divider,parent.indexOfChild(anchor),lp);
 }
 void setBottomGap(LinearLayout parent,View child,int gap){
  if(child==null)return;
  ViewGroup.LayoutParams base=child.getLayoutParams();
  if(base instanceof LinearLayout.LayoutParams){
   LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)base;
   lp.bottomMargin=dp(gap);child.setLayoutParams(lp);
  }
 }
 void progressStyle(boolean lastFive){GradientDrawable track=roundedBox(0xff40516A,0xff40516A);GradientDrawable fill=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,lastFive?new int[]{0xff236B43,0xff70D9A4,0xffF6A3A0,0xffB50016}:new int[]{0xff236B43,0xff70D9A4});fill.setCornerRadius(dp(6));android.graphics.drawable.ClipDrawable clip=new android.graphics.drawable.ClipDrawable(fill,Gravity.LEFT,android.graphics.drawable.ClipDrawable.HORIZONTAL);android.graphics.drawable.LayerDrawable layers=new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{track,clip});layers.setId(0,android.R.id.background);layers.setId(1,android.R.id.progress);appProgress.setProgressDrawable(layers);}
 void updateInsurancePreview(){try{if(insuranceNetPreview==null||insuranceResult==null)return;long gross=effectiveGross();long[] a=InsuranceEstimate.calculate(gross,pensionOn.isChecked(),healthOn.isChecked(),employmentOn.isChecked());long total=a[0]+a[1]+a[2]+a[3];insuranceNetPreview.setText(SalaryCalc.won(Math.max(0,gross-total)));insuranceResult.setText("예상 공제액  "+SalaryCalc.won(total));if(applyInsuranceNet!=null&&applyInsuranceNet.isChecked()&&netSalary!=null){netSalary.setText(NumberFormat.getIntegerInstance(Locale.KOREA).format(Math.max(0,gross-total)));}}catch(Exception ignored){}}

 long effectiveGross(){
  int type=payType==null?0:payType.getSelectedItemPosition();
  if(type==2)return Math.round(number(annualSalary)/12.0);
  if(type==1)return Math.round(number(hourlyRate)*monthlyWorkHours());
  return number(salary);
 }
 double monthlyWorkHours(){
  try{
   double weekly=Double.parseDouble(weeklyHours.getText().toString().trim());
   if(weekly<0||weekly>168)return 0;
   boolean[] selected=new boolean[7];int count=0;
   for(int i=0;i<7;i++){selected[i]=workDays[i].isChecked();if(selected[i])count++;}
   if(count==0)return 0;
   LocalDate[] period=SalaryFeatures.periodWithDays(periodStartDay,periodEndDay,LocalDate.now());
   return SalaryFeatures.workHours(period[0],period[1],weekly,selected);
  }catch(Exception e){return 0;}
 }
 void updateHourlyEstimate(){
  if(hourlyEstimate!=null){try{hourlyEstimate.setText("이번 달 월급 예상 기본급  "+SalaryCalc.won(effectiveGross()));}catch(Exception ignored){}}
  updateInsurancePreview();
 }
 @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("salary",MODE_PRIVATE);ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(BG);LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(18),dp(48),dp(18),dp(20));scroll.addView(l);setContentView(scroll);
 TextView title=text("월급이 자란다 💵",22);title.setTypeface(android.graphics.Typeface.DEFAULT,1);add(l,title);
 periodText=text("",12);add(l,periodText);
 add(l,text("현재까지 쌓인 월급",13));preview=text("",29);preview.setTextColor(0xffD71935);preview.setTypeface(android.graphics.Typeface.create("sans-serif-black",android.graphics.Typeface.BOLD));preview.setSingleLine(true);preview.setAutoSizeTextTypeUniformWithConfiguration(17,29,1,1);add(l,preview);
 LinearLayout percentageRow=new LinearLayout(this);percentageRow.setGravity(Gravity.CENTER_VERTICAL);percentageRow.addView(label("이번 달 적립률"),new LinearLayout.LayoutParams(0,-2,1));previewPercent=text("",15);previewPercent.setTextColor(0xffD71935);previewPercent.setTypeface(null,1);previewPercent.setGravity(Gravity.RIGHT);percentageRow.addView(previewPercent,new LinearLayout.LayoutParams(0,-2,1));add(l,percentageRow);
 appProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);appProgress.setMax(10000);appProgress.setMinimumHeight(dp(8));LinearLayout.LayoutParams barParams=new LinearLayout.LayoutParams(-1,dp(9));barParams.bottomMargin=dp(16);l.addView(appProgress,barParams);progressStyle(false);
 section(l,"월급 설정");
 salary=field(prefs.getLong("salary_amount",prefs.getLong("net",2500000)));salary.setTypeface(android.graphics.Typeface.create("sans-serif-black",android.graphics.Typeface.BOLD));commas(salary);row(l,"세전 월급",salary);
 netSalary=field(prefs.getLong("net_salary_amount",prefs.getLong("net",2200000)));netSalary.setTypeface(android.graphics.Typeface.create("sans-serif-black",android.graphics.Typeface.BOLD));commas(netSalary);row(l,"세후 월급",netSalary);
 displayMode=new Spinner(this);String[] displayOptions={"구분 없이 월급만 표시", "세전 월급 표시", "세후 월급 표시"};ArrayAdapter<String> displayAdapter=new ArrayAdapter<>(this,R.layout.spinner_item_soft,displayOptions);displayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_soft);displayMode.setAdapter(displayAdapter);styleSpinner(displayMode);displayMode.setSelection(Math.max(0,Math.min(2,prefs.getInt("display_mode",0))));row(l,"위젯 표시",displayMode);
 payday=field(prefs.getInt("payday",10));row(l,"임금 지급일",payday);
 section(l,"급여 형태 · 적립 방식");
 payType=spinner(new String[]{"월급제","시급제","연봉제"},prefs.getInt("pay_type",0));row(l,"급여 형태",payType);
 annualSalary=field(prefs.getLong("annual_salary",34560000));commas(annualSalary);row(l,"세전 연봉",annualSalary);
 hourlyRate=field(prefs.getLong("hourly_rate",11000));commas(hourlyRate);row(l,"시급",hourlyRate);
 weeklyHours=field(prefs.getInt("weekly_work_hours",40));row(l,"주당 근무시간",weeklyHours);
 workDaysRow=new LinearLayout(this);workDaysRow.setOrientation(LinearLayout.HORIZONTAL);
 String[] dayNames={"월","화","수","목","금","토","일"};int mask=prefs.getInt("work_days_mask",31);
 for(int i=0;i<7;i++){final int idx=i;CheckBox cb=new CheckBox(this);cb.setText(dayNames[i]);cb.setTextColor(SOFT_TEXT);cb.setTextSize(12);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(0xff70D9A4));cb.setChecked((mask&(1<<i))!=0);workDays[i]=cb;workDaysRow.addView(cb,new LinearLayout.LayoutParams(0,-2,1));}
 row(l,"근무 요일",workDaysRow);
 periodStartDay=prefs.getInt("period_start_day",1);periodEndDay=prefs.getInt("period_end_day",0);
 // Match the app-wide label-to-field proportions (0.95 : 1.65).
 LinearLayout periodRow=new LinearLayout(this);periodRow.setOrientation(LinearLayout.HORIZONTAL);periodRow.setGravity(Gravity.CENTER_VERTICAL);
 TextView periodLabel=label("급여 산정 기간");periodRow.addView(periodLabel,new LinearLayout.LayoutParams(0,dp(44),.95f));
 LinearLayout periodControls=new LinearLayout(this);periodControls.setOrientation(LinearLayout.VERTICAL);
 periodRow.addView(periodControls,new LinearLayout.LayoutParams(0,-2,1.65f));
 periodStartButton=text("",15);periodEndButton=text("",15);
 for(int i=0;i<2;i++){TextView t=i==0?periodStartButton:periodEndButton;t.setGravity(Gravity.CENTER_VERTICAL);t.setBackground(roundedBox(FIELD,LINE));t.setPadding(dp(16),dp(14),dp(12),dp(14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);if(i==0)lp.bottomMargin=dp(8);periodControls.addView(t,lp);}
 LinearLayout.LayoutParams periodLp=new LinearLayout.LayoutParams(-1,-2);periodLp.bottomMargin=dp(8);l.addView(periodRow,periodLp);
 updatePeriodButtons();periodStartButton.setOnClickListener(v->pickPeriodDay(true));periodEndButton.setOnClickListener(v->pickPeriodDay(false));
 payType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,View v,int pos,long id){annualSalary.setVisibility(pos==2?View.VISIBLE:View.GONE);hourlyRate.setVisibility(pos==1?View.VISIBLE:View.GONE);}});
 section(l,"월급날 설정");
 holidayRule=spinner(new String[]{"설정한 날짜 그대로","직전 평일","다음 평일"},prefs.getInt("holiday_rule",0));row(l,"주말 지급일",holidayRule);
 ddayWidgetOn=new CheckBox(this);ddayWidgetOn.setText("위젯에 D-day 표시");ddayWidgetOn.setTextColor(SOFT_TEXT);ddayWidgetOn.setChecked(prefs.getBoolean("dday_widget_on",false));add(l,ddayWidgetOn);ddayWidgetOn.setOnCheckedChangeListener((buttonView,checked)->{prefs.edit().putBoolean("dday_widget_on",checked).apply();SalaryWidget.refresh(this);});
 ddayText=text("",14);add(l,ddayText);
 section(l,"알림 설정");
 notifyPay=new CheckBox(this);notifyPay.setText("월급날 알림 (오전 9시)");notifyPay.setTextColor(SOFT_TEXT);notifyPay.setChecked(false);add(l,notifyPay);
 notifyProgress=new CheckBox(this);notifyProgress.setText("적립률 25% · 50% · 75% · 100% 알림");notifyProgress.setTextColor(SOFT_TEXT);notifyProgress.setChecked(false);add(l,notifyProgress);
 section(l,"위젯 디자인 미리보기");
 themeChoice=new Spinner(this);String[] themes={"화이트","블랙","투명"};ArrayAdapter<String> ta=new ArrayAdapter<>(this,R.layout.spinner_item_soft,themes);ta.setDropDownViewResource(R.layout.spinner_dropdown_soft);themeChoice.setAdapter(ta);styleSpinner(themeChoice);themeChoice.setSelection(prefs.getBoolean("transparent_bg",false)?2:(prefs.getInt("widget_theme",0)==3?1:0));row(l,"배경색",themeChoice);
 opacityBar=new SeekBar(this);opacityBar.setMax(100);opacityBar.setProgress(prefs.getInt("widget_opacity",100));opacityBar.setEnabled(themeChoice.getSelectedItemPosition()!=2);opacityLabel=label("배경 불투명도 "+opacityBar.getProgress()+"%");LinearLayout opacityRow=new LinearLayout(this);opacityRow.setGravity(Gravity.CENTER_VERTICAL);opacityRow.addView(opacityLabel,new LinearLayout.LayoutParams(0,-2,1));opacityRow.addView(opacityBar,new LinearLayout.LayoutParams(0,-2,1.65f));add(l,opacityRow);
 previewSize=new Spinner(this);String[] sizes={"1×1","2×1","3×1","4×1","5×1","6×1"};ArrayAdapter<String> sa=new ArrayAdapter<>(this,R.layout.spinner_item_soft,sizes);sa.setDropDownViewResource(R.layout.spinner_dropdown_soft);previewSize.setAdapter(sa);styleSpinner(previewSize);previewSize.setSelection(3);row(l,"미리보기 크기",previewSize);
 TextView previewHint=text("설정 변경 시 미리보기에 즉시 반영됩니다. 실제 홈 화면 배경에 따라 투명도는 다르게 보일 수 있습니다.",11);add(l,previewHint);
 widgetPreview=new LinearLayout(this);widgetPreview.setOrientation(LinearLayout.VERTICAL);widgetPreview.setPadding(dp(12),dp(10),dp(12),dp(10));LinearLayout previewBackdrop=new LinearLayout(this);previewBackdrop.setOrientation(LinearLayout.VERTICAL);previewBackdrop.setPadding(dp(10),dp(10),dp(10),dp(10));previewBackdrop.setBackground(roundedBox(0xff303D4D,0xff303D4D));previewBackdrop.addView(widgetPreview,new LinearLayout.LayoutParams(-1,-2));add(l,previewBackdrop);
 themeChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){opacityBar.setEnabled(pos!=2);updateWidgetPreview();}});
 previewSize.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){updateWidgetPreview();}});
 opacityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int value,boolean user){opacityLabel.setText("배경 불투명도 "+(themeChoice.getSelectedItemPosition()==2?0:value)+"%");updateWidgetPreview();}});

 updateWidgetPreview();
 section(l,"4대보험 예상 계산");
 LinearLayout insuranceRow=new LinearLayout(this);insuranceRow.setGravity(Gravity.CENTER_VERTICAL);insuranceRow.setOrientation(LinearLayout.HORIZONTAL);insuranceRow.addView(label("보험 적용"),new LinearLayout.LayoutParams(0,-2,1));pensionOn=new Switch(this);pensionOn.setText("연금");pensionOn.setTextSize(11);pensionOn.setTextColor(SOFT_TEXT);pensionOn.setChecked(prefs.getBoolean("insurance_pension",true));healthOn=new Switch(this);healthOn.setText("건강");healthOn.setTextSize(11);healthOn.setTextColor(SOFT_TEXT);healthOn.setChecked(prefs.getBoolean("insurance_health",true));employmentOn=new Switch(this);employmentOn.setText("고용");employmentOn.setTextSize(11);employmentOn.setTextColor(SOFT_TEXT);employmentOn.setChecked(prefs.getBoolean("insurance_employment",true));
 LinearLayout switches=new LinearLayout(this);switches.setGravity(Gravity.CENTER_VERTICAL);switches.setOrientation(LinearLayout.VERTICAL);switches.addView(pensionOn);switches.addView(healthOn);switches.addView(employmentOn);insuranceRow.addView(switches);add(l,insuranceRow);
 LinearLayout resultCard=new LinearLayout(this);resultCard.setOrientation(LinearLayout.VERTICAL);resultCard.setPadding(dp(12),dp(12),dp(12),dp(12));resultCard.setBackground(roundedBox(FIELD,LINE));insuranceResult=text("",13);resultCard.addView(insuranceResult);LinearLayout resultLine=new LinearLayout(this);resultLine.setGravity(Gravity.CENTER_VERTICAL);resultLine.addView(label("예상 세후 월급"),new LinearLayout.LayoutParams(0,-2,1));insuranceNetPreview=text("",21);insuranceNetPreview.setGravity(Gravity.RIGHT);insuranceNetPreview.setTextColor(0xff70D9A4);insuranceNetPreview.setTypeface(android.graphics.Typeface.create("sans-serif-black",android.graphics.Typeface.BOLD));resultLine.addView(insuranceNetPreview,new LinearLayout.LayoutParams(0,-2,1.5f));resultCard.addView(resultLine);TextView disclaimer=text("※ 예상 계산 금액으로 실제 지급되는 월급과 차이가 있을 수 있습니다.",11);disclaimer.setPadding(0,dp(9),0,dp(9));resultCard.addView(disclaimer);View separator=new View(this);separator.setBackgroundColor(LINE);resultCard.addView(separator,new LinearLayout.LayoutParams(-1,dp(1)));applyInsuranceNet=new CheckBox(this);applyInsuranceNet.setText("계산 결과를 세후 월급에 반영");applyInsuranceNet.setTextColor(SOFT_TEXT);applyInsuranceNet.setTextSize(13);applyInsuranceNet.setPadding(0,dp(9),0,0);applyInsuranceNet.setChecked(prefs.getBoolean("apply_insurance_net",false));resultCard.addView(applyInsuranceNet);add(l,resultCard);
 Button calculate=new Button(this);calculate.setText("공제 상세 내역 보기 / 재계산");calculate.setTextSize(13);calculate.setAllCaps(false);calculate.setBackground(roundedBox(FIELD,LINE));calculate.setTextColor(SOFT_TEXT);add(l,calculate);
 TextView detail=text("",12);detail.setVisibility(View.GONE);add(l,detail);
 calculate.setOnClickListener(v->{try{long gross=effectiveGross();long[] a=InsuranceEstimate.calculate(gross,pensionOn.isChecked(),healthOn.isChecked(),employmentOn.isChecked());long total=a[0]+a[1]+a[2]+a[3];updateInsurancePreview();if(applyInsuranceNet.isChecked())netSalary.setText(NumberFormat.getIntegerInstance(Locale.KOREA).format(Math.max(0,gross-total)));detail.setText("국민연금: "+SalaryCalc.won(a[0])+"\n건강보험: "+SalaryCalc.won(a[1])+"\n장기요양보험: "+SalaryCalc.won(a[2])+"\n고용보험: "+SalaryCalc.won(a[3])+"\n산재보험: 근로자 부담 0원\n\n※ 소득세·지방소득세 제외. 실제 공제액은 개인별 조건에 따라 다릅니다.");detail.setVisibility(detail.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);}catch(Exception ex){Toast.makeText(this,"월급을 확인해",Toast.LENGTH_SHORT).show();}});
 applyInsuranceNet.setOnCheckedChangeListener((button,checked)->{if(checked){try{long gross=effectiveGross();long[] a=InsuranceEstimate.calculate(gross,pensionOn.isChecked(),healthOn.isChecked(),employmentOn.isChecked());netSalary.setText(NumberFormat.getIntegerInstance(Locale.KOREA).format(Math.max(0,gross-a[0]-a[1]-a[2]-a[3])));}catch(Exception ignored){}}netSalary.setEnabled(!checked);netSalary.setAlpha(checked?0.7f:1f);updateInsurancePreview();});netSalary.setEnabled(!applyInsuranceNet.isChecked());netSalary.setAlpha(applyInsuranceNet.isChecked()?0.7f:1f);
 for(Switch sw:new Switch[]{pensionOn,healthOn,employmentOn})sw.setOnCheckedChangeListener((v,checked)->updateInsurancePreview());salary.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){updateInsurancePreview();}public void afterTextChanged(Editable e){}});updateInsurancePreview();
 Button save=new Button(this);saveButton=save;save.setText("설정 저장 / 위젯 갱신");save.setBackground(roundedBox(0xffA5B5C6,0xffA5B5C6));save.setTextColor(0xff192334);save.setAllCaps(false);save.setTypeface(null,1);save.setMinHeight(dp(52));add(l,save);
 save.setOnClickListener(v->{saveSucceeded=false;try{long g=effectiveGross();long netValue=number(netSalary);if(applyInsuranceNet.isChecked()){long[] a=InsuranceEstimate.calculate(g,pensionOn.isChecked(),healthOn.isChecked(),employmentOn.isChecked());netValue=Math.max(0,g-a[0]-a[1]-a[2]-a[3]);netSalary.setText(NumberFormat.getIntegerInstance(Locale.KOREA).format(netValue));}if(netValue<0)throw new Exception();int d=Integer.parseInt(payday.getText().toString());if(g<0||d<1||d>31)throw new Exception();prefs.edit().putBoolean("insurance_pension",pensionOn.isChecked()).putBoolean("insurance_health",healthOn.isChecked()).putBoolean("insurance_employment",employmentOn.isChecked()).putLong("salary_amount",g).putLong("net_salary_amount",netValue).putBoolean("apply_insurance_net",applyInsuranceNet.isChecked()).putInt("display_mode",displayMode.getSelectedItemPosition()).putInt("payday",d).putBoolean("transparent_bg",themeChoice.getSelectedItemPosition()==2).putInt("widget_theme",selectedWidgetTheme()).putInt("widget_opacity",opacityBar.getProgress()).apply();prefs.edit().putInt("pay_type",payType.getSelectedItemPosition()).putInt("accrual_type",0).putLong("annual_salary",number(annualSalary)).putLong("hourly_rate",number(hourlyRate)).putInt("weekly_work_hours",Integer.parseInt(weeklyHours.getText().toString())).putInt("work_days_mask",workDaysMask()).putInt("period_start_day",periodStartDay).putInt("period_end_day",periodEndDay).putBoolean("dday_widget_on",ddayWidgetOn.isChecked()).putInt("holiday_rule",holidayRule.getSelectedItemPosition()).putBoolean("notify_pay",false).putBoolean("notify_progress",false).apply();SalaryWidget.refresh(this);saveSucceeded=true;Toast.makeText(this,"저장 완료",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"입력값을 확인해",Toast.LENGTH_LONG).show();}});
 View footLine=new View(this);footLine.setBackgroundColor(LINE);LinearLayout.LayoutParams flp=new LinearLayout.LayoutParams(-1,dp(1));flp.topMargin=dp(20);flp.bottomMargin=dp(12);l.addView(footLine,flp);
 LinearLayout footer=new LinearLayout(this);footer.setGravity(Gravity.CENTER);String[] footerLabels={"개발자는 누구인가 👤","개인정보처리방침"};for(int k=0;k<footerLabels.length;k++){final int index=k;TextView link=text(footerLabels[k],12);link.setPadding(dp(4),dp(8),dp(4),dp(8));link.setOnClickListener(v->footerAction(index));footer.addView(link);if(k==0){TextView slash=text("/",12);slash.setPadding(dp(3),dp(8),dp(3),dp(8));footer.addView(slash);}}add(l,footer);TextView version=text("월급이 자란다 💵 · v2.38",10);version.setGravity(Gravity.CENTER);version.setTextColor(0xff70D9A4);add(l,version);
 organizeSettings(l,scroll);
 ticker=new Runnable(){public void run(){int mode=prefs.getInt("display_mode",0);long target=mode==2?prefs.getLong("net_salary_amount",prefs.getLong("net",2200000)):prefs.getLong("salary_amount",prefs.getLong("net",2500000));LocalDateTime now=LocalDateTime.now();if(prefs.getInt("pay_type",0)==1){long gross=SalaryFeatures.hourlyGross(prefs,now.toLocalDate());if(mode==2&&prefs.getBoolean("apply_insurance_net",false)){long[] deductions=InsuranceEstimate.calculate(gross,prefs.getBoolean("insurance_pension",true),prefs.getBoolean("insurance_health",true),prefs.getBoolean("insurance_employment",true));for(long deduction:deductions)gross-=deduction;}target=mode==2&&!prefs.getBoolean("apply_insurance_net",false)?prefs.getLong("net_salary_amount",gross):Math.max(0,gross);}int days=YearMonth.from(now).lengthOfMonth();double f=SalaryFeatures.fraction(prefs,now);periodText.setText(SalaryFeatures.periodLabel(prefs,now.toLocalDate()));if(ddayText!=null){ddayText.setText("");ddayText.setVisibility(View.GONE);}preview.setText(NumberFormat.getIntegerInstance(Locale.KOREA).format((long)(target*f))+"원");boolean rainbow=SalaryFeatures.isPeriodEnd(prefs,now.toLocalDate());preview.getPaint().setShader(rainbow?new android.graphics.LinearGradient(0,0,Math.max(dp(240),preview.getWidth()),0,new int[]{0xffff4545,0xffff9e3c,0xffffe34d,0xff55d66d,0xff52a6ff,0xff6a65d9,0xffc36fe9},null,android.graphics.Shader.TileMode.CLAMP):null);preview.invalidate();previewPercent.setText(String.format(Locale.KOREA,"%.2f%%",f*100));progressStyle(SalaryFeatures.daysUntilPeriodEnd(prefs,now.toLocalDate())<=4);appProgress.setProgress((int)Math.round(f*10000));if(widgetPreview!=null&&now.getSecond()%5==0)updateWidgetPreview();handler.postDelayed(this,1000);}};handler.post(ticker);
 }

 int workDaysMask(){int mask=0;for(int i=0;i<7;i++)if(workDays[i].isChecked())mask|=1<<i;return mask;}
 void updatePeriodButtons(){periodStartButton.setText("시작 · "+(periodStartDay==0?"매월 말일":periodStartDay+"일"));periodEndButton.setText("종료 · "+(periodEndDay==0?"매월 말일":periodEndDay+"일"));}
 void pickPeriodDay(boolean start){
  final android.widget.NumberPicker picker=new android.widget.NumberPicker(this);
  String[] labels=new String[32];for(int i=0;i<31;i++)labels[i]=(i+1)+"일";labels[31]="매월 말일";
  picker.setMinValue(0);picker.setMaxValue(31);picker.setDisplayedValues(labels);
  picker.setWrapSelectorWheel(false);
  int selected=start?periodStartDay:periodEndDay;
  picker.setValue(selected==0?31:Math.max(0,Math.min(30,selected-1)));
  LinearLayout holder=new LinearLayout(this);holder.setGravity(Gravity.CENTER);holder.setPadding(dp(12),dp(8),dp(12),dp(8));holder.addView(picker,new LinearLayout.LayoutParams(dp(180),dp(190)));
  new android.app.AlertDialog.Builder(this).setTitle(start?"매월 시작일":"매월 종료일")
   .setView(holder).setNegativeButton("취소",null)
   .setPositiveButton("선택",(dialog,which)->{int value=picker.getValue()==31?0:picker.getValue()+1;if(start)periodStartDay=value;else periodEndDay=value;updatePeriodButtons();updateHourlyEstimate();})
   .show();
 }
 // Existing controls are moved into dedicated settings pages without recreating their state.
 void organizeSettings(LinearLayout root,ScrollView scroll){
  ArrayList<View> original=new ArrayList<>();for(int i=0;i<root.getChildCount();i++)original.add(root.getChildAt(i));
  root.removeAllViews();
  LinearLayout main=new LinearLayout(this);main.setOrientation(LinearLayout.VERTICAL);
  LinearLayout settings=new LinearLayout(this);settings.setOrientation(LinearLayout.VERTICAL);
  LinearLayout widgetPage=new LinearLayout(this);widgetPage.setOrientation(LinearLayout.VERTICAL);
  LinearLayout paydayPage=new LinearLayout(this);paydayPage.setOrientation(LinearLayout.VERTICAL);
  LinearLayout salaryPage=new LinearLayout(this);salaryPage.setOrientation(LinearLayout.VERTICAL);
  LinearLayout insurancePage=new LinearLayout(this);insurancePage.setOrientation(LinearLayout.VERTICAL);
  LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);
  TextView appTitle=text("월급이 자란다 💵",22);appTitle.setTypeface(null,1);
  heading.addView(appTitle,new LinearLayout.LayoutParams(0,dp(48),1));
  TextView gear=text("⚙",23);gear.setGravity(Gravity.CENTER);gear.setContentDescription("설정");
  gear.setBackground(roundedBox(FIELD,LINE));heading.addView(gear,new LinearLayout.LayoutParams(dp(44),dp(44)));
  main.addView(heading,new LinearLayout.LayoutParams(-1,-2));
  TextView back=text("‹  저장하고 돌아가기",15);back.setPadding(dp(4),dp(14),0,dp(14));settings.addView(back);
  LinearLayout tabs=new LinearLayout(this);tabs.setOrientation(LinearLayout.HORIZONTAL);
  TextView widgetTab=text("▣  위젯 설정",15);widgetTab.setGravity(Gravity.CENTER);widgetTab.setTypeface(null,1);widgetTab.setPadding(dp(8),dp(15),dp(8),dp(15));
  TextView paydayTab=text("▦  월급날 설정",15);paydayTab.setGravity(Gravity.CENTER);paydayTab.setPadding(dp(8),dp(15),dp(8),dp(15));
  LinearLayout.LayoutParams leftTab=new LinearLayout.LayoutParams(0,dp(54),1);leftTab.rightMargin=dp(6);
  LinearLayout.LayoutParams rightTab=new LinearLayout.LayoutParams(0,dp(54),1);rightTab.leftMargin=dp(6);
  tabs.addView(widgetTab,leftTab);tabs.addView(paydayTab,rightTab);
  LinearLayout.LayoutParams tabParams=new LinearLayout.LayoutParams(-1,-2);tabParams.bottomMargin=dp(18);settings.addView(tabs,tabParams);
  settings.addView(widgetPage);settings.addView(paydayPage);
  Runnable showWidget=()->{widgetPage.setVisibility(View.VISIBLE);paydayPage.setVisibility(View.GONE);
   widgetTab.setBackground(roundedBox(0xff394D63,0xffA5B5C6));paydayTab.setBackground(roundedBox(FIELD,LINE));
   widgetTab.setTextColor(SOFT_TEXT);paydayTab.setTextColor(SOFT_TEXT);widgetTab.setTypeface(null,1);paydayTab.setTypeface(null,0);};
  Runnable showPayday=()->{widgetPage.setVisibility(View.GONE);paydayPage.setVisibility(View.VISIBLE);
   paydayTab.setBackground(roundedBox(0xff394D63,0xffA5B5C6));widgetTab.setBackground(roundedBox(FIELD,LINE));
   paydayTab.setTextColor(SOFT_TEXT);widgetTab.setTextColor(SOFT_TEXT);paydayTab.setTypeface(null,1);widgetTab.setTypeface(null,0);};
  showWidget.run();
  widgetTab.setOnClickListener(v->showWidget.run());paydayTab.setOnClickListener(v->showPayday.run());
  gear.setOnClickListener(v->{main.setVisibility(View.GONE);settings.setVisibility(View.VISIBLE);scroll.post(()->scroll.scrollTo(0,0));});
  back.setOnClickListener(v->{saveButton.performClick();if(!saveSucceeded)return;settings.setVisibility(View.GONE);main.setVisibility(View.VISIBLE);scroll.post(()->scroll.scrollTo(0,0));});
  int category=0; // 0 main, 1 payday, 2 notifications hidden, 3 widget, 4 backup
  for(View v:original){
   if(v instanceof TextView && !(v instanceof Button)){
    String t=((TextView)v).getText().toString();
    if(t.equals("월급이 자란다 💵"))continue;
    if(t.equals("월급날 설정")){category=1;section(paydayPage,t);continue;}
    if(t.equals("알림 설정")){category=2;continue;}
    if(t.equals("위젯 디자인 미리보기")){category=3;section(widgetPage,"위젯 디자인 미리보기");continue;}
    if(t.equals("4대보험 예상 계산")){category=0;section(main,"4대보험 · 급여 형태");continue;}
    
   }
   if(v.getParent() instanceof ViewGroup)((ViewGroup)v.getParent()).removeView(v);
   if(category==2)continue;
   if(v==ddayText){category=1;main.addView(v,new LinearLayout.LayoutParams(-1,-2));continue;}
   LinearLayout destination=(category==1)?paydayPage:(category==3||hasChild(v,displayMode))?widgetPage:main;
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);destination.addView(v,lp);
  }
  // Dedicated navigation: right arrow opens a subpage, left arrow saves and returns.
  View insuranceHeader=findText(main,"4대보험 · 급여 형태");
  View payHeader=findText(main,"급여 형태 · 적립 방식");
  if(payHeader!=null&&insuranceHeader!=null){
   int first=main.indexOfChild(payHeader),last=main.indexOfChild(insuranceHeader);
   ArrayList<View> moved=new ArrayList<>();
   for(int i=first+1;i<last;i++)moved.add(main.getChildAt(i));
   for(View item:moved){main.removeView(item);salaryPage.addView(item,new LinearLayout.LayoutParams(-1,-2));}
   main.removeView(payHeader);
   TextView salaryBack=text("‹  저장하고 돌아가기",15);salaryBack.setPadding(0,dp(14),0,dp(16));salaryPage.addView(salaryBack,0);
   TextView salaryHeading=text("급여 형태 · 적립 방식",18);salaryHeading.setTypeface(null,1);salaryPage.addView(salaryHeading,1);
   LinearLayout insuranceHeading=new LinearLayout(this);insuranceHeading.setGravity(Gravity.CENTER_VERTICAL);
   TextView headingText=text("4대보험 · 급여 형태",16);headingText.setTypeface(null,1);
   insuranceHeading.addView(headingText,new LinearLayout.LayoutParams(0,dp(48),1));
   TextView forward=text("급여 형태 · 적립 방식  ›",11);forward.setTextColor(0xffB9C7D7);forward.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
   insuranceHeading.addView(forward,new LinearLayout.LayoutParams(-2,dp(48)));
   int index=main.indexOfChild(insuranceHeader);main.removeView(insuranceHeader);
   LinearLayout.LayoutParams sectionLp=new LinearLayout.LayoutParams(-1,-2);sectionLp.topMargin=dp(24);main.addView(insuranceHeading,index,sectionLp);
   // Separate salary inputs from the insurance category with a subtle 1dp rule.
   sectionDividerBefore(main,insuranceHeading,24,12);
   // The salary subpage uses the same rule between pay amount and pay period.
   View salaryPeriod=parentRowDeep(salaryPage,periodStartButton);
   sectionDividerBefore(salaryPage,salaryPeriod,18,14);
   // Keep controls visibly distinct without adding extra cards.
   setBottomGap(salaryPage,parentRowDeep(salaryPage,payType),12);
   setBottomGap(salaryPage,parentRowDeep(salaryPage,hourlyRate),14);
   setBottomGap(salaryPage,parentRowDeep(salaryPage,annualSalary),14);
   View periodRowView=parentRowDeep(salaryPage,periodStartButton);
   setBottomGap(salaryPage,periodRowView,16);
   Runnable openSalary=()->{main.setVisibility(View.GONE);salaryPage.setVisibility(View.VISIBLE);scroll.post(()->scroll.scrollTo(0,0));};
   forward.setOnClickListener(v->openSalary.run());headingText.setOnClickListener(v->openSalary.run());
   salaryBack.setOnClickListener(v->{saveButton.performClick();if(!saveSucceeded)return;salaryPage.setVisibility(View.GONE);main.setVisibility(View.VISIBLE);scroll.post(()->scroll.scrollTo(0,0));});
   // Weekly hours and selected weekdays determine hours in the recurring pay period.
   for(CheckBox cb:workDays)cb.setOnCheckedChangeListener((button,checked)->updateHourlyEstimate());
   hourlyEstimate=text("",16);hourlyEstimate.setTextColor(0xff70D9A4);
   LinearLayout.LayoutParams estimateLp=new LinearLayout.LayoutParams(-1,-2);
   estimateLp.topMargin=dp(14);estimateLp.bottomMargin=dp(12);
   salaryPage.addView(hourlyEstimate,estimateLp);
   for(EditText e:new EditText[]{weeklyHours,hourlyRate})e.addTextChangedListener(new TextWatcher(){
    public void beforeTextChanged(CharSequence t,int a,int c,int f){}
    public void onTextChanged(CharSequence t,int a,int b,int c){updateHourlyEstimate();}
    public void afterTextChanged(Editable e){}
   });
   // The insurance breakdown is another subpage, not an expanding block.
   View calcButton=findText(main,"공제 상세 내역 보기 / 재계산");
   if(calcButton!=null){TextView detailBack=text("‹  뒤로 가기",15);detailBack.setPadding(0,dp(14),0,dp(16));add(insurancePage,detailBack);section(insurancePage,"공제 상세 내역");
    detailBack.setOnClickListener(v->{insurancePage.setVisibility(View.GONE);main.setVisibility(View.VISIBLE);scroll.post(()->scroll.scrollTo(0,0));});
    calcButton.setOnClickListener(v->{try{long[] a=InsuranceEstimate.calculate(effectiveGross(),pensionOn.isChecked(),healthOn.isChecked(),employmentOn.isChecked());TextView breakdown=text("국민연금: "+SalaryCalc.won(a[0])+"\n건강보험: "+SalaryCalc.won(a[1])+"\n장기요양보험: "+SalaryCalc.won(a[2])+"\n고용보험: "+SalaryCalc.won(a[3])+"\n산재보험: 근로자 부담 0원",14);if(insurancePage.getChildCount()>2)insurancePage.removeViewAt(2);add(insurancePage,breakdown);main.setVisibility(View.GONE);insurancePage.setVisibility(View.VISIBLE);scroll.post(()->scroll.scrollTo(0,0));}catch(Exception e){Toast.makeText(this,"급여 금액을 확인해",Toast.LENGTH_SHORT).show();}});
   }
   // Hourly inputs are not shown to monthly/annual salary users.
   View hourlyRow=parentRowDeep(salaryPage,hourlyRate);
   Runnable updateSalaryType=()->{int t=payType.getSelectedItemPosition();View annualRow=parentRowDeep(salaryPage,annualSalary);if(annualRow!=null)annualRow.setVisibility(t==2?View.VISIBLE:View.GONE);if(hourlyRow!=null)hourlyRow.setVisibility(t==1?View.VISIBLE:View.GONE);View hoursRow=parentRowDeep(salaryPage,weeklyHours);if(hoursRow!=null)hoursRow.setVisibility(t==1?View.VISIBLE:View.GONE);View daysRow=parentRowDeep(salaryPage,workDaysRow);if(daysRow!=null)daysRow.setVisibility(t==1?View.VISIBLE:View.GONE);hourlyEstimate.setVisibility(t==1?View.VISIBLE:View.GONE);updateHourlyEstimate();};
   payType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> a){}public void onItemSelected(AdapterView<?> a,View v,int pos,long id){updateSalaryType.run();}});
   updateSalaryType.run();
  }
  for(LinearLayout page:new LinearLayout[]{main,widgetPage,paydayPage})normalizeForm(page);
  // Uniform full-width settings cards with stacked labels and controls.
  styleSettingsCards(widgetPage,false);
  styleSettingsCards(paydayPage,true);

  themeChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> a){}public void onItemSelected(AdapterView<?> a,View v,int pos,long id){prefs.edit().putInt("widget_theme",selectedWidgetTheme()).putBoolean("transparent_bg",pos==2).apply();opacityBar.setEnabled(pos!=2);updateWidgetPreview();SalaryWidget.refresh(MainActivity.this);}});
  displayMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> a){}public void onItemSelected(AdapterView<?> a,View v,int pos,long id){prefs.edit().putInt("display_mode",pos).apply();SalaryWidget.refresh(MainActivity.this);}});
  opacityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int value,boolean user){opacityLabel.setText("배경 불투명도 "+(themeChoice.getSelectedItemPosition()==2?0:value)+"%");if(user){prefs.edit().putInt("widget_opacity",value).apply();SalaryWidget.refresh(MainActivity.this);}updateWidgetPreview();}});

  root.addView(main,new LinearLayout.LayoutParams(-1,-2));root.addView(salaryPage,new LinearLayout.LayoutParams(-1,-2));root.addView(insurancePage,new LinearLayout.LayoutParams(-1,-2));root.addView(settings,new LinearLayout.LayoutParams(-1,-2));
  salaryPage.setVisibility(View.GONE);insurancePage.setVisibility(View.GONE);settings.setVisibility(View.GONE);
 }
 LinearLayout newSettingsCard(){
  LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
  card.setPadding(dp(10),dp(12),dp(10),dp(12));card.setBackgroundColor(BG);
  return card;
 }
 void styleSettingsCards(LinearLayout page,boolean payday){
  ArrayList<View> elements=new ArrayList<>();
  for(int i=0;i<page.getChildCount();i++)elements.add(page.getChildAt(i));
  page.removeAllViews();
  LinearLayout card=newSettingsCard();int cardIndex=0;
  for(View element:elements){
   if(element.getParent() instanceof ViewGroup)((ViewGroup)element.getParent()).removeView(element);
   boolean isHeader=element instanceof TextView&&!(element instanceof Button)&&
    (((TextView)element).getText().toString().equals("위젯 디자인 미리보기")||
     ((TextView)element).getText().toString().equals("월급날 설정"));
   boolean preview=hasChild(element,widgetPreview);
   boolean holiday=hasChild(element,holidayRule);
   if(isHeader){continue;}
   if((!payday&&preview)||(payday&&holiday)){
    cardIndex++;
    TextView header=text(payday?"주말 지급일":"위젯 미리보기",16);
    header.setTypeface(null,1);card.addView(header);
   }
   if(element instanceof LinearLayout&&((LinearLayout)element).getOrientation()==LinearLayout.HORIZONTAL&&
     ((LinearLayout)element).getChildCount()==2&&
     ((LinearLayout)element).getChildAt(0) instanceof TextView){
    LinearLayout r=(LinearLayout)element;
    r.setOrientation(LinearLayout.VERTICAL);
    View labelView=r.getChildAt(0),control=r.getChildAt(1);
    LinearLayout.LayoutParams labelParams=new LinearLayout.LayoutParams(-1,dp(32));
    LinearLayout.LayoutParams controlParams=new LinearLayout.LayoutParams(-1,-2);
    control.setMinimumHeight(dp(48));
    labelView.setLayoutParams(labelParams);control.setLayoutParams(controlParams);
   }
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(13);
   card.addView(element,lp);
  }
  if(card.getChildCount()>0)addSettingsCard(page,card);
 }
 void addSettingsCard(LinearLayout page,LinearLayout card){
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
  lp.bottomMargin=dp(14);page.addView(card,lp);
 }
 View parentRowDeep(ViewGroup root,View target){for(int i=0;i<root.getChildCount();i++){View v=root.getChildAt(i);if(hasChild(v,target))return v;if(v instanceof ViewGroup){View found=parentRowDeep((ViewGroup)v,target);if(found!=null)return found;}}return null;}
 View findTextDeep(ViewGroup root,String value){for(int i=0;i<root.getChildCount();i++){View v=root.getChildAt(i);if(v instanceof TextView&&((TextView)v).getText().toString().equals(value))return v;if(v instanceof ViewGroup){View found=findTextDeep((ViewGroup)v,value);if(found!=null)return found;}}return null;}
 View parentRow(LinearLayout root,View target){for(int i=0;i<root.getChildCount();i++){View v=root.getChildAt(i);if(hasChild(v,target))return v;}return null;}
 View findText(LinearLayout root,String value){for(int i=0;i<root.getChildCount();i++){View v=root.getChildAt(i);if(v instanceof TextView&&((TextView)v).getText().toString().equals(value))return v;}return null;}
 void normalizeForm(LinearLayout page){for(int i=0;i<page.getChildCount();i++){
  View v=page.getChildAt(i);if(v instanceof LinearLayout){LinearLayout r=(LinearLayout)v;
   if(r.getOrientation()==LinearLayout.HORIZONTAL&&r.getChildCount()==2&&r.getChildAt(0) instanceof TextView){
    View first=r.getChildAt(0),second=r.getChildAt(1);
    if(first.getLayoutParams() instanceof LinearLayout.LayoutParams&&second.getLayoutParams() instanceof LinearLayout.LayoutParams){
     LinearLayout.LayoutParams a=(LinearLayout.LayoutParams)first.getLayoutParams(),b=(LinearLayout.LayoutParams)second.getLayoutParams();
     if(a.width==0&&b.width==0){a.weight=1.05f;b.weight=1.55f;a.height=-2;b.height=-2;first.setLayoutParams(a);second.setLayoutParams(b);}
    }
   }
  }
  if(v instanceof Button){((Button)v).setAllCaps(false);((Button)v).setTextSize(14);v.setMinimumHeight(dp(48));}
 }}
 boolean hasChild(View parent,View target){if(parent==target)return true;if(parent instanceof android.view.ViewGroup){android.view.ViewGroup g=(android.view.ViewGroup)parent;for(int i=0;i<g.getChildCount();i++)if(hasChild(g.getChildAt(i),target))return true;}return false;}
 int findChildIndex(LinearLayout page,View child){for(int i=0;i<page.getChildCount();i++)if(hasChild(page.getChildAt(i),child))return i;return page.getChildCount()-1;}
 void showSettingsPage(LinearLayout[] pages,LinearLayout navigation,int page,ScrollView scroll){for(int i=0;i<pages.length;i++)pages[i].setVisibility(i==page?View.VISIBLE:View.GONE);scroll.post(()->scroll.scrollTo(0,0));}

 Spinner spinner(String[] options,int index){Spinner sp=new Spinner(this);ArrayAdapter<String> ad=new ArrayAdapter<>(this,R.layout.spinner_item_soft,options);ad.setDropDownViewResource(R.layout.spinner_dropdown_soft);sp.setAdapter(ad);styleSpinner(sp);sp.setSelection(Math.max(0,Math.min(index,options.length-1)));return sp;}
 EditText plainField(String val){EditText e=new EditText(this);e.setText(val);e.setTextColor(SOFT_TEXT);e.setSingleLine(true);styleField(e);return e;}
 void footerAction(int which){
  if(which!=0){new AlertDialog.Builder(this).setTitle("개인정보처리방침").setPositiveButton("닫기",null).show();return;}
  LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setGravity(Gravity.CENTER_HORIZONTAL);content.setPadding(dp(16),dp(20),dp(16),dp(12));
  ImageView avatar=new ImageView(this);avatar.setImageResource(R.drawable.developer_profile);avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
  LinearLayout.LayoutParams avatarLp=new LinearLayout.LayoutParams(dp(156),dp(156));avatarLp.gravity=Gravity.CENTER_HORIZONTAL;avatarLp.bottomMargin=dp(14);content.addView(avatar,avatarLp);
  TextView insta=text("@tsukiwotabetayoru",14);insta.setTextColor(0xff4D7CFF);insta.setPaintFlags(insta.getPaintFlags()|android.graphics.Paint.UNDERLINE_TEXT_FLAG);insta.setGravity(Gravity.CENTER);insta.setPadding(0,dp(4),0,dp(10));insta.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.instagram.com/tsukiwotabetayoru?vrfl=YmQ4ZHJnb3kzaGNm"));try{startActivity(i);}catch(Exception ignored){}});content.addView(insta);
  View topLine=new View(this);topLine.setBackgroundColor(0xff626262);LinearLayout.LayoutParams lineLp=new LinearLayout.LayoutParams(-1,dp(1));lineLp.topMargin=dp(6);lineLp.bottomMargin=dp(18);content.addView(topLine,lineLp);
  String[] paragraphs={
   "안녕하세요. 감사합니다. 개발자 전혜나입니다.",
   "저는 백화점에서 일하고 있습니다. 시향지를 나눠드리다가 손님이 뜸한 틈에 짬짬이 수정하며 만들어 본 앱이 여기까지 왔네요.",
   "긴말을 늘어놓는 것도 식상하겠지요?",
   "저는 일이 힘들 때마다 위젯에 조금씩 쌓여가는 월급을 보며 작게나마 위안을 얻곤 합니다.\n여러분도 그랬으면 좋겠다는 마음에 25,000원을 들여 스토어에 앱을 등록하고 배포했습니다.",
   "다들 고생이 많습니다. 다들 잘하고 있습니다.",
   "우리는 하루 종일 전력을 다하고 있습니다. 그것만으로도 충분히 숭고하고 대단한 일이지만,\n그런 하루가 이틀이 되고 사흘이 되고, 한 달이 되고 일 년이 되면 지치지 않을 수가 없습니다.\n단거리를 달릴 때 사용해야 할 전력을 우리는 장거리를 달리며 쏟아붓고 있으니까요.",
   "그런데 주위를 둘러보면 모두 비슷하게 살아가고 있으니, 힘들다는 말조차 쉽게 꺼내지 못합니다. 오히려 그 무게를 버거워하는 자신이 나약한 건 아닐까 자책하기도 하지요.",
   "저는 그게 참 안타깝습니다.",
   "이미 최선을 다하고 있는 사람들이, 자신이 부족해서 힘든 거라고 생각하는 현실이요.\n물론 저 역시 늘 같은 번뇌 속에서 살아갑니다.",
   "그런 마음을 조금이라도 덜어내려고 이런 앱도 만들어 보고,  글도 남기는 것 같습니다.",
   "다들 잘하고 있습니다. 모두 다 괜찮습니다.\n너무 자신을 몰아세우지 않았으면 좋겠습니다.",
   "오늘 하루도 무탈하시길 바랍니다!"};
  for(String paragraph:paragraphs)developerParagraph(content,paragraph,14);
  View divider=new View(this);divider.setBackgroundColor(0xff707070);LinearLayout.LayoutParams dividerLp=new LinearLayout.LayoutParams(-1,dp(1));dividerLp.topMargin=dp(14);dividerLp.bottomMargin=dp(22);content.addView(divider,dividerLp);
  developerParagraph(content,"* 건의 사항 및 질문은 인스타그램 DM 요청으로 부탁드립니다.",14);
  developerParagraph(content,"* 마지막으로 저에게도 작은 즐거움 하나 만들어 주시겠어요?\n위젯에 조금씩 쌓이는 월급을 보며 힘을 얻는 것처럼, 제 통장에도 여러분의 이름이 하나둘 쌓이면 좋겠습니다.\n일하다가 잠깐씩 알림을 열어 보고, 낯선 이름들을 발견하면서 오늘은 이분들과 함께라는 생각이 들면 기쁠 것 같습니다.\n메시지나 메일보다 빠르고 편하면서, 사생활이 공유되지 않는 방식이\n의외로 계좌번호밖에 없길래 후원 요구처럼 보이는 방식이 채택 되었네요 😅",14);
  developerParagraph(content,"딱 '1원'만 남겨주세요, 여러분의 이름이 제 하루의 자산입니다!",14);
  LinearLayout accountRow=new LinearLayout(this);accountRow.setGravity(Gravity.CENTER);accountRow.setOrientation(LinearLayout.HORIZONTAL);
  TextView account=text("카카오뱅크 3333011457979 전혜나",14);account.setGravity(Gravity.CENTER);accountRow.addView(account,new LinearLayout.LayoutParams(0,-2,1));
  TextView copy=text("▢",19);copy.setContentDescription("계좌번호 복사");copy.setGravity(Gravity.CENTER);copy.setPadding(dp(6),dp(8),dp(6),dp(8));copy.setOnClickListener(v->{android.content.ClipboardManager cb=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);cb.setPrimaryClip(ClipData.newPlainText("계좌번호","3333011457979"));Toast.makeText(this,"계좌번호가 복사됐어",Toast.LENGTH_SHORT).show();});accountRow.addView(copy);
  LinearLayout.LayoutParams accountLp=new LinearLayout.LayoutParams(-1,-2);accountLp.topMargin=dp(3);accountLp.bottomMargin=dp(22);content.addView(accountRow,accountLp);
  ScrollView scroller=new ScrollView(this);scroller.setFillViewport(false);scroller.addView(content);
  AlertDialog dialog=new AlertDialog.Builder(this).setView(scroller).create();
  TextView close=text("닫기",13);close.setGravity(Gravity.CENTER);close.setPadding(dp(24),dp(9),dp(24),dp(9));close.setBackground(roundedBox(0xff424242,0xff999999));close.setOnClickListener(v->dialog.dismiss());
  LinearLayout wrapper=new LinearLayout(this);wrapper.setOrientation(LinearLayout.VERTICAL);wrapper.setBackgroundColor(0xff424242);wrapper.addView(scroller,new LinearLayout.LayoutParams(-1,0,1));LinearLayout.LayoutParams closeLp=new LinearLayout.LayoutParams(-2,-2);closeLp.gravity=Gravity.CENTER;closeLp.topMargin=dp(8);closeLp.bottomMargin=dp(12);wrapper.addView(close,closeLp);
  dialog.setView(wrapper);dialog.show();dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.92f),(int)(getResources().getDisplayMetrics().heightPixels*.82f));
 }
 void developerParagraph(LinearLayout content,String value,int fontSize){TextView t=text(value,fontSize);t.setGravity(Gravity.CENTER);t.setTextColor(0xffFFFFFF);t.setTextSize(fontSize);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(17);content.addView(t,lp);}


 int blendPreview(int front,int back,int opacity){float a=Math.max(0,Math.min(100,opacity))/100f;int r=Math.round(Color.red(front)*a+Color.red(back)*(1-a));int g=Math.round(Color.green(front)*a+Color.green(back)*(1-a));int b=Math.round(Color.blue(front)*a+Color.blue(back)*(1-a));return Color.rgb(r,g,b);}
 int selectedWidgetTheme(){int choice=themeChoice.getSelectedItemPosition();return choice==0?4:choice==1?3:5;}
 void updateWidgetPreview(){if(widgetPreview==null||themeChoice==null||previewSize==null||opacityBar==null)return;
 int theme=selectedWidgetTheme();int base=SalaryWidget.themeColor(theme);boolean light=theme==4;int opacity=themeChoice.getSelectedItemPosition()==2?0:opacityBar.getProgress();int textColor=light&&opacity>=65?0xff243247:0xffF5F7FC;int secondary=light&&opacity>=65?0xff526277:0xffDCE6F3;
 widgetPreview.removeAllViews();GradientDrawable bg=new GradientDrawable();bg.setColor(blendPreview(base,0xff303D4D,opacity));bg.setCornerRadius(dp(4));widgetPreview.setBackground(bg);
 TextView t=text("월급이 자라는 중..💵",11);t.setTextColor(textColor);widgetPreview.addView(t);
 LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);TextView amount=text(preview!=null?preview.getText().toString():"0원",Math.min(24,17+previewSize.getSelectedItemPosition()*2));amount.setTextColor(0xffD71935);amount.setTypeface(android.graphics.Typeface.create("sans-serif-black",1));amount.setSingleLine(true);amount.setAutoSizeTextTypeUniformWithConfiguration(12,24,1,1);r.addView(amount,new LinearLayout.LayoutParams(0,-2,1));TextView percent=text(previewPercent!=null?previewPercent.getText().toString():"0%",11);percent.setTextColor(secondary);r.addView(percent);widgetPreview.addView(r);
 ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(10000);LocalDateTime now=LocalDateTime.now();int days=YearMonth.from(now).lengthOfMonth();boolean last=now.getDayOfMonth()>=days-4;GradientDrawable track=roundedBox(light?0xff40516A:0xff293B55,light?0xff40516A:0xff293B55);GradientDrawable fill=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,last?new int[]{0xff236B43,0xff70D9A4,0xffF6A3A0,0xffB50016}:new int[]{0xff236B43,0xff70D9A4});fill.setCornerRadius(dp(5));android.graphics.drawable.LayerDrawable layers=new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{track,new android.graphics.drawable.ClipDrawable(fill,Gravity.LEFT,android.graphics.drawable.ClipDrawable.HORIZONTAL)});layers.setId(0,android.R.id.background);layers.setId(1,android.R.id.progress);bar.setProgressDrawable(layers);int value=0;try{if(previewPercent!=null&&!previewPercent.getText().toString().trim().isEmpty())value=(int)(Double.parseDouble(previewPercent.getText().toString().replace("%",""))*100);}catch(NumberFormatException ignored){value=0;}bar.setProgress(Math.max(0,Math.min(10000,value)));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(6));lp.topMargin=dp(5);widgetPreview.addView(bar,lp);
 }
 @Override protected void onDestroy(){handler.removeCallbacks(ticker);super.onDestroy();}
}