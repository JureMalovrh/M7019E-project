package se.ltu.erasmus.time_attandance;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {
    UserHelper helper;

    EditText email;
    EditText name;
    EditText surname;
    EditText username;
    EditText password;

    TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewHandlers();
    }
    /* To get all handlers of the different EditText etc. */
    private void setViewHandlers() {
        helper = ((UserHelper) getApplicationContext());
        setContentView(R.layout.activity_register);

        email = (EditText) findViewById(R.id.email_tw);
        name = (EditText) findViewById(R.id.name_tw);
        surname = (EditText) findViewById(R.id.surname_tw);
        username = (EditText) findViewById(R.id.username_tw);
        password = (EditText) findViewById(R.id.password_tw);

        error = (TextView) findViewById(R.id.error_tw);
        setEmailTextListeners();
    }
    /* Listeners for change in email EditText */
    private void setEmailTextListeners() {
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final String emailText = email.getText().toString();
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
                    email.setError("Invalid email address");
                }
            }
        });
    }

    public void registerButtonClicked(View v){
        new RegisterHandler(this).execute();
    }

    /** Handler for a Registration, creates a POST request with JSON object and registrate the user */
    public class RegisterHandler extends AsyncTask<Void, Void, Boolean> {

        String urlString;
        private Context context;
        public RegisterHandler(Context contex) {
            this.context = contex;
        }

        @Override
        protected void onPreExecute() {
            urlString = helper.getSignUpApi();
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

            if (!response.equals("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    if(jRoot.has("message")){ //if we have message, maybe some problem
                        status = false;
                        final String mes = jRoot.getString("message");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                error.setText(mes);
                            }
                        });
                    }
                    else {
                        status = true;
                        /* Store new user into helper class */
                        helper.setId(jRoot.getString("_id"));
                        helper.setDisplayname(jRoot.getString("displayName"));
                        helper.setEmail(jRoot.getString("email"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        error.setText("Error during registration of user");
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
            return status;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) { // if it is ok, go to main activity with new user
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }
        /* Create POST call, create JSON */
        public String performPostCall(String requestURL) {
            URL url;
            String response = "";
            HttpURLConnection conn = null;
            try {
                url = new URL(requestURL);
                /* Open connection */
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                /* JSON object for creating user */
                JSONObject root = new JSONObject();
                root.put("username", username.getText().toString());
                root.put("password", password.getText().toString());
                root.put("lastName", surname.getText().toString());
                root.put("firstName", name.getText().toString());
                root.put("email", email.getText().toString());

                String str = root.toString();
                byte[] outputBytes = str.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes);
                int responseCode = conn.getResponseCode();
                /* Get response if connection is ok */
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    br.close();
                } else {
                    response = "";
                }
                os.close();
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "There is some problem with connection, please check your network", Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            } finally {
                if(conn != null) {
                    conn.disconnect();
                }
            }
            return response;
        }
    }

}
