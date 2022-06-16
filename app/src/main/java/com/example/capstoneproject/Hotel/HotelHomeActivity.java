package com.example.capstoneproject.Hotel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.capstoneproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HotelHomeActivity extends AppCompatActivity {

    //UI Components Declaration
    BottomNavigationView hotelNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_home);

        //Variable Initialization
        hotelNavigationView = findViewById(R.id.bottom_navigation_hotel);
        //To display the home fragment on loading
        getSupportFragmentManager().beginTransaction().replace(R.id.body_container, new HomeFragment()).commit();
        hotelNavigationView.setSelectedItemId(R.id.nav_hotel_home);

        //hotelNavigationView.setOnNavigationItemReselectedListener();
        //switching fragment
        hotelNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()){
                    case R.id.nav_hotel_home:
                        fragment = new HomeFragment();
                        //hotelNavigationView.setSelectedItemId(R.id.nav_hotel_home);
                        break;
//                    case R.id.nav_hotel_messages:
//                        fragment = new MessagesFragment();
//                        //hotelNavigationView.setSelectedItemId(R.id.nav_hotel_messages);
//                        break;
                    case R.id.nav_hotel_order:
                        fragment = new OrdersFragment();
                        //hotelNavigationView.setSelectedItemId(R.id.nav_hotel_order);
                        break;
                    case R.id.nav_hotel_profile:
                        fragment = new ProfileFragment();
                        //hotelNavigationView.setSelectedItemId(R.id.nav_hotel_profile);
                        break;
                    case R.id.nav_hotel_add_room:
                        fragment = new AddRoomTypeFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.body_container, fragment).commit();
                return true;
            }
        });
    }
}