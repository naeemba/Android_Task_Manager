package baghi.naeem.com.final_project.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import baghi.naeem.com.final_project.util.DateUtil;

public class Task {

    private String id;
    private String title;
    private String description;
    private Calendar dueDate;
    private Integer duration;
    private Status status;

    public Task() {}

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.getId());
        jsonObject.put("title", this.getTitle());
        jsonObject.put("description", this.getDescription());
        jsonObject.put("dueDate", DateUtil.getFormattedString(this.getDueDate()));
        jsonObject.put("duration", this.getDuration());
        jsonObject.put("status", this.getStatus().getValue());
        return jsonObject;
    }

    public void fromJson(JSONObject jsonObject) throws JSONException {
        this.setId(jsonObject.getString("id"));
        this.setTitle(jsonObject.getString("title"));
        this.setDescription(jsonObject.getString("description"));
        this.setDueDate(DateUtil.getCalendarByString(jsonObject.getString("dueDate")));
        this.setDuration(jsonObject.getInt("duration"));
        this.setStatus(Status.getByValue(jsonObject.getString("status")));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static enum Status {
        NEW("NEW"),
        STARTED("STARTED"),
        FINISHED("FINISHED");

        private String value;
        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static Status getByValue(String value) {
            for(Status e: Status.values())
                if(e.value.equals(value))
                    return e;
            return null;
        }
    }
}
