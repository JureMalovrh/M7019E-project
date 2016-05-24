package se.ltu.erasmus.time_attandance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
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

        setViewHandlers();

    }

    public void registerButtonClicked(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void loginButtonClicked(View v) {
        new LoginHandler(this).execute();
    }

    public void setViewHandlers() {
        helper = ((UserHelper) getApplicationContext());
        username_tw = (TextView) findViewById(R.id.username_tw);
        password_tw = (TextView) findViewById(R.id.password_tw);
        error = (TextView) findViewById(R.id.error_tw);
    }


    /* Class for handling login of the user */
    public class LoginHandler extends AsyncTask<Void, Void, Boolean> {
        String urlString;
        private Context context;
        public LoginHandler(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            urlString = helper.getLoginApi(); //get API for login
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            String response = "";
            try {
                response = performPostCall(urlString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!response.equals("") && !response.equals("connection-problem")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    if(jRoot.has("message")){ // wrong username or pass
                        status = false;
                    }
                    else {
                        status = true;
                        UserHelper helper = (UserHelper) getApplicationContext(); // fill the helper class, so we know everything about user when we need.
                        helper.setId(jRoot.getString("_id"));
                        helper.setDisplayname(jRoot.getString("displayName"));
                        helper.setEmail(jRoot.getString("email"));
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
            /* if result is true, the log the user in, otherwise, show error message */
            if (result) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else {
                error.setVisibility(View.VISIBLE);
            }
            return;
        }
        /* Creates POST call to a given url, we create new JSON and send it to a server */
        public String performPostCall(String requestURL) {

            URL url;
            String response = "";
            HttpURLConnection conn=null;
            try {
                url = new URL(requestURL);
                /* open url connection */
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                /*JSON obj for sending*/
                JSONObject root = new JSONObject();
                root.put("username", username_tw.getText().toString());
                root.put("password", sha256(password_tw.getText().toString()));

                String str = root.toString();
                byte[] outputBytes = str.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes);
                os.close();
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    /* Read whole response */
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    br.close();
                } else { //just empty response -> error
                    response = "";
                }

            }catch (Exception e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "There was some problem with the connection. Check your internet connection", Toast.LENGTH_LONG).show();
                    }

                });
                response = "connection-problem";
                e.printStackTrace();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }

            return response;
        }
    }
    /* Returns the sha256 string */
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
