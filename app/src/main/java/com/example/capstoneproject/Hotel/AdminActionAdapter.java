package com.example.capstoneproject.Hotel;

import static android.graphics.Color.rgb;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.capstoneproject.Models.Hotel;
import com.example.capstoneproject.Models.Orders;
import com.example.capstoneproject.Models.RoomType;
import com.example.capstoneproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdminActionAdapter extends RecyclerView.Adapter<AdminActionAdapter.adminActionHolder> {

    private List<Orders> orders = new ArrayList<>();
    private List<String> orderIds = new ArrayList<>();
    private AdminActionAdapter.OnItemClickListener listener;

    //Firebase Storage Reference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;

    @NonNull
    @Override
    public AdminActionAdapter.adminActionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_action_item,parent,false);
        //Initializing storage reference
        storageRef = storage.getReference();
        return new AdminActionAdapter.adminActionHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminActionAdapter.adminActionHolder holder, int position) {
        Orders order = orders.get(position);

        holder.adminActionCheckIn.setText("CheckIn: " + order.checkInDate);
        holder.adminActionCheckOut.setText("CheckOut: " + order.checkOutDate);
        holder.adminActionRooms.setText("Rooms: " + order.numberOfRooms);
        holder.adminActionGuests.setText("Guests: " + order.numberOfGuests);

        //Database Reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference hotelRef = database.getReference("hotels");
        hotelRef.child(order.hotelId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Hotel hotel = snapshot.getValue(Hotel.class);
                holder.adminActionHotelName.setText(hotel.hotelName +", "+order.roomType);
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


    class adminActionHolder extends RecyclerView.ViewHolder{
        private TextView adminActionHotelName, adminActionRooms, adminActionCheckIn, adminActionGuests, adminActionCheckOut;
        private Button btnAdminActionReject, btnAdminActionApprove;
        public adminActionHolder(@NonNull View itemView) {
            super(itemView);
            adminActionHotelName = itemView.findViewById(R.id.adminActionHotelName);
            adminActionRooms = itemView.findViewById(R.id.adminActionRooms);
            adminActionCheckIn = itemView.findViewById(R.id.adminActionCheckIn);
            adminActionGuests = itemView.findViewById(R.id.adminActionGuests);
            adminActionCheckOut = itemView.findViewById(R.id.adminActionCheckOut);
            btnAdminActionApprove = itemView.findViewById(R.id.btnAdminActionApprove);
            btnAdminActionReject = itemView.findViewById(R.id.btnAdminActionReject);

            btnAdminActionApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onApprove(orderIds.get(getAdapterPosition()));
                }
            });

            btnAdminActionReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onReject(orderIds.get(getAdapterPosition()));
                }
            });
        }

    }

    public interface OnItemClickListener{
        void onApprove(String orderId);
        void onReject(String orderId);
    }

    public void setOnItemClickListener(AdminActionAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

}
