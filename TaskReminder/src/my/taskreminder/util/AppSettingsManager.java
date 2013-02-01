package my.taskreminder.util;

import my.taskreminder.beans.AppSettings;
import my.taskreminder.beans.Constants;
import android.content.Context;
import android.content.SharedPreferences;

public class AppSettingsManager {
	private Context context;
	private AppSettings appSettings;

	public AppSettingsManager(Context ctx) {
		this.context = ctx;
	}

	public void loadSettings() {
		SharedPreferences settings = context.getSharedPreferences(
				Constants.APP_INTERNAL_NAME, Context.MODE_PRIVATE);
		appSettings = new AppSettings();
		appSettings.setPlaySound(settings.getBoolean(Constants.PLAY_SOUND, true));
		appSettings.setRemoveCompleted(settings.getBoolean(Constants.REMOVE_COPLETE,
				true));
		appSettings.setRemoveMissed(settings.getBoolean(Constants.REMOVE_MISSED, true));
		appSettings
				.setDisplayDialog(settings.getBoolean(Constants.DISPLAY_DIALOG, true));

	}

	/**
	 * @return the appSettings
	 */
	public AppSettings getAppSettings() {
		return appSettings;
	}

	public void saveSettings() {
		SharedPreferences settings = context.getSharedPreferences(
				Constants.APP_INTERNAL_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = settings.edit();
		prefEditor.putBoolean(Constants.PLAY_SOUND, appSettings.isPlaySound());
		prefEditor.putBoolean(Constants.REMOVE_COPLETE,
				appSettings.isRemoveCompleted());
		prefEditor.putBoolean(Constants.REMOVE_MISSED, appSettings.isRemoveMissed());
		prefEditor.putBoolean(Constants.DISPLAY_DIALOG, appSettings.isDisplayDialog());
		prefEditor.commit();
	}

}
