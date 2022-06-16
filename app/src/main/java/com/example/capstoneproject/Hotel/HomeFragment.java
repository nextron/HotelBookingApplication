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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    //UI components declaration
    RecyclerView rvAdminAction;
    AdminActionAdapter actionAdapter;

    //Local Variable
    List<Orders> ordersList = new ArrayList<>();
    List<String> orderIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //variable initialization
        rvAdminAction = view.findViewById(R.id.rvAdminAction);

        //Recycler view adapter
        rvAdminAction.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAdminAction.setHasFixedSize(true);
        actionAdapter = new AdminActionAdapter();
        rvAdminAction.setAdapter(actionAdapter);

        //Firebase reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ordersRef = database.getReference("orders");

        //Getting list of orders of a logged in user
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderIds.clear();
                ordersList.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren()){
                    Orders order = postSnapshot.getValue(Orders.class);
                    if(order.hotelId.equals(FirebaseAuth.getInstance().getUid()) && order.orderStatus.equals("Pending")){
                        ordersList.add(order);
                        orderIds.add(postSnapshot.getKey());
                    }
                }
                if(ordersList.size() > 0){
                    actionAdapter.setOrders(ordersList, orderIds);
                }else{
                    actionAdapter.setOrders(ordersList, orderIds);
                    toastForNoOrder();
                    //Toast.makeText(getContext(), "You don't have any pending order.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        actionAdapter.setOnItemClickListener(new AdminActionAdapter.OnItemClickListener() {
            @Override
            public void onApprove(String orderId) {
                ordersRef.child(orderId).child("orderStatus").setValue("Approved").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), "Order has been approved.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onReject(String orderId) {
                ordersRef.child(orderId).child("orderStatus").setValue("Rejected").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), "Order has been rejected.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        return view;
    }
    public void toastForNoOrder(){
        //Toast.makeText(getContext(), "You don't have any pending order.", Toast.LENGTH_SHORT).show();
    }
}