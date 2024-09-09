package com.example.todo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.database.TodoDAO;
import com.example.todo.todo.Todo;
import com.example.todo.todo.TodoAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TodoDAO todoDAO;
    private List<Todo> todoList;
    private List<Todo> filteredTodoList;
    private RecyclerView recyclerView;
    private TodoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        todoDAO = new TodoDAO(this);
        todoDAO.open();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        todoList = todoDAO.getAllTodos();
        filteredTodoList = new ArrayList<>(todoList);
        adapter = new TodoAdapter(filteredTodoList, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
            startActivityForResult(intent, 1);
        });

        applyFilters();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterTodos(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTodos(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        todoList.clear();
        todoList.addAll(todoDAO.getAllTodos());
        applyFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        todoList.clear();
        todoList.addAll(todoDAO.getAllTodos());
        applyFilters();
    }

    private void filterTodos(String query) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hideCompletedTasks = prefs.getBoolean("hide_completed_tasks", false);
        Set<String> visibleCategories = prefs.getStringSet("visible_categories", new HashSet<>());

        filteredTodoList.clear();
        for (Todo todo : todoList) {
            if (todo.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredTodoList.add(todo);
            }
        }

        if (hideCompletedTasks) {
            filteredTodoList.removeIf(Todo::isCompleted);
        }

        filteredTodoList.removeIf(todo -> !visibleCategories.contains(todo.getCategory()));

        Collections.sort(filteredTodoList, Comparator.comparingLong(Todo::getDueAt));

        adapter.setTodoList(filteredTodoList);
        adapter.notifyDataSetChanged();
    }

    public void applyFilters() {
        filterTodos("");
    }
}
