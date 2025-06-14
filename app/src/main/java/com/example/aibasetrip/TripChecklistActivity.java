package com.example.aibasetrip;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TripChecklistActivity extends AppCompatActivity {

    private RecyclerView recyclerChecklist;
    private ProgressBar progressChecklist;
    private TextView tvProgressText;
    private Button btnSave, btnReset, btnAddTask;
    private EditText etNewTask;
    private List<TripTask> taskList;
    private ChecklistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_checklist);

        // Status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkGray));
        }

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // UI references
        recyclerChecklist = findViewById(R.id.recyclerChecklist);
        progressChecklist = findViewById(R.id.progressChecklist);
        tvProgressText = findViewById(R.id.tvProgressText);
        btnSave = findViewById(R.id.btnSave);
        btnReset = findViewById(R.id.btnReset);
        btnAddTask = findViewById(R.id.btnAddTask);
        etNewTask = findViewById(R.id.etNewTask);

        // Init task list and adapter
        taskList = new ArrayList<>();
        adapter = new ChecklistAdapter(this, taskList, position -> {
            // Remove task when delete button is clicked
            taskList.remove(position);
            adapter.notifyItemRemoved(position);
            saveTasks();
            updateProgress();
        });

        recyclerChecklist.setLayoutManager(new LinearLayoutManager(this));
        recyclerChecklist.setAdapter(adapter);

        // Load saved tasks
        loadSavedTasks();

        // Save Button functionality
        btnSave.setOnClickListener(v -> {
            saveTasks();
            Toast.makeText(this, "Checklist saved!", Toast.LENGTH_SHORT).show();
        });

        // Reset Button functionality
        btnReset.setOnClickListener(v -> {
            resetTasks();
            adapter.notifyDataSetChanged();
            saveTasks();
            updateProgress();
            Toast.makeText(this, "Checklist reset!", Toast.LENGTH_SHORT).show();
        });

        // Add Task Button functionality
        btnAddTask.setOnClickListener(v -> {
            String newTaskTitle = etNewTask.getText().toString().trim();
            if (!newTaskTitle.isEmpty()) {
                TripTask newTask = new TripTask(newTaskTitle, false);
                taskList.add(newTask);
                adapter.notifyItemInserted(taskList.size() - 1);  // Notify for new task
                etNewTask.setText("");
                saveTasks();
                updateProgress();
                Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("TripPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString("task_list", json);
        editor.apply();
    }

    private void loadSavedTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("TripPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task_list", null);

        List<TripTask> savedList;

        if (json != null) {
            Type type = new TypeToken<List<TripTask>>() {}.getType();
            savedList = gson.fromJson(json, type);
        } else {
            savedList = new ArrayList<>();
            savedList.add(new TripTask("Book Hotel", false));
            savedList.add(new TripTask("Pack Bags", false));
            savedList.add(new TripTask("Print Tickets", false));
            savedList.add(new TripTask("Arrange Transport", false));
        }

        taskList.clear();
        taskList.addAll(savedList);
        adapter.notifyDataSetChanged();  // Notify adapter when tasks are loaded
        updateProgress();
    }

    private void resetTasks() {
        for (TripTask task : taskList) {
            task.setChecked(false);
        }
    }

    void updateProgress() {
        int totalTasks = taskList.size();
        int completedTasks = 0;

        for (TripTask task : taskList) {
            if (task.isChecked()) {
                completedTasks++;
            }
        }

        int progress = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;
        progressChecklist.setProgress(progress);
        tvProgressText.setText(progress + "% Complete");
    }
}
