package se.ltu.erasmus.time_attandance;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class BackupActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    UserHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        helper = (UserHelper) getApplicationContext();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
        getMenuInflater().inflate(R.menu.backup, menu);
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



    public void createBackup(View v) {
        new CreateBackupHandler(this).execute();
    }
    public void uploadBackup(View v) {
        new UploadBackupHandler(this).execute();
    }


    public class CreateBackupHandler extends AsyncTask<Void, Void, Boolean> {

        String urlString;

        private Context context;
        private final ProgressDialog dialog= new ProgressDialog(BackupActivity.this);
        public CreateBackupHandler(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Wait\nDowloading is on the way...");
            this.dialog.show();
            urlString = helper.downloadBookingsApi();
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
                    String FILENAME = helper.getFilename();


                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(jo.toString().getBytes());
                    fos.close();
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
            if(this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
        }
    }

        public class UploadBackupHandler extends AsyncTask<Void, Void, Boolean> {

            String urlString;

            private Context context;
            private final ProgressDialog dialog= new ProgressDialog(BackupActivity.this);

            public UploadBackupHandler(Context contex) {
                this.context = contex;
            }

            @Override
            protected void onPreExecute() {
                this.dialog.setMessage("Wait\nFile is on the way...");
                this.dialog.show();
                urlString = helper.uploadBookingsApi();
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
            protected void onPostExecute(Boolean result){
                if(this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
                urlString = helper.uploadBookingsApi();
            }

            public String performPostCall(String requestURL, HashMap<String, Object> postDataParams) {

                URL url;
                String response = "";
                HttpURLConnection conn = null;
                try {
                    url = new URL(requestURL);

                     conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    conn.setRequestProperty("Content-Type", "application/json");
                    JSONObject root = new JSONObject();

                    try{
                        FileInputStream fos = openFileInput(helper.getFilename());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(fos));
                        StringBuilder out = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            out.append(line);
                        }
                        System.out.println(out.toString());   //Prints the string content read from input stream
                        reader.close();
                        root.put("id", helper.getId());
                        root.put("values", out.toString());

                    } catch (FileNotFoundException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "No backup on phone", Toast.LENGTH_LONG).show();
                            }
                        });
                        return "";
                    }






                    String str = root.toString();
                    Log.e("str", str);
                    byte[] outputBytes = str.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputBytes);
                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        String lineBuff;
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                conn.getInputStream()));
                        while ((lineBuff = br.readLine()) != null) {
                            response += lineBuff;
                        }
                    } else {
                        response = "";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    assert conn != null;
                    conn.disconnect();
                }

                return response;
            }
        }
}

