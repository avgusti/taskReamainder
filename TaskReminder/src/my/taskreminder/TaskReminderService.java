package my.taskreminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import my.taskreminder.entity.Task;
import my.taskreminder.sql.MySQLiteHelper;
import my.taskreminder.sql.TaskDataSource;
import my.taskreminder.util.DateTimeFormater;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TaskReminderService extends IntentService {
	public TaskReminderService() {
		super(TaskReminderService.class.getSimpleName());
	}

	public static final int ServiceID = R.string.app_name;

	NotificationManager notificationManager;
	private List<Task> tasks;
	private TaskDataSource dataSource;
	private long nextNotification;
	private List<Task> tasksToFire = new ArrayList<Task>();

	private boolean stopForeva = false;

	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// show ongoing the icon in the status bar
		// showPermanetNotification(getString(R.string.not_scheduled));
		dataSource = new TaskDataSource(this);
		super.onCreate();
		// Intent intent = new Intent(this, this.getClass());
		// /PendingIntent pendingIntent = PendingIntent.getService(this, 0,
		// intent,
		// PendingIntent.FLAG_UPDATE_CURRENT);

		// AlarmManager alarmManager = (AlarmManager)
		// getSystemService(Context.ALARM_SERVICE);
		// nextNotification=Calendar.getInstance().getTimeInMillis()+1000;
		// alarmManager.set(AlarmManager.RTC_WAKEUP, nextNotification,
		// pendingIntent);
	}

	@Override
	public void onDestroy() {
		if (stopForeva) {
			// Cancel the notification -- we use the same ID that we had used to
			// start it
			notificationManager.cancel(ServiceID);
			// notify thread to stop
			// Tell the user we stopped.
			Toast.makeText(this, R.string.serviceStoped, Toast.LENGTH_SHORT)
					.show();
		}
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, startId, startId);
		return START_STICKY;
	}

	private void scheduleTasks() {
		dataSource.open();
		tasks = dataSource.getAllTasks();
		// mark missed
		long date = DateTimeFormater.getDate();
		long time = DateTimeFormater.getTime();
		int weekday = DateTimeFormater.getWeekDay();
		boolean delCompleted = ((TaskRemainderApplication) getApplication())
				.getAppSettingsManager().getAppSettings().isRemoveCompleted();
		boolean delMissed = ((TaskRemainderApplication) getApplication())
				.getAppSettingsManager().getAppSettings().isRemoveMissed();
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
		// nextNotification keeps next date/time
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showPermanetNotification(String msg) {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.showmain);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.clock, text, 0);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, TaskReminderActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.serviceName),
				text + "next :" + msg, contentIntent);
		notification.flags |= Notification.DEFAULT_SOUND
				| Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		notificationManager.notify(ServiceID, notification);

	}

	private void showReminderNotification(long time, CharSequence taskName,
			CharSequence message) {
		if (((TaskRemainderApplication) getApplication())
				.getAppSettingsManager().getAppSettings().isDisplayDialog()) {
			Intent intent = new Intent();
			intent.setClass(this, TaskInfoDialogActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(getString(R.string.taskname), taskName);
			intent.putExtra(getString(R.string.taskmeassage), message);
			intent.putExtra(getString(R.string.tasktime), time);
			// No sound ^-) lazy implementation. Notification option will play
			startActivity(intent);

		} else {
			// Set the icon, scrolling text and timestamp
			Notification notification = new Notification(R.drawable.accept,
					taskName, 0);
			// show something on click
			// can't show edit/view form because task may be deleted
			Intent taskInfoIntent = new Intent(this,
					TaskInfoDialogActivity.class);

			taskInfoIntent.putExtra(getString(R.string.taskname), taskName);
			taskInfoIntent.putExtra(getString(R.string.taskmeassage), message);
			taskInfoIntent.putExtra(getString(R.string.tasktime), time);

			// The PendingIntent to launch our activity if the user selects this
			// notification
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					taskInfoIntent, 0);

			// Set the info for the views that show in the notification panel.
			notification.setLatestEventInfo(this, taskName, message,
					contentIntent);
			notification.flags |= (((TaskRemainderApplication) getApplication())
					.getAppSettingsManager().getAppSettings().isPlaySound() ? Notification.DEFAULT_SOUND
					: 0)
					| Notification.FLAG_AUTO_CANCEL;
			// Send the notification.
			// We use a layout id because it is a unique number. We use it later
			// to cancel.
			notificationManager.notify(ServiceID + (int) time % 100000,
					notification);
		}
	}

	@Override
	protected void onHandleIntent(Intent handleintent) {
		Intent intent = new Intent(this, this.getClass());
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// reset Schedule
		alarmManager.cancel(pendingIntent);

		if (handleintent.getBooleanExtra("stop", false)) {
			stopForeva = true;
			this.stopSelf();
			return;
		}

		// search for task to fire (there may be several tasks so prefer to
		// rescan instead of storing id's)
		scheduleTasks();
		if (nextNotification != 0 && nextNotification < Long.MAX_VALUE)
			showPermanetNotification(DateTimeFormater
					.longToDate(nextNotification)
					+ " "
					+ DateTimeFormater.longToTime(nextNotification));
		else
			showPermanetNotification(getString(R.string.not_scheduled));
		if (nextNotification <= System.currentTimeMillis()) {
			dataSource.open();
			for (Task task : tasksToFire) {
				showReminderNotification(nextNotification, task.getName(),
						task.getMessage());
				task.setStatus(MySQLiteHelper.STATUS_COMPLETED);
				dataSource.updateTask(task);
			}
			dataSource.close();
		}
		// look up for new time
		scheduleTasks();
		if (nextNotification < Long.MAX_VALUE
				&& nextNotification > Calendar.getInstance().getTimeInMillis()) {
			alarmManager.set(AlarmManager.RTC_WAKEUP, nextNotification,
					pendingIntent);
		}
	}

}