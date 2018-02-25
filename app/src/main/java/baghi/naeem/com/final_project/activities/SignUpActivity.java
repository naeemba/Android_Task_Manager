package baghi.naeem.com.final_project.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import baghi.naeem.com.final_project.dao.DBHelper;
import baghi.naeem.com.final_project.entities.User;
import baghi.naeem.com.final_project.network.INetworkCallback;
import baghi.naeem.com.final_project.util.ToastUtil;

public class SignUpActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private EditText firstName;
    private EditText lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        username = (EditText) findViewById(R.id.signup_username);
        password = (EditText) findViewById(R.id.signup_password);
        firstName = (EditText) findViewById(R.id.signup_first_name);
        lastName = (EditText) findViewById(R.id.signup_last_name);

        Button signUp = (Button) findViewById(R.id.signup_signup);
        Button clear = (Button) findViewById(R.id.signup_clear);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidForm(username.getText().toString(), password.getText().toString()))
                    new SignUpAsyncTask().execute(
                            new Command(
                                    Command.IS_VALID_USER,
                                    SignUpActivity.this,
                                    Arrays.<Object>asList(getEnteredDataAsUser())));
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setText("");
                password.setText("");
                firstName.setText("");
                lastName.setText("");
            }
        });
    }

    private User getEnteredDataAsUser() {
        User user = new User();
        user.setFirstName(firstName.getText().toString());
        user.setLastName(lastName.getText().toString());
        user.setUsername(username.getText().toString());
        user.setPassword(password.getText().toString());
        return user;
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

    public static class SaveUserCallback implements INetworkCallback<User> {

        private Context context;

        SaveUserCallback(Context context) {
            this.context = context;
        }

        @Override
        public void callback(User user, String errorMessage) {
            if ((errorMessage != null) || (user == null)) {
                if (TextUtils.isEmpty(errorMessage))
                    errorMessage = "Failed to save new user to server!";
                ToastUtil.make(context, errorMessage);
                return;
            }

            new SignUpAsyncTask().execute(new Command(Command.SIGN_UP, context, Arrays.<Object>asList(user)));
        }
    }

    private static class SignUpAsyncTask extends AsyncTask<Command, Void, Command> {

        @Override
        protected Command doInBackground(Command... commands) {
            Command command = commands[0];
            User user = (User) command.getData().get(0);
            switch(command.getCommand()) {
                case Command.IS_VALID_USER:
                    boolean isUserAvailable = command.getDBHelper().isUsernameAvailable(user.getUsername());
                    command.setResult(isUserAvailable? Command.Result.SUCCESS : Command.Result.FAILED);
                    return command;
                case Command.SIGN_UP:
                    boolean result = command.getDBHelper().insertUser(user);
                    command.setResult(result? Command.Result.SUCCESS : Command.Result.FAILED);
                    return command;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Command command) {
            if(command != null) {
                switch(command.getCommand()) {
                    case Command.IS_VALID_USER:
                        if(command.isSuccessful()) {
                            command.getNetworkHandler().saveUser((User) command.getData().get(0), new SaveUserCallback(command.getContext()));
                        } else {
                            ToastUtil.make(command.getContext(), "Username exist, choose another one");
                        }
                        break;
                    case Command.SIGN_UP:
                        if(command.isSuccessful()) {
                            Intent intent = new Intent(command.getContext(), LoginActivity.class);
                            command.getContext().startActivity(intent);
                            ((Activity) command.getContext()).finish();
                        } else {
                            ToastUtil.make(command.getContext(), "Saving user failed");
                        }
                        break;
                }
            }
            super.onPostExecute(command);
        }
    }
}
