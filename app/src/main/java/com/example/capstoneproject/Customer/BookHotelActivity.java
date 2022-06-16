package com.example.capstoneproject.Customer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstoneproject.Models.Hotel;
import com.example.capstoneproject.Models.Orders;
import com.example.capstoneproject.Models.RoomType;
import com.example.capstoneproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookHotelActivity extends AppCompatActivity {

    //UI components Delaration
    ImageView imgBookHotelDisplay, imgBookHotelRoomTypeImage;
    Button btnBookHotelBook, btnBookHotelBack;
    TextView txvBookHotelName, txvBookHotelAmenities, txvBookHotelCheckInDate, txvBookHotelCheckOutDate
            , txvBookHotelGuests, txvBookHotelRoomTypeName, txvBookHotelRoomCharges
            , txvBookHotelTotalCharges, txvBookHotelDetails, txvBookHotelGuestDetails;
    Spinner spBookHotelRoomTYpe;

    //Local Variable
    String bookSearchText, bookCheckInDate, bookCheckOutDate, bookNoOfRooms, bookNoOfGuests, bookHotelId;
    ProgressDialog bookHotelLoader;
    final String hotelDisplayImage = "displayImage";
    final String singleRoomImage = "singleRoom";
    final String doubleRoomImage = "doubleRoom";
    String orderAmount = "";
    String selectedRoomType = "";
    List<RoomType> roomTypeList = new ArrayList<>();

    //Firebase Storage Reference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_hotel);

        //Components Intialization
        imgBookHotelDisplay = findViewById(R.id.imgBookHotelDisplay);
        imgBookHotelRoomTypeImage = findViewById(R.id.imgBookHotelRoomTypeImage);
        btnBookHotelBook = findViewById(R.id.btnBookHotelBook);
        btnBookHotelBack = findViewById(R.id.btnBookHotelBack);
        txvBookHotelName = findViewById(R.id.txvBookHotelName);
        txvBookHotelAmenities = findViewById(R.id.txvBookHotelAmenities);
        txvBookHotelCheckInDate = findViewById(R.id.txvBookHotelCheckInDate);
        txvBookHotelCheckOutDate = findViewById(R.id.txvBookHotelCheckOutDate);
        txvBookHotelGuests = findViewById(R.id.txvBookHotelGuests);
        txvBookHotelRoomTypeName = findViewById(R.id.txvBookHotelRoomTypeName);
        txvBookHotelRoomCharges = findViewById(R.id.txvBookHotelRoomCharges);
        txvBookHotelTotalCharges = findViewById(R.id.txvBookHotelTotalCharges);
        txvBookHotelDetails = findViewById(R.id.txvBookHotelDetails);
        spBookHotelRoomTYpe = findViewById(R.id.spBookHotelRoomTYpe);
        txvBookHotelGuestDetails = findViewById(R.id.txvBookHotelGuestDetails);

        //Initializing storage reference
        storageRef = storage.getReference();

        //Checking for extra's
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            bookSearchText = extras.getString("bookSearchText");
            bookCheckInDate = extras.getString("bookCheckInDate");
            bookCheckOutDate = extras.getString("bookCheckOutDate");
            bookNoOfRooms = extras.getString("bookNoOfRooms");
            bookNoOfGuests = extras.getString("bookNoOfGuests");
            bookHotelId = extras.getString("bookHotelId");

            //Setting values
            txvBookHotelCheckInDate.setText(bookCheckInDate);
            txvBookHotelCheckOutDate.setText(bookCheckOutDate);
            txvBookHotelGuests.setText(bookNoOfGuests);
        }

        //Setting up spinner

        //Fill Hotel Details
        fillHotelDetails();
        getRoomTypeData();
        getDateDiff();

        spBookHotelRoomTYpe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fillRoomTypeDate(roomTypeList.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Book Button
        btnBookHotelBook.setOnClickListener(v -> {
            //Database Reference
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ordersRef = database.getReference("orders");

            //Unique orderID
            String key = database.getReference("orders").push().getKey();

            //Order Object
            Orders order = new Orders(FirebaseAuth.getInstance().getUid(), bookHotelId, bookCheckInDate, bookCheckOutDate, selectedRoomType, bookNoOfGuests, bookNoOfRooms, orderAmount, "Pending");
            //Pushing data in database
            ordersRef.child(key).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getBaseContext(), "Your Order has been placed. Please check order status in orders tab.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(BookHotelActivity.this, CustomerHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getBaseContext(), "Something went wrong, Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }

            });
        });
        //Closing the activity
        btnBookHotelBack.setOnClickListener(v -> {
            finish();
        });
    }
    //Fill Hotel Details
    public void fillHotelDetails(){
        //Get Hotel Detials
        //Hotel reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference hotelRef = database.getReference("hotels");

        //hotels
        hotelRef.child(bookHotelId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Hotel hotel = snapshot.getValue(Hotel.class);
                txvBookHotelAmenities.setText("Amenities: "+hotel.hotelAmenities);
                txvBookHotelName.setText(hotel.hotelName + ", " + hotel.hotelCity);
                loadImage(imgBookHotelDisplay,hotelDisplayImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Fill Room Type Spinner
    public void getRoomTypeData(){
        //Get Hotel Detials
        //Hotel reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference roomsRef = database.getReference("rooms");

        //hotels
        roomsRef.child(bookHotelId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomTypeList.clear();
                for(DataSnapshot postSnapshot: snapshot.getChildren()){
                    roomTypeList.add(postSnapshot.getValue(RoomType.class));
                }
                String[] roomTypeData = new String[roomTypeList.size()];
                for (int i = 0; i < roomTypeList.size(); i++) {
                    roomTypeData[i] = roomTypeList.get(i).roomType;
                }
                ArrayAdapter aaRoomType = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_1,roomTypeData);
                spBookHotelRoomTYpe.setAdapter(aaRoomType);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Fill Room Type Data
    public void fillRoomTypeDate(RoomType roomType){
        txvBookHotelRoomTypeName.setText(roomType.roomType);
        txvBookHotelRoomCharges.setText(roomType.roomCharges + " $/Night");
        Double totalCharges = 0.0;
        Double singleNightCharges = Double.parseDouble(roomType.roomCharges);
        Double noOfDays = Double.valueOf(getDateDiff());
        totalCharges += singleNightCharges*noOfDays*Double.valueOf(bookNoOfRooms);
        orderAmount = String.valueOf(totalCharges);
        selectedRoomType = roomType.roomType;
        txvBookHotelTotalCharges.setText(totalCharges + " $");
        txvBookHotelDetails.setText("includes fees & taxes");
        txvBookHotelGuestDetails.setText(bookNoOfRooms +" Rooms, "+bookNoOfGuests+" Guests");
        //txvBookHotelTotalCharges.setText(roomType.roomCharges);
        if(roomType.roomType.equals("Single Room")){
            loadImage(imgBookHotelRoomTypeImage, singleRoomImage);
        }else if(roomType.roomType.equals("Double Room")){
            loadImage(imgBookHotelRoomTypeImage, doubleRoomImage);
        }
    }
    //Image Loader
    public void loadImage(ImageView imgViewName, String imageName){
        //Showing progressbar
        bookHotelLoader = new ProgressDialog(BookHotelActivity.this);
        bookHotelLoader.setMessage("Loading Image...");
        bookHotelLoader.setCancelable(true);
        if(!bookHotelLoader.isShowing()){
            bookHotelLoader.show();
        }
        StorageReference imgRef = storageRef.child(bookHotelId+"/"+imageName+".jpg");

        //Loading a profile image
        try {
            File localFile = File.createTempFile(imageName,".jpg");
            imgRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                imgViewName.setImageBitmap(bitmap);
                if(bookHotelLoader.isShowing()){
                    bookHotelLoader.dismiss();
                }
            }).addOnFailureListener(e -> {
                if(bookHotelLoader.isShowing()){
                    bookHotelLoader.dismiss();
                }
                Toast.makeText(getBaseContext(), "Failed to Load Image. Please reload the profile.", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            if(bookHotelLoader.isShowing()){
                bookHotelLoader.dismiss();
            }
            e.printStackTrace();
        }
        //return "false";
//        if(bookHotelLoader.isShowing()){
//            bookHotelLoader.dismiss();
//        }
    }

    public long getDateDiff(){
        SimpleDateFormat DateFor = new SimpleDateFormat("MMM dd, yyyy");
        Date localCheckInDate = null;
        Date localCheckOutDate = null;
        try{
            localCheckInDate = DateFor.parse(bookCheckInDate);
        }catch (ParseException e) {e.printStackTrace();}
        try{
            localCheckOutDate = DateFor.parse(bookCheckOutDate);
        }catch (ParseException e) {e.printStackTrace();}
        long diffInMillies = localCheckOutDate.getTime() - localCheckInDate.getTime();
        //Toast.makeText(getBaseContext(), "DAYS:"+TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS), Toast.LENGTH_SHORT).show();
        return TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }
    
}