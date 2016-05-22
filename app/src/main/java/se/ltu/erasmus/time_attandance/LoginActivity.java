package se.ltu.erasmus.time_attandance;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {
    TextView username_tw;
    TextView password_tw;
    TextView error;
    UserHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        try{
            int off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if(off==0){
                Toast.makeText(this, "Application needs location enabled for working", Toast.LENGTH_LONG).show();
                Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(onGPS);
            }

        } catch (Settings.SettingNotFoundException e){
            e.printStackTrace();
        }
        helper = ((UserHelper) getApplicationContext());
        username_tw = (TextView) findViewById(R.id.username_tw);
        password_tw = (TextView) findViewById(R.id.password_tw);
        error = (TextView) findViewById(R.id.error_tw);

    }

    public void registerButtonClicked(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void loginButtonClicked(View v) {
        /*TODO: create request to check if persons inserted data is correct, save his requests into SharedPreferences so he don't need to login every time */

        new LoginHandler(this).execute();


    }



    public class LoginHandler extends AsyncTask<Void, Void, Boolean> {

        String urlString;

        private Context context;

        public LoginHandler(Context contex) {
            this.context = contex;
        }

        @Override
        protected void onPreExecute() {

            urlString = "http://52.30.221.7:3000/api/auth/signin";
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            String response = "";
            try {
                response = performPostCall(urlString, new HashMap<String, String>() {
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
                    if(jRoot.has("message")){
                        status = false;
                    }
                    else {
                        status = true;
                        helper.setId(jRoot.getString("_id"));
                        helper.setDisplayname(jRoot.getString("displayName"));

                    }

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
            if (result) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else {
                error.setVisibility(View.VISIBLE);
            }
            return;
        }

        public String performPostCall(String requestURL, HashMap<String, String> postDataParams) {

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


                root.put("username", username_tw.getText().toString());
                root.put("password", sha256(password_tw.getText().toString()));
                //root.put("username", "username");
                //root.put("password", "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");

                String str = root.toString();
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

    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
