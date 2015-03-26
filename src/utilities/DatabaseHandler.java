package utilities;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import entities.Task;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "taskManager";

    // Task table name
    private static final String TABLE_TASKS = "tasks";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_DATE_YEAR = "year";
    private static final String KEY_DATE_MONTH = "month";
    private static final String KEY_DATE_DAY = "day";
    private static final String KEY_TIME_HOUR = "hour";
    private static final String KEY_TIME_MINUTES = "minutes";
    private static final String KEY_LOCATION_LONGITUDE = "longitude";
    private static final String KEY_LOCATION_LATITUDE = "latitude";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS
                + "("
                + KEY_ID                + " INTEGER PRIMARY KEY, "
                + KEY_MESSAGE           + " , "
                + KEY_DATE_YEAR         + " , "
                + KEY_DATE_MONTH        + " , "
                + KEY_DATE_DAY          + " , "
                + KEY_TIME_HOUR         + " , "
                + KEY_TIME_MINUTES      
               
                + ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new task
    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        /*
         * save task Message, Date(year+month+day), Time(Hour+Time), Location(Longitude+Latitude)
         * */
        values.put(KEY_MESSAGE, task._taskMessage);
        values.put(KEY_DATE_YEAR, task._dateYear);
        values.put(KEY_DATE_MONTH, task._dateMonth);
        values.put(KEY_DATE_DAY, task._dateDay);
        values.put(KEY_TIME_HOUR, task._timeHour);
        values.put(KEY_TIME_MINUTES, task._timeMinute);
       
        // Inserting Row
        db.insert(TABLE_TASKS, null, values );
        db.close(); // Closing database connection
    }



    // Getting All Tasks
    public List<Task> getAllTasks() {
        List<Task> contactList = new ArrayList<Task>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setID(Integer.parseInt(cursor.getString(0)));
                task.setTaskMessage(cursor.getString(1));
                // Adding contact to list
                contactList.add(task);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }


    // Deleting single task
    public void deleteTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?",
                new String[]{String.valueOf(task.getID())});
        db.close();
    }

    // Update a single task
    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
         // The fields
        ContentValues con = new ContentValues();
        con.put(KEY_MESSAGE, task._taskMessage);
        con.put(KEY_DATE_YEAR, task._dateYear);
        con.put(KEY_DATE_MONTH, task._dateMonth);
        con.put(KEY_DATE_DAY, task._dateDay);
        con.put(KEY_TIME_HOUR, task._timeHour);
        con.put(KEY_TIME_MINUTES, task._timeMinute);
       
        db.update(TABLE_TASKS, con, KEY_ID + " = ?",
                new String[] { String.valueOf(task.getID()) });
        db.close();
    }

    // Getting tasks Count
    public int getTaskCount() 
    {
    	int count;
    	
    	String countQuery = "SELECT * FROM " + TABLE_TASKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();

        return count;
    }
}
