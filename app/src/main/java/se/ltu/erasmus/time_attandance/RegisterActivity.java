package se.ltu.erasmus.time_attandance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {
    EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = (EditText) findViewById(R.id.email_tw);
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
        /*TODO: create a request to a web api, show loading screen, take care of responses, log user in automatically */
    }
}
