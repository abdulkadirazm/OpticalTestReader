package com.iuce.opticaltestreader;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity implements View.OnClickListener  {

    private Button signUp;
    private Button loginNow;

    private AutoCompleteTextView signUpName;
    private AutoCompleteTextView signUpEmail;
    private AutoCompleteTextView signUpPassword;


    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        signUp = findViewById(R.id.signUp);
        loginNow = findViewById(R.id.loginNow);

        signUpName = findViewById(R.id.signUpName);
        signUpEmail = findViewById(R.id.signUpEmail);
        signUpPassword = findViewById(R.id.signUpPassword);

        signUp.setOnClickListener(this);
        loginNow.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

    }

    @Override
    public void onClick(View v) {
        if (v == loginNow){
            startActivity(new Intent(SignUp.this,Login.class));
        }
        else if (v == signUp){
            registerUser();
        }
    }

    public boolean isEmailValid(String email) {
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        final Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void registerUser() {
        String name = signUpName.getText().toString().trim();
        String email = signUpEmail.getText().toString().trim();
        String password = signUpPassword.getText().toString().trim();

        if (name.isEmpty()){
            //Toast.makeText(getApplicationContext(),"Name should not be empty",Toast.LENGTH_SHORT).show();
            signUpName.setError("Name should not be empty");
        } else if (email.isEmpty()){
            //Toast.makeText(getApplicationContext(),"Email should not be empty",Toast.LENGTH_SHORT).show();
            signUpEmail.setError("Email should not be empty");
        } else if (!isEmailValid(email)){
            //Toast.makeText(getApplicationContext(),"Enter valid email",Toast.LENGTH_LONG).show();
            signUpEmail.setError("Enter valid email");
        } else if (password.isEmpty()){
            Toast.makeText(getApplicationContext(),"Password should not be empty",Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6){
            Toast.makeText(getApplicationContext(),"Password too short, enter minimum 6 characters!",Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
            User user = new User();

            user.setName(signUpName.getText().toString());
            databaseReference.push().setValue(user);

            progressDialog.setMessage("Registering User...");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {

                if (task.isSuccessful()){

                    if (firebaseAuth.getCurrentUser() != null){
                        startActivity(new Intent(getApplicationContext(), HomePage.class));
                        finish();
                    }

                } else {
                    Toast.makeText(getApplicationContext(),"Could not register.Please try again",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
