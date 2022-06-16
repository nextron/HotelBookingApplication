package com.example.capstoneproject.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    //User Class variables
    public String fullName, phoneNumber, gender, userType;

    //Empty Constructor
    public User(){}

    public User(String fullName, String phoneNumber, String gender, String userType){
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.userType = userType;
    }
}
