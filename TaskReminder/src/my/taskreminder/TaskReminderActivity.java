package my.taskreminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TaskReminderActivity extends Activity {

	private static final int EXIT_DIALOG = 1;
	private Dialog exitDlg;

	private Button btn_taskList, btn_settings, btn_exit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startService();

		setContentView(R.layout.main);

		btn_taskList = (Button) findViewById(R.id.TasksList);
		btn_settings = (Button) findViewById(R.id.Settings);
		btn_exit = (Button) findViewById(R.id.Exit);

		btn_exit.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				TaskReminderActivity.this.showDialog(EXIT_DIALOG);
			}
		});
		btn_settings.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(TaskReminderActivity.this,
						SettingsActivity.class);
				startActivity(intent);
			}
		});
		btn_taskList.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(TaskReminderActivity.this,
						TaskListActivity.class);
				startActivity(intent);
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.wannaexit)
				.setCancelable(true)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								stopService();
								// there are two ways to perform exit
								// and indeed they are equal.
								// system prefers to call OnCreate so service
								// will start again
								// TaskReminderActivity.this.moveTaskToBack(true);
								TaskReminderActivity.this.finish();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		exitDlg = builder.create();

	}

	private void startService() {
		startService(new Intent(TaskReminderActivity.this,
				TaskReminderService.class));
	}

	private void stopService() {
		// And cancel the alarm.
		// AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		// am.cancel(mAlarmSender);
		stopService(new Intent(TaskReminderActivity.this,
				TaskReminderService.class));
		// Tell the user about what we did.
		Toast.makeText(this, R.string.serviceStoped, Toast.LENGTH_LONG).show();
		Intent intent = new Intent(this, TaskReminderService.class);
		intent.putExtra("stop", true);
		startService(intent);
	}

	@Override
	protected void onStop() {
		// just for case will save at any stop
		((TaskRemainderApplication) getApplication()).getAppSettingsManager()
				.saveSettings();
		super.onStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == EXIT_DIALOG)
			return exitDlg;
		return super.onCreateDialog(id);
	}

}
