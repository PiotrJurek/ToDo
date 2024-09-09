package com.example.todo.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.todo.database.TodoDAO;
import com.example.todo.todo.Todo;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationScheduler notificationScheduler = new NotificationScheduler(context);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            TodoDAO todoDAO = new TodoDAO(context);
            todoDAO.open();
            List<Todo> todos = todoDAO.getAllTodos();
            for (Todo todo : todos) {
                if (!todo.isCompleted() && todo.isNotificationEnabled()) {
                    notificationScheduler.scheduleNotification(todo);
                }
            }
            todoDAO.close();
        }
    }
}
