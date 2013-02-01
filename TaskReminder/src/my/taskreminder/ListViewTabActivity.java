package my.taskreminder;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import my.taskreminder.entity.Task;
import my.taskreminder.sql.MySQLiteHelper;
import my.taskreminder.sql.TaskDataSource;
import my.taskreminder.util.DateTimeFormater;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewTabActivity extends ListActivity {

	public static final int TAB_TODAY = 0;
	public static final int TAB_WEEK = 1;
	public static final int TAB_ALL = 2;
	public static final int TAB_MISSED = 3;

	protected static final int KEY_POS = R.id.task_name;

	private TaskDataSource datasource;
	private TaskAdapter adapter;
	private List<Task> values = new ArrayList<Task>();

	private int fwidth;
	private ListView listView;
	private int tabCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view_tab);
		editTaskIntent = new Intent(ListViewTabActivity.this,
				TaskInfoActivity.class);

		datasource = new TaskDataSource(this);
		// datasource.open();
		tabCode = getIntent().getIntExtra("TAB", TAB_TODAY);

		adapter = new TaskAdapter(this, R.layout.list_item_task, values);
		setListAdapter(adapter);
		listView = (ListView) findViewById(android.R.id.list);
		// listView.setItemsCanFocus(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		// listView.setFocusableInTouchMode(true);

		Display display = getWindowManager().getDefaultDisplay();
		fwidth = display.getWidth();
		fixLayout(findViewById(R.id.task_list_header));
		registerForContextMenu(listView);
	}

	private void fixLayout(View pv) {
		int ww = fwidth / 10;
		View img, name, time;
		img = pv.findViewById(R.id.task_check);
		name = pv.findViewById(R.id.task_name);
		time = pv.findViewById(R.id.task_time);
		img.setLayoutParams(new LinearLayout.LayoutParams(ww,
				LayoutParams.MATCH_PARENT));
		name.setLayoutParams(new LinearLayout.LayoutParams(ww * 5,
				LayoutParams.MATCH_PARENT));
		time.setLayoutParams(new LinearLayout.LayoutParams(ww * 4,
				LayoutParams.MATCH_PARENT));
	}

	// will be enabled in case of disabling onclick for listView items
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == android.R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(values.get(info.position).getName());
			MenuItem item = menu.add(Menu.NONE, R.id.add, 0, R.string.new_str);
			// Note: Context menu items do not support icons or shortcut keys.
			// Just for case ^-)
			item.setIcon(R.drawable.add_page);
			item = menu.add(Menu.NONE, R.id.edit, 1, R.string.edit);
			item.setIcon(R.drawable.edit_page);
			item = menu.add(Menu.NONE, R.id.delete, 2, R.string.delete);
			item.setIcon(R.drawable.delete_page);
		}
	}

	// see onCreateContextMenu
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();
		Task task = values.get(info.position);
		performAction(menuItemIndex, task);
		Toast.makeText(this, item.getTitle() + "for " + task.toString(),
				Toast.LENGTH_LONG).show();
		return true;
	}

	// Inflate the menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.taskoptions, menu);
		return true;
	}

	int sellPos = -1;

	// Handle click events
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add: {
			performAction(item.getItemId(), null);
			Toast.makeText(this, "Add", Toast.LENGTH_SHORT).show();
			return true;
		}
		case R.id.edit: {
			if (listView.getSelectedItemPosition() >= 0 || sellPos >= 0) {
				int pos = listView.getSelectedItemPosition() >= 0 ? listView
						.getSelectedItemPosition() : sellPos;
				sellPos = -1;
				performAction(item.getItemId(), values.get(pos));
			} else {
				Toast.makeText(this, "No item selected", Toast.LENGTH_SHORT)
						.show();
			}

			return true;
		}
		case R.id.delete: {
			if (listView.getSelectedItemPosition() >= 0) {
				performAction(item.getItemId(),
						(Task) listView.getSelectedItem());
			} else {
				Toast.makeText(this, "No item selected", Toast.LENGTH_SHORT)
						.show();
			}
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// initialized onCreate()
	private Intent editTaskIntent;

	private void performAction(int code, Task task) {

		switch (code) {
		case R.id.add: {
			editTaskIntent.putExtra(MySQLiteHelper.COLUMN_ID, -1);
			startActivityForResult(editTaskIntent, 0);
			break;
		}
		case R.id.edit: {
			if (checkForId(task)) {
				editTaskIntent.putExtra(MySQLiteHelper.COLUMN_ID, task.getId());
				startActivityForResult(editTaskIntent, 0);
			}
			break;

		}
		case R.id.delete: {
			if (checkForId(task)) {
				datasource.deleteTask(task);
				adapter.remove(task);
			}

		}
		}
	}

	private boolean checkForId(Task task) {
		if (datasource.loadTask(task.getId()) != null)
			return true;
		else {
			Toast.makeText(this, "", Toast.LENGTH_LONG);
			updateTaskFromDB();
			return false;
		}
	}

	// / can't force tab host to reuse activity
	// /* (non-Javadoc)
	// * @see android.app.Activity#onNewIntent(android.content.Intent)
	// */
	// @Override
	protected void onNewIntent(Intent intent) {

		super.onNewIntent(intent);
		setIntent(intent);
		tabCode = intent.getIntExtra("TAB", TAB_TODAY);
		updateTaskFromDB();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Will reload data on any resume

		// if (resultCode >= 0) {
		// // just reload data
		// datasource.open();
		// values = getData(tabCode);
		// adapter.setItems(values);
		// adapter.notifyDataSetChanged();
		// }
	}

	private List<Task> getData(int code) {
		List<Task> tmp = new ArrayList<Task>();
		switch (code) {
		case TAB_TODAY: {
			tmp = datasource.getTodayTasks();
			break;
		}
		case TAB_WEEK: {
			tmp = datasource.getWeekTasks();
			break;
		}
		case TAB_ALL: {
			tmp = datasource.getAllTasks();
			break;
		}
		case TAB_MISSED: {
			tmp = datasource.getTasksByStatus(MySQLiteHelper.STATUS_MISSED);
			break;
		}
		default:
			break;
		}
		return tmp;
	}

	private class TaskAdapter extends ArrayAdapter<Task> {

		private List<Task> items;

		public TaskAdapter(Context context, int textViewResourceId,
				List<Task> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item_task, null);
				fixLayout(v);
			}

			Task item = (Task) items.get(position);

			if (item != null) {
				v.setTag(KEY_POS, position);
				ImageView taskCheck = (ImageView) v
						.findViewById(R.id.task_check);
				TextView taskName = (TextView) v.findViewById(R.id.task_name);
				View taskTime = v.findViewById(R.id.task_time);
				if (item.getStatus() == MySQLiteHelper.STATUS_COMPLETED) {
					taskCheck.setImageResource(R.drawable.accept);
				} else if (item.getStatus() == MySQLiteHelper.STATUS_MISSED) {
					taskCheck.setImageResource(R.drawable.delete);
				} else {
					taskCheck.setImageDrawable(null);
				}
				taskName.setText(item.getName());
				TextView time_rec, time_weekday, time_date, time_time;
				time_rec = (TextView) taskTime
						.findViewById(R.id.time_cell_recurent);
				time_date = (TextView) taskTime
						.findViewById(R.id.time_cell_date);
				time_weekday = (TextView) taskTime
						.findViewById(R.id.time_cell_weekday);
				time_time = (TextView) taskTime
						.findViewById(R.id.time_cell_time);

				time_rec.setText(" ");
				time_weekday.setText("   ");
				time_date.setText("        ");
				time_time.setText(String.format("%1$tH:%1$tM", item.getTime()));

				if (item.getType() == MySQLiteHelper.REPEAT_WEEKDAY) {
					time_rec.setText("R");
					if (item.getDay() > 0 && item.getDay() < 8) {
						time_weekday.setText(((new DateFormatSymbols())
								.getWeekdays()[(int) item.getDay()]).substring(
								0, 3));
					}

					// Move to next week once scheduled day_of_week is in
					// past
					Date actual = new Date(
							DateTimeFormater.getaAdjustedToWeekDayDate(
									(int) item.getDay(), item.getTime()));
					time_date.setText(dateFormat.format(actual.getTime()));
				} else {
					Date d = new Date(item.getDate());
					time_date.setText(dateFormat.format(d));
				}

			}
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					sellPos = (Integer) v.getTag(ListViewTabActivity.KEY_POS);
					openOptionsMenu();

				}
			});
			return v;
		}
	}

	@Override
	protected void onResume() {
		// update list on each resume
		// task may be deleted by service or status may change
		datasource.open();
		updateTaskFromDB();

		super.onResume();
	}

	private void updateTaskFromDB() {
		values = getData(tabCode);
		adapter.clear();
		// @TODO migrate to addAll since API v11
		for (Task task : values) {
			adapter.add(task);
		}
	}

	@Override
	protected void onPause() {
		datasource.close();
		super.onPause();
	}

}