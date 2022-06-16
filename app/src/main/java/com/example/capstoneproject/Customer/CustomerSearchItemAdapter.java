package com.example.capstoneproject.Customer;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstoneproject.Models.Hotel;
import com.example.capstoneproject.Models.RoomType;
import com.example.capstoneproject.R;
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
import java.util.ArrayList;
import java.util.List;

public class CustomerSearchItemAdapter extends RecyclerView.Adapter<CustomerSearchItemAdapter.customerSearchHolder> {

    private List<Hotel> hotels = new ArrayList<>();
    private List<String> hotelsID = new ArrayList<>();
    private OnItemClickListener listener;

    //Firebase Storage Reference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;

    @NonNull
    @Override
    public customerSearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_search_item,parent,false);
        //Initializing storage reference
        storageRef = storage.getReference();
        return new customerSearchHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull customerSearchHolder holder, int position) {
        Hotel hotel = hotels.get(position);
        String id = hotelsID.get(position);
        holder.txvSearchItemHotelName.setText(hotel.hotelName+", "+hotel.hotelCity);

        //code for hotel charges: it will take single room charges by default
        //if single room is not available it will go for double room
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference roomsRef = database.getReference("rooms");
        roomsRef.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()){
                    RoomType room = postSnapshot.getValue(RoomType.class);
                    holder.txvSearchItemHotelCharges.setText(room.roomCharges + " $/Night");
                    if(postSnapshot.getKey().equals("singleRoom")){
                        //charges = room.roomCharges;
                        holder.txvSearchItemHotelCharges.setText(room.roomCharges+ " $/Night");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Loading hotel Image
        String imgName = "displayImage";
        StorageReference imgRef = storageRef.child(id+"/"+imgName+".jpg");

        //Loading a image
        try {
            File localFile = File.createTempFile(imgName,".jpg");
            imgRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                holder.imgSearchItemImage.setImageBitmap(bitmap);
            }).addOnFailureListener(e -> {

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    public void setHotels(List<Hotel> hotels, List<String> hotelsID){
        this.hotelsID = hotelsID;
        this.hotels = hotels;
        notifyDataSetChanged();
    }


    class customerSearchHolder extends RecyclerView.ViewHolder{
        private TextView txvSearchItemHotelCharges;
        private ImageView imgSearchItemImage;
        private TextView txvSearchItemHotelName;

        public customerSearchHolder(@NonNull View itemView) {
            super(itemView);
            txvSearchItemHotelCharges = itemView.findViewById(R.id.txvSearchItemHotelCharges);
            imgSearchItemImage = itemView.findViewById(R.id.imgSearchItemImage);
            txvSearchItemHotelName = itemView.findViewById(R.id.txvSearchItemHotelName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(listener != null && position != RecyclerView.NO_POSITION){
                        listener.onItemClick(hotels.get(position),hotelsID.get(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(Hotel hotel, String hotelId);
    }

    public void setOnItemClickListener(CustomerSearchItemAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

}
