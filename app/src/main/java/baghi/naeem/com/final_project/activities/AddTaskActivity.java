package baghi.naeem.com.final_project.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

public class AddTaskActivity extends AppCompatActivity {

    private EditText title;
    private EditText description;
    private EditText dueDate;
    private EditText dueTime;
    private EditText duration;

    private DatePickerDialog dueDateDialog;
    private TimePickerDialog dueTimeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        title = (EditText) findViewById(R.id.add_task_title);
        description = (EditText) findViewById(R.id.add_task_description);
        dueDate = (EditText) findViewById(R.id.add_task_due_date);
        dueTime = (EditText) findViewById(R.id.add_task_due_time);
        duration = (EditText) findViewById(R.id.add_task_duration);

        Button add = (Button) findViewById(R.id.add_task_add);
        Button clear = (Button) findViewById(R.id.add_task_clear);

        dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();

                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);

                dueDateDialog = new DatePickerDialog(AddTaskActivity.this,
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

                dueTimeDialog = new TimePickerDialog(AddTaskActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                dueTime.setText(DateUtil.repairTimeString(String.format(Locale.US, "%d:%d", hourOfDay, minute)));
                            }
                        }, hour, minute, false);
                dueTimeDialog.show();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidForm())
                    new AddTaskAsyncTask().execute(new Command(Command.ADD_TASK, AddTaskActivity.this, Arrays.<Object>asList(getTaskFromForm())));
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanForm();
            }
        });
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

    public void cleanForm() {
        this.title.setText("");
        this.description.setText("");
        this.dueDate.setText("");
        this.dueTime.setText("");
        this.duration.setText("");
    }

    private Task getTaskFromForm() {
        Task task = new Task();
        task.setTitle(title.getText().toString());
        task.setDescription(description.getText().toString());
        task.setDueDate(DateUtil.getCalendarByString(DateUtil.repairDateString(dueDate.getText().toString()) + " " + DateUtil.repairTimeString(dueTime.getText().toString())));
        task.setDuration(Integer.valueOf(duration.getText().toString()));
        task.setStatus(Task.Status.NEW);
        return task;
    }

    public static class AddTaskCallback implements INetworkCallback<Task> {

        private Context context;

        AddTaskCallback(Context context) {
            this.context = context;
        }

        @Override
        public void callback(Task task, String errorMessage) {
            if ((errorMessage != null) || (task == null)) {
                if (TextUtils.isEmpty(errorMessage))
                    errorMessage = "Failed to save new task to server!";
                ToastUtil.make(context, errorMessage);
                return;
            }

            new AddTaskAsyncTask().execute(new Command(Command.SAVE_TASK_IN_DB, context, Arrays.<Object>asList(task)));
        }
    }

    private static class AddTaskAsyncTask extends AsyncTask<Command, Void, Command> {

        @Override
        protected Command doInBackground(Command... commands) {
            Command command = commands[0];
            switch(command.getCommand()) {
                case Command.ADD_TASK:
                    command.getNetworkHandler().addTask((Task) command.getData().get(0), new AddTaskCallback(command.getContext()));
                    break;
                case Command.SAVE_TASK_IN_DB:
                    boolean result = command.getDBHelper().addTask((Task) command.getData().get(0));
                    command.setResult(result? Command.Result.SUCCESS : Command.Result.FAILED);
                    return command;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Command command) {
            if(command != null) {
                switch(command.getCommand()) {
                    case Command.SAVE_TASK_IN_DB:
                        if(command.isSuccessful()) {
                            ToastUtil.make(command.getContext(), "New task added successfully");
                            ((AddTaskActivity) command.getContext()).cleanForm();
                        }
                        break;
                }
            }
            super.onPostExecute(command);
        }
    }
}
