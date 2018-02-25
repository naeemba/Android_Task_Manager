package baghi.naeem.com.final_project.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtil {

    private static final String GENERAL_CALENDAR_FORMAT = "yyyy/MM/dd HH:mm";
    private static final String GENERAL_CALENDAR_DATE_FORMAT = "yyyy/MM/dd";
    private static final String GENERAL_CALENDAR_TIME_FORMAT = "HH:mm";

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(GENERAL_CALENDAR_FORMAT, Locale.ENGLISH);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(GENERAL_CALENDAR_DATE_FORMAT, Locale.ENGLISH);
    private static SimpleDateFormat timeFormat = new SimpleDateFormat(GENERAL_CALENDAR_TIME_FORMAT, Locale.ENGLISH);

    public static Calendar getCalendarByString(String dateString) {
        try {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(simpleDateFormat.parse(dateString));
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDateString(Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }

    public static String getTimeString(Calendar calendar) {
        return timeFormat.format(calendar.getTime());
    }

    public static String getFormattedString(Calendar calendar) {
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String repairTimeString(String timeString) {
        String[] times = timeString.split(":");
        times[0] = times[0].length() > 1 ? times[0] : "0" + times[0];
        times[1] = times[1].length() > 1 ? times[1] : "0" + times[1];
        return times[0] + ":" + times[1];
    }

    public static String repairDateString(String timeString) {
        String[] dates = timeString.split("/");
        dates[1] = dates[1].length() > 1 ? dates[1] : "0" + dates[1];
        dates[2] = dates[2].length() > 1 ? dates[2] : "0" + dates[2];
        return dates[0] + "/" + dates[1] + "/" + dates[2];
    }
}
