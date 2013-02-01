package my.taskreminder.beans;

public class AppSettings {
private boolean removeCompleted,removeMissed,playSound,displayDialog;

/**
 * @return the removeCompleted
 */
public boolean isRemoveCompleted() {
	return removeCompleted;
}

/**
 * @param removeCompleted the removeCompleted to set
 */
public void setRemoveCompleted(boolean removeCompleted) {
	this.removeCompleted = removeCompleted;
}

/**
 * @return the removeMissed
 */
public boolean isRemoveMissed() {
	return removeMissed;
}

/**
 * @param removeMissed the removeMissed to set
 */
public void setRemoveMissed(boolean removeMissed) {
	this.removeMissed = removeMissed;
}

/**
 * @return the playSound
 */
public boolean isPlaySound() {
	return playSound;
}

/**
 * @param playSound the playSound to set
 */
public void setPlaySound(boolean playSound) {
	this.playSound = playSound;
}

/**
 * @return the displayDialog
 */
public boolean isDisplayDialog() {
	return displayDialog;
}

/**
 * @param displayDialog the displayDialog to set
 */
public void setDisplayDialog(boolean displayDialog) {
	this.displayDialog = displayDialog;
}

}
