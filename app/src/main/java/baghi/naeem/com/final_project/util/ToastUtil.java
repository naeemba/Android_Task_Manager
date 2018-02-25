package baghi.naeem.com.final_project.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public static void makeRequiredToast(Context context, String fieldName) {
        ToastUtil.make(context, fieldName + " is required.");
    }

    public static void make(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
