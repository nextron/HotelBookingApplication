package com.example.capstoneproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstoneproject.Customer.CustomerHomeActivity;
import com.example.capstoneproject.Hotel.HotelHomeActivity;
import com.example.capstoneproject.Models.User;
import com.example.capstoneproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //UI Components Declaration
    EditText edtLoginEmailId, edtLoginPassword;
    Button btnLoginSignIn, btnLoginSignUp;
    ProgressBar pbLoginProgress;

    //Firebase object
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Variable Initialization
        edtLoginEmailId = findViewById(R.id.edtLoginEmailId);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLoginSignIn = findViewById(R.id.btnLoginSignIn);
        btnLoginSignUp = findViewById(R.id.btnLoginSignUp);
        //txvLoginForgotPassword = findViewById(R.id.txvLoginForgotPassword);
        pbLoginProgress = findViewById(R.id.pbLoginProgress);

        //Initializing firebase instance
        mAuth = FirebaseAuth.getInstance();

        //Check if user is already logged in or not
        if(mAuth.getCurrentUser() != null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("users");

            userRef.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    Toast.makeText(LoginActivity.this, ""+user.userType, Toast.LENGTH_SHORT).show();
                    if(user.userType.equals("Hotel Admin")){
                        Intent intent = new Intent(LoginActivity.this, HotelHomeActivity.class);
                        startActivity(intent);
                    }else if(user.userType.equals("Customer")){
                        Intent intent = new Intent(LoginActivity.this, CustomerHomeActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        //Login Admin/Hotel/User(Customer)
        btnLoginSignIn.setOnClickListener(v -> {
            //Toast.makeText(LoginActivity.this, "Sign In Clicked", Toast.LENGTH_SHORT).show();
            authenticateUser();
        });

        //Redirecting to SignUp Activity
        btnLoginSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    //Authenticate User
    public void authenticateUser(){
        String emailId = edtLoginEmailId.getText().toString().trim();
        String password = edtLoginPassword.getText().toString().trim();

        //Error flag -> used to identify if data is entered or not
        boolean flag = false;

        if(emailId.isEmpty()){
            edtLoginEmailId.setError("EmailID is required to login into account.");
            edtLoginEmailId.requestFocus();
            flag = true;
        }

        if(password.isEmpty()){
            edtLoginPassword.setError("Password is required in order to authenticate.");
            edtLoginPassword.requestFocus();
            flag = true;
        }

        //Returning the control of code if any of the field is empty
        if(flag){
            return;
        }

        mAuth.signInWithEmailAndPassword(emailId,password).addOnCompleteListener(task -> {
            toggleProgressBarAndSignInButton();
           if(task.isSuccessful()){
               //Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
               FirebaseDatabase database = FirebaseDatabase.getInstance();
               DatabaseReference userRef = database.getReference("users");

               userRef.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       toggleProgressBarAndSignInButton();
                       User user = snapshot.getValue(User.class);
                       Toast.makeText(LoginActivity.this, ""+user.userType, Toast.LENGTH_SHORT).show();
                       if(user.userType.equals("Hotel Admin")){
                           Intent intent = new Intent(LoginActivity.this, HotelHomeActivity.class);
                           startActivity(intent);
                       }else if(user.userType.equals("Customer")){
                           Intent intent = new Intent(LoginActivity.this, CustomerHomeActivity.class);
                           startActivity(intent);
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               });
           }else{
               toggleProgressBarAndSignInButton();
               Toast.makeText(LoginActivity.this, "Please check your credentials.", Toast.LENGTH_SHORT).show();
           }
        });
    }

    //Used to display/hide progress bar and enable/disable signIn button
    public void toggleProgressBarAndSignInButton(){
        if(btnLoginSignIn.isEnabled()){
            btnLoginSignIn.setEnabled(false);
            pbLoginProgress.setVisibility(View.VISIBLE);
        }else{
            btnLoginSignIn.setEnabled(true);
            pbLoginProgress.setVisibility(View.GONE);
        }
    }
}