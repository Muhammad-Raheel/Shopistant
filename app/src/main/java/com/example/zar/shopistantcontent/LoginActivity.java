package com.example.zar.shopistantcontent;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_REGISTER = 1;
    private static String TAG = "LoginActivity";
    FloatingActionButton fab;
    ProgressDialog dialog;
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private EditText editTextUsername, editTextPassword;
    FirebaseAuth auth;
    private ProgressDialog waitingDialog;
    String pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initCom();
    }

    public void initCom() {
        dialog=new ProgressDialog(this);
        dialog.setMessage("Authenticating....");
        auth=FirebaseAuth.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
            Intent intent=new Intent(LoginActivity.this,MapActivity.class);
            startActivity(intent);
            finish();
        }
        fab = (FloatingActionButton) findViewById(R.id.fab);
        editTextUsername = (EditText) findViewById(R.id.et_username);
        editTextPassword = (EditText) findViewById(R.id.et_password);
    }




    public void clickLogin(View view) {
        String username = editTextUsername.getText().toString();
        pass = editTextPassword.getText().toString();
        if (validate(username,pass)) {
        networkCall(username,pass);
        }
        else {
            Toast.makeText(LoginActivity.this,"Invalid email or password",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validate(String emailStr, String password) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return (password.length() > 0 || password.equals(";")) && matcher.find();
    }


    public void networkCall(String email,String pass) {
        dialog.show();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (isConnected) {
           //auth user
            auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Intent intent=new Intent(LoginActivity.this,MapActivity.class);
                        startActivity(intent);
                        finish();
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this,"Login",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this,"User not found",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(),"No network found",Toast.LENGTH_LONG).show();
        }
    }

}
