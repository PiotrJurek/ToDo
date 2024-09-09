package com.example.todo.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.example.todo.notification.NotificationReceiver;
import com.example.todo.todo.Todo;

public class NotificationScheduler {

    private Context context;

    public NotificationScheduler(Context context) {
        this.context = context;
    }

    public void scheduleNotification(Todo todo) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String notificationTime = prefs.getString("notification_time", "0");
        long notificationTimeMillis = getNotificationTimeMillis(notificationTime);
        long systemTime = System.currentTimeMillis();

        if(todo.getDueAt() - notificationTimeMillis <= systemTime) {
            return;
        }

        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra("TODO_ID", todo.getId());
        notificationIntent.putExtra("TODO_TITLE", todo.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) todo.getId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, todo.getDueAt() - notificationTimeMillis, pendingIntent);
    }

    public void cancelNotification(long todoId) {
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) todoId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public long getNotificationTimeMillis(String notificationTime) {
        switch (notificationTime) {
            case "5 minutes":
                return 5 * 60 * 1000;
            case "10 minutes":
                return 10 * 60 * 1000;
            case "15 minutes":
                return 15 * 60 * 1000;
            case "30 minutes":
                return 30 * 60 * 1000;
            case "1 hour":
                return 60 * 60 * 1000;
            case "2 hours":
                return 2 * 60 * 60 * 1000;
            case "1 day":
                return 24 * 60 * 60 * 1000;
            case "2 days":
                return 2 * 24 * 60 * 60 * 1000;
            case "1 week":
                return 7 * 24 * 60 * 60 * 1000;
            default:
                return 0;
        }
    }
}