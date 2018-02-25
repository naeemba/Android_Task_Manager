package baghi.naeem.com.final_project.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import baghi.naeem.com.final_project.R;
import baghi.naeem.com.final_project.command.Command;
import baghi.naeem.com.final_project.entities.Task;
import baghi.naeem.com.final_project.network.INetworkCallback;
import baghi.naeem.com.final_project.util.DateUtil;
import baghi.naeem.com.final_project.util.ToastUtil;

public class EditTaskActivity extends AppCompatActivity {

    private EditText title;
    private EditText description;
    private EditText dueDate;
    private EditText dueTime;
    private EditText duration;
    private EditText status;

    private DatePickerDialog dueDateDialog;
    private TimePickerDialog dueTimeDialog;

    private String taskId;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        taskId = getIntent().getExtras().getString("taskId");

        title = (EditText) findViewById(R.id.edit_task_title);
        description = (EditText) findViewById(R.id.edit_task_description);
        dueDate = (EditText) findViewById(R.id.edit_task_due_date);
        dueTime = (EditText) findViewById(R.id.edit_task_due_time);
        duration = (EditText) findViewById(R.id.edit_task_duration);
        status = (EditText) findViewById(R.id.edit_task_status);

        Button update = (Button) findViewById(R.id.edit_task_update);
        Button back = (Button) findViewById(R.id.edit_task_back);

        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_task_toolbar);
        setSupportActionBar(toolbar);

        dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();

                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);

                dueDateDialog = new DatePickerDialog(EditTaskActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                dueDate.setText(DateUtil.repairDateString(String.format(Locale.US, "%d/%d/%d", year, month + 1, dayOfMonth)));
                            }
                        }, year, month, day);

                dueDateDialog.show();
            }
        });

        dueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                dueTimeDialog = new TimePickerDialog(EditTaskActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                dueTime.setText(DateUtil.repairTimeString(String.format(Locale.US, "%d:%d", hourOfDay, minute)));
                            }
                        }, hour, minute, false);
                dueTimeDialog.show();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidForm())
                    new EditTaskAsyncTask().execute(new Command(Command.UPDATE_TASK, EditTaskActivity.this, Arrays.<Object>asList(getTaskFromForm())));
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditTaskActivity.this, TasksActivity.class);
                startActivity(intent);
                finish();
            }
        });

        new EditTaskAsyncTask().execute(new Command(Command.GET_TASK, this, Arrays.<Object>asList(taskId)));
    }

    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_text_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Task.Status status = Task.Status.getByValue(this.status.getText().toString());
        switch(item.getItemId()) {
            case R.id.edit_task_menu_start:
                if(status.equals(Task.Status.NEW)) {
                    new EditTaskAsyncTask().execute(new Command(Command.UPDATE_TASK_STATUS, this, Arrays.<Object>asList(task, Task.Status.STARTED)));
                }
                break;
            case R.id.edit_task_menu_finish:
                if(status.equals(Task.Status.STARTED)  || status.equals(Task.Status.NEW)) {
                    new EditTaskAsyncTask().execute(new Command(Command.UPDATE_TASK_STATUS, this, Arrays.<Object>asList(task, Task.Status.FINISHED)));
                }
                break;
            case R.id.edit_task_menu_delete:
                new EditTaskAsyncTask().execute(new Command(Command.DELETE_TASK, this, Arrays.<Object>asList(taskId)));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isValidForm() {
        if(TextUtils.isEmpty(title.getText().toString())) {
            ToastUtil.makeRequiredToast(this, "Title");
            return false;
        }
        if(TextUtils.isEmpty(description.getText().toString())) {
            ToastUtil.makeRequiredToast(this, "Description");
            return false;
        }
        if(TextUtils.isEmpty(dueDate.getText().toString())) {
            ToastUtil.makeRequiredToast(this, "Due date");
            return false;
        }
        if(TextUtils.isEmpty(dueTime.getText().toString())) {
            ToastUtil.makeRequiredToast(this, "Due time");
            return false;
        }
        if(TextUtils.isEmpty(duration.getText().toString())) {
            ToastUtil.makeRequiredToast(this, "Duration");
            return false;
        }
        try {
            Integer.valueOf(duration.getText().toString());
        } catch (Exception e) {
            ToastUtil.make(this, "Duration is not valid. It should be an integer");
            return false;
        }
        try {
            String[] dateValues = dueDate.getText().toString().split("/");
            if(dateValues.length != 3) {
                throw new Exception("");
            }
            Integer.valueOf(dateValues[0]);
            Integer.valueOf(dateValues[1]);
            Integer.valueOf(dateValues[2]);
        } catch(Exception e) {
            ToastUtil.make(this, "Due date is not valid.");
            return false;
        }
        try {
            String[] timeValues = dueTime.getText().toString().split(":");
            if(timeValues.length != 2) {
                throw new Exception("");
            }
            Integer.valueOf(timeValues[0]);
            Integer.valueOf(timeValues[1]);
        } catch(Exception e) {
            ToastUtil.make(this, "Due time is not valid.");
            return false;
        }

        return true;
    }

    private Task getTaskFromForm() {
        Task task = new Task();
        task.setId(taskId);
        task.setTitle(title.getText().toString());
        task.setDescription(description.getText().toString());
        task.setDueDate(DateUtil.getCalendarByString(DateUtil.repairDateString(dueDate.getText().toString()) + " " + DateUtil.repairTimeString(dueTime.getText().toString())));
        task.setDuration(Integer.valueOf(duration.getText().toString()));
        task.setStatus(Task.Status.getByValue(status.getText().toString()));
        return task;
    }

    public static class EditTaskCallback implements INetworkCallback<Task> {

        private Context context;

        EditTaskCallback(Context context) {
            this.context = context;
        }

        @Override
        public void callback(Task task, String errorMessage) {
            if ((errorMessage != null) || (task == null)) {
                if (TextUtils.isEmpty(errorMessage))
                    errorMessage = "Failed to update task to server!";
                ToastUtil.make(context, errorMessage);
                return;
            }

            new EditTaskAsyncTask().execute(new Command(Command.UPDATE_TASK_IN_DB, context, Arrays.<Object>asList(task)));
        }
    }

    public static class RemoveTaskCallback implements INetworkCallback<String> {

        private Context context;

        RemoveTaskCallback(Context context) {
            this.context = context;
        }

        @Override
        public void callback(String taskId, String errorMessage) {
            if ((errorMessage != null) || (taskId == null)) {
                if (TextUtils.isEmpty(errorMessage))
                    errorMessage = "Failed to update task to server!";
                ToastUtil.make(context, errorMessage);
                return;
            }
            new EditTaskAsyncTask().execute(new Command(Command.DELETE_TASK_FROM_DB, context, Arrays.<Object>asList(taskId)));
        }
    }

    private static class EditTaskAsyncTask extends AsyncTask<Command, Void, Command> {

        @Override
        protected Command doInBackground(Command... commands) {
            Command command = commands[0];
            switch(command.getCommand()) {
                case Command.GET_TASK: {
                    Task task = command.getDBHelper().getTask((String) command.getData().get(0));
                    if (task != null) {
                        command.setData(Arrays.<Object>asList(task));
                    }
                    command.setResult(task != null ? Command.Result.SUCCESS : Command.Result.FAILED);
                    return command;
                } case Command.UPDATE_TASK:
                    command.getNetworkHandler().updateTask((Task) command.getData().get(0), new EditTaskCallback(command.getContext()));
                    break;
                case Command.UPDATE_TASK_IN_DB: {
                    boolean result = command.getDBHelper().updateTask((Task) command.getData().get(0));
                    command.setResult(result ? Command.Result.SUCCESS : Command.Result.FAILED);
                    return command;
                } case Command.UPDATE_TASK_STATUS:
                    Task task = (Task) command.getData().get(0);
                    task.setStatus((Task.Status) command.getData().get(1));
                    command.getNetworkHandler().updateTask(task, new EditTaskCallback(command.getContext()));
                    break;
                case Command.DELETE_TASK:
                    command.getNetworkHandler().removeTask((String) command.getData().get(0), new RemoveTaskCallback(command.getContext()));
                    break;
                case Command.DELETE_TASK_FROM_DB:
                    boolean result = command.getDBHelper().removeTask((String) command.getData().get(0));
                    command.setResult(result? Command.Result.SUCCESS : Command.Result.FAILED);
                    return command;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Command command) {
            if(command != null) {
                switch(command.getCommand()) {
                    case Command.GET_TASK:
                        Task task = (Task) command.getData().get(0);
                        EditTaskActivity context = (EditTaskActivity) command.getContext();
                        context.setTask(task);
                        ((EditText) context.findViewById(R.id.edit_task_title)).setText(task.getTitle());
                        ((EditText) context.findViewById(R.id.edit_task_description)).setText(task.getDescription());
                        ((EditText) context.findViewById(R.id.edit_task_due_date)).setText(DateUtil.getDateString(task.getDueDate()));
                        ((EditText) context.findViewById(R.id.edit_task_due_time)).setText(DateUtil.getTimeString(task.getDueDate()));
                        ((EditText) context.findViewById(R.id.edit_task_duration)).setText(task.getDuration() + "");
                        ((EditText) context.findViewById(R.id.edit_task_status)).setText(task.getStatus().getValue());
                        break;
                    case Command.UPDATE_TASK_IN_DB:
                    case Command.DELETE_TASK_FROM_DB:
                        Intent intent = new Intent(command.getContext(), TasksActivity.class);
                        command.getContext().startActivity(intent);
                        ((Activity) command.getContext()).finish();
                        break;
                }
            }
            super.onPostExecute(command);
        }
    }
}
