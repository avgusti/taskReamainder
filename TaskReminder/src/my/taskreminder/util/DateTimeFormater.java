package my.taskreminder.util;

import java.util.Calendar;
import java.util.Date;

import android.text.format.DateFormat;

public class DateTimeFormater {

	public static CharSequence longToDate(long dt) {
		return DateFormat.format("dd.MM.yy", dt);

	}

	public static CharSequence longToTime(long dt) {
		return DateFormat.format("kk:mm", dt);
	}

	public static long dateToLong(String dt) {
		return Date.parse(dt);

	}

	public static long timeToLong(String dt) {
		return Date.parse(dt);
	}

	public static long getDate() {
		Calendar cal = Calendar.getInstance();
		return setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH));
	}

	public static long getTime() {
		Calendar cal = Calendar.getInstance();
		return setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

	}

	public static long setDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return cal.getTimeInMillis();
	}

	public static long setTime(int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		return cal.getTimeInMillis();
	}

	public static int getWeekDay() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	// Move to next week once scheduled day_of_week is in
	// past
	public static long getaAdjustedToWeekDayDate(int weekday, long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getDate());
		if (cal.get(Calendar.DAY_OF_WEEK) > weekday
				&& cal.getTime().getTime() > time) {
			cal.add(Calendar.DAY_OF_WEEK, 7);
			
		} 
		cal.set(Calendar.DAY_OF_WEEK,weekday);
		return cal.getTimeInMillis();
	}

	public static long getWeekStart() {
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(getDate());
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		return cal.getTimeInMillis(); 
	}

	public static long getWeekEnd() {
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(getWeekStart());
		cal.add(Calendar.DAY_OF_WEEK,6);
		return cal.getTimeInMillis();
	}
}
