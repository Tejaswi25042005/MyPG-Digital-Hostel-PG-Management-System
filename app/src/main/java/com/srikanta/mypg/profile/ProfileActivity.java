package com.srikanta.mypg.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.auth.LoginActivity;
import com.srikanta.mypg.hostels.OwnerHostelsActivity;
import com.srikanta.mypg.notices.NoticeActivity;
import com.srikanta.mypg.menu.MenuActivity;
import com.srikanta.mypg.servicerequests.ServiceRequestActivity;
import com.srikanta.mypg.vacate.VacateRequestActivity;
import com.srikanta.mypg.helpers.PricingDialogHelper;
import com.srikanta.mypg.hostels.FacilitiesActivity;
import com.srikanta.mypg.tenants.RequestActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    // -------- Views --------
    private ImageView imgOwner;
    private TextView tvOwnerName, tvOwnerMobile, tvOwnerEmail;
    private TextView tvHostelCount;

    // subscription
    private TextView tvSubscriptionPlan, tvSubscriptionStatus, tvSubscriptionExpiry;

    private CardView btnManageHostels;

    private View cardNotice, cardFoodMenu, cardService,
            cardVacate, cardPrices, cardFacilities, cardTenantRequests;

    // -------- Firebase --------
    private FirebaseAuth auth;
    private DatabaseReference profileRef, ownerHostelsRef, ownerRef;

    private String ownerId;
    private String hostelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        initViews();
        initFirebase();
        loadOwnerProfile();
        loadHostelCount();
        loadDefaultHostel();
        setupClicks();
    }

    // ================= INIT =================
    private void initViews() {

        imgOwner = findViewById(R.id.imgOwner);

        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvOwnerMobile = findViewById(R.id.tvOwnerMobile);
        tvOwnerEmail = findViewById(R.id.tvOwnerEmail);

        tvHostelCount = findViewById(R.id.tvHostelCount);

        tvSubscriptionPlan = findViewById(R.id.tvSubscriptionPlan);
        tvSubscriptionStatus = findViewById(R.id.tvSubscriptionStatus);
        tvSubscriptionExpiry = findViewById(R.id.tvSubscriptionExpiry);

        btnManageHostels = findViewById(R.id.btnManageHostels);

        cardNotice = findViewById(R.id.cardNotice);
        cardFoodMenu = findViewById(R.id.cardFoodMenu);
        cardService = findViewById(R.id.cardService);
        cardVacate = findViewById(R.id.cardVacate);
        cardPrices = findViewById(R.id.cardPrices);
        cardFacilities = findViewById(R.id.cardFacilities);
        cardTenantRequests = findViewById(R.id.cardTenantRequests);

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void initFirebase() {

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            logout();
            return;
        }

        ownerId = auth.getCurrentUser().getUid();

        ownerRef = FirebaseDatabase.getInstance()
                .getReference("Owners")
                .child(ownerId);

        profileRef = ownerRef.child("profile");
        ownerHostelsRef = ownerRef.child("hostels");
    }

    // ================= DEFAULT HOSTEL =================
    private void loadDefaultHostel() {

        ownerRef.child("defaultHostelId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        hostelId = snapshot.getValue(String.class);
                        loadSubscription(); // load after hostel available
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ================= LOAD OWNER =================
    private void loadOwnerProfile() {

        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                String name = snapshot.child("name").getValue(String.class);
                String mobile = snapshot.child("mobile").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                tvOwnerName.setText(name != null ? name : "Owner");
                tvOwnerMobile.setText(mobile != null ? mobile : "");
                tvOwnerEmail.setText(email != null ? email : "");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileActivity.this,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================= HOSTEL COUNT =================
    private void loadHostelCount() {

        ownerHostelsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tvHostelCount.setText(
                        String.valueOf(snapshot.getChildrenCount())
                );
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tvHostelCount.setText("0");
            }
        });
    }

    // ================= SUBSCRIPTION =================
    private void loadSubscription() {

        if (hostelId == null) return;

        FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("subscription")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        tvSubscriptionPlan.setText("FREE");
                        tvSubscriptionStatus.setText("Inactive");
                        tvSubscriptionExpiry.setText("End: --");
                        return;
                    }

                    String plan = snapshot.child("plan").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String end = snapshot.child("endDate").getValue(String.class);

                    tvSubscriptionPlan.setText(plan == null ? "FREE" : plan);
                    tvSubscriptionStatus.setText(status == null ? "Inactive" : status);
                    tvSubscriptionExpiry.setText(
                            "End: " + (end == null ? "--" : formatDate(end))
                    );


                    // color
                    if ("active".equalsIgnoreCase(status)) {
                        tvSubscriptionStatus.setTextColor(getColor(R.color.green));
                    } else {
                        tvSubscriptionStatus.setTextColor(getColor(R.color.red));
                    }
                });
    }

    // ================= CLICKS =================
    private void setupClicks() {

        btnManageHostels.setOnClickListener(v ->
                startActivity(new Intent(
                        ProfileActivity.this,
                        OwnerHostelsActivity.class))
        );

        cardNotice.setOnClickListener(v -> openWithHostel(NoticeActivity.class));
        cardFoodMenu.setOnClickListener(v -> openWithHostel(MenuActivity.class));
        cardService.setOnClickListener(v -> openWithHostel(ServiceRequestActivity.class));
        cardVacate.setOnClickListener(v -> openWithHostel(VacateRequestActivity.class));

        cardPrices.setOnClickListener(v -> {
            if (hostelId == null) {
                Toast.makeText(this, "Select hostel first", Toast.LENGTH_SHORT).show();
                return;
            }
            new PricingDialogHelper(this, hostelId).openPricing();
        });

        cardFacilities.setOnClickListener(v -> openWithHostel(FacilitiesActivity.class));
        cardTenantRequests.setOnClickListener(v -> openWithHostel(RequestActivity.class));
    }

    private void openWithHostel(Class<?> cls) {

        if (hostelId == null) {
            Toast.makeText(this,
                    "Select hostel first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new Intent(this, cls)
                .putExtra("hostelId", hostelId));
    }

    // ================= LOGOUT =================
    private void logout() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatDate(String raw) {
        try {
            java.text.SimpleDateFormat from =
                    new java.text.SimpleDateFormat("ddMMyyyy", java.util.Locale.getDefault());

            java.text.SimpleDateFormat to =
                    new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault());

            java.util.Date d = from.parse(raw);
            return to.format(d);

        } catch (Exception e) {
            return raw;
        }
    }

}
