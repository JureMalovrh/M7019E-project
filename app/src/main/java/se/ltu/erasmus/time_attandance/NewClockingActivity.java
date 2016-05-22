package se.ltu.erasmus.time_attandance;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class NewClockingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GpsStatus.Listener, LocationListener {
    LocationManager lm;
    Location l;
    boolean locationUpdated = false;
    TextView longitude;
    TextView latitude;
    TextView calendar;
    TextView clock;
    UserHelper helper;


    int minute;
    int hour;
    int day;
    int month;
    int year;
    double latitudeVal;
    double longitudeVal;
    String typeOfEvent;
    Spinner spinner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = (UserHelper) getApplicationContext();
        setContentView(R.layout.activity_new_clocking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Location service must be turned on to use app", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        else {
            lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            if(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null ) {
                createMap();
                lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, this.getMainLooper());
            }
            else {
                if(lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                    createMap();
                    lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, this.getMainLooper());
                }
                lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, this.getMainLooper());
                lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, this.getMainLooper());
            }
        }
        //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.possible_access, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        longitude = (TextView) findViewById(R.id.longitude);
        latitude = (TextView) findViewById(R.id.latitude);
        calendar = (TextView) findViewById(R.id.calendar_tw);
        clock = (TextView) findViewById(R.id.clock_tw);
        Calendar mcurrentTime = Calendar.getInstance();
        hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mcurrentTime.get(Calendar.MINUTE);

        clock.setText(hour+":"+minute);

        day = mcurrentTime.get(Calendar.DAY_OF_MONTH);
        month = mcurrentTime.get(Calendar.MONTH);
        year = mcurrentTime.get(Calendar.YEAR);
        calendar.setText(day+"."+month+"."+year);



    }

    public void createMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public Location getLocation() {
        try {
            lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {

                if (isGPSEnabled) {
                    if (l == null) {
                        try{
                            lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, this.getMainLooper());
                            //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, this);

                        } catch (SecurityException e){
                            e.printStackTrace();
                        }

                        Log.d("GPS", "GPS Enabled");
                        if (lm != null) {
                            try{
                                l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            } catch (SecurityException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return l;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(this, NewClockingActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "You cannot use app without location enabled", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_clocking, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_main_page){

        }
        else if (id == R.id.nav_all_bookings) {
            Intent intent = new Intent(this, AllBookingsActivity.class);
            startActivity(intent);


        } else if (id == R.id.nav_clocking) {

            /*AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float leftVolume = curVolume/maxVolume;
            float rightVolume = curVolume/maxVolume;
            int priority = 1;
            int no_loop = 0;
            float normal_playback_rate = 1f;
            soundPool.play(soundPool.load(this, SoundEffectConstants.CLICK, 1), leftVolume, rightVolume, priority, no_loop, normal_playback_rate);*/


            Intent intent = new Intent(this, NewClockingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_backup) {

        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(l == null){
                l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            double lat = l.getLatitude();
            double lon = l.getLongitude();
            longitude.setText(""+lon);
            latitude.setText(""+lat);

            CameraPosition position = CameraPosition.builder()
                    .target(new LatLng(lat,lon))
                    .zoom(16)
                    .bearing(0)
                    .tilt(45)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000, null);
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title("Your location"));
            googleMap.setMyLocationEnabled(true);
        }catch (SecurityException e){
            e.printStackTrace();
        }


    }

    @Override
    public void onGpsStatusChanged(int event) {
        createMap();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("LOCATION", "Location changed");

        if(!locationUpdated){
            locationUpdated = true;
            createMap();
            try{
                lm.removeUpdates(this);
            } catch (SecurityException e){e.printStackTrace();}
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("LOCATION", "Status changed");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e("LOCATION", "Provider enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void registerNewEvent(View v) {
        if(longitude.getText().toString().equals("Wait for location")){
            Toast.makeText(this, "Location not yet received. Please wait.", Toast.LENGTH_LONG).show();
        }
        else{

            longitudeVal = Double.parseDouble(longitude.getText().toString());
            latitudeVal = Double.parseDouble(latitude.getText().toString());

            typeOfEvent = spinner.getSelectedItem().toString();
            new NewEventHandler(this).execute();
        }
    }

    public void timeClicked(View v){
        Calendar mcurrentTime = Calendar.getInstance();

        hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mcurrentTime.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Log.e("time", ""+selectedHour +" "+ selectedMinute);
                TextView tw1 = (TextView) findViewById(R.id.clock_tw);
                //TODO: check if time selected is in 15 min interval
                tw1.setText(""+selectedHour+":"+selectedMinute);
                hour = selectedHour;
                minute = selectedMinute;
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();

    }

    public void dateHandler(View v) {
        Calendar mcurrentTime = Calendar.getInstance();
        day = mcurrentTime.get(Calendar.DAY_OF_MONTH);
        month = mcurrentTime.get(Calendar.MONTH);
        year = mcurrentTime.get(Calendar.YEAR);
        DatePickerDialog mDatePicker;

        // TODO Auto-generated method stub
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int yearC, int monthOfYear, int dayOfMonth) {

                TextView tw1 = (TextView) findViewById(R.id.calendar_tw);
                //TODO: check if time selected is in 15 min interval
                tw1.setText( dayOfMonth+"."+monthOfYear+"."+yearC);
                day = dayOfMonth;
                month = monthOfYear;
                year = yearC;
            }
        }, year, month, day).show();
    }


    public class NewEventHandler extends AsyncTask<Void, Void, Boolean> {

        String urlString;

        private Context context;

        public NewEventHandler(Context contex) {
            this.context = contex;
        }

        @Override
        protected void onPreExecute() {

            urlString = helper.getNewBookingApi();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            String response = "";
            try {
                response = performPostCall(urlString, new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("Accept", "application/json");
                        put("Content-Type", "application/json");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!response.equalsIgnoreCase("")) {
                try {

                    JSONObject jRoot = new JSONObject(response);
                    System.out.println(jRoot.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                status = false;
            }
            return status;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            urlString = helper.getNewBookingApi();
        }

        public String performPostCall(String requestURL, HashMap<String, Object> postDataParams) {

            URL url;
            String response = "";
            try {
                url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                JSONObject root = new JSONObject();
                //
                root.put("user", helper.getId());
                root.put("minute", minute);
                root.put("hour", hour);
                root.put("day", day);
                root.put("month", month);
                root.put("year", year);
                root.put("longitude", longitudeVal);
                root.put("latitude", latitudeVal);
                root.put("typeOfEvent", typeOfEvent);



                String str = root.toString();
                Log.e("str", str);
                byte[] outputBytes = str.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes);
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }
    }

}
