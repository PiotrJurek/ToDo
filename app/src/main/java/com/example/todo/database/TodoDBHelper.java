package com.example.todo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TodoDBHelper extends SQLiteOpenHelper {

    public static final String TABLE_TODOS = "todos";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_DUE_AT = "due_at";
    public static final String COLUMN_COMPLETED = "completed";
    public static final String COLUMN_NOTIFICATION_ENABLED = "notification_enabled";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_ATTACHMENT = "attachment";

    private static final String DATABASE_NAME = "todos.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table "
            + TABLE_TODOS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_DESCRIPTION + " text, "
            + COLUMN_CREATED_AT + " integer, "
            + COLUMN_DUE_AT + " integer, "
            + COLUMN_COMPLETED + " integer, "
            + COLUMN_NOTIFICATION_ENABLED + " integer, "
            + COLUMN_CATEGORY + " text, "
            + COLUMN_ATTACHMENT + " text);";

    public TodoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
        onCreate(db);
    }
}
