package se.ltu.erasmus.time_attandance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Jure on 23.5.2016.
 */
public class RecordSoundReceiver  extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent ) {
            String id = intent.getStringExtra("id");
            Intent i = new Intent(context, RawSoundRecordingActivity.class);
            i.putExtra("id", id);


        }
    }

