package my.taskreminder.broadcast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import my.taskreminder.R;
import my.taskreminder.TaskRemainderApplication;
import my.taskreminder.entity.Task;
import my.taskreminder.sql.MySQLiteHelper;
import my.taskreminder.sql.TaskDataSource;
import my.taskreminder.util.DateTimeFormater;
import my.taskreminder.util.TaskNotificationManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	public static final int ServiceID = R.string.app_name;
	Context context;
	private List<Task> tasks;
	private TaskDataSource dataSource;
	private long nextNotification;
	private List<Task> tasksToFire = new ArrayList<Task>();
	private TaskNotificationManager notificationManager;
	private static TaskRemainderApplication application;
	
	public AlarmReceiver(TaskRemainderApplication application) {
		AlarmReceiver.application=application;
		 notificationManager=new TaskNotificationManager(application);
		// get a Calendar object with current time
		 Calendar cal = Calendar.getInstance();
		 // add 5 minutes to the calendar object
		 cal.add(Calendar.SECOND, 5);
		 Intent intent = new Intent(context, AlarmReceiver.class);
		 // In reality, you would want to have a static variable for the request code instead of 192837
		 PendingIntent sender = PendingIntent.getBroadcast(context, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		 // Get the AlarmManager service
		 AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		 am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		try {

			scheduleTasks();
			if (nextNotification != 0
					&& nextNotification < Long.MAX_VALUE)
				notificationManager.showPermanetNotification(DateTimeFormater
						.longToDate(nextNotification)
						+ " "
						+ DateTimeFormater
								.longToTime(nextNotification));
			else
				notificationManager.showPermanetNotification(context.getString(R.string.not_scheduled));
			if (nextNotification <= System.currentTimeMillis()) {
				dataSource.open();
				for (Task task : tasksToFire) {
					notificationManager.showReminderNotification(nextNotification,
							task.getName(), task.getMessage());
					task.setStatus(MySQLiteHelper.STATUS_COMPLETED);
					dataSource.updateTask(task);
				}
				dataSource.close();
				nextNotification = 0;
			}
			
		} catch (Exception e) {
			Toast.makeText(
					context,
					"There was an error somewhere, but we still received an alarm",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();

		}
	}
	private void scheduleTasks() {
		dataSource.open();
		tasks = dataSource.getAllTasks();
		// mark missed
		long date = DateTimeFormater.getDate();
		long time = DateTimeFormater.getTime();
		int weekday = DateTimeFormater.getWeekDay();
		boolean delCompleted = application.getAppSettingsManager().getAppSettings().isRemoveCompleted();
		boolean delMissed = application.getAppSettingsManager().getAppSettings().isRemoveMissed();
		nextNotification = Long.MAX_VALUE;
		tasksToFire.clear();
		for (Task task : tasks) {
			// check for options
			if (task.getStatus() == MySQLiteHelper.STATUS_NEW) {
				if ((task.getType() == MySQLiteHelper.TYPE_DATE && (task
						.getDate() < date || (task.getDate() == date && task
						.getTime() < time - 2 * 60 * 1000)))
						|| (task.getType() == MySQLiteHelper.TYPE_WEEKDAY
								&& task.getDay() == weekday && task.getTime() < time - 2 * 60 * 1000)) {
					task.setStatus(MySQLiteHelper.STATUS_MISSED);
					if (!delMissed) {
						dataSource.updateTask(task);
					}
				}
				if (task.getStatus() == MySQLiteHelper.STATUS_NEW) {
					// start time
					long start = Long.MAX_VALUE;
					if (task.getType() == MySQLiteHelper.TYPE_DATE)
						start = task.getDate() + task.getTime();
					if (task.getType() == MySQLiteHelper.TYPE_WEEKDAY) {
						start = DateTimeFormater.getaAdjustedToWeekDayDate(
								(int) task.getDay(), task.getTime())
								+ task.getTime();
					}
					String log=(new Date(start)).toString();
					if (start <= nextNotification) {
						nextNotification = start;
						tasksToFire.clear();
					}
					if (start == nextNotification) {
						
						tasksToFire.add(task);
					}
				}
			}

			if (delCompleted
					&& task.getStatus() == MySQLiteHelper.STATUS_COMPLETED) {
				dataSource.deleteTask(task);
			}

			if (delMissed && task.getStatus() == MySQLiteHelper.STATUS_MISSED) {
				dataSource.deleteTask(task);
			}

		}

		dataSource.close();

	}

	
}