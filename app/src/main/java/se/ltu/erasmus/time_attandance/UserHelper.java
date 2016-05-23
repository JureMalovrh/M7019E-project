package se.ltu.erasmus.time_attandance;

import android.app.Application;

/**
 * Created by Jure on 21.5.2016.
 */
public class UserHelper extends Application{

    public int LENGTH_SOUND_RECORDING = 5;
    public int SOUND_RECORDING_OCCURANCES = 60;
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    String server = "http://52.30.221.7:3000/api/";

    public String getFilename() {
        return filename+"_"+id+".json";
    }


    public void setFilename(String filename) {
        this.filename = filename;
    }

    String filename = "backup";

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
    //http://52.30.221.7:3000/api/bookings/list/574197e7e852934362e56faa
    public String getAllBookingsApi() {return  server+"bookings/list/"+id;}
    public String uploadBookingsApi() { return  server+"bookings/backup/load";}
    public String downloadBookingsApi() { return  server+"bookings/backup/save"+id;}

}

