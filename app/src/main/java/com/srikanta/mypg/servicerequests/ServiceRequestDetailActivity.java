package com.srikanta.mypg.servicerequests;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.srikanta.mypg.R;
import com.srikanta.mypg.models.ServiceRequestModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServiceRequestDetailActivity extends AppCompatActivity {

    private String hostelId, requestId;

    // UI
    private ImageView ivBack;
    private TextView tvAvatar, tvTenantName, tvRoomInfo;
    private TextView tvStatus, tvIssueTitle, tvCategory, tvDescription, tvCreatedAt;
    private MaterialButton btnAction;

    private DatabaseReference requestRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_request_detail);

        hostelId = getIntent().getStringExtra("HOSTEL_ID");
        requestId = getIntent().getStringExtra("REQUEST_ID");

        if (hostelId == null || requestId == null) {
            finish();
            return;
        }

        requestRef = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("serviceRequests")
                .child(requestId);

        initViews();
        setupClicks();
        loadRequestDetails();
    }

    // ================= INIT =================
    private void initViews() {

        ivBack = findViewById(R.id.ivBack);

        tvAvatar = findViewById(R.id.tvAvatar);
        tvTenantName = findViewById(R.id.tvTenantName);
        tvRoomInfo = findViewById(R.id.tvRoomInfo);

        tvStatus = findViewById(R.id.tvStatus);
        tvIssueTitle = findViewById(R.id.tvIssueTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvDescription = findViewById(R.id.tvDescription);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);

        btnAction = findViewById(R.id.btnAction);
    }

    // ================= CLICKS =================
    private void setupClicks() {
        ivBack.setOnClickListener(v -> finish());
    }

    // ================= LOAD DATA =================
    private void loadRequestDetails() {

        requestRef.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            ServiceRequestModel model =
                    snapshot.getValue(ServiceRequestModel.class);

            if (model == null) return;

            // -------- TENANT --------
            tvTenantName.setText(model.getTenantName());

            String avatar =
                    model.getTenantName() != null && !model.getTenantName().isEmpty()
                            ? model.getTenantName().substring(0, 1).toUpperCase()
                            : "?";
            tvAvatar.setText(avatar);

            // -------- ROOM --------
            if (model.getRoomNo() != null && !model.getRoomNo().isEmpty()) {
                int floor = getFloorFromRoom(model.getRoomNo());
                tvRoomInfo.setText(
                        "Room " + model.getRoomNo() + " • Floor " + floor
                );
            } else {
                tvRoomInfo.setText("");
            }

            // -------- ISSUE --------
            tvIssueTitle.setText(model.getTitle());
            tvCategory.setText(model.getCategory());
            tvDescription.setText(model.getDescription());

            // -------- STATUS --------
            tvStatus.setText(model.getStatus());
            setStatusColor(model.getStatus());

            // -------- DATE --------
            if (model.getCreatedAt() != null) {
                try {
                    Date d = new SimpleDateFormat(
                            "ddMMyyyyHHmm",
                            Locale.getDefault()
                    ).parse(model.getCreatedAt());

                    tvCreatedAt.setText(
                            "Requested on " +
                                    new SimpleDateFormat(
                                            "dd MMM yyyy",
                                            Locale.getDefault()
                                    ).format(d)
                    );
                } catch (Exception e) {
                    tvCreatedAt.setText("");
                }
            }

            setupActionButton(model.getStatus());
        });
    }

    // ================= ACTION BUTTON =================
    private void setupActionButton(String status) {

        switch (status) {

            case "OPEN":
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText("Mark In Progress");
                btnAction.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#FF9800"))
                );
                btnAction.setOnClickListener(v ->
                        updateStatus("IN_PROGRESS")
                );
                break;

            case "IN_PROGRESS":
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText("Mark Resolved");
                btnAction.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                );
                btnAction.setOnClickListener(v ->
                        updateStatus("RESOLVED")
                );
                break;

            default:
                btnAction.setVisibility(View.GONE);
                break;
        }
    }

    private void updateStatus(String newStatus) {

        requestRef.child("status")
                .setValue(newStatus)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            this,
                            "Status updated to " + newStatus,
                            Toast.LENGTH_SHORT
                    ).show();

                    tvStatus.setText(newStatus);
                    setStatusColor(newStatus);
                    setupActionButton(newStatus);
                });
    }

    // ================= HELPERS =================
    private void setStatusColor(String status) {

        switch (status) {
            case "OPEN":
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge_red);
                break;

            case "IN_PROGRESS":
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge_orange);
                break;

            case "RESOLVED":
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge_green);
                break;
        }
    }

    private int getFloorFromRoom(String roomNo) {
        try {
            return Integer.parseInt(roomNo.substring(0, 1));
        } catch (Exception e) {
            return 0;
        }
    }
}
