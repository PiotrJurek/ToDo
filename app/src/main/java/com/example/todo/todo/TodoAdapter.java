package com.example.todo.todo;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.EditTodoActivity;
import com.example.todo.notification.NotificationScheduler;
import com.example.todo.R;
import com.example.todo.database.TodoDAO;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private final NotificationScheduler notificationScheduler;

    private List<Todo> todoList;
    private final Context context;
    private final TodoDAO todoDAO;

    public TodoAdapter(List<Todo> todoList, Context context) {
        this.todoList = todoList;
        this.context = context;
        this.todoDAO = new TodoDAO(context);
        this.todoDAO.open();
        this.notificationScheduler = new NotificationScheduler(context);
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        holder.todoTitle.setText(todo.getTitle());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        holder.todoDueDate.setText(dateFormat.format(todo.getDueAt()));
        holder.todoCompleted.setOnCheckedChangeListener(null);
        holder.todoCompleted.setChecked(todo.isCompleted());

        if (todo.getAttachment() != null && !todo.getAttachment().isEmpty()) {
            holder.attachmentIcon.setVisibility(View.VISIBLE);
            holder.attachmentIcon.setOnClickListener(v -> openAttachment(todo.getAttachment()));
        } else {
            holder.attachmentIcon.setVisibility(View.INVISIBLE);
        }

        holder.todoCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            todo.setCompleted(isChecked);
            todoDAO.updateTodo(todo);

            if (isChecked) {
                notificationScheduler.cancelNotification(todo.getId());
            } else if (todo.isNotificationEnabled()) {
                notificationScheduler.scheduleNotification(todo);
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean hideCompletedTasks = prefs.getBoolean("hide_completed_tasks", false);

            if (hideCompletedTasks && isChecked) {
                todoList.remove(todo);
                notifyDataSetChanged();
            }
        });

        holder.detailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTodoActivity.class);
            intent.putExtra("TODO_ID", todo.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public void setTodoList(List<Todo> todoList) {
        this.todoList = todoList;
    }

    private void openAttachment(String filePath) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(context, "com.example.todo.fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, getMimeType(uri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No application found to open this file type.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        private final TextView todoTitle;
        private final TextView todoDueDate;
        private final CheckBox todoCompleted;
        private final Button detailsButton;
        private final ImageView attachmentIcon;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            todoTitle = itemView.findViewById(R.id.todo_title);
            todoDueDate = itemView.findViewById(R.id.todo_due_date);
            todoCompleted = itemView.findViewById(R.id.todo_completed);
            detailsButton = itemView.findViewById(R.id.button_details);
            attachmentIcon = itemView.findViewById(R.id.attachment_icon);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        todoDAO.close();
    }
}
