package com.srikanta.mypg.hostels;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;

public class FacilitiesActivity extends AppCompatActivity {

    public static final String EXTRA_HOSTEL_ID = "hostelId";

    private String hostelId;
    private DatabaseReference facilitiesRef;

    private SwitchMaterial swWifi, swCctv, swPower, swParking, swHotWater,
            swLaundry, swAc, swFridge, swLockers, swStudyHall, swGym, swLift;

    private boolean isLoading = true; // 🔑 IMPORTANT

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facilities);

        hostelId = getIntent().getStringExtra(EXTRA_HOSTEL_ID);
        if (hostelId == null) {
            finish();
            return;
        }

        facilitiesRef = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("facilities");

        initViews();
        loadFacilities();
        setupAutoSave();
    }

    /* ================= INIT ================= */

    private void initViews() {
        swWifi = findViewById(R.id.swWifi);
        swCctv = findViewById(R.id.swCctv);
        swPower = findViewById(R.id.swPower);
        swParking = findViewById(R.id.swParking);
        swHotWater = findViewById(R.id.swHotWater);
        swLaundry = findViewById(R.id.swLaundry);
        swAc = findViewById(R.id.swAc);
        swFridge = findViewById(R.id.swFridge);
        swLockers = findViewById(R.id.swLockers);
        swStudyHall = findViewById(R.id.swStudyHall);
        swGym = findViewById(R.id.swGym);
        swLift = findViewById(R.id.swLift);
    }

    /* ================= LOAD ================= */

    private void loadFacilities() {
        facilitiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                setSwitch(swWifi, snapshot, "wifi");
                setSwitch(swCctv, snapshot, "cctv");
                setSwitch(swPower, snapshot, "powerBackup");
                setSwitch(swParking, snapshot, "parking");
                setSwitch(swHotWater, snapshot, "hotWater");
                setSwitch(swLaundry, snapshot, "laundry");
                setSwitch(swAc, snapshot, "ac");
                setSwitch(swFridge, snapshot, "fridge");
                setSwitch(swLockers, snapshot, "lockers");
                setSwitch(swStudyHall, snapshot, "studyHall");
                setSwitch(swGym, snapshot, "gym");
                setSwitch(swLift, snapshot, "lift");

                isLoading = false; // 🔓 enable saving
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setSwitch(SwitchMaterial sw, DataSnapshot snap, String key) {
        Boolean val = snap.child(key).getValue(Boolean.class);
        sw.setChecked(val != null && val);
    }

    /* ================= AUTO SAVE ================= */

    private void setupAutoSave() {

        bind(swWifi, "wifi");
        bind(swCctv, "cctv");
        bind(swPower, "powerBackup");
        bind(swParking, "parking");
        bind(swHotWater, "hotWater");
        bind(swLaundry, "laundry");
        bind(swAc, "ac");
        bind(swFridge, "fridge");
        bind(swLockers, "lockers");
        bind(swStudyHall, "studyHall");
        bind(swGym, "gym");
        bind(swLift, "lift");
    }

    private void bind(SwitchMaterial sw, String key) {
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isLoading) return; // 🚫 ignore during initial load

            facilitiesRef.child(key).setValue(isChecked)
                    .addOnSuccessListener(v ->
                            Toast.makeText(
                                    FacilitiesActivity.this,
                                    key + " updated",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        });
    }
}
