package my.taskreminder.util;


import my.taskreminder.R;
import my.taskreminder.TaskInfoDialogActivity;
import my.taskreminder.TaskRemainderApplication;
import my.taskreminder.TaskReminderActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class TaskNotificationManager {
	public static final int ServiceID = R.string.app_name;
	Context context;
	NotificationManager notificationManager;
	private TaskRemainderApplication application;
	public TaskNotificationManager(TaskRemainderApplication application) {
		this.application=application;
		this.context=application.getApplicationContext();
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	/**
	 * Show a notification while this service is running.
	 */
	public void showPermanetNotification(String msg) {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = context.getText(R.string.showmain);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.clock, text, 0);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, TaskReminderActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, context.getText(R.string.serviceName),
				text + "next :" + msg, contentIntent);
		notification.flags |= Notification.DEFAULT_SOUND
				| Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		notificationManager.notify(ServiceID, notification);
	}

	public void showReminderNotification(long time, CharSequence taskName,
			CharSequence message) {
		if (application
				.getAppSettingsManager().getAppSettings().isDisplayDialog()) {
			Intent intent = new Intent();
			intent.setClass(context, TaskInfoDialogActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(context.getString(R.string.taskname), taskName);
			intent.putExtra(context.getString(R.string.taskmeassage), message);
			intent.putExtra(context.getString(R.string.tasktime), time);
			// No sound ^-) lazy implementation. Notification option will play
			context.startActivity(intent);

		} else {
			// Set the icon, scrolling text and timestamp
			Notification notification = new Notification(R.drawable.accept,
					taskName, 0);
			// show something on click
			// can't show edit/view form because task may be deleted
			Intent taskInfoIntent = new Intent(context,
					TaskInfoDialogActivity.class);

			taskInfoIntent.putExtra(context.getString(R.string.taskname), taskName);
			taskInfoIntent.putExtra(context.getString(R.string.taskmeassage), message);
			taskInfoIntent.putExtra(context.getString(R.string.tasktime), time);

			// The PendingIntent to launch our activity if the user selects this
			// notification
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					taskInfoIntent, 0);

			// Set the info for the views that show in the notification panel.
			notification.setLatestEventInfo(context, taskName, message,
					contentIntent);
			notification.flags |= (application
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
}
