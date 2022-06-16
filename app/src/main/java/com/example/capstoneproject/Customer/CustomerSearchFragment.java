package com.example.capstoneproject.Customer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstoneproject.Models.Orders;
import com.example.capstoneproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class CustomerSearchFragment extends Fragment {
    //UI Components Declaration
    Button btnCustSearchSearch;
    TextView txvCustSearchCheckIn, txvCustSearchCheckOut, txvCustSearchRooms, txvCustSearchGuests;
    EditText edtCustSearchSearch;
    ImageView imgCustSearchCheckIn, imgCustSearchCheckOut, imgCustSearchMinusRooms, imgCustSearchAddRooms, imgCustSearchMinusGuest, imgCustSearchAddGuest;

    //Local Variables
    private DatePickerDialog datePickerDialog;
    boolean checkIn, checkOut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_search, container, false);

        //Variable Initialization
        btnCustSearchSearch = view.findViewById(R.id.btnCustSearchSearch);
        txvCustSearchCheckIn = view.findViewById(R.id.txvCustSearchCheckIn);
        txvCustSearchCheckOut = view.findViewById(R.id.txvCustSearchCheckOut);
        txvCustSearchRooms = view.findViewById(R.id.txvCustSearchRooms);
        txvCustSearchGuests = view.findViewById(R.id.txvCustSearchGuests);
        edtCustSearchSearch = view.findViewById(R.id.edtCustSearchSearch);
        imgCustSearchCheckIn = view.findViewById(R.id.imgCustSearchCheckIn);
        imgCustSearchAddGuest = view.findViewById(R.id.imgCustSearchAddGuest);
        imgCustSearchMinusGuest = view.findViewById(R.id.imgCustSearchMinusGuest);
        imgCustSearchCheckOut = view.findViewById(R.id.imgCustSearchCheckOut);
        imgCustSearchMinusRooms = view.findViewById(R.id.imgCustSearchMinusRooms);
        imgCustSearchAddRooms = view.findViewById(R.id.imgCustSearchAddRooms);

        //initializing date picker
        initDatePicker();

        //checkIn checkOut textview
        checkIn = false;
        checkOut = false;

        //checkIn date
        imgCustSearchCheckIn.setOnClickListener(view1 -> {
            //showDatePickerDialog(view);
            checkOut = false;
            checkIn = true;
            openDatePicker(view1);
        });
        //checkout date
        imgCustSearchCheckOut.setOnClickListener(v -> {
            checkIn = false;
            checkOut = true;
            openDatePicker(view);
        });

        //add rooms
        imgCustSearchAddRooms.setOnClickListener(v -> {
            int numberOfRooms = Integer.parseInt(txvCustSearchRooms.getText().toString());
            numberOfRooms += 1;
            txvCustSearchRooms.setText(String.valueOf(numberOfRooms));
        });

        imgCustSearchMinusRooms.setOnClickListener(v -> {
            int numberOfRooms = Integer.parseInt(txvCustSearchRooms.getText().toString());
            if(!(numberOfRooms == 1)){
                numberOfRooms -= 1;
            }
            txvCustSearchRooms.setText(String.valueOf(numberOfRooms));
        });

        //add guests
        imgCustSearchAddGuest.setOnClickListener(v -> {
            int numberOfGuests = Integer.parseInt(txvCustSearchGuests.getText().toString());
            numberOfGuests += 1;
            txvCustSearchGuests.setText(String.valueOf(numberOfGuests));
        });

        imgCustSearchMinusGuest.setOnClickListener(v -> {
            int numberOfGuests = Integer.parseInt(txvCustSearchGuests.getText().toString());
            if(!(numberOfGuests == 1)){
                numberOfGuests -= 1;
            }
            txvCustSearchGuests.setText(String.valueOf(numberOfGuests));
        });

        //Search Button
        btnCustSearchSearch.setOnClickListener(v -> {
            //checking if all the required fields are provided or not
            boolean flag = false;
            String checkInDate = txvCustSearchCheckIn.getText().toString().trim();
            String checkOutDate = txvCustSearchCheckOut.getText().toString().trim();
            String noOfRooms = txvCustSearchRooms.getText().toString().trim();
            String noOfGuests = txvCustSearchGuests.getText().toString().trim();
            String searchText = edtCustSearchSearch.getText().toString().trim();

            if(searchText.isEmpty()){
                edtCustSearchSearch.setError("Area/Hotel/City is required.");
                edtCustSearchSearch.requestFocus();
                flag = true;
            }

            if(checkInDate.isEmpty() || checkOutDate.isEmpty()){
                Toast.makeText(getContext(), "CheckIn and CheckOut Date are required.", Toast.LENGTH_SHORT).show();
                flag = true;
            }

            if(searchText.length() < 3 && !searchText.isEmpty()){
                edtCustSearchSearch.setError("Please enter at least 3 characters.");
                edtCustSearchSearch.requestFocus();
                flag = true;
            }

            if(flag){
                return;
            }

            //Orders newOrder = new Orders(FirebaseAuth.getInstance().getUid(), null, checkInDate, checkOutDate, noOfRooms, noOfGuests, null, null);

            if(getDateDiffResult(checkInDate,checkOutDate)){
                Intent intent = new Intent(getActivity(),SearchResultActivity.class);
                //intent.putExtra("Order", (Parcelable) newOrder);
                intent.putExtra("searchText",searchText);
                intent.putExtra("checkInDate",checkInDate);
                intent.putExtra("checkOutDate",checkOutDate);
                intent.putExtra("noOfRooms",noOfRooms);
                intent.putExtra("noOfGuests",noOfGuests);
                startActivity(intent);
            }else{
                Toast.makeText(getContext(), "CheckOut should be greater than CheckIn date.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    //Date picker
    private void initDatePicker()
    {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                month = month + 1;
                String date = makeDateString(day, month, year);
                //dateButton.setText(date);
                if(checkIn){
                    txvCustSearchCheckIn.setText(date);
                }else if(checkOut){
                    txvCustSearchCheckOut.setText(date);
                }
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(getContext(), style, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-86400000);
    }

    private String makeDateString(int day, int month, int year)
    {
        return getMonthFormat(month) + " " + day + ", " + year;
    }

    private String getMonthFormat(int month)
    {
        if(month == 1)
            return "Jan";
        if(month == 2)
            return "Feb";
        if(month == 3)
            return "Mar";
        if(month == 4)
            return "Apr";
        if(month == 5)
            return "May";
        if(month == 6)
            return "Jun";
        if(month == 7)
            return "Jul";
        if(month == 8)
            return "Aug";
        if(month == 9)
            return "Sep";
        if(month == 10)
            return "Oct";
        if(month == 11)
            return "Nov";
        if(month == 12)
            return "Dec";

        //default should never happen
        return "Jan";
    }

    public void openDatePicker(View view)
    {
        datePickerDialog.show();
    }

    public boolean getDateDiffResult(String bookCheckInDate, String bookCheckOutDate){
        SimpleDateFormat DateFor = new SimpleDateFormat("MMM dd, yyyy");
        Date localCheckInDate = null;
        Date localCheckOutDate = null;
        try{
            localCheckInDate = DateFor.parse(bookCheckInDate);
        }catch (ParseException e) {e.printStackTrace();}
        try{
            localCheckOutDate = DateFor.parse(bookCheckOutDate);
        }catch (ParseException e) {e.printStackTrace();}
        long diffInMillies = localCheckOutDate.getTime() - localCheckInDate.getTime();
        //Toast.makeText(getContext(), "DAYS:"+ TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS), Toast.LENGTH_SHORT).show();
        if((TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS)) > 0){
            return true;
        }else{
            return false;
        }
    }
}