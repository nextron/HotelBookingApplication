package com.example.capstoneproject.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Orders {
    public String customerId, hotelId, checkInDate, checkOutDate, roomType, numberOfRooms, numberOfGuests, orderAmount, orderStatus;

    public Orders(){}

    public Orders(String customerId, String hotelId, String checkInDate, String checkOutDate, String roomType, String numberOfRooms, String numberOfGuests, String orderAmount, String orderStatus){
        this.customerId = customerId;
        this.hotelId = hotelId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomType = roomType;
        this.numberOfRooms = numberOfRooms;
        this.numberOfGuests = numberOfGuests;
        this.orderAmount = orderAmount;
        this.orderStatus = orderStatus;
    }
}
