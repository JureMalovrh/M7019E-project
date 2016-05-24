package se.ltu.erasmus.time_attandance;

import android.Manifest;
import android.app.AlarmManager;
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
import android.util.Log;
import android.view.View;

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
    int MAX_FREQ = RECORDER_SAMPLERATE / 2;
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

        super.onCreate(savedInstanceState);
        helper = (UserHelper) getApplicationContext();
        checkPermission();
        Intent iin = getIntent();
        Bundle b = iin.getExtras();

        if (b != null) {
            user_id = (String) b.get("id_user");
        }
        setContentView(R.layout.activity_raw_sound_recording);


    }
    @Override
    public void onBackPressed() {

        android.os.Process.killProcess(android.os.Process.myPid());
        // This above line close correctly
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }
    public void recordSound(View v) {
        checkPermission();


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int seconds = sharedPref.getInt(getString(R.string.time_recording_seconds), helper.LENGTH_SOUND_RECORDING);
        int minutes = sharedPref.getInt(getString(R.string.time_notification_minutes), helper.SOUND_RECORDING_OCCURANCES);
        bufferSize = seconds *2048;
        buffer = new short[bufferSize];
        // use the mic with Auto Gain Control turned off!
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);


        new Thread(new Runnable() {
            @Override
            public void run() {
                if ((audioRecord != null) && (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)) {


                    try {
                        audioRecord.startRecording();

                    } catch (Exception e) {
                        Log.e("exception e", e.toString());
                    }
                        short[] tmpBuf = new short[200];
                        bufferReadResult = audioRecord.read(tmpBuf, 0, 200);
                        bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                        new NewRecordingHandler(getApplicationContext()).execute();
                        audioRecord.release();
                    }
                }
        }).start();

        Intent notificationIntent = new Intent(this, RecordSoundReceiver.class);
        notificationIntent.putExtra("id", 0);
        notificationIntent.putExtra("id_user", helper.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + minutes*1000;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

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
            onBackPressed();
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
                root.put("user", user_id);
                root.put("rawRecording", Arrays.toString(buffer));




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
