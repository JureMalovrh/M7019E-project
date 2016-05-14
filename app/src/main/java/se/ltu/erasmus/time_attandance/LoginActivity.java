package se.ltu.erasmus.time_attandance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);



    }

    protected void registerButtonClicked(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    protected void loginButtonClicked(View v) {
        /*TODO: create request to check if persons inserted data is correct, save his requests into SharedPreferences so he don't need to login every time */

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
}
