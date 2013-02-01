package my.taskreminder;

import java.util.ArrayList;
import java.util.List;

import my.taskreminder.beans.Constants;
import my.taskreminder.entity.Task;
import my.taskreminder.sql.MySQLiteHelper;
import my.taskreminder.sql.TaskDataSource;
import my.taskreminder.util.DateTimeFormater;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.Toast;

public class TaskReminderServiceMyTimer extends Service {
	public static final int ServiceID = R.string.app_name;

	NotificationManager notificationManager;
	private boolean keepRunning = true;
	private List<Task> tasks;
	private TaskDataSource dataSource;
	private long nextNotification;
	private List<Task> tasksToFire = new ArrayList<Task>();

	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// show ongoing the icon in the status bar
		showPermanetNotification(getString(R.string.not_scheduled));

		// load tasks
		dataSource = new TaskDataSource(this);

		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.
		Thread thr = new Thread(null, mTask, Constants.TASK_REMAINDER_SERVICE);
		thr.start();
	}

	@Override
	public void onDestroy() {

		// Cancel the notification -- we use the same ID that we had used to
		// start it
		notificationManager.cancel(ServiceID);
		// notify thread to stop
		keepRunning = false;
		// Tell the user we stopped.
		Toast.makeText(this, R.string.serviceStoped, Toast.LENGTH_SHORT).show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	/**
	 * The function that runs in our worker thread
	 */
	Runnable mTask = new Runnable() {
		public void run() {
			// Get task

			// Normally we would do some work here... for our sample, we will
			// just sleep for 30 seconds.
			long interval = 30 * 1000;
			while (keepRunning) {
				synchronized (mBinder) {
					try {

						scheduleTasks();
						if (nextNotification != 0
								&& nextNotification < Long.MAX_VALUE)
							showPermanetNotification(DateTimeFormater
									.longToDate(nextNotification)
									+ " "
									+ DateTimeFormater
											.longToTime(nextNotification));
						else
							showPermanetNotification(getString(R.string.not_scheduled));
						if (nextNotification <= System.currentTimeMillis()) {
							dataSource.open();
							for (Task task : tasksToFire) {
								showReminderNotification(nextNotification,
										task.getName(), task.getMessage());
								task.setStatus(MySQLiteHelper.STATUS_COMPLETED);
								dataSource.updateTask(task);
							}
							dataSource.close();
							nextNotification = 0;
						}
						mBinder.wait(interval);
					} catch (Exception e) {
					}
				}
			}
		}
	};

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

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
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
			intent.setClass(TaskReminderServiceMyTimer.this,
					TaskInfoDialogActivity.class);
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

	/**
	 * This is the object that receives interactions from clients. See
	 * RemoteService for a more complete example.
	 */
	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};
}