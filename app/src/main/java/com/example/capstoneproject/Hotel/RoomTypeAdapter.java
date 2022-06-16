package com.example.capstoneproject.Hotel;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstoneproject.Models.RoomType;
import com.example.capstoneproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeAdapter extends RecyclerView.Adapter<RoomTypeAdapter.roomTypeHolder> {

    private List<RoomType> rooms = new ArrayList<>();
    private OnItemClickListener listener;

    //room images local path
    String singleRoomImageLocalPath = "";
    String doubleRoomImageLocalPath = "";

    //Firebase Storage Reference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;

    @NonNull
    @Override
    public roomTypeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_rv_item,parent,false);
        //Initializing storage reference
        storageRef = storage.getReference();
        return new roomTypeHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull roomTypeHolder holder, int position) {
        RoomType roomType = rooms.get(position);
        holder.rvCustomItemCharges.setText(roomType.roomCharges + " $/Night");
        holder.rvCustomItemRoomType.setText(roomType.roomType);
        String imgName = "";
        if(roomType.roomType.equals("Single Room")){
            imgName = "singleRoom";
        }else if(roomType.roomType.equals("Double Room")){
            imgName = "doubleRoom";
        }
        //Loading image in recycle view

        StorageReference imgRef = storageRef.child(FirebaseAuth.getInstance().getUid()+"/"+imgName+".jpg");

        //Loading a image
        try {
            File localFile = File.createTempFile(imgName,".jpg");
            String finalImgName = imgName;
            imgRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                if(finalImgName.equals("singleRoom")){
                    singleRoomImageLocalPath = localFile.getAbsolutePath();
                }else if(finalImgName.equals("doubleRoom")){
                    doubleRoomImageLocalPath = localFile.getAbsolutePath();
                }
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                //Toast.makeText(holder.itemView.getContext(), ""+singleRoomImageLocalPath, Toast.LENGTH_SHORT).show();
                holder.rvCustomItemImage.setImageBitmap(bitmap);
//                if(hotelAddRoomLoader.isShowing()){
//                    hotelAddRoomLoader.dismiss();
//                }
            }).addOnFailureListener(e -> {
                //Toast.makeText(getClass(), "Failed to Load Profile Image. Please reload the profile.", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public void setRooms(List<RoomType> rooms){
        this.rooms = rooms;
        notifyDataSetChanged();
    }

    public RoomType getRoomAt(int position){
        return rooms.get(position);
    }

    class roomTypeHolder extends RecyclerView.ViewHolder{
        private TextView rvCustomItemRoomType;
        private TextView rvCustomItemCharges;
        private ImageView rvCustomItemImage;
        private Button rvCustomItemButton;

        public roomTypeHolder(@NonNull View itemView) {
            super(itemView);
            rvCustomItemButton = itemView.findViewById(R.id.rvCustomItemButton);
            rvCustomItemImage = itemView.findViewById(R.id.rvCustomItemImage);
            rvCustomItemRoomType = itemView.findViewById(R.id.rvCustomItemRoomType);
            rvCustomItemCharges = itemView.findViewById(R.id.rvCustomItemCharges);

            rvCustomItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(listener != null && position != RecyclerView.NO_POSITION){
                        String path = "";
                        if(rooms.get(position).roomType.equals("Single Room")){
                            path = singleRoomImageLocalPath;
                        }else if(rooms.get(position).roomType.equals("Double Room")){
                            path = doubleRoomImageLocalPath;
                        }
                        listener.onEditButtonClick(rooms.get(position), path);
                    }
                    //Toast.makeText(view.getContext(), "clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onEditButtonClick(RoomType roomType, String path);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
