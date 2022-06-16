package com.example.capstoneproject.Customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.capstoneproject.Hotel.HomeFragment;
import com.example.capstoneproject.Hotel.ProfileFragment;
import com.example.capstoneproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class CustomerHomeActivity extends AppCompatActivity {

    //UI Components Declaration
    BottomNavigationView customerNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        //Variable Initialization
        customerNavigationView = findViewById(R.id.bottom_navigation_customer);

        //To display the home fragment on loading
        getSupportFragmentManager().beginTransaction().replace(R.id.body_container_customer, new CustomerSearchFragment()).commit();
        customerNavigationView.setSelectedItemId(R.id.nav_customer_search);
        
        //
        //switching fragment
        customerNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                switch (item.getItemId()){
                    case R.id.nav_customer_search:
                        fragment = new CustomerSearchFragment();
                        break;
//                    case R.id.nav_customer_messages:
//                        fragment = new CustomerMessagesFragment();
//                        break;
                    case R.id.nav_customer_order:
                        fragment = new CustomerOrdersFragment();
                        break;
                    case R.id.nav_customer_profile:
                        fragment = new ProfileFragment();
                        //customerNavigationView.setSelectedItemId(R.id.nav_customer_profile);
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.body_container_customer, fragment).commit();
                return true;
            }
        });
    }
}