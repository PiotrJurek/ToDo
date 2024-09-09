package com.example.todo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.todo.database.TodoDAO;
import com.example.todo.notification.NotificationScheduler;
import com.example.todo.todo.Todo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditTodoActivity extends AppCompatActivity {

    private static final int PICK_ATTACHMENT_REQUEST = 1;
    private final NotificationScheduler notificationScheduler = new NotificationScheduler(this);

    private TodoDAO todoDAO;
    private Todo currentTodo;
    private EditText titleInput, descriptionInput;
    private CheckBox notificationCheckbox;
    private TextView dueDateText, attachmentName;
    private Spinner categorySpinner;
    private Calendar dueDateCalendar;
    private String selectedCategory;
    private Uri attachmentUri;
    private Button changeAttachmentButton, removeAttachmentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_todo);

        todoDAO = new TodoDAO(this);
        todoDAO.open();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        dueDateText = findViewById(R.id.dueDateText);
        notificationCheckbox = findViewById(R.id.notificationCheckbox);
        categorySpinner = findViewById(R.id.categorySpinner);
        attachmentName = findViewById(R.id.attachmentName);
        changeAttachmentButton = findViewById(R.id.changeAttachmentButton);
        removeAttachmentButton = findViewById(R.id.removeAttachmentButton);

        Button dueDateButton = findViewById(R.id.dueDateButton);
        dueDateButton.setOnClickListener(v -> showDateTimePicker());

        changeAttachmentButton.setOnClickListener(v -> pickAttachment());
        removeAttachmentButton.setOnClickListener(v -> removeAttachment());

        setupCategorySpinner();

        long todoId = getIntent().getLongExtra("TODO_ID", -1);
        if (todoId != -1) {
            currentTodo = todoDAO.getTodoById(todoId);
            populateFields();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_todo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_delete) {
            deleteTodo();
            return true;
        } else if (itemId == R.id.action_save) {
            saveTodo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_entries, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = null;
            }
        });
    }

    private void populateFields() {
        titleInput.setText(currentTodo.getTitle());
        descriptionInput.setText(currentTodo.getDescription());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dueDateText.setText(dateFormat.format(currentTodo.getDueAt()));
        dueDateCalendar = Calendar.getInstance();
        dueDateCalendar.setTimeInMillis(currentTodo.getDueAt());
        notificationCheckbox.setChecked(currentTodo.isNotificationEnabled());

        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) categorySpinner.getAdapter();
        int position = adapter.getPosition(currentTodo.getCategory());
        categorySpinner.setSelection(position);

        if (currentTodo.getAttachment() != null) {
            attachmentUri = Uri.parse(currentTodo.getAttachment());
            try {
                attachmentName.setText(getFileName(attachmentUri));
                removeAttachmentButton.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                removeAttachment();
            }
        }
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        dueDateCalendar = Calendar.getInstance();
        new DatePickerDialog(EditTodoActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
            dueDateCalendar.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(EditTodoActivity.this, (view1, hourOfDay, minute) -> {
                dueDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                dueDateCalendar.set(Calendar.MINUTE, minute);
                dueDateText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(dueDateCalendar.getTime()));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void saveTodo() {
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        boolean notificationEnabled = notificationCheckbox.isChecked();

        if (dueDateCalendar == null) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        long dueAt = dueDateCalendar.getTimeInMillis();

        notificationScheduler.cancelNotification(currentTodo.getId());

        currentTodo.setTitle(title);
        currentTodo.setDescription(description);
        currentTodo.setDueAt(dueAt);
        currentTodo.setNotificationEnabled(notificationEnabled);
        currentTodo.setCategory(selectedCategory);

        if (attachmentUri != null) {
            String privateFilePath = copyFileToPrivateStorage(attachmentUri);
            if (privateFilePath != null) {
                currentTodo.setAttachment(privateFilePath);
            }
        } else {
            currentTodo.setAttachment(null);
        }

        todoDAO.updateTodo(currentTodo);

        if (notificationEnabled && !currentTodo.isCompleted()) {
            notificationScheduler.scheduleNotification(currentTodo);
        }

        setResult(RESULT_OK);
        finish();
    }

    private void deleteTodo() {
        notificationScheduler.cancelNotification(currentTodo.getId());
        todoDAO.deleteTodoById(currentTodo.getId());
        setResult(RESULT_OK);
        finish();
    }


    private void pickAttachment() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_ATTACHMENT_REQUEST);
    }

    private void removeAttachment() {
        attachmentUri = null;
        attachmentName.setText("");
        removeAttachmentButton.setVisibility(View.GONE);
    }

    private String copyFileToPrivateStorage(Uri uri) {
        File privateDir = new File(getFilesDir(), "attachments");
        if (!privateDir.exists()) {
            privateDir.mkdirs();
        }

        String fileName = getFileName(uri);
        File privateFile = new File(privateDir, fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(privateFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "Filed to copy file", Toast.LENGTH_SHORT).show();
            return null;
        }

        return privateFile.getAbsolutePath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ATTACHMENT_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                attachmentUri = data.getData();
                attachmentName.setText(getFileName(attachmentUri));
                removeAttachmentButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        todoDAO.close();
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString( cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
