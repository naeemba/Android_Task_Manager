package baghi.naeem.com.final_project.ui;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import baghi.naeem.com.final_project.R;
import baghi.naeem.com.final_project.entities.Task;
import baghi.naeem.com.final_project.util.DateUtil;

public class TasksRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static List<Task> tasks = new ArrayList<>();
    private static ClickListener clickListener;

    public void setOnItemClickListener(ClickListener clickListener) {
        TasksRecyclerViewAdapter.clickListener = clickListener;
    }

    public void setTasks(List<Object> tasks) {
        TasksRecyclerViewAdapter.tasks = new ArrayList<>();
        if(tasks != null) {
            for(Object task : tasks) {
                TasksRecyclerViewAdapter.tasks.add((Task) task);
            }
        }
    }

    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TasksRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TasksRecyclerViewAdapter.ViewHolder viewHolder = (ViewHolder) holder;
        final Task task = tasks.get(position);
        viewHolder.title.setText(task.getTitle());
        viewHolder.description.setText(task.getDescription());
        viewHolder.dueDate.setText(DateUtil.getFormattedString(task.getDueDate()));
        if(task.getDueDate().getTime().compareTo(new Date()) < 0 && !task.getStatus().equals(Task.Status.FINISHED)) {
            viewHolder.dueDate.setTextColor(Color.parseColor("#FF4081"));
        } else {
            viewHolder.dueDate.setTextColor(Color.parseColor("#000000"));
        }
        viewHolder.duration.setText(MessageFormat.format("{0} hours", task.getDuration()));
        viewHolder.status.setText(MessageFormat.format("Status: {0}", task.getStatus().getValue().toLowerCase()));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;
        TextView description;
        TextView dueDate;
        TextView duration;
        TextView status;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.task_item_title);
            description = (TextView) itemView.findViewById(R.id.task_item_description);
            dueDate = (TextView) itemView.findViewById(R.id.task_item_due_date);
            duration = (TextView) itemView.findViewById(R.id.task_item_duration);
            status = (TextView) itemView.findViewById(R.id.task_item_status);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(tasks.get(getAdapterPosition()).getId());
        }
    }

    public interface ClickListener {
        void onItemClick(String taskId);
    }
}
