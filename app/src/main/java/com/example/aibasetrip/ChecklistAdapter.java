package com.example.aibasetrip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {

    private final List<TripTask> taskList;
    private final OnTaskDeleteListener deleteListener;
    private final Context context;

    public ChecklistAdapter(Context context, List<TripTask> taskList, OnTaskDeleteListener deleteListener) {
        this.context = context;
        this.taskList = taskList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.checklist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TripTask task = taskList.get(position);
        holder.tvTask.setText(task.getTitle());
        holder.checkBox.setChecked(task.isChecked());

        // Make sure the checkbox click updates the task
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setChecked(isChecked);
            // Update progress
            ((TripChecklistActivity) context).updateProgress();
        });

        // Set the delete button functionality
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onTaskDeleted(position); // Remove task when the button is clicked
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTask;
        CheckBox checkBox;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTask = itemView.findViewById(R.id.tvTask);
            checkBox = itemView.findViewById(R.id.checkBox);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface OnTaskDeleteListener {
        void onTaskDeleted(int position);
    }
}
