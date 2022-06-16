package com.example.capstoneproject.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class RoomType {
    //User Class variables
    public String roomType, roomCharges;

    //Empty constructor
    public RoomType(){}

    public RoomType(String roomType, String roomCharges){
        this.roomType = roomType;
        this.roomCharges = roomCharges;
    }
}
