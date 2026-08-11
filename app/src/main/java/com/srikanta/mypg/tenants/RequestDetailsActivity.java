package com.srikanta.mypg.tenants;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.AvailableRoomAdapter;
import com.srikanta.mypg.helpers.tenants.TenantActionHelper;
import com.srikanta.mypg.helpers.tenants.TenantAssignHelper;
import com.srikanta.mypg.helpers.tenants.TenantPaymentHelper;
import com.srikanta.mypg.models.RoomModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestDetailsActivity extends AppCompatActivity {

    // -------- INTENT --------
    private String tenantId, hostelId, sharing;

    // -------- UI --------
    private TextView tvTenantName, tvTenantMobile, tvSharingRent;
    private RecyclerView rvRooms;
    private Button btnAssign;

    // -------- DATA --------
    private final List<RoomModel> roomList = new ArrayList<>();
    private AvailableRoomAdapter adapter;
    private RoomModel selectedRoom;

    // -------- TENANT --------
    private String tenantName = "", tenantMobile = "";

    // -------- PRICING --------
    private int deposit = 0, deduction = 0, calculatedRent = 0;

    // -------- FIREBASE --------
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_details);

        tenantId = getIntent().getStringExtra("tenantId");
        hostelId = getIntent().getStringExtra("hostelId");
        sharing = getIntent().getStringExtra("sharing");

        rootRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        loadTenantDetails();
        loadPricing();
        loadAvailableRooms();

        btnAssign.setOnClickListener(v -> showConfirmationDialog());
    }

    // ================= UI =================
    private void initViews() {
        tvTenantName = findViewById(R.id.tvTenantName);
        tvTenantMobile = findViewById(R.id.tvTenantMobile);
        tvSharingRent = findViewById(R.id.tvSharingRent);

        rvRooms = findViewById(R.id.rvAvailableRooms);
        btnAssign = findViewById(R.id.btnAssign);

        rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new AvailableRoomAdapter(roomList, room -> selectedRoom = room);
        rvRooms.setAdapter(adapter);
    }

    // ================= TENANT =================
    private void loadTenantDetails() {
        rootRef.child("Users")
                .child(tenantId)
                .child("Profile")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (!snap.exists()) return;

                        tenantName = snap.child("name").getValue(String.class);
                        tenantMobile = snap.child("mobile").getValue(String.class);

                        tvTenantName.setText(tenantName);
                        tvTenantMobile.setText("📞 " + tenantMobile);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // ================= PRICING =================
    private void loadPricing() {
        rootRef.child("Hostels")
                .child(hostelId)
                .child("pricing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (!snap.exists()) return;

                        deposit = snap.child("deposit").getValue(Integer.class);
                        deduction = snap.child("deduction").getValue(Integer.class);
                        calculatedRent = snap.child("sharingPrices")
                                .child(sharing)
                                .getValue(Integer.class);

                        tvSharingRent.setText(
                                sharing + " Sharing · ₹" + calculatedRent
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // ================= ROOMS =================
    private void loadAvailableRooms() {

        int requiredBeds = Integer.parseInt(sharing);

        rootRef.child("Hostels")
                .child(hostelId)
                .child("rooms")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        roomList.clear();

                        for (DataSnapshot floorSnap : snapshot.getChildren()) {
                            int floorNo = Integer.parseInt(floorSnap.getKey());

                            for (DataSnapshot roomSnap : floorSnap.getChildren()) {
                                RoomModel room = roomSnap.getValue(RoomModel.class);
                                if (room == null) continue;

                                room.setFloorNo(floorNo);

                                int freeBeds =
                                        room.getTotalBeds() - room.getOccupiedBeds();

                                if (room.getTotalBeds() == requiredBeds && freeBeds > 0) {
                                    roomList.add(room);
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // ================= CONFIRM =================
    private void showConfirmationDialog() {

        if (selectedRoom == null) {
            Toast.makeText(this, "Select a room", Toast.LENGTH_SHORT).show();
            return;
        }

        int refund = Math.max(deposit - deduction, 0);

        String msg =
                "Tenant: " + tenantName + "\n" +
                        "Room: " + selectedRoom.getRoomNo() + "\n" +
                        "Floor: " + selectedRoom.getFloorNo() + "\n\n" +
                        "Rent: ₹" + calculatedRent + "\n" +
                        "Deposit: ₹" + deposit + "\n" +
                        "Refund: ₹" + refund + "\n\n" +
                        "Confirm assignment?";

        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage(msg)
                .setPositiveButton("CONFIRM", (d, w) -> assignRoom())
                .setNegativeButton("CANCEL", null)
                .show();
    }

    // ================= ASSIGN =================
    private void assignRoom() {

        // ✅ today's date for request approval
        long nowMillis = System.currentTimeMillis();

        String assignedAt = new SimpleDateFormat(
                "ddMMyyyyHHmm",
                Locale.getDefault()
        ).format(new Date(nowMillis));

        String joinedMonth = new SimpleDateFormat(
                "MMM-yyyy",
                Locale.getDefault()
        ).format(new Date(nowMillis));

        TenantAssignHelper.assignTenant(
                rootRef,
                hostelId,
                tenantId,
                tenantName,
                tenantMobile,
                selectedRoom,
                sharing,
                calculatedRent,
                deposit,
                assignedAt,
                joinedMonth,
                new TenantAssignHelper.Callback() {

                    @Override
                    public void onSuccess(String a, String j) {

                        TenantPaymentHelper.createJoiningPayment(
                                rootRef,
                                hostelId,
                                tenantId,
                                j,
                                calculatedRent,
                                deposit
                        );

                        TenantActionHelper.logTenantAdded(
                                rootRef,
                                hostelId,
                                tenantId,
                                tenantName,
                                selectedRoom.getRoomNo(),
                                selectedRoom.getFloorNo(),
                                a
                        );

                        rootRef.child("HostelRequests")
                                .child(hostelId)
                                .child(tenantId)
                                .removeValue();

                        DatabaseReference userRef = rootRef
                                .child("Users")
                                .child(tenantId);

                        userRef.child("Profile/status").setValue("ASSIGNED");
                        userRef.child("Profile/createdAt").setValue(a);
                        userRef.child("Profile/createdAtMillis").setValue(nowMillis);

                        rootRef.child("Hostels")
                                .child(hostelId)
                                .child("info")
                                .child("pgName")
                                .get()
                                .addOnSuccessListener(snap -> {

                                    String hostelName = snap.getValue(String.class);

                                    userRef.child("hostel/hostelName")
                                            .setValue(hostelName != null ? hostelName : "");
                                });

                        userRef.child("hostel/hostelId").setValue(hostelId);
                        userRef.child("hostel/roomNo").setValue(selectedRoom.getRoomNo());
                        userRef.child("hostel/floorNo").setValue(selectedRoom.getFloorNo());
                        userRef.child("hostel/sharing").setValue(Integer.parseInt(sharing));
                        userRef.child("hostel/rent").setValue(calculatedRent);
                        userRef.child("hostel/deposit").setValue(deposit);
                        userRef.child("hostel/status").setValue("ACTIVE");

                        userRef.child("hostel/assignedAt").setValue(a);
                        userRef.child("hostel/joinedMonth").setValue(j);

                        Toast.makeText(
                                RequestDetailsActivity.this,
                                "Tenant assigned successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onFailure(String reason) {
                        Toast.makeText(
                                RequestDetailsActivity.this,
                                reason,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


}
