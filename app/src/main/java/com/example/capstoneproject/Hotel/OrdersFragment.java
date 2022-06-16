package com.example.capstoneproject.Hotel;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.capstoneproject.CommonOrderAdapter;
import com.example.capstoneproject.Models.Orders;
import com.example.capstoneproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    //UI components declaration
    RecyclerView rvAdminOrder;
    CommonOrderAdapter orderAdapter;

    //Local Variable
    List<Orders> ordersList = new ArrayList<>();
    List<String> orderIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        //variable initialization
        rvAdminOrder = view.findViewById(R.id.rvAdminOrder);

        //Recycler view adapter
        rvAdminOrder.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAdminOrder.setHasFixedSize(true);
        orderAdapter = new CommonOrderAdapter();
        rvAdminOrder.setAdapter(orderAdapter);


        //Firebase reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ordersRef = database.getReference("orders");

        //Getting list of orders of a logged in user
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();
                orderIds.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren()){
                    Orders order = postSnapshot.getValue(Orders.class);
                    if(order.hotelId.equals(FirebaseAuth.getInstance().getUid())){
                        ordersList.add(order);
                        orderIds.add(postSnapshot.getKey());
                    }
                }
                if(ordersList.size() > 0){
                    orderAdapter.setOrders(ordersList, orderIds);
                }else{
                    Toast.makeText(getContext(), "You have not received any order.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}