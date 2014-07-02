package ru.mami.schedule.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import ru.mami.schedule.utils.Subject;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseAdapter extends SQLiteOpenHelper {
    private static DatabaseAdapter instance;

    public static final String DB_NAME = "schedule_db";
    private static final int DB_VERSION = 1;

    public static final String[] columns = {
        "id", "subject", "checked", "period", "dt_start", "dt_end",
        "dow", "t_start", "t_end", "groups", "place", "activities"};

    public static final String TABLE_NAME = "T_SUBJECT";
    public static final String PLACE = "place";
    public static final String CHECKED = "checked";
    public static final String SUBJECT = "subject";
    public static final String PERIOD = "period";
    public static final String DT_START = "dt_start";
    public static final String DT_END = "dt_end";
    public static final String DOW = "dow";
    public static final String T_START = "t_start";
    public static final String T_END = "t_end";
    public static final String GROUPS = "groups";
    public static final String ACTIVITIES = "activities";
    private static final String CREATE_TABLE = "create table " + TABLE_NAME +
            " ( id integer primary key, " +
            SUBJECT + " TEXT, " +
            PERIOD + " TEXT, " +
            DT_START + " TEXT, " +
            DT_END + " TEXT, " +
            DOW + " TEXT, " +
            T_START + " TEXT, " +
            T_END + " TEXT, " +
            CHECKED + " TEXT, " +
            PLACE + " TEXT, " +
            GROUPS + " TEXT, " +
            ACTIVITIES + " TEXT)";

    private DatabaseAdapter(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    
    public static DatabaseAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAdapter(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(getClass().getSimpleName(), "onCreate()");
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        Log.i(getClass().getSimpleName(), "onUpgrade() - //TODO//");
    }

    public void saveSubject(Subject subj) {
        ContentValues cv = new ContentValues();
        cv.put("subject", subj.getSubject());
        cv.put("place", subj.getPlace());
        cv.put("period", subj.getPeriod());
        cv.put("dt_start", subj.getDt_start());
        cv.put("dt_end", subj.getDt_end());
        cv.put("t_start", subj.getT_start());
        cv.put("t_end", subj.getT_end());
        cv.put("groups", subj.getGroups());
        cv.put("dow", subj.getDow());
        cv.put("checked", subj.getChecked());
        cv.put("activities", subj.getActivities());

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int updCount = db.update(DatabaseAdapter.TABLE_NAME, cv,
                    "id = ?", new String[] {subj.getId()});
            if (updCount == 0) {
                Log.i(getClass().getSimpleName(), "Insert subject: " + subj.getId());
                cv.put("id", subj.getId());
                db.insert(DatabaseAdapter.TABLE_NAME, null, cv);
            }
            else {
                Log.i(getClass().getSimpleName(), "Update subject: " + subj.getId());
            }
        }
        finally {
            db.close();
        }
    }

    public void deleteSubject(Subject subj) {
        Log.i(getClass().getSimpleName(), "Delete subject: " + subj.getId());
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(DatabaseAdapter.TABLE_NAME,
                    "id = ?", new String[] {subj.getId()});
        }
        finally {
            db.close();
        }
    }

    public void syncDB(ArrayList<Subject> subjects) {
        if (subjects != null) {
            for (Subject subj : subjects) {
                if (subj.getMode().equals("add"))
                    this.saveSubject(subj);
                else
                    this.deleteSubject(subj);
            }
        }
    }

    private ArrayList<Subject> loadSubjects(Cursor cursor) {
        Log.i(getClass().getSimpleName(), "loadSubjects()");
        ArrayList<Subject> subjects = new ArrayList<Subject>();
        if (cursor != null && cursor.moveToFirst()) {
            String[] names = cursor.getColumnNames();
            do {
                HashMap<String, String> rawSubj = new HashMap<String, String>();
                Log.i(getClass().getSimpleName(), "New object");
                for (int i = 0; i < names.length; ++i) {
                    rawSubj.put(names[i], cursor.getString(i));
                    Log.i(getClass().getSimpleName(), "Name - " + names[i] + 
                            ", value - " + cursor.getString(i));
                }
                Subject subj = new Subject(rawSubj);
                subjects.add(subj);
            } while (cursor.moveToNext());
        }
        Log.i(getClass().getSimpleName(), "Total count - " + subjects.size());
        return subjects;
    }

    public ArrayList<Subject> getNewSubjects() {
        Log.i(getClass().getSimpleName(), "getNewSubjects()");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, columns, 
                "checked = ?", new String[] {"false"}, null, null, null);
        ArrayList<Subject> subjects = loadSubjects(cursor);
        db.close();
        return subjects;
    }

    public ArrayList<Subject> getSubjectsByDate(String day) {
        Log.i(getClass().getSimpleName(), "getSubjectByDate() - " + day);
/*      
        HashMap<String, String> ruDays = new HashMap<String, String>();
        ruDays.put("Mon", "Понедельник");
        ruDays.put("Tue", "Вторник");
        ruDays.put("Wed", "Среда");
        ruDays.put("Thu", "Четверг");
        ruDays.put("Fri", "Пятница");
        ruDays.put("Sat", "Суббота");
        ruDays.put("Sun", "Воскресенье");
        String dow = ruDays.get(day);
*/
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, columns,
                "dow = ?", new String[] {day}, null, null, null);
        ArrayList<Subject> subjects = loadSubjects(cursor);
        db.close();
        return subjects;
    }

    public Subject getSubjectById(String id) {
        Log.i(getClass().getSimpleName(), "getSubjectById() -" + id);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, columns,
                "id = ?", new String[] {id}, null, null, null);
        ArrayList<Subject> subjects = loadSubjects(cursor);
        db.close();
        if (subjects.isEmpty())
            return null;
        return subjects.get(0);
    }

    public ArrayList<Subject> getAll() {
        Log.i(getClass().getSimpleName(), "getAll()");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_NAME,
                null, null, null, null, null, null, null);
        ArrayList<Subject> subjects = loadSubjects(cursor);
        db.close();
        return subjects;
    }

}
