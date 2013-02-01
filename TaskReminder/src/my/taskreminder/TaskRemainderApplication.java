package my.taskreminder;

import my.taskreminder.util.AppSettingsManager;
import android.app.Application;

public class TaskRemainderApplication extends Application {
	private AppSettingsManager appSettingsManager;
	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		 appSettingsManager=new AppSettingsManager(this);
	        appSettingsManager.loadSettings();
		super.onCreate();
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public void onTerminate() {
		appSettingsManager.saveSettings();
		super.onTerminate();
	}

	/**
	 * @return the appSettingsManager
	 */
	public AppSettingsManager getAppSettingsManager() {
		return appSettingsManager;
	}

	/**
	 * @param appSettingsManager the appSettingsManager to set
	 */
	public void setAppSettingsManager(AppSettingsManager appSettingsManager) {
		this.appSettingsManager = appSettingsManager;
	}

}
