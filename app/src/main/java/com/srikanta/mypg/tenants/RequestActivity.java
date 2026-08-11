package com.srikanta.mypg.tenants;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.RequestAdapter;
import com.srikanta.mypg.models.RequestModel;

import java.util.ArrayList;
import java.util.List;

public class RequestActivity extends AppCompatActivity {

    // -------- UI --------
    private Toolbar toolbar;
    private RecyclerView rvRequests;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    // -------- DATA --------
    private String hostelId;
    private RequestAdapter adapter;
    private final List<RequestModel> requestList = new ArrayList<>();

    // -------- FIREBASE --------
    private DatabaseReference requestRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request);

        initViews();
        setupToolbar();
        getIntentData();
        setupRecyclerView();
        loadRequests();
    }

    // ================= INIT =================
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvRequests = findViewById(R.id.rvRequests);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void getIntentData() {
        hostelId = getIntent().getStringExtra("hostelId");
        if (hostelId == null) {
            Toast.makeText(this, "Hostel not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        adapter = new RequestAdapter(
                this,
                requestList,
                hostelId,
                model -> {

                    Intent intent = new Intent(
                            this,
                            RequestDetailsActivity.class
                    );

                    intent.putExtra("tenantId", model.getTenantId());
                    intent.putExtra("hostelId", hostelId);
                    intent.putExtra("sharing", model.getSharing());
                    intent.putExtra("rent", model.getRent());
                    intent.putExtra("status", model.getStatus());
                    intent.putExtra("requestedAt", model.getRequestedAt());

                    startActivity(intent);
                }
        );

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(adapter);
    }

    // ================= FIREBASE =================
    private void loadRequests() {

        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        requestRef = FirebaseDatabase.getInstance()
                .getReference("HostelRequests")
                .child(hostelId);

        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                requestList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {

                        RequestModel model = child.getValue(RequestModel.class);
                        if (model == null) continue;

                        // Only pending requests
                        if ("PENDING".equals(model.getStatus())) {
                            model.setTenantId(child.getKey());
                            requestList.add(model);
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);

                if (requestList.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(
                        RequestActivity.this,
                        "Failed to load requests",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
