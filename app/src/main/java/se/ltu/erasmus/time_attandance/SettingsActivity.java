package se.ltu.erasmus.time_attandance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    EditText time_recording;
    EditText time_occurance;
    EditText server;
    UserHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* NW classes */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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

        setViewHandlers();
    }

    private void setViewHandlers() {
        time_recording = (EditText) findViewById(R.id.seconds_recording);
        time_occurance = (EditText) findViewById(R.id.time_notification);
        server = (EditText) findViewById(R.id.server_address_tw);
        server.setText(helper.getServer());

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int seconds = sharedPref.getInt(getString(R.string.time_recording_seconds), helper.LENGTH_SOUND_RECORDING);
        int minutes = sharedPref.getInt(getString(R.string.time_notification_minutes), helper.SOUND_RECORDING_OCCURANCES);

        time_recording.setText(seconds+"");
        time_occurance.setText(minutes+"");
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
        getMenuInflater().inflate(R.menu.settings, menu);
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
            return true;
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
    /* Method for saving data */
    public void saveSettings(View v) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE); // get shared preferences
        SharedPreferences.Editor editor = sharedPref.edit();
        int seconds_rec = Integer.parseInt(time_recording.getText().toString());
        int notif_occ = Integer.parseInt(time_occurance.getText().toString());

        editor.putInt(getString(R.string.time_recording_seconds), seconds_rec); // save values to a sp
        editor.putInt(getString(R.string.time_notification_minutes), notif_occ);
        editor.commit(); // commit the differences
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
