package com.example.capstoneproject.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Hotel {

    //Hotel Class variables
    public String hotelName, hotelCity, hotelAmenities;

    //Empty constructor
    public Hotel(){}

    public Hotel(String hotelName, String hotelCity, String hotelAmenities){
        this.hotelName = hotelName;
        this.hotelCity = hotelCity;
        this.hotelAmenities = hotelAmenities;
    }
}
