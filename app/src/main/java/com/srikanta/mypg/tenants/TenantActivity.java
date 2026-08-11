package com.srikanta.mypg.tenants;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.HomeActivity;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.FullTenantAdapter;
import com.srikanta.mypg.models.TenantModel;

import java.util.ArrayList;
import java.util.List;

public class TenantActivity extends AppCompatActivity {

    // ================= UI =================
    private TextView tvTenantCount;
    private RecyclerView rvTenants;
    private FloatingActionButton fabAddTenant;

    // ================= FIREBASE =================
    private DatabaseReference rootRef;
    private String hostelId;

    // ================= DATA =================
    private final List<TenantModel> tenantList = new ArrayList<>();
    private FullTenantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tenant);

        hostelId = getIntent().getStringExtra("hostelId");
        if (hostelId == null) {
            Toast.makeText(this, "Invalid hostel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rootRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRecyclerView();
        loadTenants();
        setupActions();
    }

    // ================= INIT =================
    private void initViews() {
        tvTenantCount = findViewById(R.id.tvTenantCount);
        rvTenants = findViewById(R.id.rvTenants);
        fabAddTenant = findViewById(R.id.fabAddTenant);
    }

    private void setupRecyclerView() {
        rvTenants.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FullTenantAdapter(
                tenantList,
                tenant -> {
                    Intent intent = new Intent(
                            TenantActivity.this,
                            TenantDetailActivity.class
                    );
                    intent.putExtra("hostelId", hostelId);
                    intent.putExtra("tenantId", tenant.getTenantId());
                    startActivity(intent);
                }
        );

        rvTenants.setAdapter(adapter);
    }

    // ================= LOAD TENANTS =================
    private void loadTenants() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("tenants")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        tenantList.clear();

                        for (DataSnapshot tenantSnap : snapshot.getChildren()) {

                            String tenantId = tenantSnap.getKey();
                            if (tenantId == null) continue;

                            String name =
                                    tenantSnap.child("name").getValue(String.class);
                            String mobile =
                                    tenantSnap.child("mobile").getValue(String.class);
                            Integer floorNo =
                                    tenantSnap.child("floorNo").getValue(Integer.class);
                            Integer roomNo =
                                    tenantSnap.child("roomNo").getValue(Integer.class);

                            tenantList.add(
                                    new TenantModel(
                                            tenantId,
                                            name == null ? "Tenant" : name,
                                            mobile == null ? "" : mobile,
                                            floorNo == null ? 0 : floorNo,
                                            roomNo == null ? 0 : roomNo
                                    )
                            );
                        }

                        tvTenantCount.setText(
                                String.valueOf(tenantList.size())
                        );

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(
                                TenantActivity.this,
                                "Failed to load tenants",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // ================= ACTIONS =================
    private void setupActions() {

        fabAddTenant.setOnClickListener(v -> {
            if (hostelId == null) {
                Toast.makeText(this, "Select hostel first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(
                    TenantActivity.this,
                    AddTenantActivity.class
            );
            intent.putExtra("hostelId", hostelId);
            startActivity(intent);
        });

    }
}
