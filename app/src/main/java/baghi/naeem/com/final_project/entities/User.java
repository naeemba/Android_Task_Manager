package baghi.naeem.com.final_project.entities;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    private String id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;

    public User() {}

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.getId());
        jsonObject.put("username", this.getUsername());
        jsonObject.put("password", this.getPassword());
        jsonObject.put("firstName", this.getFirstName());
        jsonObject.put("lastName", this.getLastName());
        return jsonObject;
    }

    public void fromJson(JSONObject jsonObject) throws JSONException {
        this.setId(jsonObject.getString("id"));
        this.setUsername(jsonObject.getString("username"));
        this.setFirstName(jsonObject.getString("firstName"));
        this.setLastName(jsonObject.getString("lastName"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
