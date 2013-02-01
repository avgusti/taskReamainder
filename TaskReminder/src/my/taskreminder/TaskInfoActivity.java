package my.taskreminder;

import java.util.Calendar;
import my.taskreminder.entity.Task;
import my.taskreminder.sql.MySQLiteHelper;
import my.taskreminder.sql.TaskDataSource;
import my.taskreminder.util.DateTimeFormater;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

public class TaskInfoActivity extends Activity implements OnTimeSetListener,
		OnDateSetListener {
	// fields
	private Spinner sp_type, sp_day;
	private EditText ed_name, ed_message, ed_date, ed_time;
	private LinearLayout l_day, l_date;
	// View is good enough to handle click
	private View btn_save, btn_cancel;
	private TaskDataSource datasource;
	private Task task;
	private int year, month, day, hour, minute;
	static final int DATE_DIALOG_ID = 1;
	static final int TIME_DIALOG_ID = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taskinfo);

		datasource = new TaskDataSource(this);
		datasource.open();

		l_date = (LinearLayout) findViewById(R.id.datelayout);
		l_day = (LinearLayout) findViewById(R.id.daylayout);

		sp_type = (Spinner) findViewById(R.id.typespiner);
		sp_day = (Spinner) findViewById(R.id.dayspiner);
		
		sp_day.setSelection(DateTimeFormater.getWeekDay()-1);
		
		//see implementation at var definition
		sp_type.setOnItemSelectedListener(itemSelectedListener);

		
		ed_name = (EditText) findViewById(R.id.nameinput);
		ed_message = (EditText) findViewById(R.id.messageinput);

		ed_message.setOnFocusChangeListener(hideKeyboard);
		ed_name.setOnFocusChangeListener(hideKeyboard);

		ed_date = (EditText) findViewById(R.id.dateinput);
		ed_time = (EditText) findViewById(R.id.timeinput);

		btn_save = findViewById(R.id.save);
		btn_cancel = findViewById(R.id.cancel);

		btn_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(-1);
				finish();
			}
		});

		btn_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				saveData();
				setResult((int) task.getId());
				finish();
			}
		});

		long taskId = getIntent().getLongExtra(MySQLiteHelper.COLUMN_ID, -1);
		if (taskId >= 0) {
			loadData(taskId);
		} else {
			task = new Task();
			fillDateTimeFromTask(task);
		}

		ed_date.setOnClickListener(onDate);
		ed_time.setOnClickListener(onTime);
		updateControls();
	}

	@Override
	protected void onPause() {
		datasource.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		datasource.open();
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, this, year, month, day);

		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, this, hour, minute, true);

		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DATE_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(year, month, day);
			break;
		case TIME_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(hour, minute);
			break;
		}
		super.onPrepareDialog(id, dialog);
	}

	private View.OnClickListener onDate = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			showDialog(DATE_DIALOG_ID);

		}
	};

	private View.OnClickListener onTime = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			showDialog(TIME_DIALOG_ID);

		}
	};

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		this.year = year;
		month = monthOfYear;
		day = dayOfMonth;
		updateControls();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		hour = hourOfDay;
		this.minute = minute;
		updateControls();

	}
	
	private OnItemSelectedListener itemSelectedListener=new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			updateControls();
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing 
			
		}
	};
	
	private void updateControls() {
		if (sp_type.getSelectedItemPosition() == MySQLiteHelper.TYPE_WEEKDAY) {
			l_date.setVisibility(View.GONE);
			l_day.setVisibility(View.VISIBLE);
		} else {
			l_day.setVisibility(View.GONE);
			l_date.setVisibility(View.VISIBLE);
		}

		
		task.setDate(DateTimeFormater.setDate(year, month, day));
		ed_date.setText(DateTimeFormater.longToDate(task.getDate()));
		task.setTime(DateTimeFormater.setTime(hour,minute));
		ed_time.setText(DateTimeFormater.longToTime(task.getTime()));

	}
   
	private void fillDateTimeFromTask(Task task) {
		 //@TODO try to refactor. Move to DataTimeFormater?
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(task.getDate());
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);
		cal.setTimeInMillis(task.getTime());
		hour = cal.get(Calendar.HOUR_OF_DAY);
		minute = cal.get(Calendar.MINUTE);
	}

	private void loadData(long taskId) {
		task = datasource.loadTask(taskId);
		// map fields
		ed_name.setText(task.getName());
		ed_message.setText(task.getMessage());
		fillDateTimeFromTask(task);
		updateControls();
		sp_type.setSelection(task.getType());
		if (task.getType() == MySQLiteHelper.TYPE_WEEKDAY) {
			sp_day.setSelection((int) task.getDay() - 1);
		}
		// positions should be synchronized with constants
		// TYPE_DATE=0,TYPE_WEEKDAY=1, TYPE_DAYOFMONTH=2;
		task.setType(sp_type.getSelectedItemPosition());
		task.setDay(sp_day.getSelectedItemPosition());

	}

	private OnFocusChangeListener hideKeyboard = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				InputMethodManager imm = (InputMethodManager) v.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		}
	};

	protected void saveData() {
		//mark as new after editing
		task.setStatus(MySQLiteHelper.STATUS_NEW);
		// map fields
		task.setName(ed_name.getText().toString());
		task.setMessage(ed_name.getText().toString());
		updateControls();
		// positions should be synchronized with constants
		// TYPE_DATE=0,TYPE_WEEKDAY=1, TYPE_DAYOFMONTH=2;
		task.setType(sp_type.getSelectedItemPosition());
		if (task.getType() == MySQLiteHelper.TYPE_WEEKDAY) {
			task.setDay(sp_day.getSelectedItemPosition() + 1);
		}

		if (task.getId() > 0) {
			datasource.updateTask(task);
		} else {
			task = datasource.createTask(task);
		}
		Intent intent = new Intent(this, TaskReminderService.class);
		startService(intent);
	}
   
}
