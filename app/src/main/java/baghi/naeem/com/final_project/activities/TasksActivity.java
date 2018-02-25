package baghi.naeem.com.final_project.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import baghi.naeem.com.final_project.R;
import baghi.naeem.com.final_project.command.Command;
import baghi.naeem.com.final_project.entities.Task;
import baghi.naeem.com.final_project.network.INetworkCallback;
import baghi.naeem.com.final_project.session.Session;
import baghi.naeem.com.final_project.ui.TasksRecyclerViewAdapter;
import baghi.naeem.com.final_project.util.ToastUtil;

public class TasksActivity extends AppCompatActivity {

    private List<Task> tasks = new ArrayList<>();
    private Timer timer;
    private TimerTask timerTask = new TimerTask() {

        private Handler handler = new TasksHandler(TasksActivity.this);

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        Boolean notInDb = getIntent().getBooleanExtra("NOT_IN_DB", false);
        if(notInDb) {
            new TasksAsyncTask().execute(new Command(Command.LOAD_TASKS_FROM_NETWORK, this, null));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.tasks_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton addTask = (FloatingActionButton) findViewById(R.id.tasks_add);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TasksActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.tasks_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setHasFixedSize(true);

        TasksRecyclerViewAdapter tasksRecyclerViewAdapter = new TasksRecyclerViewAdapter();
        recyclerView.setAdapter(tasksRecyclerViewAdapter);

        tasksRecyclerViewAdapter.setOnItemClickListener(new TasksRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(String taskId) {
                Intent intent = new Intent(TasksActivity.this, EditTaskActivity.class);
                intent.putExtra("taskId", taskId);
                startActivity(intent);
            }
        });

        new TasksAsyncTask().execute(new Command(Command.LOAD_TASKS_FROM_DB, this, null));
        this.startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopTimer();
    }

