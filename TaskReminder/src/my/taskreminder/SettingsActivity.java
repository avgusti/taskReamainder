package my.taskreminder;

import my.taskreminder.beans.AppSettings;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class SettingsActivity extends Activity {
	private Button btn_Save, btn_Cancel;
	private CheckBox chk_sound, chk_completed, chk_missed;
	private RadioButton rb_display, rb_notification;
	private AppSettings appSettings;
	private TaskRemainderApplication application;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);

		application = ((TaskRemainderApplication) getApplication());
		appSettings = application.getAppSettingsManager().getAppSettings();

		btn_Save = (Button) findViewById(R.id.save);
		btn_Cancel = (Button) findViewById(R.id.cancel);

		chk_sound = (CheckBox) findViewById(R.id.sound);
		chk_completed = (CheckBox) findViewById(R.id.removecompleted);
		chk_missed = (CheckBox) findViewById(R.id.removemissed);

		rb_notification = (RadioButton) findViewById(R.id.shownotification);
		rb_display = (RadioButton) findViewById(R.id.showdialog);

		btn_Cancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				SettingsActivity.this.finish();
			}
		});

		btn_Save.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateSettings();
				SettingsActivity.this.finish();
			}
		});

		populateSettings();

	}

	// not thread safe, don't expect two simultaneous clicks
	// anyway - will be synchronized on exit
	private boolean isUnderPopulation = false;

	private void populateSettings() {
		isUnderPopulation = true;
		chk_sound.setChecked(appSettings.isPlaySound());
		chk_completed.setChecked(appSettings.isRemoveCompleted());
		chk_missed.setChecked(appSettings.isRemoveMissed());
		rb_display.setChecked(appSettings.isDisplayDialog());
		rb_notification.setChecked(!appSettings.isDisplayDialog());
		isUnderPopulation = false;
	}

	private void updateSettings() {
		if (!isUnderPopulation) {
			appSettings.setPlaySound(chk_sound.isChecked());
			appSettings.setRemoveCompleted(chk_completed.isChecked());
			appSettings.setRemoveMissed(chk_missed.isChecked());
			appSettings.setDisplayDialog(rb_display.isChecked());
		}
	}
}
