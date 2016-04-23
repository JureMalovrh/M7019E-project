package se.ltu.erasmus.time_attandance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {
    EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = (EditText) findViewById(R.id.email_tw);
        setEmailListeners();

    }

    private void setEmailListeners() {
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
                    email.setError("Invalid Email Address");
                }
            }
        });
    }
}
