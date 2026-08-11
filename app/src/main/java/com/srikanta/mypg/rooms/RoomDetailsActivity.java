package com.srikanta.mypg.rooms;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.TenantAdapter;

import java.util.ArrayList;
import java.util.List;

public class RoomDetailsActivity extends AppCompatActivity {

    // ================= UI =================
    private TextView tvRoomTitle, tvFloorInfo, tvBedsInfo;
    private RecyclerView rvTenants;

    // ================= FIREBASE =================
    private DatabaseReference rootRef;

    // ================= INTENT =================
    private String hostelId;
    private int floorNo, roomNo;

    // ================= DATA =================
    private int totalBeds = 0;
    private int occupiedBeds = 0;

    private final List<String> tenantIds = new ArrayList<>();
    private TenantAdapter tenantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_room_details);

        getIntentData();
        initViews();

        rootRef = FirebaseDatabase.getInstance().getReference();

        loadRoomDetails();
        loadTenants();

    }

    // ================= INTENT =================
    private void getIntentData() {
        hostelId = getIntent().getStringExtra("hostelId");
        floorNo = getIntent().getIntExtra("floorNo", -1);
        roomNo = getIntent().getIntExtra("roomNo", -1);

        if (hostelId == null || floorNo == -1 || roomNo == -1) {
            Toast.makeText(this, "Invalid room details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ================= INIT =================
    private void initViews() {
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvFloorInfo = findViewById(R.id.tvFloorInfo);
        tvBedsInfo = findViewById(R.id.tvBedsInfo);

        rvTenants = findViewById(R.id.rvTenants);
        rvTenants.setLayoutManager(new LinearLayoutManager(this));

        tenantAdapter = new TenantAdapter(tenantIds, hostelId);
        rvTenants.setAdapter(tenantAdapter);

        tvRoomTitle.setText("Room " + roomNo);
        tvFloorInfo.setText("Floor " + floorNo);
    }

    // ================= LOAD ROOM =================
    private void loadRoomDetails() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("rooms")
                .child(String.valueOf(floorNo))
                .child(String.valueOf(roomNo))
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) return;

                        Integer beds = snapshot.child("totalBeds").getValue(Integer.class);
                        Integer occupied = snapshot.child("occupiedBeds").getValue(Integer.class);

                        totalBeds = beds == null ? 0 : beds;
                        occupiedBeds = occupied == null ? 0 : occupied;

                        int freeBeds = totalBeds - occupiedBeds;

                        tvBedsInfo.setText(
                                "Beds: " + totalBeds + " | Free: " + freeBeds
                        );

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ================= LOAD TENANTS =================
    private void loadTenants() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("tenants")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        tenantIds.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Integer floor =
                                    snap.child("floorNo").getValue(Integer.class);
                            Integer room =
                                    snap.child("roomNo").getValue(Integer.class);

                            // ✅ tenantId = node key
                            String tenantId = snap.getKey();

                            if (tenantId == null || floor == null || room == null)
                                continue;

                            // ✅ only tenants of THIS room
                            if (floor == floorNo && room == roomNo) {
                                tenantIds.add(tenantId);
                            }
                        }

                        tenantAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

}
