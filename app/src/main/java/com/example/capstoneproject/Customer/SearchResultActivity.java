package com.example.capstoneproject.Customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstoneproject.Models.Hotel;
import com.example.capstoneproject.Models.RoomType;
import com.example.capstoneproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    //UI Components Declaration
    TextView txvSearchResultText, txvSearchResultDetails;
    ImageView imgSearchResultBack, imgSearchResultEdit;
    RecyclerView rvSearchResults;

    //Local Variables
    String searchText, checkInDate, checkOutDate, noOfRooms, noOfGuests;
    CustomerSearchItemAdapter customerSearchItemAdapter;
    List<Hotel> hotelsList = new ArrayList<>();
    List<String> hotelsId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        //Variable Initialization
        txvSearchResultDetails = findViewById(R.id.txvSearchResultDetails);
        txvSearchResultText = findViewById(R.id.txvSearchResultText);
        imgSearchResultEdit = findViewById(R.id.imgSearchResultEdit);
        imgSearchResultBack = findViewById(R.id.imgSearchResultBack);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        //RecyclerView
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        rvSearchResults.setHasFixedSize(true);
        customerSearchItemAdapter = new CustomerSearchItemAdapter();
        rvSearchResults.setAdapter(customerSearchItemAdapter);

        //Handle click event on an item.
        customerSearchItemAdapter.setOnItemClickListener(new CustomerSearchItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Hotel hotel,String hotelId) {
                //Toast.makeText(getBaseContext(), "CLICKED"+hotel.hotelName+ hotelId, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(),BookHotelActivity.class);
                //intent.putExtra("Order", (Parcelable) newOrder);
                intent.putExtra("bookSearchText",searchText);
                intent.putExtra("bookCheckInDate",checkInDate);
                intent.putExtra("bookCheckOutDate",checkOutDate);
                intent.putExtra("bookNoOfRooms",noOfRooms);
                intent.putExtra("bookNoOfGuests",noOfGuests);
                intent.putExtra("bookHotelId",hotelId);
                startActivity(intent);
            }
        });

//        Hotel hotel = new Hotel("HOTEL","ag","12");
//        List<Hotel> hotelList = new ArrayList<>();
//        hotelList.add(hotel);
//        hotelList.add(hotel);
        //customerSearchItemAdapter.setHotels(hotelList);

        //Filling Relative layoutData
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            searchText = extras.getString("searchText");
            checkInDate = extras.getString("checkInDate");
            checkOutDate = extras.getString("checkOutDate");
            noOfRooms = extras.getString("noOfRooms");
            noOfGuests = extras.getString("noOfGuests");

            //Setting values
            txvSearchResultText.setText(searchText);
            String searchDetails = checkInDate + " - " + checkOutDate + ", " + noOfRooms + " Room" + ", " + noOfGuests + " Guests";
            txvSearchResultDetails.setText(searchDetails);
        }
        //Toast.makeText(getBaseContext(), ""+extras.getString("searchText"), Toast.LENGTH_SHORT).show();

        //calling search method to get result from firebase and setting data in adapter
        search();

        //Back or Edit button code
        imgSearchResultBack.setOnClickListener(v -> {
            finish();
        });
        imgSearchResultEdit.setOnClickListener(v -> {
            finish();
        });
    }

    public void search(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference hotelsRef = database.getReference("hotels");

        hotelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hotelsList.clear();
                hotelsId.clear();
                for(DataSnapshot postSnapshot: snapshot.getChildren()){
                    Hotel hotel = postSnapshot.getValue(Hotel.class);
                    if(hotel.hotelName.toLowerCase().contains(searchText.toLowerCase()) || hotel.hotelCity.toLowerCase().contains(searchText.toLowerCase())){
                        hotelsList.add(postSnapshot.getValue(Hotel.class));
                        hotelsId.add(postSnapshot.getKey());
                    }
                }
                if(hotelsList.size() > 0){
                    customerSearchItemAdapter.setHotels(hotelsList,hotelsId);
                    //checkRoomData(hotelsList,hotelsId);
                }else{
                    Toast.makeText(getBaseContext(), "No result found. Please try again.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}