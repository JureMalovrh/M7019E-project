package se.ltu.erasmus.time_attandance;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllBookingsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    UserHelper helper;
    ListView listview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = (UserHelper) getApplicationContext();
        setContentView(R.layout.activity_all_bookings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_all_bookings);
        navigationView.setNavigationItemSelectedListener(this);

        listview = (ListView) findViewById(R.id.listview);

        new AllBookingsHandler(this).execute();
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
        getMenuInflater().inflate(R.menu.all_bookings, menu);
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

    public class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;

        public MySimpleArrayAdapter(Context context, String[] values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.label);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            textView.setText(values[position]);
            // change the icon for Windows and iPhone
            String s = values[position];
            //Log.e("s", s);
            if (s.startsWith("Arrival")) {
                imageView.setImageResource(R.drawable.ic_alarm_black_24dp);
            } else if(s.startsWith("Departure")) {
                imageView.setImageResource(R.drawable.ic_alarm_on_black_24dp);
            } else if(s.startsWith("Lunch departure")){
                imageView.setImageResource(R.drawable.ic_pause_light);
            } else {
                imageView.setImageResource(R.drawable.ic_skip_next_black_24dp);
            }

            return rowView;
        }
    }


    public class AllBookingsHandler extends AsyncTask<Void, Void, Boolean> {

        String urlString;

        private Context context;

        public AllBookingsHandler(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            urlString = helper.getAllBookingsApi();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL(urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);

                //urlConnection.setDoOutput(true);
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

                    System.out.println("JSON: " + jsonString);
                    JSONArray jo = new JSONArray(jsonString);
                    final String[] values = new String[jo.length()] ;
                    for(int i = 0; i < jo.length(); i++){
                        JSONObject jsonObj = jo.getJSONObject(i);
                        String tmpString;
                        String event = jsonObj.getString("typeOfEvent");
                        String year = jsonObj.getString("year");
                        String month = jsonObj.getString("month");
                        String day = jsonObj.getString("day");
                        String hour = jsonObj.getString("hour");
                        String minute = jsonObj.getString("minute");

                        tmpString = event+", "+ day+". "+month+". "+year+", "+hour+": "+minute;
                        values[i] = tmpString;
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(getApplicationContext(), values);
                            listview.setAdapter(adapter);
                        }
                    });



                    System.out.println(jo.toString());

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }



}
