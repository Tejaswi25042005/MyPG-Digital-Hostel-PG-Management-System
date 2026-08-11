package com.srikanta.mypg.vacate;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.TenantPaymentAdapter;
import com.srikanta.mypg.models.TenantPaymentModel;

import java.text.SimpleDateFormat;
import java.util.*;

public class SettlementActivity extends AppCompatActivity {

    private String hostelId, tenantId;

    // UI
    private TextView tvTenantName, tvTenantMobile, tvJoinedDate;
    private TextView tvRoomDetails, tvMonthlyRent, tvDeposit;

    private TextView tvTotalPaid, tvTotalDue, tvTotalMonths;

    private CardView cardVacateDate;
    private EditText etVacateDate;

    private TextView tvJoinedInfo, tvPaidTillInfo, tvVacateInfo,
            tvVacateStatus, tvRefundInfo, tvOwnerNote;

    private Button btnSubmit;

    private RecyclerView rvPayments;

    // DATA
    private DatabaseReference rootRef;
    private final List<TenantPaymentModel> payments = new ArrayList<>();

    private long monthlyRent = 0;
    private long deposit = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settlement);

        hostelId = getIntent().getStringExtra("hostelId");
        tenantId = getIntent().getStringExtra("tenantId");

        if (hostelId == null || tenantId == null) {
            finish();
            return;
        }

        rootRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupToolbar();
        loadTenant();
        loadPayments();
        setupDatePicker();

        btnSubmit.setOnClickListener(v -> createOwnerVacateRequest());
    }

    private void initViews() {

        tvTenantName = findViewById(R.id.tvTenantName);
        tvTenantMobile = findViewById(R.id.tvTenantMobile);
        tvJoinedDate = findViewById(R.id.tvJoinedDate);

        tvRoomDetails = findViewById(R.id.tvRoomDetails);
        tvMonthlyRent = findViewById(R.id.tvMonthlyRent);
        tvDeposit = findViewById(R.id.tvDeposit);

        tvTotalPaid = findViewById(R.id.tvTotalPaid);
        tvTotalDue = findViewById(R.id.tvTotalDue);
        tvTotalMonths = findViewById(R.id.tvTotalMonths);

        cardVacateDate = findViewById(R.id.cardVacateDate);
        etVacateDate = findViewById(R.id.etVacateDate);

        tvJoinedInfo = findViewById(R.id.tvJoinedInfo);
        tvPaidTillInfo = findViewById(R.id.tvPaidTillInfo);
        tvVacateInfo = findViewById(R.id.tvVacateInfo);
        tvVacateStatus = findViewById(R.id.tvVacateStatus);
        tvRefundInfo = findViewById(R.id.tvRefundInfo);
        tvOwnerNote = findViewById(R.id.tvOwnerNote);

        btnSubmit = findViewById(R.id.btnSubmitVacate);

        rvPayments = findViewById(R.id.rvVacatePayments);
        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        rvPayments.setAdapter(new TenantPaymentAdapter(payments));
    }

    private void setupToolbar() {
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
    }

    // ================= LOAD TENANT =================

    private void loadTenant() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("tenants")
                .child(tenantId)
                .get()
                .addOnSuccessListener(snap -> {

                    if (!snap.exists()) return;

                    String name = snap.child("name").getValue(String.class);
                    String mobile = snap.child("mobile").getValue(String.class);

                    Long room = snap.child("roomNo").getValue(Long.class);
                    Long floor = snap.child("floorNo").getValue(Long.class);
                    Long rent = snap.child("rent").getValue(Long.class);
                    Long dep = snap.child("deposit").getValue(Long.class);
                    String assignedAt = snap.child("assignedAt").getValue(String.class);

                    monthlyRent = rent == null ? 0 : rent;
                    deposit = dep == null ? 0 : dep;

                    tvTenantName.setText(name);
                    tvTenantMobile.setText("Mobile: " + mobile);

                    tvRoomDetails.setText("Room " + room + " • Floor " + floor);
                    tvMonthlyRent.setText("Rent: ₹" + monthlyRent);
                    tvDeposit.setText("Deposit: ₹" + deposit);

                    tvJoinedDate.setText("Joined on: " + formatDate(assignedAt));

                    tvJoinedInfo.setText("Joined On: " + formatDate(assignedAt));
                });
    }

    // ================= PAYMENTS =================

    private void loadPayments() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("tenants")
                .child(tenantId)
                .child("payments")
                .get()
                .addOnSuccessListener(snapshot -> {

                    payments.clear();

                    long totalPaid = 0;
                    long totalDue = 0;

                    for (DataSnapshot m : snapshot.getChildren()) {

                        Long rentPaid = m.child("rentPaid").getValue(Long.class);
                        Long rentDue = m.child("rentDue").getValue(Long.class);

                        totalPaid += rentPaid == null ? 0 : rentPaid;
                        totalDue += rentDue == null ? 0 : rentDue;

                        payments.add(new TenantPaymentModel(
                                m.getKey(),
                                rentPaid == null ? 0 : rentPaid.intValue(),
                                0,
                                "",
                                ""
                        ));
                    }

                    tvTotalPaid.setText("₹" + totalPaid);
                    tvTotalDue.setText("₹" + totalDue);
                    tvTotalMonths.setText(String.valueOf(payments.size()));

                    rvPayments.getAdapter().notifyDataSetChanged();

                    cardVacateDate.setVisibility(View.VISIBLE);
                });
    }

    // ================= DATE PICKER =================

    private void setupDatePicker() {

        etVacateDate.setOnClickListener(v -> {

            Calendar c = Calendar.getInstance();

            new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {

                        Calendar sel = Calendar.getInstance();
                        sel.set(y, m, d);

                        String date = new SimpleDateFormat(
                                "dd MMM yyyy",
                                Locale.getDefault()
                        ).format(sel.getTime());

                        etVacateDate.setText(date);

                        tvVacateInfo.setText("Vacate Date: " + date);
                        tvVacateStatus.setText("Settlement will be based on this date");
                        tvVacateStatus.setTextColor(Color.parseColor("#424242"));

                        btnSubmit.setVisibility(View.VISIBLE);

                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    // ================= CREATE REQUEST =================

    private void createOwnerVacateRequest() {

        String vacateDate = etVacateDate.getText().toString();

        if (vacateDate.isEmpty()) {
            Toast.makeText(this, "Select date", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reqRef =
                rootRef.child("Hostels")
                        .child(hostelId)
                        .child("vacateRequests")
                        .child(tenantId);

        reqRef.child("tenantId").setValue(tenantId);
        reqRef.child("vacateDate").setValue(vacateDate);
        reqRef.child("status").setValue("APPROVED"); // owner initiated
        reqRef.child("requestedAt").setValue(System.currentTimeMillis());

        Toast.makeText(this, "Vacate created", Toast.LENGTH_LONG).show();
        finish();
    }

    private String formatDate(String raw) {
        try {
            return new SimpleDateFormat("dd MMM yyyy", Locale.US)
                    .format(new SimpleDateFormat("ddMMyyyyHHmm", Locale.US).parse(raw));
        } catch (Exception e) {
            return "--";
        }
    }
}
