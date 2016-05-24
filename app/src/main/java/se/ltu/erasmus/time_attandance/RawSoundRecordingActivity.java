package se.ltu.erasmus.time_attandance;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.WindowDecorActionBar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class RawSoundRecordingActivity extends AppCompatActivity {

    int RECORDER_SAMPLERATE = 44100;
    final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_DEFAULT;
    final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


    short[] buffer = null;
    int bufferReadResult = 0;
    AudioRecord audioRecord = null;
    int bufferSize = 2048;

    UserHelper helper;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Raw recording, for recording rew sound samples from user */
        super.onCreate(savedInstanceState);
        helper = (UserHelper) getApplicationContext();
        checkPermission(); //check permissions
        Intent iin = getIntent(); // get intent that created activity
        Bundle b = iin.getExtras();

        if (b != null) {
            user_id = (String) b.get("id_user"); // get user id
        }
        setContentView(R.layout.activity_raw_sound_recording);


    }
    @Override
    public void onBackPressed() {
        this.finish();
    }
    /* check if application has right permissions */
    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }
    public void recordSound(View v) {
        checkPermission(); // check permission once again
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE); // get shared context to get second of recording and minutes between recordings
        int seconds = sharedPref.getInt(getString(R.string.time_recording_seconds), helper.LENGTH_SOUND_RECORDING);
        int minutes = sharedPref.getInt(getString(R.string.time_notification_minutes), helper.SOUND_RECORDING_OCCURANCES);
        bufferSize = seconds *2048; // limit this, because otherwise the server wont handle before 44100;
        buffer = new short[bufferSize];
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize); // new audiorecorder, not media recorder

        /* go in separate thread */
        new Thread(new Runnable() {
            @Override
            public void run() {
                if ((audioRecord != null) && (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)) { // is everything ok?
                    try {
                        audioRecord.startRecording();

                    } catch (Exception e) {
                        Log.e("exception e", e.toString());
                    }
                        short[] tmpBuf = new short[200];
                        bufferReadResult = audioRecord.read(tmpBuf, 0, 200); // read first two signs just to warm up the recorder and dont get all zeros inside
                        bufferReadResult = audioRecord.read(buffer, 0, bufferSize); //actually read the data
                        new NewRecordingHandler(getApplicationContext()).execute(); // create new server post
                        audioRecord.release();
                    }
                }
        }).start();
        buildNotification(minutes);
    }

    public void buildNotification(int minutes) {
         /* build new notification for later */
        Intent notificationIntent = new Intent(this, RecordSoundReceiver.class); // new intent for notif. receiver
        notificationIntent.putExtra("id", 0);
        notificationIntent.putExtra("id_user", helper.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); // new pending intent
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll(); // cancel previous notifications, to get rid of them from the screen

        long futureInMillis = SystemClock.elapsedRealtime() + minutes*1000; //calculate time in minutes
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent); //activate app later

    }
    /* handling new recording  */
    public class NewRecordingHandler extends AsyncTask<Void, Void, Boolean> {

        String urlString;

        private Context context;

        public NewRecordingHandler(Context contex) {
            this.context = contex;
        }

        @Override
        protected void onPreExecute() {
            urlString = helper.getNewRecordingApi();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status;
            String response = "";
            try {
                response = performPostCall(urlString); //perform post
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!response.equalsIgnoreCase("")) {
                status = true;
            } else {
                status = false;
            }
            return status;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Sound recorded! Thanks", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Sound record problems!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            onBackPressed();
        }

        public String performPostCall(String requestURL) {

            URL url;
            String response = "";
            HttpURLConnection conn = null;
            try {
                url = new URL(requestURL);

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                JSONObject root = new JSONObject();
                // create json that sends the data
                root.put("user", user_id);
                root.put("rawRecording", Arrays.toString(buffer));

                String str = root.toString();
                byte[] outputBytes = str.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes);

                int responseCode = conn.getResponseCode(); //get the response
                os.close();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    br.close();
                } else {
                    response = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(conn != null){
                    conn.disconnect();
                }
            }
            return response;
        }
    }
}
