package com.example.tom.battleships;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {


    private static final String KEY_ID = "_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LAST_GAME = "lastGame";

    private static final String TAG = "DBAdapter";

    private static final String DATABASE_NAME = "UserData";
    private static final String DATABASE_TABLE = "UserData";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
            "CREATE TABLE " + DATABASE_TABLE + "( " +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_USERNAME + " TEXT, " +
                    KEY_PASSWORD + " TEXT, " +
                    KEY_LAST_GAME + " TEXT)";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter (Context ctx){
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS titles");
            onCreate(db);
        }
    }


    public DBAdapter open() throws SQLException{
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        DBHelper.close();
    }

    public long insertNewUser(
            String username, String password)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_USERNAME, username);
        initialValues.put(KEY_PASSWORD, password);

        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteUser(long rowId){
        return db.delete(DATABASE_TABLE, KEY_ID + " = " + rowId, null) > 0;
    }

    public boolean checkUserData(String strLoginName, String strLoginPassword){

        Cursor mCursor =
                db.query(DATABASE_TABLE,
                        new String[]{KEY_USERNAME, KEY_PASSWORD},
                        KEY_USERNAME + " =? AND " +
                        KEY_PASSWORD + " =?",
                        new String[] {strLoginName, strLoginPassword},
                        null, null, null, null);

        return (mCursor == null);
    }

    public Cursor getName(long rowId) throws SQLException {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE,
                        new String[]{KEY_USERNAME},
                        KEY_ID + " = " + rowId,
                        null, null, null, null, null);

        if (mCursor != null){
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updatePassword(
            long rowId, String password){
        ContentValues args = new ContentValues();
        args.put(KEY_PASSWORD, password);

        return db.update(DATABASE_TABLE , args, KEY_ID + " = " + rowId, null) > 0;
    }

    public boolean updateLastGamePlayed(
            long rowId, String lastGamePlayed){
        ContentValues args = new ContentValues();
        args.put(KEY_LAST_GAME, lastGamePlayed);

        return db.update(DATABASE_TABLE, args, KEY_ID + " = " + rowId, null) > 0;
    }
}