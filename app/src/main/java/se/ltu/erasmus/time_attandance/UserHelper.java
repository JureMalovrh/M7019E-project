package se.ltu.erasmus.time_attandance;

import android.app.Application;

/**
 * Created by Jure on 21.5.2016.
 */
public class UserHelper extends Application{
    String server = "http://52.30.221.7:3000/api/";

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String displayname = "";
    String id = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    String email = "";
    public UserHelper(){}

    public String getLoginApi(){ return server+"auth/signin";}
    public String getSignUpApi(){ return server+"auth/signup";}
    public String getLastBookingApi() {return  server+"bookings/user/"+ id;}
    public String getNewBookingApi() {return  server+"bookings";}

}

