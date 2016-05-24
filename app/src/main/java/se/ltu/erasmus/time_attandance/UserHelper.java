package se.ltu.erasmus.time_attandance;

import android.app.Application;

/**
 * Created by Jure on 21.5.2016.
 */
public class UserHelper extends Application{
    /* class for all possible helpers of running app, holds user that is logged in, returns api calls etc. */
    public int LENGTH_SOUND_RECORDING = 5;
    public int SOUND_RECORDING_OCCURANCES = 60;
    String server = "http://52.30.221.7:3000/api/";
    String filename = "backup";
    String displayname = "";
    String id = "";
    Boolean notificationFired = false;
    String email = "";

    public UserHelper(){}

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getFilename() {
        return filename+"_"+id+".json";
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getNotificationFired() {
        return notificationFired;
    }
    public void setNotificationFired(Boolean notificationFired) {
        this.notificationFired = notificationFired;
    }
    /* API's for server */
    public String getLoginApi(){ return server+"auth/signin";}
    public String getSignUpApi(){ return server+"auth/signup";}
    public String getLastBookingApi() {return  server+"bookings/user/"+ id;}
    public String getNewBookingApi() {return  server+"bookings";}
    //http://52.30.221.7:3000/api/bookings/list/574197e7e852934362e56faa
    public String getAllBookingsApi() {return  server+"bookings/list/"+id;}
    public String uploadBookingsApi() { return  server+"bookings/backup/load";}
    public String downloadBookingsApi() { return  server+"bookings/backup/save/"+id;}
    public String getNewRecordingApi() { return  server+"recordings";}

}

