package com.srikanta.mypg.hostels;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.helpers.PricingDialogHelper;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    public static final String EXTRA_HOSTEL_ID = "hostelId";

    private String hostelId;

    private CardView cardPricing;
    private CardView cardFacilities; // future use

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1️⃣ Get hostelId
        hostelId = getIntent().getStringExtra(EXTRA_HOSTEL_ID);

        if (hostelId == null || hostelId.isEmpty()) {
            Toast.makeText(this, "Hostel not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClicks();
    }

    private void initViews() {
        cardPricing = findViewById(R.id.btnPricing);
        cardFacilities = findViewById(R.id.btnFacilities);
    }

    private void setupClicks() {

        // 💰 Pricing
        cardPricing.setOnClickListener(v -> openPricing());

        // 🏢 Facilities (future)
        cardFacilities.setOnClickListener(v ->
                openFacilities());
    }

    private void openFacilities() {
        Intent intent = new Intent(this, FacilitiesActivity.class);
        intent.putExtra(FacilitiesActivity.EXTRA_HOSTEL_ID, hostelId);
        startActivity(intent);
    }


    /* ================= PRICING ================= */

    private void openPricing() {
        new PricingDialogHelper(this, hostelId)
                .openPricing();
    }


    /* ================= SHARING ROW ================= */



    /* ================= SAVE ================= */


}
