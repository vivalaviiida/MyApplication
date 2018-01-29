package com.wen.gun.service;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;

import com.wen.gun.activity.MonitorActivity;
import com.wen.gun.utils.L;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MonitorService extends Service {

	boolean flag = true;// 用于停止线程
	private ActivityManager activityManager;
	private Timer timer;
	private TimerTask task = new TimerTask() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (activityManager == null) {
				activityManager = (ActivityManager) MonitorService.this
						.getSystemService(ACTIVITY_SERVICE);
			}

//			List<RecentTaskInfo> recentTasks = activityManager.getRecentTasks(
//					2, ActivityManager.RECENT_WITH_EXCLUDED);
			//	RecentTaskInfo recentInfo = recentTasks.get(0);
//			Context context = getApplication();
//			Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//			context.startActivity(intent);
			String recentTaskName = getTopAppPackageName(getBaseContext());

			if (!recentTaskName.equals("com.wen.gun")
					) {
				L.i("MonitorService", "Yes--recentTaskName=" + recentTaskName);
				Intent intentNewActivity = new Intent(MonitorService.this,
						MonitorActivity.class);
				intentNewActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentNewActivity);

			}else{
				L.i("MonitorService", "No--recentTaskName="+recentTaskName);

			}
		}


	};


	public static String getTopAppPackageName(Context context) {
		String packageName = "";
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		try {
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
				List<ActivityManager.RunningTaskInfo> rti = activityManager.getRunningTasks(1);
				packageName = rti.get(0).topActivity.getPackageName();
			} else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
				List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
				if (processes.size() == 0) {
					return packageName;
				}
				for (ActivityManager.RunningAppProcessInfo process : processes) {
					if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
						return process.processName;
					}
				}
			} else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
				final long end = System.currentTimeMillis();
				final UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService( Context.USAGE_STATS_SERVICE);
				if (null == usageStatsManager) {
					return packageName;
				}
				final UsageEvents events = usageStatsManager.queryEvents((end - 60 * 1000), end);
				if (null == events) {
					return packageName;
				}
				UsageEvents.Event usageEvent = new UsageEvents.Event();
				UsageEvents.Event lastMoveToFGEvent = null;
				while (events.hasNextEvent()) {
					events.getNextEvent(usageEvent);
					if (usageEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
						lastMoveToFGEvent = usageEvent;
					}
				}
				if (lastMoveToFGEvent != null) {
					packageName = lastMoveToFGEvent.getPackageName();
				}
			}
		}catch (Exception ignored){
		}
		return packageName;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (flag == true) {
			timer = new Timer();
			timer.schedule(task, 0, 100);
			flag = false;
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		timer.cancel();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
