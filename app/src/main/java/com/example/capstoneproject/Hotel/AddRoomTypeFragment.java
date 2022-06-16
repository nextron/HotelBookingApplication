package com.example.capstoneproject.Hotel;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstoneproject.Models.Hotel;
import com.example.capstoneproject.Models.RoomType;
import com.example.capstoneproject.R;
import com.example.capstoneproject.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddRoomTypeFragment extends Fragment {

    //UI components declaration
    EditText edtHotelAddRoomHotelName,edtHotelAddRoomCity;
    TextView txvHotelAddRoomListOfAmenities;
    Spinner spHotelAddRoomAmenities;
    Button btnHotelAddRoomSave, btnHotelAddRoomAddRoom;
    RecyclerView rvHotelAddRoomList;
    ImageView imgHotelAddRoomHotelImage,imgHotelAddRoomHotelImageSelect;
    
    //Local Variables
    ProgressDialog hotelAddRoomLoader;
    final String hotelDisplayImage = "displayImage";
    final String singleRoomImage = "singleRoom";
    final String doubleRoomImage = "doubleRoom";
    //final String suiteRoomImage = "suiteRoom";
    Bitmap bitmapImg;
    RoomTypeAdapter roomTypeAdapter;

    //Firebase Storage Reference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;

    //List of Amenities
    String[] amenitiesData = {"---","WiFi","Parking","Hot Tub","Living Room"};

    //Room Types
    String[] roomTypeData = {"---","Single Room","Double Room"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_room_type, container, false);

        //Variable Initialization
        edtHotelAddRoomCity = view.findViewById(R.id.edtHotelAddRoomCity);
        edtHotelAddRoomHotelName = view.findViewById(R.id.edtHotelAddRoomHotelName);
        txvHotelAddRoomListOfAmenities = view.findViewById(R.id.txvHotelAddRoomListOfAmenities);
        spHotelAddRoomAmenities = view.findViewById(R.id.spHotelAddRoomAmenities);
        btnHotelAddRoomAddRoom = view.findViewById(R.id.btnHotelAddRoomAddRoom);
        btnHotelAddRoomSave = view.findViewById(R.id.btnHotelAddRoomSave);
        rvHotelAddRoomList = view.findViewById(R.id.rvHotelAddRoomList);
        imgHotelAddRoomHotelImageSelect = view.findViewById(R.id.imgHotelAddRoomHotelImageSelect);
        imgHotelAddRoomHotelImage = view.findViewById(R.id.imgHotelAddRoomHotelImage);

        //Initializing storage reference
        storageRef = storage.getReference();

        //Setting values in Amenities spinner
        ArrayAdapter aaAmenites = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1,amenitiesData);
        spHotelAddRoomAmenities.setAdapter(aaAmenites);

        //Recycler View
        rvHotelAddRoomList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHotelAddRoomList.setHasFixedSize(true);
        roomTypeAdapter = new RoomTypeAdapter();
        rvHotelAddRoomList.setAdapter(roomTypeAdapter);

        //Recycler view click events
        roomTypeAdapter.setOnItemClickListener(new RoomTypeAdapter.OnItemClickListener() {
            @Override
            public void onEditButtonClick(RoomType roomType, String path) {
                showCustomDialog(roomType,path);
                //Toast.makeText(getContext(), "RoomType: "+roomType.roomType+" path: "+path, Toast.LENGTH_SHORT).show();
            }
        });

        //Loading the hotel details if available
        loadDetails();

        //Loading room type data if available
        loadRoomTypeData();

        //amenities select event //Amenitiy add or delete
        spHotelAddRoomAmenities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Amenities will can be added or deleted with the item select from the spinner
                String list = txvHotelAddRoomListOfAmenities.getText().toString();
                if(i == 0){
                    //Toast.makeText(getContext(), "Please select a item", Toast.LENGTH_SHORT).show();
                }else {
                    if (list.length() == 0) {
                        list += amenitiesData[i];
                    } else {
                        Boolean flag = false;
                        String updatedList = "";
                        String[] listSplit = list.split(", ");
                        for (String s : listSplit) {
                            if (s.equals(amenitiesData[i])) {
                                flag = true;
                            } else {
                                updatedList += s;
                                updatedList += ", ";
                            }
                        }
                        if(flag){
                            if(updatedList.length() == 0){
                                list = "";
                            }else {
                                list = updatedList.substring(0, updatedList.length() - 2);
                            }
                        }else{
                            list += ", ";
                            list += amenitiesData[i];
                        }
                    }
                    txvHotelAddRoomListOfAmenities.setText(list);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getContext(), "Please select a item.", Toast.LENGTH_SHORT).show();
            }
        });

        //Save the Hotel details
        btnHotelAddRoomSave.setOnClickListener(v -> {
            saveHotelDetails();
        });

        //Add a new room type
        btnHotelAddRoomAddRoom.setOnClickListener(v->{
            showCustomDialog(null, null);
        });

        //calling image picker
        imgHotelAddRoomHotelImageSelect.setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });

        return view;
    }

    public void loadRoomTypeData(){
        //Recycle View for list of room types
        List<RoomType> roomTypeList = new ArrayList<>();
        //Firebase reference for room types
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference roomRef = database.getReference("rooms");
        //Getting list of room types
        roomRef.child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomTypeList.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren()){
                    RoomType type = postSnapshot.getValue(RoomType.class);
                    roomTypeList.add(type);
                }
                //Toast.makeText(getContext(), "list"+roomTypeList.size(), Toast.LENGTH_SHORT).show();
                if(roomTypeList.size() > 0){
                    roomTypeAdapter.setRooms(roomTypeList);
                    if(roomTypeList.size() == roomTypeData.length - 1){
                        btnHotelAddRoomAddRoom.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error getting the data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Custom dialog to save new room type
    public void showCustomDialog(RoomType roomType, String path) {
        Dialog dialog = new Dialog(getContext());
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.hotel_custom_dialog);

        //Initializing the views of the dialog.
        ImageView imgHotelAddRoomDialogHotelImage = dialog.findViewById(R.id.imgHotelAddRoomDialogHotelImage);
        Spinner spHotelAddRoomType = dialog.findViewById(R.id.spHotelAddRoomType);
        EditText edtHotelAddRoomCharges = dialog.findViewById(R.id.edtHotelAddRoomCharges);
        Button btnHotelAddRoomClose = dialog.findViewById(R.id.btnHotelAddRoomClose);
        Button btnHotelAddRoomSubmit = dialog.findViewById(R.id.btnHotelAddRoomSubmit);
        ImageView imgHotelAddRoomCloseDialog = dialog.findViewById(R.id.imgHotelAddRoomCloseDialog);
        Button btnHotelAddRoomUpload = dialog.findViewById(R.id.btnHotelAddRoomUpload);

        //hide load and upload button
        btnHotelAddRoomUpload.setEnabled(false);

        //filling room type spinner
        ArrayAdapter aaRoomType = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1,roomTypeData);
        spHotelAddRoomType.setAdapter(aaRoomType);

        //loading data in custom dialog
        if(roomType != null){
            edtHotelAddRoomCharges.setText(roomType.roomCharges);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            imgHotelAddRoomDialogHotelImage.setImageBitmap(bitmap);
            spHotelAddRoomType.setSelection(aaRoomType.getPosition(roomType.roomType));
            //spHotelAddRoomType.setSelected(roomType.roomType);
        }

        //logic for the submit button
        btnHotelAddRoomSubmit.setOnClickListener(v -> {
            String charges = edtHotelAddRoomCharges.getText().toString().trim();

            //checking if the room type is selected or not
            if(spHotelAddRoomType.getSelectedItemId() == 0){
                return;
            }
            //checking if charges are provided or not
            if(charges.isEmpty()){
                edtHotelAddRoomCharges.setError("Room charges are required");
                edtHotelAddRoomCharges.requestFocus();
                return;
            }
            RoomType room = new RoomType(roomTypeData[(int) spHotelAddRoomType.getSelectedItemId()],charges);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference roomsRef = database.getReference("rooms");
            String roomTypeChild = "";
            if(spHotelAddRoomType.getSelectedItemId() == 1){
                roomTypeChild = singleRoomImage;
            }else if(spHotelAddRoomType.getSelectedItemId() == 2){
                roomTypeChild = doubleRoomImage;
            }
            roomsRef.child(FirebaseAuth.getInstance().getUid()).child(roomTypeChild).setValue(room).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(), "Details has been saved.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }else{
                    Toast.makeText(getContext(), "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            //Toast.makeText(getContext(), "All good", Toast.LENGTH_SHORT).show();
        });

        //Image Upload to Firebase
        imgHotelAddRoomDialogHotelImage.setOnClickListener(v -> {
            if(spHotelAddRoomType.getSelectedItemId() == 0){
                Toast.makeText(getContext(), "Please select a Room Type.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imgContent.launch(intent);
            btnHotelAddRoomUpload.setEnabled(true);
        });

        //Image upload to firebase
        btnHotelAddRoomUpload.setOnClickListener(v -> {
            if(imgHotelAddRoomDialogHotelImage.getDrawable() != null && bitmapImg != null) {
                imgHotelAddRoomDialogHotelImage.setImageBitmap(bitmapImg);
                String imgName = "";
                if(spHotelAddRoomType.getSelectedItemId() == 1){
                    imgName = singleRoomImage;
                }else if(spHotelAddRoomType.getSelectedItemId() == 2){
                    imgName = doubleRoomImage;
                }
                imageToFirebaseBitmap(bitmapImg, imgName);
                bitmapImg = null;
            }else{
                Toast.makeText(getContext(), "Please select a image to upload.", Toast.LENGTH_SHORT).show();
            }
        });

        //to close the dialog box
        btnHotelAddRoomClose.setOnClickListener(v -> {
            dialog.dismiss();
        });
        imgHotelAddRoomCloseDialog.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
    //refresh dialog box

    //Custom Dialog Operation
    ActivityResultLauncher<Intent> imgContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent intent = result.getData();
                    if(intent != null){
                        //Toast.makeText(getContext(), "got Image"+result, Toast.LENGTH_SHORT).show();
                        try {
                            bitmapImg = MediaStore.Images.Media.getBitmap(
                                getContext().getContentResolver(),intent.getData()
                            );
                            //imgHotelAddRoomDialogHotelImage.setImageBitmap(bitmapImg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    //Image to firebase custom dialog
    public void imageToFirebaseBitmap(Bitmap bitmap, String imageName){
        //Showing progressbar
        hotelAddRoomLoader = new ProgressDialog(getActivity());
        hotelAddRoomLoader.setMessage("Uploading Image...");
        hotelAddRoomLoader.setCancelable(false);
        hotelAddRoomLoader.show();

        //Storage reference to firebase
        StorageReference imgRef = storageRef.child(FirebaseAuth.getInstance().getUid()+"/"+imageName+".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imgRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(getContext(), "Failed to upload image. Please try again.", Toast.LENGTH_SHORT).show();
            if(hotelAddRoomLoader.isShowing()){
                hotelAddRoomLoader.dismiss();
            }
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getContext(), "Image has been uploaded successfully.", Toast.LENGTH_SHORT).show();
            if(hotelAddRoomLoader.isShowing()){
                hotelAddRoomLoader.dismiss();
            }
        });
    }

    //Save Hotel details
    public void saveHotelDetails(){
        //Showing a loader
        hotelAddRoomLoader = new ProgressDialog(getActivity());
        hotelAddRoomLoader.setMessage("Saving Details...");
        hotelAddRoomLoader.setCancelable(false);
        hotelAddRoomLoader.show();
        
        String hotelName = edtHotelAddRoomHotelName.getText().toString().trim();
        String cityName = edtHotelAddRoomCity.getText().toString().trim();
        String listOfAmenities = txvHotelAddRoomListOfAmenities.getText().toString().trim();

        //flag to check if any of the required field is not empty
        Boolean flag = false;

        //checking all the fields if they are filled or not
        if(hotelName.isEmpty()){
            edtHotelAddRoomHotelName.setError("Hotel Name is required");
            edtHotelAddRoomHotelName.requestFocus();
            flag = true;
        }

        if(cityName.isEmpty()){
            edtHotelAddRoomCity.setError("Hotel City is required");
            edtHotelAddRoomCity.requestFocus();
            flag = true;
        }

        if(listOfAmenities.isEmpty()){
            Toast.makeText(getContext(), "Please select the amenities.", Toast.LENGTH_SHORT).show();
            flag = true;
        }

        //if any of the field is empty control of code will be returned from here
        if(flag){
            if(hotelAddRoomLoader.isShowing()){
                hotelAddRoomLoader.dismiss();
            }
            return;
        }else{
            //if all the fields have values then details will be stored to database
            Hotel hotel = new Hotel(hotelName,cityName,listOfAmenities);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("hotels");
            usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(hotel).addOnCompleteListener(task1 -> {
                if(task1.isSuccessful()){
                    Toast.makeText(getContext(), "Hotel details has been saved..", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Failed to save Hotel details,Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
            if(hotelAddRoomLoader.isShowing()){
                hotelAddRoomLoader.dismiss();
            }
        }
    }

    //display Hotel Details
    public void loadDetails(){
        //Showing a loader
        hotelAddRoomLoader = new ProgressDialog(getActivity());
        hotelAddRoomLoader.setMessage("Loading Details...");
        hotelAddRoomLoader.setCancelable(false);
        hotelAddRoomLoader.show();

        //Firebase reference for hotel details
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference hotelRef = database.getReference("hotels");

        hotelRef.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Hotel hotel = snapshot.getValue(Hotel.class);
                if(hotel != null) {
                    edtHotelAddRoomCity.setText(hotel.hotelCity);
                    edtHotelAddRoomHotelName.setText(hotel.hotelName);
                    txvHotelAddRoomListOfAmenities.setText(hotel.hotelAmenities);
                    loadImage(imgHotelAddRoomHotelImage, hotelDisplayImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Details not available.", Toast.LENGTH_SHORT).show();
            }
        });

        //if the loader is visible. It will be dismissed
        if(hotelAddRoomLoader.isShowing()){
            hotelAddRoomLoader.dismiss();
        }
    }

    //Load Images
    public void loadImage(ImageView imgViewName, String imageName){

        //Showing progressbar
        hotelAddRoomLoader = new ProgressDialog(getActivity());
        hotelAddRoomLoader.setMessage("Loading Image...");
        hotelAddRoomLoader.setCancelable(false);
        hotelAddRoomLoader.show();

        StorageReference imgRef = storageRef.child(FirebaseAuth.getInstance().getUid()+"/"+imageName+".jpg");

        //Loading a profile image
        try {
            File localFile = File.createTempFile(imageName,".jpg");
            imgRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                imgViewName.setImageBitmap(bitmap);
                if(hotelAddRoomLoader.isShowing()){
                    hotelAddRoomLoader.dismiss();
                }
            }).addOnFailureListener(e -> {
                if(hotelAddRoomLoader.isShowing()){
                    hotelAddRoomLoader.dismiss();
                }
                Toast.makeText(getContext(), "Failed to Load Image. Please reload the profile.", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return "false";
    }

    //Image Selector
    //For image pick from gallary.
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    imgHotelAddRoomHotelImage.setImageURI(result);
                    imageToFirebase(imgHotelAddRoomHotelImage,hotelDisplayImage);
                }
            });

    //Upload Image to Firebase
    public void imageToFirebase(ImageView imgViewName, String imageName){
        //Showing progressbar
        hotelAddRoomLoader = new ProgressDialog(getActivity());
        hotelAddRoomLoader.setMessage("Uploading Image...");
        hotelAddRoomLoader.setCancelable(false);
        hotelAddRoomLoader.show();

        StorageReference imgRef = storageRef.child(FirebaseAuth.getInstance().getUid()+"/"+imageName+".jpg");
        //profileImgRef.getName().equals(profileImgRef.getName());

        // Get the data from an ImageView as bytes
        imgViewName.setDrawingCacheEnabled(true);
        imgViewName.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgViewName.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imgRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(getContext(), "Failed to upload image. Please try again.", Toast.LENGTH_SHORT).show();
            if(hotelAddRoomLoader.isShowing()){
                hotelAddRoomLoader.dismiss();
            }
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getContext(), "Image has been uploaded successfully.", Toast.LENGTH_SHORT).show();
            if(hotelAddRoomLoader.isShowing()){
                hotelAddRoomLoader.dismiss();
            }
        });
    }
}