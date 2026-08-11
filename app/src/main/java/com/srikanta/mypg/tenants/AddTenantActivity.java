package com.srikanta.mypg.tenants;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.AvailableRoomAdapter;
import com.srikanta.mypg.helpers.tenants.TenantActionHelper;
import com.srikanta.mypg.helpers.tenants.TenantAssignHelper;
import com.srikanta.mypg.helpers.tenants.TenantPaymentHelper;
import com.srikanta.mypg.models.RoomModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTenantActivity extends AppCompatActivity {

    // -------- UI --------
    private ImageView ivBack;
    private EditText etName, etEmail, etMobile;
    private Spinner spinnerSharing;
    private TextView tvRent, tvDepositRefund;
    private RecyclerView rvRooms;
    private CardView btnAddTenant;

    // -------- DATA --------
    private final List<RoomModel> roomList = new ArrayList<>();
    private AvailableRoomAdapter adapter;
    private RoomModel selectedRoom;

    // -------- PRICING --------
    private int deposit = 0, rent = 0, deduction = 0, refund = 0;

    // -------- FIREBASE --------
    private DatabaseReference rootRef;
    private String hostelId;

    // -------- JOINING DATE --------
    private TextView tvJoiningDate;
    private long joiningDateMillis = 0L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_tenant);

        hostelId = getIntent().getStringExtra("hostelId");
        rootRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRooms();
        loadPricing();

        ivBack.setOnClickListener(v -> finish());
        btnAddTenant.setOnClickListener(v -> validateAndConfirm());

        tvJoiningDate.setOnClickListener(v -> {

            Calendar c = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    AddTenantActivity.this,
                    (view, year, month, dayOfMonth) -> {

                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth, 0, 0, 0);

                        joiningDateMillis = selected.getTimeInMillis();

                        String formatted = new SimpleDateFormat(
                                "dd MMM yyyy", Locale.getDefault()
                        ).format(selected.getTime());

                        tvJoiningDate.setText(formatted);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );

            // ❌ Prevent past dates (recommended)
            //dialog.getDatePicker().setMinDate(System.currentTimeMillis());

            dialog.show();
        });

    }

    // ================= INIT =================
    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etName = findViewById(R.id.etTenantName);
        etEmail = findViewById(R.id.etTenantEmail);
        etMobile = findViewById(R.id.etTenantMobile);
        spinnerSharing = findViewById(R.id.spinnerSharing);
        tvRent = findViewById(R.id.tvCalculatedRent);
        tvDepositRefund = findViewById(R.id.tvDepositRefund);
        rvRooms = findViewById(R.id.rvAvailableRooms);
        btnAddTenant = findViewById(R.id.btnAddTenant);
        tvJoiningDate = findViewById(R.id.tvJoiningDate);

    }

    private void setupRooms() {
        rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new AvailableRoomAdapter(roomList, room -> selectedRoom = room);
        rvRooms.setAdapter(adapter);
    }

    // ================= PRICING =================
    private void loadPricing() {
        rootRef.child("Hostels").child(hostelId).child("pricing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {

                        if (!snap.exists() || !snap.hasChild("sharingPrices")) {
                            Toast.makeText(AddTenantActivity.this,
                                    "Pricing not available", Toast.LENGTH_SHORT).show();
                            return;
                        }


                        deposit = snap.child("deposit").getValue(Integer.class);
                        deduction = snap.child("deduction").getValue(Integer.class);
                        refund = Math.max(deposit - deduction, 0);

                        List<String> sharingList = new ArrayList<>();
                        for (DataSnapshot s : snap.child("sharingPrices").getChildren()) {
                            sharingList.add(s.getKey());
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(AddTenantActivity.this,
                                        android.R.layout.simple_spinner_item, sharingList);

                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        spinnerSharing.setAdapter(adapter);

                        updateRentAndRooms(sharingList.get(0));

                        spinnerSharing.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(
                                            AdapterView<?> parent, View view,
                                            int position, long id) {
                                        updateRentAndRooms(sharingList.get(position));
                                    }

                                    @Override public void onNothingSelected(AdapterView<?> parent) { }
                                });
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void updateRentAndRooms(String sharing) {
        rootRef.child("Hostels").child(hostelId)
                .child("pricing").child("sharingPrices").child(sharing)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        rent = snap.getValue(Integer.class);
                        tvRent.setText("Rent: ₹" + rent);
                        tvDepositRefund.setText(
                                "Deposit: ₹" + deposit + " | Refund: ₹" + refund);
                        loadAvailableRooms(Integer.parseInt(sharing));
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // ================= ROOMS =================
    private void loadAvailableRooms(int sharing) {
        rootRef.child("Hostels").child(hostelId).child("rooms")
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

                                if (room.getTotalBeds() == sharing &&
                                        room.getOccupiedBeds() < room.getTotalBeds()) {
                                    roomList.add(room);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // ================= VALIDATION =================
    private void validateAndConfirm() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();

        if (name.isEmpty()) { etName.setError("Required"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email required"); return;
        }
        if (mobile.length() != 10) {
            etMobile.setError("Valid mobile required"); return;
        }
        if (selectedRoom == null) {
            Toast.makeText(this, "Select a room", Toast.LENGTH_SHORT).show();
            return;
        }

        showConfirmDialog(name, email, mobile);
    }

    // ================= CONFIRM =================
    private void showConfirmDialog(String name, String email, String mobile) {
        String msg =
                "Name: " + name + "\n" +
                        "Room: " + selectedRoom.getRoomNo() + "\n" +
                        "Floor: " + selectedRoom.getFloorNo() + "\n\n" +
                        "Rent: ₹" + rent + "\n" +
                        "Deposit: ₹" + deposit + "\n" +
                        "Refund on exit: ₹" + refund + "\n\n" +
                        "Confirm tenant addition?";

        new AlertDialog.Builder(this)
                .setTitle("Confirm Tenant")
                .setMessage(msg)
                .setPositiveButton("ADD", (d, w) ->
                        createTenantAuth(name, email, mobile))
                .setNegativeButton("CANCEL", null)
                .show();
    }

    // ================= SECOND AUTH =================
    private FirebaseAuth getSecondaryAuth() {

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey(getString(R.string.google_api_key))
                .setApplicationId(getString(R.string.google_app_id))
                .setDatabaseUrl(getString(R.string.firebase_database_url))
                .build();

        FirebaseApp app;
        try {
            app = FirebaseApp.initializeApp(this, options, "SecondaryAuth");
        } catch (IllegalStateException e) {
            app = FirebaseApp.getInstance("SecondaryAuth");
        }

        return FirebaseAuth.getInstance(app);
    }

    // ================= CREATE TENANT AUTH =================
    private void createTenantAuth(String name, String email, String mobile) {

        FirebaseAuth secondaryAuth = getSecondaryAuth();
        String password = "Tenant@" + mobile.substring(6);

        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String tenantId = result.getUser().getUid();
                    secondaryAuth.signOut();

                    // ✅ USE SELECTED DATE OR TODAY
                    long effectiveDate =
                            joiningDateMillis == 0
                                    ? System.currentTimeMillis()
                                    : joiningDateMillis;

                    String assignedAt = new SimpleDateFormat(
                            "ddMMyyyyHHmm",
                            Locale.getDefault()
                    ).format(new Date(effectiveDate));

                    String joinedMonth = new SimpleDateFormat(
                            "yyyy-MM",
                            Locale.getDefault()
                    ).format(new Date(effectiveDate));


                    TenantAssignHelper.assignTenant(
                            rootRef,
                            hostelId,
                            tenantId,
                            name,
                            mobile,
                            selectedRoom,
                            spinnerSharing.getSelectedItem().toString(),
                            rent,
                            deposit,
                            assignedAt,
                            joinedMonth,
                            new TenantAssignHelper.Callback() {

                                @Override
                                public void onSuccess(String a, String j) {

                                    // 1️⃣ Payment
                                    TenantPaymentHelper.createJoiningPayment(
                                            rootRef,
                                            hostelId,
                                            tenantId,
                                            j,
                                            rent,
                                            deposit
                                    );

                                    // 2️⃣ Action
                                    TenantActionHelper.logTenantAdded(
                                            rootRef,
                                            hostelId,
                                            tenantId,
                                            name,
                                            selectedRoom.getRoomNo(),
                                            selectedRoom.getFloorNo(),
                                            a
                                    );

                                    // 3️⃣ Profile
                                    DatabaseReference userRef =
                                            rootRef.child("Users").child(tenantId);

                                    userRef.child("Profile/status").setValue("ASSIGNED");
                                    userRef.child("Profile/createdAt").setValue(a);
                                    userRef.child("Profile/createdAtMillis").setValue(effectiveDate);
                                    userRef.child("Profile/email").setValue(email);
                                    userRef.child("Profile/mobile").setValue(mobile);
                                    userRef.child("Profile/name").setValue(name);

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
                                    userRef.child("hostel/sharing")
                                            .setValue(Integer.parseInt(
                                                    spinnerSharing.getSelectedItem().toString()));
                                    userRef.child("hostel/rent").setValue(rent);
                                    userRef.child("hostel/deposit").setValue(deposit);
                                    userRef.child("hostel/status").setValue("ACTIVE");
                                    userRef.child("hostel/assignedAt").setValue(a);
                                    userRef.child("hostel/joinedMonth").setValue(j);
                                    userRef.child("hostel/rentPaidTill")
                                            .setValue(getMonthEndMillis(j));

                                    Toast.makeText(
                                            AddTenantActivity.this,
                                            "Tenant added & assigned successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    finish();
                                }

                                @Override
                                public void onFailure(String reason) {
                                    Toast.makeText(
                                            AddTenantActivity.this,
                                            reason,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                    );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private long getMonthEndMillis(String month) {
        try {
            String[] p = month.split("-");
            int year = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);

            Calendar c = Calendar.getInstance();
            c.set(year, m - 1, 1, 23, 59, 59);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            c.set(Calendar.MILLISECOND, 999);

            return c.getTimeInMillis();

        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }



}
