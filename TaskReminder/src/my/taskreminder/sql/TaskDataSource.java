package my.taskreminder.sql;

import java.util.ArrayList;
import java.util.List;

import my.taskreminder.entity.Task;
import my.taskreminder.util.DateTimeFormater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TaskDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_DATE,
			MySQLiteHelper.COLUMN_DAY, MySQLiteHelper.COLUMN_STATUS,
			MySQLiteHelper.COLUMN_TIME, MySQLiteHelper.COLUMN_TYPE,
			MySQLiteHelper.COLUMN_MESSAGE };

	private Task cursorToTask(Cursor cursor) {
		Task task = new Task();
		task.setId(cursor.getLong(0));
		task.setName(cursor.getString(1));
		task.setDate(cursor.getLong(2));
		task.setDay(cursor.getLong(3));
		task.setStatus(cursor.getInt(4));
		task.setTime(cursor.getLong(5));
		task.setType(cursor.getInt(6));
		task.setMessage(cursor.getString(7));

		return task;
	}

	public TaskDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Task createTask(Task task) {
		ContentValues values = mapTaskToContentValues(task);

		long insertId = database
				.insert(MySQLiteHelper.TABLE_TASK, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK, allColumns,
				MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null,
				null);
		cursor.moveToFirst();
		Task newTask = cursorToTask(cursor);
		cursor.close();
		return newTask;
	}

	public Task createTask(String name) {
		Task task = new Task();
		task.setName(name);
		return createTask(task);
	}

	private ContentValues mapTaskToContentValues(Task task) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_NAME, task.getName());
		values.put(MySQLiteHelper.COLUMN_MESSAGE, task.getMessage());
		values.put(MySQLiteHelper.COLUMN_DATE, task.getDate());
		values.put(MySQLiteHelper.COLUMN_DAY, task.getDay());
		values.put(MySQLiteHelper.COLUMN_STATUS, task.getStatus());
		values.put(MySQLiteHelper.COLUMN_TIME, task.getTime());
		values.put(MySQLiteHelper.COLUMN_TYPE, task.getType());
		return values;
	}

	public void deleteTask(Task Task) {
		long id = Task.getId();
		System.out.println("Task deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_TASK, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	public void updateTask(Task task) {
		ContentValues values = mapTaskToContentValues(task);
		String strFilter = MySQLiteHelper.COLUMN_ID + "=" + task.getId();
		database.update(MySQLiteHelper.TABLE_TASK, values, strFilter, null);
	}

	public List<Task> getTasksByStatus(int status) {
		List<Task> Tasks = new ArrayList<Task>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK, allColumns,
				MySQLiteHelper.COLUMN_STATUS + "=" + status, null, null, null,
				null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Task Task = cursorToTask(cursor);
			Tasks.add(Task);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return Tasks;
	}

	public List<Task> getTasksByFilter(String filter) {
		List<Task> Tasks = new ArrayList<Task>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK, allColumns,
				filter, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Task Task = cursorToTask(cursor);
			Tasks.add(Task);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return Tasks;

	}

	public List<Task> getAllTasks() {
		return getTasksByFilter(null);
	}

	public List<Task> getTodayTasks() {
		String filter = "(" + MySQLiteHelper.COLUMN_DATE + "="
				+ DateTimeFormater.getDate() + " and "
				+ MySQLiteHelper.COLUMN_TYPE + "=" + MySQLiteHelper.TYPE_DATE
				+ ") or (" + MySQLiteHelper.COLUMN_DAY + "="
				+ DateTimeFormater.getWeekDay() + " and "+  MySQLiteHelper.COLUMN_TYPE + "=" + MySQLiteHelper.TYPE_WEEKDAY+")";
		return getTasksByFilter(filter);

	}

	public List<Task> getWeekTasks() {

		String filter = "(" +MySQLiteHelper.COLUMN_TYPE + "=" + MySQLiteHelper.TYPE_WEEKDAY+" and "+MySQLiteHelper.COLUMN_DAY+">="+DateTimeFormater.getWeekDay()+" ) or ("+MySQLiteHelper.COLUMN_DATE+">"+DateTimeFormater.getWeekStart()+" and "+MySQLiteHelper.COLUMN_DATE+"<"+DateTimeFormater.getWeekEnd()+" and "+MySQLiteHelper.COLUMN_TYPE + "=" + MySQLiteHelper.TYPE_DATE+")";
				
		return getTasksByFilter(filter);
	}

	public List<Task> getOldTasks() {
		String filter = MySQLiteHelper.COLUMN_STATUS + "=" + MySQLiteHelper.STATUS_MISSED;
		
		return getTasksByFilter(filter);

	}

	public Task loadTask(long taskId) {
		String strFilter = MySQLiteHelper.COLUMN_ID + "=" + taskId;
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK, allColumns,
				strFilter, null, null, null, null);
		Task task = null;
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			task = cursorToTask(cursor);
		}
		// Make sure to close the cursor
		cursor.close();
		return task;
	}

	

}