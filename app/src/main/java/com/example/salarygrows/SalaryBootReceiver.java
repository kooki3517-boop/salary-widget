package com.example.salarygrows;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class SalaryBootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){
  if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
   SalaryNotifications.schedule(context);
   SalaryWidget.refresh(context);
  }
 }
}
