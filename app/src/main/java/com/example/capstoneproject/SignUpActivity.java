package com.example.capstoneproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.capstoneproject.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    //UI components Declaration
    EditText edtSignUpName, edtSignUpEmailId, edtSignUpPassword, edtSignUpConfirmPassword, edtSignUpPhoneNumber;
    Spinner spSignUpGender, spSignUpUserType;
    Button btnSignUpBack, btnSignUpSignUp;
    ProgressBar pbSignUpProgress;

    //Spinner Gender, UserType Data
    String[] genderData = {"Select Gender","Male","Female","Prefer Not To Say"};
    String[] userTypeData = {"Select User Type","Customer","Hotel Admin"};

    //Firebase object
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Variable Initialization
        edtSignUpEmailId = findViewById(R.id.edtSignUpEmailId);
        edtSignUpName = findViewById(R.id.edtSignUpName);
        edtSignUpPassword = findViewById(R.id.edtSignUpPassword);
        edtSignUpConfirmPassword = findViewById(R.id.edtSignUpConfirmPassword);
        edtSignUpPhoneNumber = findViewById(R.id.edtSignUpPhoneNumber);
        spSignUpGender = findViewById(R.id.spSignUpGender);
        spSignUpUserType = findViewById(R.id.spSignUpUserType);
        btnSignUpBack = findViewById(R.id.btnSignUpBack);
        btnSignUpSignUp = findViewById(R.id.btnSignUpSignUp);
        pbSignUpProgress = findViewById(R.id.pbSignUpProgress);

        //Calling function to Fill Gender and User Type Data
        spinnerFillData();

        //Initializing firebase instance
        mAuth = FirebaseAuth.getInstance();

        //Sign Up User/Hotel
        btnSignUpSignUp.setOnClickListener(v ->{
            //Toast.makeText(SignUpActivity.this, "Sign Up Clicked", Toast.LENGTH_SHORT).show();
            //Calling registerUser to create user in Firebase
            registerUser();
        });

        //Redirecting back to Login Activity
        //Closing SignUp Activity
        btnSignUpBack.setOnClickListener(v -> {
            finish();
        });
    }

    //Spinner fill Data
    public void spinnerFillData(){
        //Fill gender data
        ArrayAdapter aaGender = new ArrayAdapter(this, android.R.layout.simple_list_item_1,genderData);
        spSignUpGender.setAdapter(aaGender);

        //Fill User Type Data
        ArrayAdapter aaUserType = new ArrayAdapter(this, android.R.layout.simple_list_item_1, userTypeData);
        spSignUpUserType.setAdapter(aaUserType);
    }

    //Validate User and Register
    public void registerUser(){
        //Validate user details
        String name = edtSignUpName.getText().toString().trim();
        String emailId = edtSignUpEmailId.getText().toString().trim();
        String password = edtSignUpPassword.getText().toString().trim();
        String cPassword = edtSignUpConfirmPassword.getText().toString().trim();
        String phoneNumber = edtSignUpPhoneNumber.getText().toString().trim();

        //Error flag -> used to identify if data is entered or not
        boolean flag = false;
        if(name.isEmpty()){
            edtSignUpName.setError("Full Name is required");
            edtSignUpName.requestFocus();
            flag = true;
        }
        if(emailId.isEmpty()){
            edtSignUpEmailId.setError("Email Id is required");
            edtSignUpEmailId.requestFocus();
            flag = true;
        }
        if(password.isEmpty()){
            edtSignUpPassword.setError("Password is required");
            edtSignUpPassword.requestFocus();
            flag = true;
        }
        if(cPassword.isEmpty()){
            edtSignUpConfirmPassword.setError("Password is required");
            edtSignUpConfirmPassword.requestFocus();
            flag = true;
        }
        if(phoneNumber.isEmpty()){
            edtSignUpPhoneNumber.setError("Phone number is required");
            edtSignUpPhoneNumber.requestFocus();
            flag = true;
        }

        //checking if gender and user type is selected or not
        if(spSignUpUserType.getSelectedItemId() == 0 || spSignUpGender.getSelectedItemId() == 0){
            flag = true;
            Toast.makeText(SignUpActivity.this, "Gender and User Type are required to select.", Toast.LENGTH_SHORT).show();
        }

        //Returning the control of code if any of the field is empty
        if(flag){
            return;
        }

        //Checking if user has provided a valid email or not
        if(!Patterns.EMAIL_ADDRESS.matcher(emailId).matches()){
            edtSignUpEmailId.setError("Please provide a valid Email Id");
            edtSignUpEmailId.requestFocus();
            return;
        }

        //Checking if password length is more than 6
        //Checking if password and confirm password are same
        if(password.length()<6){
            edtSignUpPassword.setError("Password length should be more than 6.");
            edtSignUpPassword.requestFocus();
            return;
        }else{
            if(!password.equals(cPassword)){
                edtSignUpConfirmPassword.setError("Password and Confirm Password should be same");
                edtSignUpPassword.requestFocus();
                edtSignUpConfirmPassword.requestFocus();
                return;
            }else{
                toggleProgressBarAndSignUpButton();

                //Registering user in the firebase
                mAuth.createUserWithEmailAndPassword(emailId,password).addOnCompleteListener(task -> {
                    //Once user emailId and password registered with firebase authentication, rest of the details will be submitted to the firebase database
                    if(task.isSuccessful()){
                        User user = new User(name, phoneNumber, genderData[Integer.parseInt(String.valueOf(spSignUpGender.getSelectedItemId()))],userTypeData[Integer.parseInt(String.valueOf(spSignUpUserType.getSelectedItemId()))]);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference usersRef = database.getReference("users");
                        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){
                                toggleProgressBarAndSignUpButton();
                                Toast.makeText(SignUpActivity.this, "User has been registered.", Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                toggleProgressBarAndSignUpButton();
                                Toast.makeText(SignUpActivity.this, "Failed to Register User,Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        toggleProgressBarAndSignUpButton();
                        Toast.makeText(SignUpActivity.this, task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    //Used to display/hide progress bar and enable/disable signup button
    public void toggleProgressBarAndSignUpButton(){
        if(btnSignUpSignUp.isEnabled()){
            btnSignUpSignUp.setEnabled(false);
            pbSignUpProgress.setVisibility(View.VISIBLE);
        }else{
            btnSignUpSignUp.setEnabled(true);
            pbSignUpProgress.setVisibility(View.GONE);
        }
    }
}