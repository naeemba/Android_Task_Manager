package baghi.naeem.com.final_project.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import baghi.naeem.com.final_project.entities.Task;
import baghi.naeem.com.final_project.entities.User;
import baghi.naeem.com.final_project.session.Session;
import baghi.naeem.com.final_project.util.DateUtil;
import baghi.naeem.com.final_project.util.LogUtil;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "TASK_MANAGER";
    private static final Integer DB_VERSION = 14;

    private final static String USER_TABLE_NAME = "user";
    private final static String USER_ID = "user_id";
    private final static String USER_FIRST_NAME = "first_name";
    private final static String USER_LAST_NAME = "last_name";
    private final static String USER_USERNAME = "username";
    private final static String USER_PASSWORD = "password";
    private static final String USER_CREATE_STATEMENT = "CREATE TABLE " + USER_TABLE_NAME + " ( " +
            USER_ID + " INT UNIQUE," +
            USER_FIRST_NAME + " TEXT NOT NULL," +
            USER_LAST_NAME + " TEXT NOT NULL," +
            USER_USERNAME + " TEXT NOT NULL," +
            USER_PASSWORD + " TEXT NOT NULL" +
            " );";
    private static final String USER_DROP_STATEMENT = "DROP TABLE IF EXISTS " + USER_TABLE_NAME + ";";


    private final static String TASK_TABLE_NAME = "task";
    private final static String TASK_ID = "task_id";
    private final static String TASK_TITLE = "title";
    private final static String TASK_DESCRIPTION = "description";
    private final static String TASK_DUE_DATE = "due_date";
    private final static String TASK_DURATION = "duration";
    private final static String TASK_STATUS = "status";
    private final static String TASK_CREATE_STATEMENT = "CREATE TABLE " + TASK_TABLE_NAME + " ( " +
            TASK_ID + " INT UNIQUE," +
            TASK_TITLE + " TEXT NOT NULL," +
            TASK_DESCRIPTION + " TEXT NOT NULL," +
            TASK_DUE_DATE + " TEXT NOT NULL," +
            TASK_DURATION + " INT NOT NULL," +
            TASK_STATUS + " TEXT NOT NULL," +
            USER_ID + " INTEGER NOT NULL," +
            "FOREIGN KEY (" + USER_ID + ") REFERENCES " + USER_TABLE_NAME + " (" + USER_ID + ")" +
            " );";
    private static final String TASK_DROP_STATEMENT = "DROP TABLE IF EXISTS " + TASK_TABLE_NAME + ";";


    private static DBHelper instance;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if(instance == null)
            instance = new DBHelper(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USER_CREATE_STATEMENT);
        db.execSQL(TASK_CREATE_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(USER_DROP_STATEMENT);
        db.execSQL(TASK_DROP_STATEMENT);
        this.onCreate(db);
    }

    public User existByUsernameAndPassword(String username, String password) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(
                DBHelper.USER_TABLE_NAME,
                new String[]{DBHelper.USER_ID, DBHelper.USER_FIRST_NAME, DBHelper.USER_LAST_NAME, DBHelper.USER_USERNAME},
                "username=?",
                new String[]{username},
                null,
                null,
                DBHelper.USER_ID + " DESC");

        if (cursor.moveToFirst()) {
            LogUtil.debug(this.getClass().getSimpleName(), "user with username " + username + " found");
            User user = new User();
            user.setId(cursor.getString(0));
            user.setFirstName(cursor.getString(1));
            user.setLastName(cursor.getString(2));
            user.setUsername(cursor.getString(3));

            cursor.close();
            database.close();
            return user;
        }
        LogUtil.error(this.getClass().getSimpleName(), "user with username " + username + " not found");

        cursor.close();
        database.close();
        return null;
    }

    public boolean isUsernameAvailable(String username) {
        SQLiteDatabase database = this.getReadableDatabase();
        boolean result = database.query(
                DBHelper.USER_TABLE_NAME,
                null,
                "username=?",
                new String[] {username},
                null,
                null,
                DBHelper.USER_ID + " DESC").moveToFirst();
        database.close();
        LogUtil.debug(getTag(), "is username available: " + username);
        return !result;
    }

    public boolean insertUser(User user) {
        SQLiteDatabase database = this.getReadableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.USER_ID, user.getId());
            contentValues.put(DBHelper.USER_FIRST_NAME, user.getFirstName());
            contentValues.put(DBHelper.USER_LAST_NAME, user.getLastName());
            contentValues.put(DBHelper.USER_USERNAME, user.getUsername());
            contentValues.put(DBHelper.USER_PASSWORD, user.getPassword());

            database.insert(DBHelper.USER_TABLE_NAME, null, contentValues);
            database.close();
            LogUtil.debug(getTag(), "user inserted successfully " + user.getUsername());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(getTag(), "Saving user failed " + user.getUsername());
            return false;
        }
    }

    public List<Task> getTasks() {
        SQLiteDatabase database = this.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();
        try {
            Cursor cursor = database.query(
                    DBHelper.TASK_TABLE_NAME,
                    new String[]{
                            DBHelper.TASK_ID,
                            DBHelper.TASK_TITLE,
                            DBHelper.TASK_DESCRIPTION,
                            DBHelper.TASK_DUE_DATE,
                            DBHelper.TASK_DURATION,
                            DBHelper.TASK_STATUS
                    },
                    USER_ID + "=?",
                    new String[]{Session.user.getId()},
                    null,
                    null,
                    DBHelper.TASK_DUE_DATE + " DESC");

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Task task = new Task();
                    task.setId(cursor.getString(0));
                    task.setTitle(cursor.getString(1));
                    task.setDescription(cursor.getString(2));
                    task.setDueDate(DateUtil.getCalendarByString(cursor.getString(3)));
                    task.setDuration(cursor.getInt(4));
                    task.setStatus(Task.Status.getByValue(cursor.getString(5)));
                    tasks.add(task);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            LogUtil.error(this.getTag(), e.getMessage());
        }
        return tasks;
    }

    public List<Task> getTasks(Task.Status status) {
        SQLiteDatabase database = this.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();
        try {
            Cursor cursor = database.query(
                    DBHelper.TASK_TABLE_NAME,
                    new String[]{
                            DBHelper.TASK_ID,
                            DBHelper.TASK_TITLE,
                            DBHelper.TASK_DESCRIPTION,
                            DBHelper.TASK_DUE_DATE,
                            DBHelper.TASK_DURATION,
                            DBHelper.TASK_STATUS
                    },
                    USER_ID + "=? AND " + TASK_STATUS + "=?",
                    new String[]{Session.user.getId(), status.getValue()},
                    null,
                    null,
                    DBHelper.TASK_DUE_DATE + " DESC");

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Task task = new Task();
                    task.setId(cursor.getString(0));
                    task.setTitle(cursor.getString(1));
                    task.setDescription(cursor.getString(2));
                    task.setDueDate(DateUtil.getCalendarByString(cursor.getString(3)));
                    task.setDuration(cursor.getInt(4));
                    task.setStatus(Task.Status.getByValue(cursor.getString(5)));
                    tasks.add(task);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            LogUtil.error(this.getTag(), e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    public Task getTask(String taskId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(
                DBHelper.TASK_TABLE_NAME,
                new String[]{
                        DBHelper.TASK_ID,
                        DBHelper.TASK_TITLE,
                        DBHelper.TASK_DESCRIPTION,
                        DBHelper.TASK_DUE_DATE,
                        DBHelper.TASK_DURATION,
                        DBHelper.TASK_STATUS },
                "task_id=?",
                new String[]{taskId},
                null,
                null,
                DBHelper.TASK_ID + " DESC");

        if (cursor.moveToFirst()) {
            LogUtil.debug(this.getClass().getSimpleName(), "task with id " + TASK_ID + " found");
            Task task = new Task();
            task.setId(cursor.getString(0));
            task.setTitle(cursor.getString(1));
            task.setDescription(cursor.getString(2));
            task.setDueDate(DateUtil.getCalendarByString(cursor.getString(3)));
            task.setDuration(cursor.getInt(4));
            task.setStatus(Task.Status.getByValue(cursor.getString(5)));

            cursor.close();
            database.close();
            return task;
        }
        LogUtil.error(this.getClass().getSimpleName(), "task with id " + taskId + " not found");

        cursor.close();
        database.close();
        return null;
    }

    public boolean addAllTasks(List<Object> tasks) {
        try {
            boolean result = true;
            for(Object task : tasks) {
                if(!addTask((Task) task))
                    result = false;
            }
            return result;
        } catch (Exception e) {
            LogUtil.error(getTag(), e.getMessage());
            return false;
        }
    }

    public boolean addTask(Task task) {
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(TASK_ID, task.getId());
            contentValues.put(TASK_TITLE, task.getTitle());
            contentValues.put(TASK_DESCRIPTION, task.getDescription());
            contentValues.put(TASK_DUE_DATE, DateUtil.getFormattedString(task.getDueDate()));
            contentValues.put(TASK_DURATION, task.getDuration());
            contentValues.put(TASK_STATUS, task.getStatus().getValue());
            contentValues.put(USER_ID, Session.user.getId());
            database.insert(TASK_TABLE_NAME, null, contentValues);
            database.close();
            LogUtil.debug(getTag(), "Task inserted successfully " + task.getId());
            return true;
        } catch (Exception e) {
            LogUtil.error(getTag(), e.getMessage());
            return false;
        }
    }

    public boolean updateTask(Task task) {
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(TASK_ID, task.getId());
            contentValues.put(TASK_TITLE, task.getTitle());
            contentValues.put(TASK_DESCRIPTION, task.getDescription());
            contentValues.put(TASK_DUE_DATE, DateUtil.getFormattedString(task.getDueDate()));
            contentValues.put(TASK_DURATION, task.getDuration());
            contentValues.put(TASK_STATUS, task.getStatus().getValue());
            contentValues.put(USER_ID, Session.user.getId());
            boolean result = database.update(TASK_TABLE_NAME, contentValues, TASK_ID + "=?", new String[] {task.getId()}) > 0;
            database.close();
            LogUtil.debug(getTag(), "Task inserted successfully " + task.getId());
            return result;
        } catch (Exception e) {
            LogUtil.error(getTag(), e.getMessage());
            return false;
        }
    }

    public boolean updateTaskStatus(String taskId, Task.Status status) {
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(TASK_STATUS, status.getValue());
            boolean result = database.update(TASK_TABLE_NAME, contentValues, TASK_ID + "=?", new String[] {taskId}) > 0;
            database.close();
            LogUtil.debug(getTag(), "Task status updated successfully " + taskId + " new status: " + status);
            return result;
        } catch (Exception e) {
            LogUtil.error(getTag(), e.getMessage());
            return false;
        }
    }

    public boolean removeTask(String id) {
        SQLiteDatabase database = this.getReadableDatabase();
        boolean result = database.delete(TASK_TABLE_NAME, TASK_ID + "=?", new String[] {id}) > 0;
        database.close();
        return result;
    }

    private String getTag() {
        return this.getClass().getSimpleName();
    }
}
