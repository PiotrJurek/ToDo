package com.example.todo.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.todo.EditTodoActivity;
import com.example.todo.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "todo_channel";
    private static final String CHANNEL_NAME = "Todo Notifications";
    private static final String CHANNEL_DESC = "Notifications for Todo app";

    @Override
    public void onReceive(Context context, Intent intent) {
        long todoId = intent.getLongExtra("TODO_ID", -1);
        String todoTitle = intent.getStringExtra("TODO_TITLE");

        if (todoId == -1 || todoTitle == null) {
            return;
        }

        Intent editIntent = new Intent(context, EditTodoActivity.class);
        editIntent.putExtra("TODO_ID", todoId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) todoId, editIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Upcoming Task: " + todoTitle)
                .setContentText("You have an upcoming task to complete.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify((int) todoId, builder.build());
    }
}