    private void startTimer() {
        if(timer != null)
            return;

        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 60000);
    }

    private void stopTimer() {
        timer.cancel();
        timer = null;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tasks_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.tasks_menu_new:
                new TasksAsyncTask().execute(new Command(Command.GET_NEW_TASKS, this, null));
                break;
            case R.id.tasks_menu_started:
                new TasksAsyncTask().execute(new Command(Command.GET_STARTED_TASKS, this, null));
                break;
            case R.id.tasks_menu_finished:
                new TasksAsyncTask().execute(new Command(Command.GET_FINISHED_TASKS, this, null));
                break;
            case R.id.tasks_menu_all:
                new TasksAsyncTask().execute(new Command(Command.LOAD_TASKS_FROM_DB_FORCE, this, null));
                break;
            case R.id.tasks_menu_log_out:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new TasksAsyncTask().execute(new Command(Command.LOAD_TASKS_FROM_DB_FORCE, this, null));
    }

    private static class TasksHandler extends Handler {

        private TasksActivity context;
        TasksHandler(Context context) {
            this.context = (TasksActivity) context;
        }
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            List<Task> tasks = context.getTasks();
            if(tasks == null)
                return;

            Calendar previousCalendar = new GregorianCalendar();
            previousCalendar.set(Calendar.HOUR_OF_DAY, (previousCalendar.get(Calendar.HOUR_OF_DAY) - 6));
            for(Task task : tasks) {
                if(task.getStatus().equals(Task.Status.FINISHED))
                    continue;

                if(task.getDueDate().getTime().compareTo(new Date()) < 0) {
                    Calendar calendar = Session.notificationsTimes.get(task.getId());
                    if(calendar == null || calendar.getTime().compareTo(previousCalendar.getTime()) < 0) {
                        Session.notificationsTimes.put(task.getId(), new GregorianCalendar());

                        Intent intent = new Intent(context, TasksActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MY_CHANNEL_ID")
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.drawable.ic_block_black_24dp)
                                .setContentTitle(task.getTitle())
                                .setContentText(task.getDescription())
                                .setContentIntent(pendingIntent)
                                .setContentInfo("Info");
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(new Random().nextInt(), builder.build());
                    }
                }
            }
        }
    }

    public static class TasksCallback implements INetworkCallback<List<Task>> {

        private Context context;

        TasksCallback(Context context) {
            this.context = context;
        }

        @Override
        public void callback(List<Task> tasks, String errorMessage) {
            if (errorMessage != null || tasks == null) {
                if (TextUtils.isEmpty(errorMessage))
                    errorMessage = "Failed to load tasks!";
                ToastUtil.make(context, errorMessage);
                return;
            }

            Collections.sort(tasks, new Comparator<Task>() {
                @Override
                public int compare(Task task1, Task task2) {
                    return task2.getDueDate().getTime().compareTo(task1.getDueDate().getTime());
                }
            });

            new TasksAsyncTask().execute(new Command(Command.SAVE_TASKS_IN_DB, context, new ArrayList<Object>(tasks)));
        }
    }

    private static class TasksAsyncTask extends AsyncTask<Command, Void, Command> {

        @Override
        protected Command doInBackground(Command... commands) {
            Command command = commands[0];
            switch(command.getCommand()) {
                case Command.LOAD_TASKS_FROM_NETWORK:
                    command.getNetworkHandler().getTasks(new TasksCallback(command.getContext()));
                    break;
                case Command.SAVE_TASKS_IN_DB:
                    boolean result = command.getDBHelper().addAllTasks(command.getData());
                    command.setResult(result? Command.Result.SUCCESS: Command.Result.FAILED);
                    return command;
                case Command.LOAD_TASKS_FROM_DB:
                case Command.LOAD_TASKS_FROM_DB_FORCE: {
                    List<Task> tasks = command.getDBHelper().getTasks();
                    ((TasksActivity) command.getContext()).setTasks(tasks);
                    command.setData(new ArrayList<Object>(tasks));
                    return command;
                } case Command.GET_NEW_TASKS: {
                    List<Task> tasks = command.getDBHelper().getTasks(Task.Status.NEW);
                    command.setData(new ArrayList<Object>(tasks));
                    return command;
                } case Command.GET_STARTED_TASKS: {
                    List<Task> tasks = command.getDBHelper().getTasks(Task.Status.STARTED);
                    command.setData(new ArrayList<Object>(tasks));
                    return command;
                } case Command.GET_FINISHED_TASKS: {
                    List<Task> tasks = command.getDBHelper().getTasks(Task.Status.FINISHED);
                    command.setData(new ArrayList<Object>(tasks));
                    return command;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Command command) {
            if(command != null) {
                switch(command.getCommand()) {
                    case Command.SAVE_TASKS_IN_DB:
                        if(command.isSuccessful()) {
                            RecyclerView recyclerView = (RecyclerView) ((AppCompatActivity) command.getContext()).findViewById(R.id.tasks_recycler_view);
                            TasksRecyclerViewAdapter adapter = (TasksRecyclerViewAdapter) recyclerView.getAdapter();
                            adapter.setTasks(command.getData());
                            adapter.notifyDataSetChanged();
                            ToastUtil.make(command.getContext(), "Tasks loaded successfully");
                        } else {
                            ToastUtil.make(command.getContext(), "Could not load tasks!");
                        }
                        break;
                    case Command.LOAD_TASKS_FROM_DB: {
                        RecyclerView recyclerView = (RecyclerView) ((AppCompatActivity) command.getContext()).findViewById(R.id.tasks_recycler_view);
                        TasksRecyclerViewAdapter adapter = (TasksRecyclerViewAdapter) recyclerView.getAdapter();
                        List<Task> tasks = adapter.getTasks();
                        if (tasks == null || tasks.size() < 1) {
                            adapter.setTasks(command.getData());
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    }
                    case Command.GET_NEW_TASKS:
                    case Command.GET_STARTED_TASKS:
                    case Command.GET_FINISHED_TASKS:
                    case Command.LOAD_TASKS_FROM_DB_FORCE: {
                        RecyclerView recyclerView = (RecyclerView) ((AppCompatActivity) command.getContext()).findViewById(R.id.tasks_recycler_view);
                        TasksRecyclerViewAdapter adapter = (TasksRecyclerViewAdapter) recyclerView.getAdapter();
                        adapter.setTasks(command.getData());
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
            super.onPostExecute(command);
        }
    }
}
