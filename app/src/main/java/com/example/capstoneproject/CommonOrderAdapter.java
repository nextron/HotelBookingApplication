package com.example.capstoneproject;

import static android.graphics.Color.rgb;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstoneproject.Hotel.AdminActionAdapter;
import com.example.capstoneproject.Models.Hotel;
import com.example.capstoneproject.Models.Orders;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class CommonOrderAdapter extends RecyclerView.Adapter<CommonOrderAdapter.commonOrderHolder> {

    private List<Orders> orders = new ArrayList<>();
    private List<String> orderIds = new ArrayList<>();
    private CommonOrderAdapter.OnItemClickListener listener;

    //Firebase Storage Reference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;

    @NonNull
    @Override
    public CommonOrderAdapter.commonOrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.common_order_item,parent,false);
        //Initializing storage reference
        storageRef = storage.getReference();
        return new CommonOrderAdapter.commonOrderHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommonOrderAdapter.commonOrderHolder holder, int position) {
        Orders order = orders.get(position);

        holder.orderItemCheckIn.setText("CheckIn: " + order.checkInDate);
        holder.orderItemCheckOut.setText("CheckOut: " + order.checkOutDate);
        holder.orderItemRooms.setText("Rooms: " + order.numberOfRooms);
        holder.orderItemGuests.setText("Guests: " + order.numberOfGuests);
        //holder.orderItemOrderStatus.setText("Status: " + order.orderStatus);
        if(order.orderStatus.equals("Pending")){
            holder.orderItemOrderStatus.setText("Pending with Hotel");
            holder.cardViewOrder.setCardBackgroundColor(rgb(255, 255, 179));
            holder.orderItemOrderStatus.setTextColor(rgb(255, 180, 0));
        }else if(order.orderStatus.equals("Rejected")){
            holder.orderItemOrderStatus.setText("Rejected by Hotel");
            holder.cardViewOrder.setCardBackgroundColor(rgb(255, 204, 204));
            holder.orderItemOrderStatus.setTextColor(rgb(255, 51, 51));
        }else if(order.orderStatus.equals("Approved")){
            holder.orderItemOrderStatus.setText("Approved by Hotel");
            holder.cardViewOrder.setCardBackgroundColor(rgb(230, 255, 179));
            holder.orderItemOrderStatus.setTextColor(rgb(71, 209, 71));
        }else if(order.orderStatus.equals("Cancelled")){
            holder.orderItemOrderStatus.setText("Cancelled by Customer");
            holder.cardViewOrder.setCardBackgroundColor(rgb(255, 204, 204));
            holder.orderItemOrderStatus.setTextColor(rgb(255, 51, 51));
        }
        //Database Reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference hotelRef = database.getReference("hotels");
        hotelRef.child(order.hotelId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Hotel hotel = snapshot.getValue(Hotel.class);
                holder.orderItemHotelName.setText(hotel.hotelName +", "+order.roomType);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Orders> orders, List<String> orderIds){
        this.orders = orders;
        this.orderIds = orderIds;
        notifyDataSetChanged();
    }


    class commonOrderHolder extends RecyclerView.ViewHolder{
        private TextView orderItemHotelName, orderItemRooms, orderItemCheckIn, orderItemGuests, orderItemOrderStatus, orderItemCheckOut;
        private CardView cardViewOrder;
        public commonOrderHolder(@NonNull View itemView) {
            super(itemView);
            cardViewOrder = itemView.findViewById(R.id.cardViewOrder);
            orderItemHotelName = itemView.findViewById(R.id.orderItemHotelName);
            orderItemRooms = itemView.findViewById(R.id.orderItemRooms);
            orderItemCheckIn = itemView.findViewById(R.id.orderItemCheckIn);
            orderItemGuests = itemView.findViewById(R.id.orderItemGuests);
            orderItemOrderStatus = itemView.findViewById(R.id.orderItemOrderStatus);
            orderItemCheckOut = itemView.findViewById(R.id.orderItemCheckOut);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(orders.get(getAdapterPosition()), orderIds.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(Orders order, String orderId);
    }

    public void setOnItemClickListener(CommonOrderAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
