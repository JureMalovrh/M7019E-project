package se.ltu.erasmus.time_attandance;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.SettingInjectorService;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    UserHelper helper;
    TextView location;
    TextView time;
    TextView event;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = (UserHelper) getApplicationContext();

        /* NAVIGATION DRAWER PROPERTIES */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //floating action button handler -> new clocking activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        try {
            assert fab != null;
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), NewClockingActivity.class);
                    startActivity(intent);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setHeaderData(navigationView);
        navigationView.setCheckedItem(R.id.nav_main_page);

        /* END OF NAVIGATION DRAWER */

        setViewHandlers();
        getLastBooking(); //get last booking from user


        setNotification();
    }
    /* Method that handles the header data in navigationView */
    private void setHeaderData(NavigationView navigationView) {
        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        TextView h_name= (TextView) navHeaderView.findViewById(R.id.header_displayname);
        h_name.setText(helper.getDisplayname());
        TextView h_email= (TextView) navHeaderView.findViewById(R.id.header_email);
        h_email.setText(helper.getEmail());
    }
    /* Method that gets handler to main TW etc. */
    private void setViewHandlers(){
        TextView main_name = (TextView) findViewById(R.id.maincontent_displayname);
        main_name.setText(helper.getDisplayname());

        location = (TextView) findViewById(R.id.maincontent_place);
        time = (TextView) findViewById(R.id.maincontent_time);
        event = (TextView) findViewById(R.id.maincontent_event);
    }
    /* Set notifications for user sound recording based on his preferences */
    private void setNotification() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE); // get hidden preferences
        int delay = sharedPref.getInt(getString(R.string.time_notification_minutes), helper.SOUND_RECORDING_OCCURANCES); // get time occurrence of notifications from user settings
        if(helper.getNotificationFired()){
            return;
        }
        helper.setNotificationFired(true);
        /* create new pending intent that will trigger once the time is right */
        Intent notificationIntent = new Intent(this, RecordSoundReceiver.class);
        notificationIntent.putExtra("id", 0);
        notificationIntent.putExtra("id_user", helper.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay*1000;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /* Navigation view new methods handler */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_main_page){
            return true;
        }
        else if (id == R.id.nav_all_bookings) {
            Intent intent = new Intent(this, AllBookingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_clocking) {
            Intent intent = new Intent(this, NewClockingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_backup) {
            Intent intent = new Intent(this, BackupActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            //Logout user, remove him from the helper
            Intent intent = new Intent(this, LoginActivity.class);
            helper.setId(null);
            helper.setDisplayname(null);
            helper.setEmail(null);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getLastBooking() {
        new LastBookingHandler(this).execute();
    }

    /* Handler for getting last booking from a user*/
    public class LastBookingHandler extends AsyncTask<Void, Void, Boolean> {

        String urlString;
        private Context context;
        public LastBookingHandler(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            urlString = helper.getLastBookingApi();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL(urlString);
                // create new urlConnection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();

                if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
                    String jsonString;
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    jsonString = sb.toString();

                    JSONObject jo = new JSONObject(jsonString);

                    if(jo.has("message")){ // we have message, that means no data yet
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                location.setText("No bookings yet");
                                time.setText("No bookings yet");
                                event.setText("No bookings yet");
                            }
                        });

                    }
                    else { // no message, we already have booking

                        /* Get all needed data from object and construct right strings */
                        String day = jo.getString("day");
                        String month = jo.getString("month");
                        String year = jo.getString("year");
                        String hour = jo.getString("hour");
                        String minute = jo.getString("minute");

                        String latitude = String.format("%.2f", jo.getDouble("latitude"));
                        String longitude = String.format("%.2f", jo.getDouble("longitude"));

                        String typeOfEvent = jo.getString("typeOfEvent");

                        final String timeString = "Time: "+ day +". "+month+". "+year +", "+ hour+": "+minute;
                        final String locationString = "Location: "+  latitude +" : "+longitude;
                        final String eventString = "Event: "+typeOfEvent;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                location.setText(locationString);
                                time.setText(timeString);
                                event.setText(eventString);
                            }
                        });
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }


}
