package my.taskreminder.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_TASK = "task";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TYPE = "coltype";
	public static final String COLUMN_DATE = "coldate";
	public static final String COLUMN_DAY = "day";
	public static final String COLUMN_TIME = "coltime";
	
	public static final String COLUMN_MESSAGE = "message";
	
	public static final String COLUMN_STATUS = "status";
	//For the feature 
	public static final String COLUMN_REPEAT = "repeat";
	
	
	
	public static final int STATUS_NEW=0,STATUS_COMPLETED=1,STATUS_MISSED=2;
	public static final int TYPE_WEEKDAY=0,TYPE_DATE=1, TYPE_DAYOFMONTH=2;

	public static final int REPEAT_WEEKDAY=0,REPEAT_NONE=1, REPEAT_DAYOFMONTH=2, REPEAT_YEARLY=3;	
	//sqlite3 /data/data/my.taskreminder/databases/taskreminder.db
	private static final String DATABASE_NAME = "taskreminder.db";
	private static final int DATABASE_VERSION = 1;
	
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_TASK + "(" + COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_NAME + " text not null, "
			+ COLUMN_MESSAGE + " text not null, "
			// may need more types
			+ COLUMN_TYPE + " integer not null, "
			// date 1 jan  - 31 dec  time is always 00:00  in UNIX
			//in case of repeat REPEAT_YEARLY year is 0
			+ COLUMN_DATE + " integer, " 
			//day of week or month
			+ COLUMN_DAY + " integer, "
			+ COLUMN_TIME + " integer not null, " //time 0:00 - 24:00 in UNIX
			+ COLUMN_STATUS + " integer not null "
			+");";
			//Not used for version 1
			//+ COLUMN_REPEAT + " integer not null"
			

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
		onCreate(db);
	}

}
