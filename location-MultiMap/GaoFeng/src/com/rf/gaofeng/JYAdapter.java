package com.rf.gaofeng;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class JYAdapter {

	private static final String DB_NAME = "JY.db";
	private static final String[] DB_TABLE_ARRAY = { "android", "amap", "bdmap" };
	private static String DB_TABLE = "android";
	private static final int DB_VERSION = 1;

	public static final String KEY_ID = "_id";

	public static final String KEY_DATE = "date";
	public static final String KEY_LNG = "longitude";
	public static final String KEY_LAT = "latitude";
	public static final String KEY_ACC = "accuracy";
	public static final String KEY_SRC = "source";
	public static final String KEY_TYPE = "type";

	private SQLiteDatabase db;
	private Context context;
	private JYHelper jyHelper;

	public JYAdapter(Context _context) {
		// TODO Auto-generated constructor stub
		context = _context;
		jyHelper = new JYHelper(context, DB_NAME, null, DB_VERSION);
	}

	public JYAdapter open() {
		try {
			db = jyHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			// TODO: handle exception
			db = jyHelper.getReadableDatabase();
		}

		return this;
	}

	public void close() {
		db.close();
	}

	public void setTabel(String table) {
		DB_TABLE = table;
	}

	public int insertEntry(ContentValues newValues) {
		db.insert(DB_TABLE, null, newValues);
		return 0;
	}

	public boolean removeEntry(long _rowIndex) {
		return db.delete(DB_TABLE, KEY_ID + "=" + _rowIndex, null) > 0;
	}

	public Cursor getAllEntries() {
		return db.query(DB_TABLE, new String[] { KEY_ID, KEY_DATE, KEY_LNG,
				KEY_LAT, KEY_ACC, KEY_SRC, KEY_TYPE }, null, null, null, null, null);
	}

	private static class JYHelper extends SQLiteOpenHelper {

		public JYHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		private String createSQL(String table) {
			return "create table " + table + " (" + KEY_ID
					+ " integer primary key autoincrement, " + KEY_DATE
					+ " datetime default current_timestamp, " + KEY_LNG
					+ " double, " + KEY_LAT + " double, " + KEY_ACC + " double, " + KEY_TYPE + " varchar, " + KEY_SRC + " varchar);";
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			// TODO Auto-generated method stub
			for (int i = 0; i < DB_TABLE_ARRAY.length; i++) {
				_db.execSQL(createSQL(DB_TABLE_ARRAY[i]));
				log("create table:" + DB_TABLE_ARRAY[i]);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
				int _newVersion) {
			// TODO Auto-generated method stub
			_db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
			onCreate(_db);
		}
		
		public void log(String string) {
			// TODO Auto-generated method stub
			Log.i("MyLog", string);
		}

	}
}
