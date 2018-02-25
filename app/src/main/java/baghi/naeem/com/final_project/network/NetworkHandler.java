package baghi.naeem.com.final_project.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import baghi.naeem.com.final_project.entities.Task;
import baghi.naeem.com.final_project.entities.User;
import baghi.naeem.com.final_project.util.LogUtil;

public class NetworkHandler {

    public static final String SAVE_USER_TAG = "SAVE_USER";
    public static final String GET_USER_TAG = "GET_USER";

    private static NetworkHandler instance;

    private RequestQueue requestQueue;
    private Context context;

    private String serverURL;
    private String encodedCredentials;

    private NetworkHandler(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public static NetworkHandler getInstance(Context context) {
        if(instance == null)
            instance = new NetworkHandler(context);
        return instance;
    }

    public String getServerURL() {
        return this.serverURL;
    }

    public void setServerURL(String url) {
        this.serverURL = url;

        if(!this.serverURL.startsWith("http://"))
            this.serverURL = "http://" + this.serverURL;

        if(!this.serverURL.endsWith("/"))
            this.serverURL = this.serverURL + "/";
    }

    public void setCommunicationProperties(String username, String password) {
        this.encodedCredentials = "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return networkInfo != null && networkInfo.isConnected();
    }

    public void saveUser(final User user, final INetworkCallback<User> listener) {
        if(listener == null) {
            Log.e(getTag(), "Null listener");
            return;
        }
        if(!isNetworkConnected()) {
            listener.callback(null, "Network is not connected!");
            return;
        }

        JSONObject object;
        try {
            object = user.toJson();
        } catch (JSONException e) {
            Log.e(getTag(), e.getMessage());
            listener.callback(null, "Failed to parse user to JSON");
            return;
        }

        Log.d(getTag(), "Saving user...");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, serverURL + "signup", object,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            User savedUser = new User();
                            savedUser.fromJson(response);
                            user.setId(savedUser.getId());

                            Log.d(getTag(), "New user saved successfully");

                            listener.callback(user, null);
                        } catch(JSONException e) {
                            Log.d(getTag(), e.getMessage());
                            listener.callback(null, "Saving user failed");
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Pair<String, String> errorMessage = getErrorMessage(error, SAVE_USER_TAG);
                        Log.e(getTag(), errorMessage.first);
                        listener.callback(null, errorMessage.second);
                    }
                }
        );

        request.setTag(SAVE_USER_TAG);
        requestQueue.add(request);
    }

    public void login(final String username, final String password, final INetworkCallback<User> listener) {
        if(listener == null) {
            Log.e(getTag(), "Null listener");
            return;
        }
        if(!isNetworkConnected()) {
            listener.callback(null, "Network is not connected!");
            return;
        }

        Log.d(getTag(), "Check login info...");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, serverURL + "signin", null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            User savedUser = new User();
                            savedUser.fromJson(response);
                            savedUser.setPassword(password);
                            Log.d(getTag(), "New user saved successfully");

                            listener.callback(savedUser, null);
                        } catch(JSONException e) {
                            Log.d(getTag(), e.getMessage());
                            listener.callback(null, "Saving user failed");
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Pair<String, String> errorMessage = getErrorMessage(error, SAVE_USER_TAG);
                        Log.e(getTag(), errorMessage.first);
                        listener.callback(null, errorMessage.second);
                    }
                }
        ) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));
                return headers;
            }
        };

        request.setTag(SAVE_USER_TAG);
        requestQueue.add(request);
    }

    public void getTasks(final INetworkCallback<List<Task>> listener) {
        if(listener == null) {
            Log.e(getTag(), "Null listener");
            return;
        }
        if(!isNetworkConnected()) {
            listener.callback(null, "Network is not connected!");
            return;
        }

        Log.d(getTag(), "Get tasks...");

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, serverURL + "Task", null,
                new Response.Listener<JSONArray>() {
                    public void onResponse(JSONArray response) {
                        List<Task> tasks = new ArrayList<>();
                        try {
                            for(int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                Task task = new Task();
                                task.fromJson(jsonObject);
                                tasks.add(task);
                            }
                            listener.callback(tasks, null);
                        } catch(JSONException e) {
                            Log.d(getTag(), e.getMessage());
                            listener.callback(null, "Saving user failed");
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Pair<String, String> errorMessage = getErrorMessage(error, SAVE_USER_TAG);
                        Log.e(getTag(), errorMessage.first);
                        listener.callback(null, errorMessage.second);
                    }
                }
        ) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", encodedCredentials);
                return headers;
            }
        };

        request.setTag(SAVE_USER_TAG);
        requestQueue.add(request);
    }

    public void addTask(final Task task, final INetworkCallback<Task> listener) {
        if(listener == null) {
            Log.e(getTag(), "Null listener");
            return;
        }
        if(!isNetworkConnected()) {
            listener.callback(null, "Network is not connected!");
            return;
        }

        JSONObject object;
        try {
            object = task.toJson();
        } catch (JSONException e) {
            Log.e(getTag(), e.getMessage());
            listener.callback(null, "Failed to parse user to JSON");
            return;
        }

        Log.d(getTag(), "Add new task...");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, serverURL + "Task", object,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            Task savedTask = new Task();
                            savedTask.fromJson(response);
                            task.setId(savedTask.getId());

                            LogUtil.debug(getTag(), "New task added successfully");

                            listener.callback(task, null);
                        } catch(JSONException e) {
                            LogUtil.debug(getTag(), e.getMessage());
                            listener.callback(null, "Saving reservation failed");
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Pair<String, String> errorMessage = getErrorMessage(error, SAVE_USER_TAG);
                        Log.e(getTag(), errorMessage.first);
                        listener.callback(null, errorMessage.second);
                    }
                }
        ) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", encodedCredentials);
                return headers;
            }
        };

        request.setTag(SAVE_USER_TAG);
        requestQueue.add(request);
    }

    public void updateTask(final Task task, final INetworkCallback<Task> listener) {
        if(listener == null) {
            Log.e(getTag(), "Null listener");
            return;
        }
        if(!isNetworkConnected()) {
            listener.callback(null, "Network is not connected!");
            return;
        }

        JSONObject object;
        try {
            object = task.toJson();
        } catch (JSONException e) {
            Log.e(getTag(), e.getMessage());
            listener.callback(null, "Failed to parse user to JSON");
            return;
        }

        Log.d(getTag(), "updating task " + task.getId());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, serverURL + "Task/" + task.getId(), object,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            Task savedTask = new Task();
                            savedTask.fromJson(response);

                            LogUtil.debug(getTag(), "New task added successfully");

                            listener.callback(task, null);
                        } catch(JSONException e) {
                            LogUtil.debug(getTag(), e.getMessage());
                            listener.callback(null, "Saving reservation failed");
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Pair<String, String> errorMessage = getErrorMessage(error, SAVE_USER_TAG);
                        Log.e(getTag(), errorMessage.first);
                        listener.callback(null, errorMessage.second);
                    }
                }
        ) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", encodedCredentials);
                return headers;
            }
        };

        request.setTag(SAVE_USER_TAG);
        requestQueue.add(request);
    }

    public void removeTask(final String taskId, final INetworkCallback<String> listener) {
        if(listener == null) {
            Log.e(getTag(), "Null listener");
            return;
        }
        if(!isNetworkConnected()) {
            listener.callback(null, "Network is not connected!");
            return;
        }

        Log.d(getTag(), "removing task " + taskId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, serverURL + "Task/" + taskId, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            Task removedTask = new Task();
                            removedTask.fromJson(response);

                            LogUtil.debug(getTag(), "New task added successfully");

                            listener.callback(taskId, null);
                        } catch(JSONException e) {
                            LogUtil.debug(getTag(), e.getMessage());
                            listener.callback(null, "Saving reservation failed");
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Pair<String, String> errorMessage = getErrorMessage(error, SAVE_USER_TAG);
                        Log.e(getTag(), errorMessage.first);
                        listener.callback(null, errorMessage.second);
                    }
                }
        ) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", encodedCredentials);
                return headers;
            }
        };

        request.setTag(SAVE_USER_TAG);
        requestQueue.add(request);
    }

    private String getTag() {
        return this.getClass().getSimpleName();
    }

    private Pair<String, String> getErrorMessage(VolleyError error, String requestTag) {
        String logMessage = "";
        String userMessage;

        String errorData = null;
        String errorMessage = error.getMessage();
        NetworkResponse response = error.networkResponse;

        if(response != null) {
            if(response.data != null)
                errorData = new String(response.data);
        }

        if(!TextUtils.isEmpty(errorMessage))
            logMessage += (!TextUtils.isEmpty(logMessage)? " - " : "") + errorMessage;

        if(!TextUtils.isEmpty(errorData))
            logMessage += (!TextUtils.isEmpty(logMessage)? " - " : "") + errorData;

        String requestInfo = "The request ";

        if(requestTag.equals(SAVE_USER_TAG))
            requestInfo += requestTag;

        if (error instanceof NetworkError) {
            userMessage = "Failed to communicate with server for " + requestInfo + "!";
        } else if (error instanceof AuthFailureError) {
            userMessage = "Cannot authenticate with server for " + requestInfo + "!";
        } else if (error instanceof ClientError) {
            userMessage = "Incomplete or inappropriate request for " + requestInfo + "!";
        } else if (error instanceof ServerError) {
            userMessage = "Some server error occurred during " + requestInfo + "!";
        } else if (error instanceof ParseError) {
            userMessage = "Failed to parse the results after " + requestInfo + "!";
        } else {
            userMessage = "Failed in " + requestInfo + "!";
        }

        if (TextUtils.isEmpty(logMessage)) {
            logMessage = userMessage;
        }

        return new Pair<>(logMessage, userMessage);
    }
}
