package baghi.naeem.com.final_project.command;

import android.content.Context;

import java.util.List;

import baghi.naeem.com.final_project.dao.DBHelper;
import baghi.naeem.com.final_project.network.NetworkHandler;

public class Command {

    // CREATE DB
    public static final String CREATE_DB = "CREATE_DB";

    // USER COMMANDS
    public static final String LOGIN = "LOGIN";
    public static final String SIGN_UP = "SIGN_UP";
    public static final String IS_VALID_USER = "IS_VALID_USER";
    public static final String SAVE_USER_IN_DB = "SAVE_USER_IN_DB";

    // TASKS COMMANDS
    public static final String LOAD_TASKS_FROM_NETWORK = "LOAD_TASKS_FROM_NETWORK";
    public static final String LOAD_TASKS_FROM_DB = "LOAD_TASKS_FROM_DB";
    public static final String LOAD_TASKS_FROM_DB_FORCE = "LOAD_TASKS_FROM_DB_FORCE";
    public static final String SAVE_TASKS_IN_DB = "SAVE_TASKS_IN_DB";
    public static final String ADD_TASK = "ADD_TASK";
    public static final String SAVE_TASK_IN_DB = "SAVE_TASK_IN_DB";
    public static final String GET_TASK = "GET_TASK";
    public static final String UPDATE_TASK = "UPDATE_TASK";
    public static final String UPDATE_TASK_IN_DB = "UPDATE_TASK_IN_DB";
    public static final String UPDATE_TASK_STATUS = "UPDATE_TASK_STATUS";
    public static final String UPDATE_TASK_STATUS_IN_DB = "UPDATE_TASK_STATUS_IN_DB";
    public static final String DELETE_TASK = "DELETE_TASK";
    public static final String DELETE_TASK_FROM_DB = "DELETE_TASK_FROM_DB";

    //TASKS STATUSES
    public static final String GET_NEW_TASKS = "GET_NEW_TASKS";
    public static final String GET_STARTED_TASKS = "GET_STARTED_TASKS";
    public static final String GET_FINISHED_TASKS = "GET_FINISHED_TASKS";

    private String command;
    private Context context;
    private List<Object> data;
    private Result result;

    public Command() {
    }

    public Command(String command, Context context, List<Object> data) {
        this.command = command;
        this.context = context;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public Context getContext() {
        return context;
    }

    public List<Object> getData() {
        return data;
    }

    public Result getResult() {
        return result;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public boolean isSuccessful() {
        return this.getResult().equals(Result.SUCCESS);
    }

    public DBHelper getDBHelper() {
        return DBHelper.getInstance(this.getContext());
    }

    public NetworkHandler getNetworkHandler() {
        return NetworkHandler.getInstance(this.getContext());
    }

    public enum Result {
        SUCCESS,
        FAILED
    }
}
