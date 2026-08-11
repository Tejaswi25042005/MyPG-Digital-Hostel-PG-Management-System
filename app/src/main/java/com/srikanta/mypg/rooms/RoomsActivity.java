package com.srikanta.mypg.rooms;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.FloorAdapter;
import com.srikanta.mypg.adapters.RoomAdapter;
import com.srikanta.mypg.helpers.BaseOwnerActivity;
import com.srikanta.mypg.helpers.FloorHelper;
import com.srikanta.mypg.helpers.HostelStatsHelper;
import com.srikanta.mypg.helpers.RoomHelper;
import com.srikanta.mypg.helpers.TenantHelper;
import com.srikanta.mypg.models.RoomModel;

import java.util.ArrayList;
import java.util.List;

public class RoomsActivity extends BaseOwnerActivity {

    // ================= UI =================
    private TextView tvHostelName;
    private TextView tvTotalRooms, tvTotalTenants, tvAvailableRooms, tvAvailableBeds;
    private RecyclerView rvFloors, rvRooms;
    private androidx.cardview.widget.CardView cardAddFloor;

    // ================= FIREBASE =================
    private DatabaseReference rootRef;
    private String hostelId;

    // ================= STATE =================
    private int selectedFloorNo = -1;
    private int lastRequestedFloorNo = -1; // 🔧 FIX

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rooms);

        hostelId = getIntent().getStringExtra("hostelId");
        if (hostelId == null || hostelId.isEmpty()) {
            Toast.makeText(this, "Invalid hostel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rootRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRecyclerViews();

        loadHostelName();
        loadFloors();
    }

    // ================= INIT =================
    private void initViews() {
        tvHostelName = findViewById(R.id.tvHostelName);
        tvTotalRooms = findViewById(R.id.tvTotalRooms);
        tvTotalTenants = findViewById(R.id.tvTotalTenants);
        tvAvailableRooms = findViewById(R.id.tvAvailableRooms);
        tvAvailableBeds = findViewById(R.id.tvAvailableBeds);

        rvFloors = findViewById(R.id.rvFloors);
        rvRooms = findViewById(R.id.rvRooms);

        cardAddFloor = findViewById(R.id.cardAddFloor);
        cardAddFloor.setOnClickListener(v -> showAddFloorDialog());
    }

    private void setupRecyclerViews() {
        rvFloors.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
    }

    // ================= HOSTEL =================
    private void loadHostelName() {
        rootRef.child("Hostels")
                .child(hostelId)
                .child("info")
                .child("pgName")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        tvHostelName.setText(snapshot.getValue(String.class));
                    }
                });
    }

    // ================= TENANTS =================
    private void observeTenantsForFloor(int floorNo) {

        lastRequestedFloorNo = floorNo; // 🔧 FIX

        TenantHelper.observeTenantsByFloor(
                rootRef,
                hostelId,
                floorNo,
                count -> {
                    if (floorNo == lastRequestedFloorNo) { // 🔧 FIX
                        tvTotalTenants.setText(String.valueOf(count));
                    }
                }
        );
    }

    // ================= FLOORS =================
    private void loadFloors() {

        FloorHelper.loadFloors(
                rootRef,
                hostelId,
                floors -> {

                    FloorAdapter adapter =
                            new FloorAdapter(
                                    floors,
                                    floor -> {

                                        selectedFloorNo = floor.floorNo;

                                        resetStatsUI(); // 🔧 FIX

                                        loadRoomsByFloor(selectedFloorNo);
                                        observeStatsForFloor(selectedFloorNo);
                                        observeTenantsForFloor(selectedFloorNo);
                                    }
                            );

                    rvFloors.setAdapter(adapter);

                    if (!floors.isEmpty()) {

                        selectedFloorNo = floors.get(0).floorNo;

                        resetStatsUI(); // 🔧 FIX

                        loadRoomsByFloor(selectedFloorNo);
                        observeStatsForFloor(selectedFloorNo);
                        observeTenantsForFloor(selectedFloorNo);
                    }
                }
        );
    }

    // ================= FLOOR STATS =================
    private void observeStatsForFloor(int floorNo) {

        lastRequestedFloorNo = floorNo; // 🔧 FIX

        HostelStatsHelper.observeStatsByFloor(
                rootRef,
                hostelId,
                floorNo,
                (totalRooms, freeRooms, freeBeds) -> {

                    if (floorNo != lastRequestedFloorNo) return; // 🔧 FIX

                    tvTotalRooms.setText(String.valueOf(totalRooms));
                    tvAvailableRooms.setText(String.valueOf(freeRooms));
                    tvAvailableBeds.setText(String.valueOf(freeBeds));
                }
        );
    }

    // ================= ROOMS =================
    private void loadRoomsByFloor(int floorNo) {

        RoomHelper.loadRoomsByFloor(
                rootRef,
                hostelId,
                floorNo,
                displayList -> {

                    RoomAdapter adapter =
                            new RoomAdapter(
                                    displayList,
                                    new RoomAdapter.RoomClickListener() {

                                        @Override
                                        public void onAddRoomClick() {
                                            showAddRoomDialog(floorNo);
                                        }

                                        @Override
                                        public void onRoomClick(RoomModel room) {

                                            android.content.Intent intent =
                                                    new android.content.Intent(
                                                            RoomsActivity.this,
                                                            RoomDetailsActivity.class
                                                    );

                                            intent.putExtra("hostelId", hostelId);
                                            intent.putExtra("floorNo", selectedFloorNo);
                                            intent.putExtra("roomNo", room.getRoomNo());

                                            startActivity(intent);
                                        }

                                    }
                            );

                    rvRooms.setAdapter(adapter);
                }
        );
    }

    // ================= ADD FLOOR =================
    private void showAddFloorDialog() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("floors")
                .get()
                .addOnSuccessListener(snapshot -> {

                    int nextFloor = (int) snapshot.getChildrenCount() + 1;

                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Add Floor")
                            .setMessage("Add Floor " + nextFloor + "?")
                            .setPositiveButton(
                                    "Add",
                                    (d, w) -> {
                                        FloorHelper.addFloor(this, rootRef, hostelId, nextFloor);
                                        loadFloors(); // 🔧 FIX
                                    }
                            )
                            .setNegativeButton("Cancel", null)
                            .show();
                });
    }

    // ================= ADD ROOM =================
    private void showAddRoomDialog(int floorNo) {

        DatabaseReference hostelRef = rootRef
                .child("Hostels")
                .child(hostelId);

        hostelRef.child("rooms")
                .child(String.valueOf(floorNo))
                .get()
                .addOnSuccessListener(roomSnap -> {

                    int roomNo =
                            (floorNo * 100) +
                                    ((int) roomSnap.getChildrenCount() + 1);

                    // 🔹 Fetch sharing options from pricing
                    hostelRef.child("pricing")
                            .child("sharingPrices")
                            .get()
                            .addOnSuccessListener(priceSnap -> {

                                if (!priceSnap.exists()) {
                                    Toast.makeText(
                                            this,
                                            "Please add pricing first",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                List<Integer> sharingList = new ArrayList<>();

                                for (DataSnapshot s : priceSnap.getChildren()) {
                                    try {
                                        sharingList.add(
                                                Integer.parseInt(s.getKey())
                                        );
                                    } catch (NumberFormatException ignored) {}
                                }

                                if (sharingList.isEmpty()) {
                                    Toast.makeText(
                                            this,
                                            "No sharing prices found",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                // 🔹 Spinner
                                Spinner spinner = new Spinner(this);

                                ArrayAdapter<Integer> adapter =
                                        new ArrayAdapter<>(
                                                this,
                                                android.R.layout.simple_spinner_item,
                                                sharingList
                                        );

                                adapter.setDropDownViewResource(
                                        android.R.layout.simple_spinner_dropdown_item
                                );

                                spinner.setAdapter(adapter);
                                spinner.setPadding(40, 30, 40, 30);

                                AlertDialog dialog =
                                        new AlertDialog.Builder(this)
                                                .setTitle("Add Room " + roomNo)
                                                .setMessage(
                                                        "Select sharing (beds)"
                                                )
                                                .setView(spinner)
                                                .setPositiveButton("Add", null)
                                                .setNegativeButton(
                                                        "Cancel", null
                                                )
                                                .show();

                                dialog.getButton(
                                        AlertDialog.BUTTON_POSITIVE
                                ).setOnClickListener(v -> {

                                    int beds =
                                            (int) spinner.getSelectedItem();

                                    // ✅ Add room
                                    RoomHelper.addRoom(
                                            this,
                                            rootRef,
                                            hostelId,
                                            floorNo,
                                            roomNo,
                                            beds
                                    );

                                    dialog.dismiss();

                                    // 🔄 Refresh UI
                                    loadRoomsByFloor(floorNo);
                                    observeStatsForFloor(floorNo);
                                });
                            });
                });
    }

    // ================= UI RESET =================
    private void resetStatsUI() { // 🔧 FIX
        tvTotalRooms.setText("0");
        tvAvailableRooms.setText("0");
        tvAvailableBeds.setText("0");
        tvTotalTenants.setText("0");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (selectedFloorNo != -1) {
            resetStatsUI(); // 🔧 FIX
            loadRoomsByFloor(selectedFloorNo);
            observeStatsForFloor(selectedFloorNo);
            observeTenantsForFloor(selectedFloorNo);
        }
    }
}
