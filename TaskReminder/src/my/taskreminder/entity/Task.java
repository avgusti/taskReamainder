package my.taskreminder.entity;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

import my.taskreminder.sql.MySQLiteHelper;

/**
 * 
 * Field values are explained in @see my.taskreminder.sql.MySQLiteHelper
 * 
 */
public class Task {
	
	
	public Task() {
		Calendar cal = Calendar.getInstance();
		time=cal.getTime().getTime();
		cal.clear(Calendar.HOUR);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		date=cal.getTimeInMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final String sstatus;
		if (this.status == MySQLiteHelper.STATUS_NEW) {
			sstatus = "new";
		} else if (this.status == MySQLiteHelper.STATUS_MISSED) {
			sstatus = "missed";
		} else if (this.status == MySQLiteHelper.STATUS_COMPLETED) {
			sstatus = "completed";
		} else {
			sstatus = "unkown";
		}
		return String.format(
				"ID=%1d task is %2s Name:%3s  on %4s at %5$tH:%5$tM ",
				id,
				sstatus,
				name,
				type == MySQLiteHelper.TYPE_DATE ? new Date(date)
						: (new DateFormatSymbols()).getWeekdays()[(int) day],
				time);
	}

	// According to specification
	private long id=-1;
	private String name;
	private int type=MySQLiteHelper.TYPE_WEEKDAY;
	private long date;
	private long day=1; //first day of week (Sunday)
	private long time;
	//not mentioned in table structure
	private String message;
	// added status
	private int status= MySQLiteHelper.STATUS_NEW;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the date
	 */
	public long getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(long date) {
		this.date = date;
	}

	/**
	 * @return the day
	 */
	public long getDay() {
		return day;
	}

	/**
	 * @param day
	 *            the day to set
	 */
	public void setDay(long day) {
		this.day = day;
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
