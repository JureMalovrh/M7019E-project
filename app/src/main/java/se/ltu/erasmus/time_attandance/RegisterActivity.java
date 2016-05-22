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
                    Log.e("jsonobh", jRoot.toString());
                    if(jRoot.has("message")){
                        status = false;
                        final String mes = jRoot.getString("message");
                        Log.e("mes", mes);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                error.setText(mes);
                            }
                        });

                    }
                    else {
                        status = true;
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
            if (result) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

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
                //{"email": "email@mail.si", "lastName": "ime", "firstName": "ime", "username": "username", "password": "abc"}

                root.put("username", username.getText().toString());
                root.put("password", password.getText().toString());
                root.put("lastName", surname.getText().toString());
                root.put("firstName", name.getText().toString());
                root.put("email", email.getText().toString());
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
