package com.srikanta.mypg.auth;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.srikanta.mypg.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RequestRegisterActivity extends AppCompatActivity {

    // UI
    private EditText etFullName, etEmail, etPgName, etPgType;
    private TextView tvMobile, tvAddress;
    private ImageView imgAddLocation;
    private Button btnSubmit;

    // Location
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private String selectedAddress = "";

    private static final int LOCATION_REQ_CODE = 101;

    // Firebase
    private DatabaseReference requestRef;

    // Location
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        tvMobile = findViewById(R.id.tvMobile);
        etPgName = findViewById(R.id.etPgName);
        etPgType = findViewById(R.id.etPgType);
        tvAddress = findViewById(R.id.tvAddress);
        imgAddLocation = findViewById(R.id.imgAddLocation);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setEnabled(false);

        requestRef = FirebaseDatabase.getInstance().getReference("OwnerRequests");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Mobile from Login dialog
        String mobile = getIntent().getStringExtra("mobile");
        if (mobile != null) {
            tvMobile.setText(mobile);
        }

        imgAddLocation.setOnClickListener(v -> checkLocationPermission());
        btnSubmit.setOnClickListener(v -> submitRequest());
    }

    /* ================= LOCATION PERMISSION ================= */

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ_CODE
            );
        } else {
            openLocationDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQ_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            openLocationDialog();
        } else {
            Toast.makeText(this,
                    "Location permission required",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /* ================= LOCATION DIALOG ================= */

    private void openLocationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_pick_location);
        dialog.setCancelable(true);

        MapView mapView = dialog.findViewById(R.id.dialogMapView);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmLocation);
        EditText etSearchPlace = dialog.findViewById(R.id.etSearchPlace);
        TextView tvDialogAddress = dialog.findViewById(R.id.tvDialogAddress);

        mapView.onCreate(null);
        mapView.onResume();

        mapView.getMapAsync(googleMap -> {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            googleMap.setMyLocationEnabled(true);

            // 📍 Default: current location pinned
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

                LatLng startLatLng;

                if (location != null) {
                    startLatLng = new LatLng(
                            location.getLatitude(),
                            location.getLongitude());

                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(startLatLng));

                    selectedLat = startLatLng.latitude;
                    selectedLng = startLatLng.longitude;
                    selectedAddress = getFullAddress(startLatLng);
                    tvDialogAddress.setText(selectedAddress);

                } else {
                    startLatLng = new LatLng(20.5937, 78.9629); // India fallback
                    tvDialogAddress.setText("Unable to fetch current location");
                }

                googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(startLatLng, 16f));

                // 👆 Tap on map
                googleMap.setOnMapClickListener(latLng -> {
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(latLng));

                    selectedLat = latLng.latitude;
                    selectedLng = latLng.longitude;
                    selectedAddress = getFullAddress(latLng);
                    tvDialogAddress.setText(selectedAddress);
                });
            });

            // 🔍 Live search (character-based)
            etSearchPlace.addTextChangedListener(new TextWatcher() {

                private long lastSearchTime = 0;

                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    String query = s.toString().trim();
                    if (query.length() < 2) return;

                    long now = System.currentTimeMillis();
                    if (now - lastSearchTime < 700) return; // throttle
                    lastSearchTime = now;

                    Geocoder geocoder = new Geocoder(
                            RequestRegisterActivity.this, Locale.getDefault());

                    try {
                        List<Address> list = geocoder.getFromLocationName(query, 1);
                        if (list != null && !list.isEmpty()) {

                            Address addr = list.get(0);
                            LatLng latLng = new LatLng(
                                    addr.getLatitude(),
                                    addr.getLongitude());

                            googleMap.clear();
                            googleMap.addMarker(
                                    new MarkerOptions().position(latLng));
                            googleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(latLng, 15f));

                            selectedLat = latLng.latitude;
                            selectedLng = latLng.longitude;
                            selectedAddress = getFullAddress(latLng);
                            tvDialogAddress.setText(selectedAddress);
                        }
                    } catch (IOException ignored) {}
                }
            });
        });

        // ✅ Confirm location
        btnConfirm.setOnClickListener(v -> {
            if (selectedLat != 0.0 && !TextUtils.isEmpty(selectedAddress)) {
                tvAddress.setText(selectedAddress);
                btnSubmit.setEnabled(true);
                dialog.dismiss();
            } else {
                Toast.makeText(this,
                        "Please select location",
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

        // 📐 Full-width dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }


    /* ================= ADDRESS ================= */

    private String getFullAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> list =
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (list != null && !list.isEmpty()) {
                Address address = list.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i != address.getMaxAddressLineIndex()) sb.append(", ");
                }
                return sb.toString();
            }
        } catch (IOException ignored) {}
        return "Address not available";
    }

    /* ================= SUBMIT REQUEST ================= */

    private void submitRequest() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = tvMobile.getText().toString().trim();
        String pgName = etPgName.getText().toString().trim();
        String pgType = etPgType.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(mobile)
                || TextUtils.isEmpty(pgName)
                || TextUtils.isEmpty(pgType)
                || TextUtils.isEmpty(selectedAddress)) {

            Toast.makeText(this,
                    "Please fill all details",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Mobile as key
        String requestId = mobile;

        // 📍 Address object
        HashMap<String, Object> addressMap = new HashMap<>();
        addressMap.put("address", selectedAddress);
        addressMap.put("latitude", selectedLat);
        addressMap.put("longitude", selectedLng);

        // ⏱️ Custom timestamp format: ddMMyyyyHHmm
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault());
        String customTimestamp = sdf.format(new java.util.Date());

        HashMap<String, Object> map = new HashMap<>();
        map.put("fullName", name);
        map.put("email", email);
        map.put("mobile", mobile);
        map.put("pgName", pgName);
        map.put("pgType", pgType);
        map.put("address", addressMap);
        map.put("status", "pending");
        map.put("timestamp", customTimestamp);

        requestRef.child(requestId).setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Request submitted successfully",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

}