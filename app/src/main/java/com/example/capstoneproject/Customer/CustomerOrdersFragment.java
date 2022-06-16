package com.example.capstoneproject.Customer;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstoneproject.CommonOrderAdapter;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CustomerOrdersFragment extends Fragment {
    //UI components declaration
    RecyclerView rvCustomerOrder;
    CommonOrderAdapter orderAdapter;

    //Local Variable
    List<Orders> ordersList = new ArrayList<>();
    List<String> orderIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_orders, container, false);

        //variable initialization
        rvCustomerOrder = view.findViewById(R.id.rvCustomerOrder);

        //Recycler view adapter
        rvCustomerOrder.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCustomerOrder.setHasFixedSize(true);
        orderAdapter = new CommonOrderAdapter();
        rvCustomerOrder.setAdapter(orderAdapter);

        //Adapter on item click listener
        orderAdapter.setOnItemClickListener(new CommonOrderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Orders order, String orderId) {
                showOrderCancelDialog(order, orderId);
            }
        });

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
                    if(order.customerId.equals(FirebaseAuth.getInstance().getUid())){
                        ordersList.add(order);
                        orderIds.add(postSnapshot.getKey());
                    }
                }
                if(ordersList.size() > 0){
                    orderAdapter.setOrders(ordersList, orderIds);
                }else{
                    Toast.makeText(getContext(), "You have not placed any order yet.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
    //Custom dialog to cancel a order
    public void showOrderCancelDialog(Orders order, String orderId) {
        Dialog dialog = new Dialog(getContext());
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.customer_order_dialog);

        ImageView imgCustOrderCloseDIalog = dialog.findViewById(R.id.imgCustOrderCloseDIalog);
        TextView custOrderHotelName = dialog.findViewById(R.id.custOrderHotelName);
        TextView custOrderCheckIn = dialog.findViewById(R.id.custOrderCheckIn);
        TextView custOrderCheckOut = dialog.findViewById(R.id.custOrderCheckOut);
        TextView custOrderGuests = dialog.findViewById(R.id.custOrderGuests);
        TextView custOrderRooms = dialog.findViewById(R.id.custOrderRooms);
        TextView custOrderAmount = dialog.findViewById(R.id.custOrderAmount);
        Button btnCustOrderCancel = dialog.findViewById(R.id.btnCustOrderCancel);

        //Setting the values
        custOrderCheckIn.setText("CheckIn: " + order.checkInDate);
        custOrderCheckOut.setText("CheckOut: " + order.checkOutDate);
        custOrderRooms.setText("Rooms: " + order.numberOfRooms);
        custOrderGuests.setText("Guests: " + order.numberOfGuests);
        custOrderAmount.setText("Order Amount: " + order.orderAmount + " $");

        //Disabling the button if order is rejected by the hotel
        if(order.orderStatus.equals("Rejected") || order.orderStatus.equals("Cancelled")){
            btnCustOrderCancel.setEnabled(false);
        }

        //Database Reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference hotelRef = database.getReference("hotels");
        hotelRef.child(order.hotelId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Hotel hotel = snapshot.getValue(Hotel.class);
                custOrderHotelName.setText(hotel.hotelName +", "+order.roomType);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Closing the dialog
        imgCustOrderCloseDIalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //Order cancel login
        btnCustOrderCancel.setOnClickListener(v -> {
            DatabaseReference localOrderRef = database.getReference("orders");
            localOrderRef.child(orderId).child("orderStatus").setValue("Cancelled").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(), "Order has been cancelled.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
        dialog.show();
    }
}