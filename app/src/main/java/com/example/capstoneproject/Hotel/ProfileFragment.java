package com.example.capstoneproject.Hotel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import com.example.capstoneproject.Models.User;
import com.example.capstoneproject.R;
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
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    //UI Components Declaration
    Button btnHotelProfileLogout, btnHotelProfileEditImage, btnHotelProfileUpdate;
    EditText edtHotelProfileEmailId, edtHotelProfileFullName, edtHotelProfilePhone;
    TextView txvHotelProfileChangePassword;
    ImageView imgHotelProfileImage;
    ProgressDialog hotelProfileLoader;

    //Local variable
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;
    String userGender;
    String userType;
    String userName;
    String userPhoneNo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Closing the activity if userId is not available
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()){
            getActivity().finish();
        }


        //Variable Initialization
        btnHotelProfileEditImage = view.findViewById(R.id.btnHotelEditProfileImage);
        btnHotelProfileLogout = view.findViewById(R.id.btnHotelProfileLogout);
        btnHotelProfileUpdate = view.findViewById(R.id.btnHotelProfileUpdate);
        edtHotelProfileEmailId = view.findViewById(R.id.edtHotelProfileEmailId);
        edtHotelProfileFullName = view.findViewById(R.id.edtHotelProfileFullName);
        edtHotelProfilePhone = view.findViewById(R.id.edtHotelProfilePhone);
        //txvHotelProfileChangePassword = view.findViewById(R.id.txvHotelProfileChangePassword);
        imgHotelProfileImage = view.findViewById(R.id.imgHotelProfileImage);

        //Calling load profile to fill UI components
        loadProfile();

        //Get a new profile image from gallary
        btnHotelProfileEditImage.setOnClickListener(v->{
            mGetContent.launch("image/*");
        });

        //Logout of the profile and going back to login activity
        btnHotelProfileLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            getActivity().finish();
        });

        //Updating profile
        btnHotelProfileUpdate.setOnClickListener(v->{
            updateUserDetails();
        });

        return view;
    }

    //Update user details
    public void updateUserDetails(){
        //Showing progressbar
        hotelProfileLoader = new ProgressDialog(getActivity());
        hotelProfileLoader.setMessage("Updating profile...");
        hotelProfileLoader.setCancelable(false);
        hotelProfileLoader.show();

        String name = edtHotelProfileFullName.getText().toString().trim();
        String phoneNo = edtHotelProfilePhone.getText().toString().trim();

        //check if fields are not empty or not
        boolean flag = false;
        if(name.isEmpty()){
            edtHotelProfileFullName.setError("Full Name is required");
            edtHotelProfileFullName.requestFocus();
            flag = true;
        }
        if(phoneNo.isEmpty()){
            edtHotelProfilePhone.setError("Phone Number is required");
            edtHotelProfilePhone.requestFocus();
            flag = true;
        }

        //if any of the field is empty control of code will be returned
        if(flag){
            if(hotelProfileLoader.isShowing()){
                hotelProfileLoader.dismiss();
            }
            return;
        }

        //check if user has changed the values
        if(name.equals(userName) && phoneNo.equals(userPhoneNo)){
            Toast.makeText(getContext(), "Please changes value in order to update details.", Toast.LENGTH_SHORT).show();
        }else{
            //Firebase reference for user table
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("users");

            //fetching userid of current user
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Creating obj of updated values
            HashMap<String, String> userObj = new HashMap<>();
            userObj.put("fullName", name);
            userObj.put("gender",userGender);
            userObj.put("phoneNumber", phoneNo);
            userObj.put("userType",userType);

            usersRef.child(userId).setValue(userObj).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(), "Profile has been updated.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Update has failed. Please try again", Toast.LENGTH_SHORT).show();
                }
            });
        }
        //if progress dialog is showing it will dismissed
        if(hotelProfileLoader.isShowing()){
            hotelProfileLoader.dismiss();
        }
    }

    //For image pick from gallary.
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        imgHotelProfileImage.setImageURI(result);
                        imageToFirebase();
                    }
                }
            });

    //Loading profile
    public void loadProfile(){
        //Showing progressbar
        hotelProfileLoader = new ProgressDialog(getActivity());
        hotelProfileLoader.setMessage("Loading Profile...");
        hotelProfileLoader.setCancelable(false);
        hotelProfileLoader.show();
        //Firebase Cloud storage reference("Profile Pic reference")
        storageRef = storage.getReference();
        StorageReference profileImgRefLoad = storageRef.child(FirebaseAuth.getInstance().getUid()+"/profile.jpg");

        //Firebase reference for user details
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");

        //Loading a profile image
        try {
            File localFile = File.createTempFile("profile",".jpg");
            profileImgRefLoad.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                imgHotelProfileImage.setImageBitmap(bitmap);
                if(hotelProfileLoader.isShowing()){
                    hotelProfileLoader.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "No Profile image available.", Toast.LENGTH_SHORT).show();
                if(hotelProfileLoader.isShowing()){
                    hotelProfileLoader.dismiss();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        userRef.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                edtHotelProfileEmailId.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                userGender = user.gender;
                userType = user.userType;
                userName = user.fullName;
                userPhoneNo = user.phoneNumber;
                edtHotelProfileFullName.setText(userName);
                edtHotelProfilePhone.setText(userPhoneNo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading the profile. Please login again.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
    }

    //Upload Image to Firebase
    public void imageToFirebase(){
        //Showing progressbar
        hotelProfileLoader = new ProgressDialog(getActivity());
        hotelProfileLoader.setMessage("Uploading Image...");
        hotelProfileLoader.setCancelable(false);
        hotelProfileLoader.show();

        StorageReference profileImgRef = storageRef.child(FirebaseAuth.getInstance().getUid()+"/profile.jpg");
        profileImgRef.getName().equals(profileImgRef.getName());

        // Get the data from an ImageView as bytes
        imgHotelProfileImage.setDrawingCacheEnabled(true);
        imgHotelProfileImage.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgHotelProfileImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profileImgRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(getContext(), "Failed to load image. Please try again.", Toast.LENGTH_SHORT).show();
            if(hotelProfileLoader.isShowing()){
                hotelProfileLoader.dismiss();
            }
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getContext(), "Image has been uploaded successfully.", Toast.LENGTH_SHORT).show();
            if(hotelProfileLoader.isShowing()){
                hotelProfileLoader.dismiss();
            }
        });
    }
}