package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//allows user to enter their username/password to log in
//redirects to UserDashboard if entered username/password matches saved username/password
public class LoginAccount extends Activity implements View.OnClickListener {
    private EditText enteredUsername, enteredPassword;
    private Button loginButton;
    public static final String DEFAULT = "Does not match saved data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_account);

        enteredUsername = (EditText) findViewById(R.id.usernameEditText);
        enteredPassword = (EditText) findViewById(R.id.passwordEditText);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String savedUsername = sharedPrefs.getString("username", DEFAULT);
        String savedPassword = sharedPrefs.getString("password", DEFAULT);

        if (enteredUsername.getText().toString().equals(savedUsername) &&
                enteredPassword.getText().toString().equals(savedPassword)) {
            Intent i = new Intent(this, UserDashboard.class);
            startActivity(i);
        }
        else {
            Toast.makeText(this, "Username or password incorrect", Toast.LENGTH_SHORT).show();
        }

    }
}
