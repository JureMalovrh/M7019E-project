package se.ltu.erasmus.time_attandance;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Jure on 23.5.2016.
 */
public class RecordSoundReceiver  extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent ) {
            /* class for receiving notification updates, this class is called when there is time to perform new notification */

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent resultIntent = new Intent(context, RawSoundRecordingActivity.class); // call correct activity
            String userid = intent.getStringExtra("id_user"); // get id from previous intent

            resultIntent.putExtra("id_user", userid); // and send it again
            // new pending intent
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            // build notification
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_keyboard_voice_black_24dp)
                            .setContentTitle("Hello, it's time to record sound again")
                            .setContentText("Record sound");
            mBuilder.setContentIntent(resultPendingIntent);

            Notification n = mBuilder.build();

            int id = intent.getIntExtra("id", 0);
            notificationManager.notify(id, n); // notify and execute notification

        }




}

