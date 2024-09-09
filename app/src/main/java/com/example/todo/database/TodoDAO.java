package com.example.todo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.todo.todo.Todo;

import java.util.ArrayList;
import java.util.List;

public class TodoDAO {

    private SQLiteDatabase database;
    private TodoDBHelper dbHelper;
    private String[] allColumns = {
            TodoDBHelper.COLUMN_ID,
            TodoDBHelper.COLUMN_TITLE,
            TodoDBHelper.COLUMN_DESCRIPTION,
            TodoDBHelper.COLUMN_CREATED_AT,
            TodoDBHelper.COLUMN_DUE_AT,
            TodoDBHelper.COLUMN_COMPLETED,
            TodoDBHelper.COLUMN_NOTIFICATION_ENABLED,
            TodoDBHelper.COLUMN_CATEGORY,
            TodoDBHelper.COLUMN_ATTACHMENT
    };

    public TodoDAO(Context context) {
        dbHelper = new TodoDBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Todo createTodo(String title, String description, long createdAt, long dueAt, boolean completed, boolean notificationEnabled, String category, String attachment) {
        ContentValues values = new ContentValues();
        values.put(TodoDBHelper.COLUMN_TITLE, title);
        values.put(TodoDBHelper.COLUMN_DESCRIPTION, description);
        values.put(TodoDBHelper.COLUMN_CREATED_AT, createdAt);
        values.put(TodoDBHelper.COLUMN_DUE_AT, dueAt);
        values.put(TodoDBHelper.COLUMN_COMPLETED, completed ? 1 : 0);
        values.put(TodoDBHelper.COLUMN_NOTIFICATION_ENABLED, notificationEnabled ? 1 : 0);
        values.put(TodoDBHelper.COLUMN_CATEGORY, category);
        values.put(TodoDBHelper.COLUMN_ATTACHMENT, attachment);

        long insertId = database.insert(TodoDBHelper.TABLE_TODOS, null, values);
        Cursor cursor = database.query(TodoDBHelper.TABLE_TODOS, allColumns, TodoDBHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        Todo newTodo = cursorToTodo(cursor);
        cursor.close();
        return newTodo;
    }

    public void updateTodo(Todo todo) {
        long id = todo.getId();
        ContentValues values = new ContentValues();
        values.put(TodoDBHelper.COLUMN_TITLE, todo.getTitle());
        values.put(TodoDBHelper.COLUMN_DESCRIPTION, todo.getDescription());
        values.put(TodoDBHelper.COLUMN_DUE_AT, todo.getDueAt());
        values.put(TodoDBHelper.COLUMN_COMPLETED, todo.isCompleted() ? 1 : 0);
        values.put(TodoDBHelper.COLUMN_NOTIFICATION_ENABLED, todo.isNotificationEnabled() ? 1 : 0);
        values.put(TodoDBHelper.COLUMN_CATEGORY, todo.getCategory());
        values.put(TodoDBHelper.COLUMN_ATTACHMENT, todo.getAttachment());

        database.update(TodoDBHelper.TABLE_TODOS, values, TodoDBHelper.COLUMN_ID + " = " + id, null);
    }

    public void deleteTodoById(long id) {
        database.delete(TodoDBHelper.TABLE_TODOS, TodoDBHelper.COLUMN_ID + " = " + id, null);
    }

    public Todo getTodoById(long id) {
        Cursor cursor = database.query(TodoDBHelper.TABLE_TODOS, allColumns, TodoDBHelper.COLUMN_ID + " = " + id, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        Todo todo = cursorToTodo(cursor);
        cursor.close();
        return todo;
    }

    public List<Todo> getAllTodos() {
        List<Todo> todos = new ArrayList<>();

        Cursor cursor = database.query(TodoDBHelper.TABLE_TODOS, allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Todo todo = cursorToTodo(cursor);
            todos.add(todo);
            cursor.moveToNext();
        }
        cursor.close();
        return todos;
    }

    private Todo cursorToTodo(Cursor cursor) {
        Todo todo = new Todo();
        todo.setId(cursor.getLong(0));
        todo.setTitle(cursor.getString(1));
        todo.setDescription(cursor.getString(2));
        todo.setCreatedAt(cursor.getLong(3));
        todo.setDueAt(cursor.getLong(4));
        todo.setCompleted(cursor.getInt(5) == 1);
        todo.setNotificationEnabled(cursor.getInt(6) == 1);
        todo.setCategory(cursor.getString(7));
        todo.setAttachment(cursor.getString(8));
        return todo;
    }
}
