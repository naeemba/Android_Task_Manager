package baghi.naeem.com.final_project.util;

import android.util.Log;

public class LogUtil {

    private static final String TAG = "Task_Manager";

    public static void debug(String clazz, String message) {
        Log.d(TAG, clazz + ", " + message);
    }

    public static void error(String clazz, String message) {
        Log.e(TAG, clazz + ", " + message);
    }
}
