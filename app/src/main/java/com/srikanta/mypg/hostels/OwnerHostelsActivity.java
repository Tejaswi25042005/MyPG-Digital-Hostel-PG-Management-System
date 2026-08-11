package com.srikanta.mypg.hostels;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.OwnerHostelsAdapter;
import com.srikanta.mypg.models.OwnerHostelModel;

import java.util.ArrayList;
import java.util.List;

public class OwnerHostelsActivity extends AppCompatActivity
        implements OwnerHostelsAdapter.HostelClickListener {

    private RecyclerView rvHostels;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAddHostel;

    private OwnerHostelsAdapter adapter;
    private final List<OwnerHostelModel> hostelList = new ArrayList<>();

    private FirebaseAuth auth;
    private DatabaseReference ownerRef;
    private DatabaseReference ownerHostelsRef;
    private DatabaseReference hostelsRootRef;

    private String ownerId;
    private String defaultHostelId;   // ⭐ IMPORTANT

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_owner_hostels);

        initViews();
        initFirebase();
        setupRecycler();
        loadOwnerData();
        setupFab();
    }

    // ================= INIT =================
    private void initViews() {
        rvHostels = findViewById(R.id.rvHostels);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAddHostel = findViewById(R.id.fabAddHostel);
    }

    private void initFirebase() {

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        ownerId = auth.getCurrentUser().getUid();

        ownerRef = FirebaseDatabase.getInstance()
                .getReference("Owners")
                .child(ownerId);

        ownerHostelsRef = ownerRef.child("hostels");

        hostelsRootRef = FirebaseDatabase.getInstance()
                .getReference("Hostels");
    }

    private void setupRecycler() {
        rvHostels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OwnerHostelsAdapter(hostelList, this);
        rvHostels.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddHostel.setOnClickListener(v ->
                startActivity(new Intent(
                        OwnerHostelsActivity.this,
                        RaiseHostelRequestActivity.class))
        );
    }

    // ================= LOAD OWNER DATA =================
    private void loadOwnerData() {

        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                defaultHostelId =
                        snapshot.child("defaultHostelId")
                                .getValue(String.class);

                loadOwnerHostels();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadOwnerHostels();
            }
        });
    }

    // ================= LOAD HOSTELS =================
    private void loadOwnerHostels() {

        ownerHostelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                hostelList.clear();

                for (DataSnapshot hostelSnap : snapshot.getChildren()) {

                    OwnerHostelModel model = new OwnerHostelModel();

                    String hostelId =
                            hostelSnap.child("hostelId")
                                    .getValue(String.class);

                    model.setHostelId(hostelId);
                    model.setName(
                            hostelSnap.child("hostelName")
                                    .getValue(String.class)
                    );
                    model.setAddress(
                            hostelSnap.child("address")
                                    .getValue(String.class)
                    );

                    model.setStatus("active");

                    // ⭐ DEFAULT HOSTEL FLAG
                    model.setDefault(
                            hostelId != null && hostelId.equals(defaultHostelId)
                    );

                    // Default stats
                    model.setRoomsCount(0);
                    model.setBedsCount(0);
                    model.setOccupiedCount(0);

                    hostelList.add(model);

                    // Load stats async
                    loadHostelStats(model);
                }

                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        OwnerHostelsActivity.this,
                        "Failed to load hostels",
                        Toast.LENGTH_SHORT
                ).show();
                updateEmptyState();
            }
        });
    }

    // ================= STATS =================
    private void loadHostelStats(OwnerHostelModel model) {

        if (model.getHostelId() == null) return;

        hostelsRootRef.child(model.getHostelId())
                .child("rooms")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int rooms = 0;
                        int totalBeds = 0;
                        int occupiedBeds = 0;

                        for (DataSnapshot floorSnap : snapshot.getChildren()) {
                            for (DataSnapshot roomSnap : floorSnap.getChildren()) {

                                rooms++;

                                Integer beds =
                                        roomSnap.child("totalBeds")
                                                .getValue(Integer.class);

                                Integer occupied =
                                        roomSnap.child("occupiedBeds")
                                                .getValue(Integer.class);

                                if (beds != null) totalBeds += beds;
                                if (occupied != null) occupiedBeds += occupied;
                            }
                        }

                        model.setRoomsCount(rooms);
                        model.setBedsCount(totalBeds);
                        model.setOccupiedCount(occupiedBeds);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ================= EMPTY STATE =================
    private void updateEmptyState() {
        if (hostelList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvHostels.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvHostels.setVisibility(View.VISIBLE);
        }
    }

    // ================= CLICK =================
    @Override
    public void onHostelClick(OwnerHostelModel hostel) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("hostelId", hostel.getHostelId());
        startActivity(intent);
    }

    @Override
    public void onSetDefaultClick(OwnerHostelModel hostel) {

        if (hostel == null || hostel.getHostelId() == null) return;

        // 1️⃣ Update database
        ownerRef.child("defaultHostelId")
                .setValue(hostel.getHostelId())
                .addOnSuccessListener(unused -> {

                    // 2️⃣ Update UI locally
                    defaultHostelId = hostel.getHostelId();

                    for (OwnerHostelModel model : hostelList) {
                        model.setDefault(
                                model.getHostelId().equals(defaultHostelId)
                        );
                    }

                    adapter.notifyDataSetChanged();

                    Toast.makeText(
                            this,
                            "Default hostel updated",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to set default hostel",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

}
