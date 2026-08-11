package com.srikanta.mypg.tenants;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.TenantPaymentAdapter;
import com.srikanta.mypg.helpers.revenue.MonthHelper;
import com.srikanta.mypg.models.TenantPaymentModel;
import com.srikanta.mypg.vacate.VacateRequestDetailActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TenantDetailActivity extends AppCompatActivity {

    // ================= UI =================
    private TextView tvName, tvMobile, tvRoom;
    private TextView tvMonthlyRent, tvDeposit;
    private RecyclerView rvPaymentHistory;

    // ================= DATA =================
    private String hostelId, tenantId;
    private DatabaseReference rootRef;
    private final List<TenantPaymentModel> paymentList = new ArrayList<>();
    private TenantPaymentAdapter paymentAdapter;

    private Button vacant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tenant_detail);

        hostelId = getIntent().getStringExtra("hostelId");
        tenantId = getIntent().getStringExtra("tenantId");

        if (hostelId == null || tenantId == null) {
            Toast.makeText(this, "Invalid tenant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rootRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRecycler();
        loadTenantDetails();
        loadPaymentHistory(); // placeholder

        vacant.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TenantDetailActivity.this,
                    VacateRequestDetailActivity.class
            );

            intent.putExtra("hostelId", hostelId);
            intent.putExtra("requestId", tenantId); // request stored by tenantId

            startActivity(intent);
        });



    }

    // ================= INIT =================
    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvMobile = findViewById(R.id.tvMobile);
        tvRoom = findViewById(R.id.tvRoom);
        tvMonthlyRent = findViewById(R.id.tvMonthlyRent);
        tvDeposit = findViewById(R.id.tvDeposit);
        rvPaymentHistory = findViewById(R.id.rvPaymentHistory);
        vacant = findViewById(R.id.btnVacate);
    }

    private void setupRecycler() {
        rvPaymentHistory.setLayoutManager(new LinearLayoutManager(this));
        paymentAdapter = new TenantPaymentAdapter(paymentList);
        rvPaymentHistory.setAdapter(paymentAdapter);
    }

    // ================= LOAD TENANT =================
    private void loadTenantDetails() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("tenants")
                .child(tenantId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snap) {

                        if (!snap.exists()) {
                            Toast.makeText(
                                    TenantDetailActivity.this,
                                    "Tenant not found",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                            return;
                        }

                        String name = snap.child("name").getValue(String.class);
                        String mobile = snap.child("mobile").getValue(String.class);
                        Integer floor = snap.child("floorNo").getValue(Integer.class);
                        Integer room = snap.child("roomNo").getValue(Integer.class);
                        Integer rent = snap.child("rent").getValue(Integer.class);
                        Integer deposit = snap.child("deposit").getValue(Integer.class);

                        tvName.setText(name == null ? "" : name);
                        tvMobile.setText(mobile == null ? "" : mobile);

                        tvRoom.setText(
                                "Floor " + (floor == null ? 0 : floor) +
                                        " • Room " + (room == null ? 0 : room)
                        );

                        tvMonthlyRent.setText("₹" + (rent == null ? 0 : rent));
                        tvDeposit.setText("₹" + (deposit == null ? 0 : deposit));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(
                                TenantDetailActivity.this,
                                "Failed to load tenant",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void loadPaymentHistory() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("tenants")
                .child(tenantId)
                .child("payments")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        paymentList.clear();

                        for (DataSnapshot monthSnap : snapshot.getChildren()) {

                            String monthKey = monthSnap.getKey(); // yyyy-MM
                            if (monthKey == null) continue;

                            int rentPaid =
                                    monthSnap.child("rentPaid").getValue(Integer.class) == null
                                            ? 0 : monthSnap.child("rentPaid").getValue(Integer.class);

                            int depositPaid =
                                    monthSnap.child("depositPaid").getValue(Integer.class) == null
                                            ? 0 : monthSnap.child("depositPaid").getValue(Integer.class);

                            String paidOn =
                                    monthSnap.child("paidOn").getValue(String.class);

                            String paidOnDisplay = "";

                            if (paidOn != null) {
                                try {
                                    Date d = new SimpleDateFormat(
                                            "ddMMyyyyHHmm",
                                            Locale.getDefault()
                                    ).parse(paidOn);

                                    paidOnDisplay = new SimpleDateFormat(
                                            "dd MMM yyyy",
                                            Locale.getDefault()
                                    ).format(d);

                                } catch (Exception ignored) {}
                            }

                            // ✅ ONE MODEL PER MONTH
                            paymentList.add(
                                    new TenantPaymentModel(
                                            monthKey,
                                            rentPaid,
                                            depositPaid,
                                            paidOn,
                                            paidOnDisplay
                                    )
                            );
                        }

                        Collections.sort(paymentList, (a, b) -> {
                            String p1 = a.getPaidOn();
                            String p2 = b.getPaidOn();

                            if (p1 == null && p2 == null) return 0;
                            if (p1 == null) return 1;
                            if (p2 == null) return -1;

                            return p2.compareTo(p1); // newest first
                        });


                        paymentAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(
                                TenantDetailActivity.this,
                                "Failed to load payment history",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }



}
