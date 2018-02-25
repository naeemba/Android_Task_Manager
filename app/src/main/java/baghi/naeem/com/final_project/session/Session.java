package baghi.naeem.com.final_project.session;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import baghi.naeem.com.final_project.entities.User;

public class Session {

    public static User user = new User();
    public static String serverURL = "http://192.168.1.103:5000";
    public static Map<String, Calendar> notificationsTimes = new HashMap<>(); // Map<taskId, Last notifications date
}
