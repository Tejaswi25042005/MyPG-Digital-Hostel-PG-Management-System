package com.srikanta.mypg.revenue;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.TenantRevenueAdapter;
import com.srikanta.mypg.helpers.revenue.MonthHelper;
import com.srikanta.mypg.helpers.revenue.RevenuePdfHelper;
import com.srikanta.mypg.models.TenantRevenueModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RevenueActivity extends AppCompatActivity {

    // ---------- UI ----------
    private TextView tvMonth, tvExpectedAmount, tvCollectedAmount,
            tvPendingAmount, tvPaidTenants;
    private ImageView btnPrevMonth, btnNextMonth, btnCalendar;

    private RecyclerView rv;
    private TenantRevenueAdapter adapter;
    private final List<TenantRevenueModel> list = new ArrayList<>();

    // ---------- DATA ----------
    private String hostelId;
    private String currentMonthKey;
    private final Calendar calendar = Calendar.getInstance();

    private String hostelName = "";
    private String hostelAddress = "";

    private FloatingActionButton fabDownload;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_revenue);

        hostelId = getIntent().getStringExtra("hostelId");
        if (hostelId == null) {
            Toast.makeText(this, "Hostel not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadHostelInfo();
        refresh();
    }

    // ================= INIT =================
    private void initViews() {

        tvMonth = findViewById(R.id.tvMonth);
        tvExpectedAmount = findViewById(R.id.tvExpectedAmount);
        tvCollectedAmount = findViewById(R.id.tvCollectedAmount);
        tvPendingAmount = findViewById(R.id.tvPendingAmount);
        tvPaidTenants = findViewById(R.id.tvPaidTenants);

        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnCalendar = findViewById(R.id.btnCalendar);

        rv = findViewById(R.id.rvTenantRevenue);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TenantRevenueAdapter(
                list,
                hostelId,
                this::refresh
        );
        rv.setAdapter(adapter);

        btnPrevMonth.setOnClickListener(v -> moveMonth(-1));
        btnNextMonth.setOnClickListener(v -> moveMonth(1));
        btnCalendar.setOnClickListener(v -> openMonthYearDialog());

        fabDownload = findViewById(R.id.fabDownload);

        fabDownload.setOnClickListener(v -> {
            if (list.isEmpty()) {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
                return;
            }

            RevenuePdfHelper.generateAndSavePdf(
                    this,
                    hostelName,
                    hostelAddress,
                    MonthHelper.getMonthText(currentMonthKey),
                    currentMonthKey,
                    tvExpectedAmount.getText().toString(),
                    tvCollectedAmount.getText().toString(),
                    tvPendingAmount.getText().toString(),
                    tvPaidTenants.getText().toString(),
                    list
            );
        });

    }

    // ================= HOSTEL INFO =================
    private void loadHostelInfo() {

        FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("info")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snap) {

                        if (!snap.exists()) return;

                        hostelName = snap.child("pgName").getValue(String.class);

                        // ✅ address is a STRING (not object)
                        hostelAddress = snap.child("address").getValue(String.class);

                        if (hostelName == null) hostelName = "";
                        if (hostelAddress == null) hostelAddress = "";
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }


    // ================= MONTH NAV =================
    private void moveMonth(int delta) {
        calendar.add(Calendar.MONTH, delta);
        refresh();
    }

    private void openMonthYearDialog() {

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_month_year, null);

        Spinner spinnerMonth = view.findViewById(R.id.spinnerMonth);
        Spinner spinnerYear = view.findViewById(R.id.spinnerYear);

        String[] months = new java.text.DateFormatSymbols().getMonths();
        List<String> monthList = new ArrayList<>();
        for (int i = 0; i < 12; i++) monthList.add(months[i]);

        spinnerMonth.setAdapter(
                new ArrayAdapter<>(this, R.layout.item_spinner_text, monthList)
        );

        List<Integer> years = new ArrayList<>();
        for (int y = 2025; y <= 2033; y++) years.add(y);

        spinnerYear.setAdapter(
                new ArrayAdapter<>(this, R.layout.item_spinner_text, years)
        );

        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));
        spinnerYear.setSelection(years.indexOf(calendar.get(Calendar.YEAR)));

        new AlertDialog.Builder(this)
                .setTitle("Select Month")
                .setView(view)
                .setPositiveButton("OK", (d, w) -> {
                    calendar.set(Calendar.MONTH,
                            spinnerMonth.getSelectedItemPosition());
                    calendar.set(Calendar.YEAR,
                            (int) spinnerYear.getSelectedItem());
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= REFRESH =================
    private void refresh() {

        list.clear();
        adapter.notifyDataSetChanged();

        currentMonthKey = MonthHelper.getMonthKey(calendar);
        tvMonth.setText(MonthHelper.getMonthText(currentMonthKey));
        adapter.setMonthKey(currentMonthKey);

        // 🔒 BLOCK FAR FUTURE MONTHS
        if (!isAllowedMonth(currentMonthKey)) {
            showEmptyRevenueState();
            return;
        }

        loadRevenueTenantsWithVisibilityCheck();
    }


    private void loadRevenueTenantsWithVisibilityCheck() {

        DatabaseReference tenantRef =
                FirebaseDatabase.getInstance()
                        .getReference("Hostels")
                        .child(hostelId)
                        .child("tenants");

        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                list.clear();

                if (snapshot.exists()) {

                    for (DataSnapshot tenantSnap : snapshot.getChildren()) {

                        String tenantId = tenantSnap.getKey();
                        if (tenantId == null) continue;

                        String joinedMonth =
                                tenantSnap.child("joinedMonth").getValue(String.class);

                        if (!shouldShowTenant(joinedMonth, currentMonthKey)) {
                            continue;
                        }

                        // ---- EXISTING LOGIC (UNCHANGED) ----
                        String name = tenantSnap.child("name").getValue(String.class);
                        int floorNo = getInt(tenantSnap, "floorNo");
                        int roomNo = getInt(tenantSnap, "roomNo");
                        int rent = getInt(tenantSnap, "rent");
                        int deposit = getInt(tenantSnap, "deposit");

                        DataSnapshot paymentSnap =
                                tenantSnap.child("payments").child(currentMonthKey);

                        int rentPaid = 0;
                        int depositPaid = 0;
                        String status = "DUE";

                        if (paymentSnap.exists()) {
                            rentPaid = getInt(paymentSnap, "rentPaid");
                            depositPaid = getInt(paymentSnap, "depositPaid");

                            if (rentPaid >= rent) status = "PAID";
                            else if (rentPaid > 0) status = "PARTIAL";
                        }

                        boolean isNew = currentMonthKey.equals(joinedMonth);
                        String type = isNew ? "NEW" : "REGULAR";

                        list.add(new TenantRevenueModel(
                                tenantId,
                                name,
                                floorNo,
                                roomNo,
                                rent,
                                rentPaid,
                                isNew ? deposit : 0,
                                rentPaid + (isNew ? depositPaid : 0),
                                status,
                                type
                        ));
                    }
                }

                // 🔥 VISIBILITY DECISION
                if (list.isEmpty()) {
                    showEmptyRevenueState();
                } else {
                    adapter.notifyDataSetChanged();
                    updateSummaryFromList();
                    enableFab();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(
                        RevenueActivity.this,
                        "Failed to load tenants",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void disableFab() {
        if (fabDownload == null) return;
        fabDownload.setVisibility(View.GONE);
    }

    private void enableFab() {
        if (fabDownload == null) return;
        fabDownload.setVisibility(View.VISIBLE);
    }


    private void showEmptyRevenueState() {

        list.clear();
        adapter.notifyDataSetChanged();

        tvExpectedAmount.setText("₹0");
        tvCollectedAmount.setText("₹0");
        tvPendingAmount.setText("₹0");
        tvPaidTenants.setText("0 / 0");

        disableFab();
    }



    private boolean shouldShowTenant(String joinedMonth, String selectedMonth) {
        if (joinedMonth == null) return false;

        // yyyy-MM string comparison works perfectly
        return joinedMonth.compareTo(selectedMonth) <= 0;
    }


    // ================= SUMMARY =================
    private void updateSummaryFromList() {

        int expected = 0;
        int collected = 0;
        int paidTenants = 0;
        int totalTenants = 0;

        for (TenantRevenueModel model : list) {

            int rent = model.getRentAmount();
            int rentPaid = model.getRentPaidAmount();
            int deposit = model.getDepositAmount();

            totalTenants++;

            if ("NEW".equalsIgnoreCase(model.getTenantType())) {
                expected += (rent + deposit);
                collected += (rentPaid + deposit);

                if ((rentPaid + deposit) >= (rent + deposit)) {
                    paidTenants++;
                }
            } else {
                expected += rent;
                collected += rentPaid;

                if (rentPaid >= rent) {
                    paidTenants++;
                }
            }
        }

        int pending = Math.max(expected - collected, 0);

        tvExpectedAmount.setText("₹" + expected);
        tvCollectedAmount.setText("₹" + collected);
        tvPendingAmount.setText("₹" + pending);
        tvPaidTenants.setText(paidTenants + " / " + totalTenants);
    }

    // ================= UTIL =================
    private int getInt(DataSnapshot snap, String key) {
        Long v = snap.child(key).getValue(Long.class);
        return v == null ? 0 : v.intValue();
    }

    private Calendar getCurrentMonthCalendar() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        resetTime(c);
        return c;
    }


    private void resetTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private boolean isAllowedMonth(String selectedMonthKey) {

        Calendar selected = MonthHelper.getCalendarFromKey(selectedMonthKey);

        Calendar current = Calendar.getInstance();
        current.set(Calendar.DAY_OF_MONTH, 1);
        resetTime(current);

        Calendar nextMonth = (Calendar) current.clone();
        nextMonth.add(Calendar.MONTH, 1);

        // Allow current month & next month
        if (!selected.after(nextMonth)) {
            return true;
        }

        return false;
    }

}
