package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

//first activity - no UI
//checks if there is username/password data saved in sharedpreferences
//redirects app to RegisterAccount activity if no data
//redirects app to LoginAccount Activity if there is data
public class AccountChecker extends Activity {
    public static final String DEFAULT = "No account data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get username and password from sharedPrefs
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String username = sharedPrefs.getString("username", DEFAULT);
        String password = sharedPrefs.getString("password", DEFAULT);

        Intent i;

        //check if username and password data exists
        if (username.equals(DEFAULT)||password.equals(DEFAULT))
        {
            i = new Intent(this, RegisterAccount.class);
        }
        else
        {
            i = new Intent(this, LoginAccount.class);
        }
        startActivity(i);
        finish(); //close this activity
    }
}
