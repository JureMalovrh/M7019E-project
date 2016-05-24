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
import android.widget.TextView;
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
        /* NW needed data */
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
        setHeaderData(navigationView);
        navigationView.setCheckedItem(R.id.nav_backup);
    }

    private void setHeaderData(NavigationView navigationView) {
        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        TextView h_name= (TextView) navHeaderView.findViewById(R.id.header_displayname);
        h_name.setText(helper.getDisplayname());
        TextView h_email= (TextView) navHeaderView.findViewById(R.id.header_email);
        h_email.setText(helper.getEmail());
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

    /* Navigation view new methods handler */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_main_page){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
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
            return  true;
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


    /* createBackup is used to crate new backup, is overwrites previous one */
    public void createBackup(View v) {
        new CreateBackupHandler(this).execute();
    }
    /* uploadBackup is used to upload previously saved backup if there exsists one */
    public void uploadBackup(View v) {
        new UploadBackupHandler(this).execute();
    }

    /* Handler of a creating backup, it gets all data from API and writes them to a file */
    public class CreateBackupHandler extends AsyncTask<Void, Void, Boolean> {
        String urlString;

        private Context context;
        private final ProgressDialog dialog= new ProgressDialog(BackupActivity.this);
        public CreateBackupHandler(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Wait\nDownload is on the way...");
            this.dialog.show();
            urlString = helper.downloadBookingsApi();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status;
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL(urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();

                if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // read from the response
                    BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
                    String jsonString;
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    jsonString = sb.toString();
                    // create new json array from response
                    JSONArray jo = new JSONArray(jsonString);
                    String FILENAME = helper.getFilename();

                    /* write to file, in the application private dir */
                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(jo.toString().getBytes());
                    fos.close();
                    status = true;
                }
                else {
                    status = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if(result){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Backup saved", Toast.LENGTH_LONG).show();
                    }
                });

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Problems saving backup", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
    /* Handler to read backup and save it to server */
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
                response = performPostCall(urlString); // perform actual post call
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!response.equals("")) { // if response is ok
                status = true;
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
            if(result){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Backup completed", Toast.LENGTH_LONG).show();
                    }

                });
            }

        }

        public String performPostCall(String requestURL) {

            URL url;
            String response = "";
            HttpURLConnection conn = null;
            try {
                url = new URL(requestURL);
                //open the connection
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                JSONObject root = new JSONObject();
                //build new JSON object
                try{
                    FileInputStream fos = openFileInput(helper.getFilename()); //read file from a mobile
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fos));
                    StringBuilder out = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.append(line);
                    }
                    reader.close();
                    /* put values to json*/
                    root.put("id", helper.getId());
                    root.put("values", out.toString());

                } catch (FileNotFoundException e) { // To be sure
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "No backup on phone", Toast.LENGTH_LONG).show();
                        }
                    });
                    return "";
                }

                String str = root.toString();
                byte[] outputBytes = str.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes); // write to server
                os.close();
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    response = "Ok"; // dont read other response form server
                } else {
                    response = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
            return response;
        }
    }
}

