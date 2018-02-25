package baghi.naeem.com.final_project.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;

import baghi.naeem.com.final_project.R;
import baghi.naeem.com.final_project.command.Command;
import baghi.naeem.com.final_project.entities.User;
import baghi.naeem.com.final_project.network.INetworkCallback;
import baghi.naeem.com.final_project.network.NetworkHandler;
import baghi.naeem.com.final_project.session.Session;
import baghi.naeem.com.final_project.util.ToastUtil;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;

    private EditText serverURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        new LoginAsyncTask().execute(new Command(Command.CREATE_DB, this, null));
        NetworkHandler.getInstance(this).setServerURL(Session.serverURL);

        username = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);

        serverURL = (EditText) findViewById(R.id.login_server_url);
        serverURL.setText(Session.serverURL);

        Button login = (Button) findViewById(R.id.login_login);
        Button signup = (Button) findViewById(R.id.login_signup);
        Button saveServerURL = (Button) findViewById(R.id.login_save_server_url);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = LoginActivity.this.username.getText().toString();
                String password = LoginActivity.this.password.getText().toString();
                if(isValidForm(username, password))
                    new LoginAsyncTask().execute(new Command(Command.LOGIN, LoginActivity.this, Arrays.<Object>asList(username, password)));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        saveServerURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(serverURL.getText().toString())) {
                    Session.serverURL = serverURL.getText().toString();
                    NetworkHandler.getInstance(LoginActivity.this).setServerURL(Session.serverURL);
                    ToastUtil.make(LoginActivity.this, "Server URL saved");
                }
            }
        });
    }

    private boolean isValidForm(String username, String password) {
        if(TextUtils.isEmpty(username)) {
            ToastUtil.makeRequiredToast(this, "Username");
            return false;
        }
        if(TextUtils.isEmpty(password)) {
            ToastUtil.makeRequiredToast(this, "Password");
        }
        return true;
    }

    public static class LoginUserCallback implements INetworkCallback<User> {

        private Context context;

        LoginUserCallback(Context context) {
            this.context = context;
        }

        @Override
        public void callback(User user, String errorMessage) {
            if(user == null) {
                ToastUtil.make(context, "Invalid login");
            } else {
                Session.user = user;
                new LoginAsyncTask().execute(new Command(Command.SAVE_USER_IN_DB, context, Arrays.<Object>asList(user)));
                NetworkHandler.getInstance(context).setCommunicationProperties(user.getUsername(), user.getPassword());
                Intent intent = new Intent(context, TasksActivity.class);
                intent.putExtra("NOT_IN_DB", true);
                context.startActivity(intent);
                ((Activity) context).finish();
            }
        }
    }

    private static class LoginAsyncTask extends AsyncTask<Command, Void, Command> {

        @Override
        protected Command doInBackground(Command... commands) {
            Command command = commands[0];
            switch(command.getCommand()) {
                case Command.CREATE_DB:
                    command.getDBHelper().getWritableDatabase();
                    break;
                case Command.LOGIN:
                    User user = command.getDBHelper().existByUsernameAndPassword((String) command.getData().get(0), (String) command.getData().get(1));
                    if(user != null) {
                        Session.user = user;
                        command.getNetworkHandler().setCommunicationProperties((String) command.getData().get(0), (String) command.getData().get(1));
                    }
                    command.setResult(user != null? Command.Result.SUCCESS : Command.Result.FAILED);
                    return command;
                case Command.SAVE_USER_IN_DB:
                    command.getDBHelper().insertUser((User) command.getData().get(0));
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Command command) {
            if(command != null) {
                switch(command.getCommand()) {
                    case Command.LOGIN:
                        if(command.isSuccessful()) {
                            Intent intent = new Intent(command.getContext(), TasksActivity.class);
                            command.getContext().startActivity(intent);
                            ((Activity) command.getContext()).finish();
                        } else {
                            command.getNetworkHandler().login((String) command.getData().get(0), (String) command.getData().get(1), new LoginUserCallback(command.getContext()));
                        }
                        break;
                }
            }
            super.onPostExecute(command);
        }
    }
}
