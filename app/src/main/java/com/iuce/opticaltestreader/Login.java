package com.iuce.opticaltestreader;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private Button forgetPassword;
    private Button loginButton;
    private Button signUpNow;

    private AutoCompleteTextView loginEmail;
    private AutoCompleteTextView loginPassword;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        forgetPassword = findViewById(R.id.forgetPassword);
        loginButton = findViewById(R.id.loginButton);
        signUpNow = findViewById(R.id.signUpNow);

        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);

        forgetPassword.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        signUpNow.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), HomePage.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == forgetPassword){
            forgetPassword();
        }
        else if (v == loginButton){
            userLogin();
        }
        else if (v == signUpNow)
            startActivity(new Intent(Login.this,SignUp.class));
    }

    public boolean isEmailValid(String email) {
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        final Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void userLogin() {

        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (email.isEmpty()){
            //Toast.makeText(getApplicationContext(),"Email should not be empty",Toast.LENGTH_LONG).show();
            loginEmail.setError("Email should not be");
        } else if (!isEmailValid(email)){
            //Toast.makeText(getApplicationContext(),"Enter valid email",Toast.LENGTH_SHORT).show();
            loginEmail.setError("Enter valid email");
        } else if (password.isEmpty()){
            Toast.makeText(getApplicationContext(),"Password should not be empty",Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6){
            Toast.makeText(getApplicationContext(),"Password too short, enter minimum 6 characters!",Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setMessage("Registering Please Wait..");
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

                progressDialog.dismiss();

                if (task.isSuccessful()){
                    startActivity(new Intent(getApplicationContext(), HomePage.class));
                    finish();
                }
                if (!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Error! the password may be incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void forgetPassword() {
        final Dialog dialog = new Dialog(Login.this);
        dialog.setContentView(R.layout.forget_password);

        final AutoCompleteTextView reset_password_email = dialog.findViewById(R.id.reset_password_email);
        final Button resetPassword = dialog.findViewById(R.id.resetPassword);

        resetPassword.setOnClickListener(v -> {

            String email = reset_password_email.getText().toString().trim();

            if (TextUtils.isEmpty(email)){
                //email is empty
                Toast.makeText(getApplicationContext(),"Please enter email",Toast.LENGTH_SHORT).show();
                return;
            }
            final String email_new = ((AutoCompleteTextView) dialog.findViewById(R.id.reset_password_email)).getText().toString();

            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            firebaseAuth.sendPasswordResetEmail(email_new).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),
                            "Reset password code has been emailed to " + email_new,Toast.LENGTH_LONG).show();
                    reset_password_email.setText("");
                }
                else{
                    Toast.makeText(getApplicationContext(),
                            "There is a problem with reset password, try latter!"+email_new,Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            });
        });
        dialog.show();
    }
}
