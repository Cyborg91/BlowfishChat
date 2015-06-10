package com.blowfishchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by radoslawjarzynka on 08.06.15.
 */
public class LoginActivity extends Activity implements TcpManagerObserver{

    private EditText loginField;
    private EditText passwordField;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_login);

        loginField = (EditText) findViewById(R.id.loginField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);

        TCPManager.getInstance().register(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = loginField.getText().toString();
                String password = passwordField.getText().toString();

                if (login != null && password != null) {
                    TCPManager.getInstance().login(login, password);
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = loginField.getText().toString();
                String password = passwordField.getText().toString();

                if (login != null && password != null) {
                    TCPManager.getInstance().register(login, password);
                }
            }
        });

    }
    public void badToken() {

    }
    public void loginOk() {
        Intent intent = new Intent(LoginActivity.this, ContactActivity.class);
        startActivity(intent);
    }

    public void registerOk() {
        LoginActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Registered succesfully!", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void registerNook() {
        LoginActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Username taken, choose another one!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void loginNook() {
        LoginActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Wrong username or password!", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void clientsDownloaded() {

    }

    public void sendOk() {

    }
    public void msgReceived(String fromUser, String message) {
    }
}


