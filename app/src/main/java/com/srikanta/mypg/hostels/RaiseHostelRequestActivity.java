package com.srikanta.mypg.hostels;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.srikanta.mypg.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RaiseHostelRequestActivity extends AppCompatActivity {

    // UI
    private EditText etHostelName, etPgType;
    private TextView tvAddress;
    private ImageView ivPickLocation;
    private Button btnSubmitRequest;

    // Location
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private String selectedAddress = "";

    private static final int LOCATION_REQ_CODE = 101;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference requestRef;

    // Location
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_raise_hostel_request);

        initViews();
        initFirebase();
        setupClicks();
    }

    /* ================= INIT ================= */

    private void initViews() {
        etHostelName = findViewById(R.id.etHostelName);
        etPgType = findViewById(R.id.etPgType);
        tvAddress = findViewById(R.id.tvAddress);
        ivPickLocation = findViewById(R.id.ivPickLocation);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        btnSubmitRequest.setEnabled(false);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        requestRef = FirebaseDatabase.getInstance()
                .getReference("OwnerRequests");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupClicks() {
        ivPickLocation.setOnClickListener(v -> checkLocationPermission());
        btnSubmitRequest.setOnClickListener(v -> submitRequest());
    }

    /* ================= LOCATION PERMISSION ================= */

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
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
                    this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) return;

            googleMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

                LatLng start;

                if (location != null) {
                    start = new LatLng(
                            location.getLatitude(),
                            location.getLongitude());

                    googleMap.addMarker(new MarkerOptions().position(start));
                    googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(start, 16f));

                    selectedLat = start.latitude;
                    selectedLng = start.longitude;
                    selectedAddress = getFullAddress(start);
                    tvDialogAddress.setText(selectedAddress);
                }

                googleMap.setOnMapClickListener(latLng -> {
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(latLng));

                    selectedLat = latLng.latitude;
                    selectedLng = latLng.longitude;
                    selectedAddress = getFullAddress(latLng);
                    tvDialogAddress.setText(selectedAddress);
                });
            });

            etSearchPlace.addTextChangedListener(new TextWatcher() {

                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (s.length() < 2) return;

                    Geocoder geocoder =
                            new Geocoder(RaiseHostelRequestActivity.this,
                                    Locale.getDefault());

                    try {
                        List<Address> list =
                                geocoder.getFromLocationName(s.toString(), 1);

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

        btnConfirm.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(selectedAddress)) {
                tvAddress.setText(selectedAddress);
                btnSubmitRequest.setEnabled(true);
                dialog.dismiss();
            }
        });

        dialog.show();

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
                    geocoder.getFromLocation(
                            latLng.latitude, latLng.longitude, 1);

            if (list != null && !list.isEmpty()) {
                return list.get(0).getAddressLine(0);
            }
        } catch (IOException ignored) {}
        return "Address not available";
    }

    /* ================= SUBMIT ================= */

    private void submitRequest() {

        String pgName = etHostelName.getText().toString().trim();
        String pgType = etPgType.getText().toString().trim();

        if (TextUtils.isEmpty(pgName)
                || TextUtils.isEmpty(pgType)
                || TextUtils.isEmpty(selectedAddress)) {

            Toast.makeText(this,
                    "Please fill all details",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this,
                    "User not authenticated",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = user.getUid();

        DatabaseReference ownerProfileRef =
                FirebaseDatabase.getInstance()
                        .getReference("Owners")
                        .child(ownerId)
                        .child("profile");

        ownerProfileRef.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                Toast.makeText(this,
                        "Owner profile not found",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ GET FROM OWNERS NODE
            String fullName = snapshot.child("name").getValue(String.class);
            String mobile = snapshot.child("mobile").getValue(String.class);
            String email = snapshot.child("email").getValue(String.class);

            if (TextUtils.isEmpty(mobile)) {
                Toast.makeText(this,
                        "Mobile number missing in profile",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ TIMESTAMP
            String timestamp = new SimpleDateFormat(
                    "ddMMyyyyHHmm",
                    Locale.getDefault()
            ).format(new java.util.Date());

            // ---------- ADDRESS ----------
            HashMap<String, Object> addressMap = new HashMap<>();
            addressMap.put("address", selectedAddress);
            addressMap.put("latitude", selectedLat);
            addressMap.put("longitude", selectedLng);

            // ---------- DETAILS ----------
            HashMap<String, Object> detailsMap = new HashMap<>();
            detailsMap.put("fullName", fullName);
            detailsMap.put("mobile", mobile);
            detailsMap.put("email", email);
            detailsMap.put("pgName", pgName);
            detailsMap.put("pgType", pgType);
            detailsMap.put("status", "pending");
            detailsMap.put("timestamp", timestamp);
            detailsMap.put("address", addressMap);

            // ---------- SAVE ----------
            requestRef.child(mobile)
                    .setValue(detailsMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this,
                                "Hostel request submitted successfully",
                                Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );

        }).addOnFailureListener(e ->
                Toast.makeText(this,
                        e.getMessage(),
                        Toast.LENGTH_SHORT).show()
        );
    }

}
