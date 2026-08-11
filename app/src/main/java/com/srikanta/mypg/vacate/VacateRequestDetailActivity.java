package com.srikanta.mypg.vacate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.srikanta.mypg.R;
import com.srikanta.mypg.models.VacateRequestModel;

public class VacateRequestDetailActivity extends AppCompatActivity {

    private String hostelId, requestId;

    private TextView tvAvatar, tvTenantName, tvRoom, tvStatus,
            tvVacateDate, tvPaidTill, tvAmountInfo, tvReason;

    private Button btnApprove, btnReject;
    private LinearLayout layoutButtons;

    private DatabaseReference vacateRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vacate_request_detail);

        // ✅ Receive BOTH
        hostelId = getIntent().getStringExtra("hostelId");
        requestId = getIntent().getStringExtra("requestId");

        if (hostelId == null || requestId == null) {
            Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        // ✅ Correct path
        vacateRef = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("vacateRequests")
                .child(requestId);

        loadData();
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvTenantName = findViewById(R.id.tvTenantName);
        tvRoom = findViewById(R.id.tvRoom);
        tvStatus = findViewById(R.id.tvStatus);
        tvVacateDate = findViewById(R.id.tvVacateDate);
        tvPaidTill = findViewById(R.id.tvPaidTill);
        tvAmountInfo = findViewById(R.id.tvAmountInfo);
        tvReason = findViewById(R.id.tvReason);

        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        layoutButtons = findViewById(R.id.layoutButtons);
    }

    private void loadData() {

        vacateRef.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                showOwnerInitiateDialog();
                return;
            }


            VacateRequestModel model = snapshot.getValue(VacateRequestModel.class);
            if (model == null) return;

            // Avatar
            if (model.getName() != null && !model.getName().isEmpty()) {
                tvAvatar.setText(model.getName().substring(0, 1).toUpperCase());
            } else {
                tvAvatar.setText("?");
            }

            tvTenantName.setText(model.getName());
            tvRoom.setText("Room No: " + model.getRoomNo());
            tvVacateDate.setText("Vacate Date: " + model.getVacateDate());
            tvPaidTill.setText("Paid Till: " + model.getPaidTill());
            tvReason.setText(model.getReason());

            // Amount + Waiver Info
            if (model.getTotalDue() > 0) {

                String text = "Due Amount: ₹" + model.getTotalDue();

                if ("WAIVE_OFF".equals(model.getDueAction())) {
                    text += "  (CAN BE WAIVE OFF)";
                }

                tvAmountInfo.setText(text);

            } else if (model.getRefundAmount() > 0) {

                tvAmountInfo.setText("Refund: ₹" + model.getRefundAmount());

            } else {
                tvAmountInfo.setText("No dues / no refund");
            }


            // Status
            String status = model.getStatus();
            tvStatus.setText(status);

            if ("PENDING".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_orange_stroke);
                layoutButtons.setVisibility(View.VISIBLE);
            } else {
                layoutButtons.setVisibility(View.GONE);

                if ("APPROVED".equals(status)) {
                    tvStatus.setBackgroundResource(R.drawable.bg_green_stroke);
                }
            }

            // Clicks
            btnApprove.setOnClickListener(v -> updateStatus("APPROVED"));
            btnReject.setOnClickListener(v -> updateStatus("REJECTED"));

        });
    }

    private void showOwnerInitiateDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Vacate Not Requested")
                .setMessage(
                        "Tenant has not raised a vacate request.\n\n" +
                                "You can initiate the vacate process from owner side."
                )
                .setNegativeButton("Cancel", (d, w) -> finish())
                .setPositiveButton("Continue", (d, w) -> showFinalOwnerConfirmDialog())
                .setCancelable(false)
                .show();
    }

    private void showFinalOwnerConfirmDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Start Owner Vacate")
                .setMessage(
                        "You are about to initiate vacate and calculate settlement.\n\n" +
                                "Proceed to settlement screen?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (d, w) -> openSettlementScreen())
                .setCancelable(false)
                .show();
    }

    private void openSettlementScreen() {

        android.content.Intent intent =
                new android.content.Intent(
                        VacateRequestDetailActivity.this,
                        SettlementActivity.class
                );

        intent.putExtra("hostelId", hostelId);
        intent.putExtra("tenantId", requestId);
        intent.putExtra("mode", "OWNER");  // important

        startActivity(intent);
        finish();
    }


    private void updateStatus(String newStatus) {

        vacateRef.child("status").setValue(newStatus)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}