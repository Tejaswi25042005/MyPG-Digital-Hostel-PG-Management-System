package com.srikanta.mypg.servicerequests;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.ServiceRequestAdapter;
import com.srikanta.mypg.models.ServiceRequestModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import androidx.appcompat.widget.SwitchCompat;


public class ServiceRequestActivity extends AppCompatActivity {

    // ================= UI =================
    private ImageView ivBack;
    private TextView tvTitle;

    private TextView filterAll, filterOpen, filterProgress, filterResolved;

    private RecyclerView rvServiceRequests;
    private LinearLayout layoutEmpty;

    // ================= DATA =================
    private String hostelId;
    private String currentFilter = "ALL";

    private DatabaseReference rootRef;

    private ServiceRequestAdapter adapter;
    private final List<ServiceRequestModel> allRequests = new ArrayList<>();
    private final List<ServiceRequestModel> filteredRequests = new ArrayList<>();
    private SwitchCompat switchServiceRequests;
    private boolean isInternalChange = false; // prevents loop



    // ================= LIFECYCLE =================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_request);

        hostelId = getIntent().getStringExtra("hostelId");

        if (hostelId == null || hostelId.isEmpty()) {
            Toast.makeText(this, "Invalid hostel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rootRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRecycler();
        setupClicks();

        // Default filter
        selectFilter(filterAll);

        loadServiceRequestToggle();
        loadServiceRequests();
    }

    // ================= INIT =================
    private void initViews() {

        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);

        filterAll = findViewById(R.id.filterAll);
        filterOpen = findViewById(R.id.filterOpen);
        filterProgress = findViewById(R.id.filterProgress);
        filterResolved = findViewById(R.id.filterResolved);

        rvServiceRequests = findViewById(R.id.rvServiceRequests);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        switchServiceRequests = findViewById(R.id.switchServiceRequests);


    }

    private void loadServiceRequestToggle() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("settings")
                .child("canRaiseServiceRequest")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        boolean enabled = snapshot.exists()
                                ? Boolean.TRUE.equals(snapshot.getValue(Boolean.class))
                                : true; // default allow

                        isInternalChange = true;
                        switchServiceRequests.setChecked(enabled);
                        isInternalChange = false;
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }


    // ================= RECYCLER =================
    private void setupRecycler() {

        rvServiceRequests.setLayoutManager(new LinearLayoutManager(this));
        rvServiceRequests.setHasFixedSize(true);

        adapter = new ServiceRequestAdapter(
                this,
                hostelId,
                filteredRequests,
                null
        );

        rvServiceRequests.setAdapter(adapter);
    }

    // ================= CLICKS =================
    private void setupClicks() {

        ivBack.setOnClickListener(v -> finish());

        filterAll.setOnClickListener(v -> {
            selectFilter(filterAll);
            currentFilter = "ALL";
            applyFilter();
        });

        filterOpen.setOnClickListener(v -> {
            selectFilter(filterOpen);
            currentFilter = "OPEN";
            applyFilter();
        });

        filterProgress.setOnClickListener(v -> {
            selectFilter(filterProgress);
            currentFilter = "IN_PROGRESS";
            applyFilter();
        });

        filterResolved.setOnClickListener(v -> {
            selectFilter(filterResolved);
            currentFilter = "RESOLVED";
            applyFilter();
        });

        switchServiceRequests.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isInternalChange) return;

            rootRef.child("Hostels")
                    .child(hostelId)
                    .child("settings")
                    .child("canRaiseServiceRequest")
                    .setValue(isChecked);
        });

    }

    // ================= FILTER UI =================
    private void selectFilter(TextView selected) {

        resetFilter(filterAll);
        resetFilter(filterOpen);
        resetFilter(filterProgress);
        resetFilter(filterResolved);

        selected.setBackgroundColor(Color.parseColor("#3F51B5"));
        selected.setTextColor(Color.WHITE);
        selected.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetFilter(TextView tv) {
        tv.setBackgroundColor(Color.parseColor("#E0E0E0"));
        tv.setTextColor(Color.parseColor("#333333"));
        tv.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    // ================= FIREBASE LOAD =================
    private void loadServiceRequests() {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("serviceRequests")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        allRequests.clear();

                        if (!snapshot.exists()) {
                            applyFilter();
                            return;
                        }

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            ServiceRequestModel model =
                                    snap.getValue(ServiceRequestModel.class);

                            if (model == null) continue;

                            model.setRequestId(snap.getKey());
                            allRequests.add(model);
                        }

                        // ✅ Correct sorting: newest first
                        Collections.sort(
                                allRequests,
                                (a, b) -> Long.compare(
                                        b.getCreatedAtMillis(),
                                        a.getCreatedAtMillis()
                                )
                        );

                        applyFilter();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ServiceRequestActivity.this,
                                "Failed to load service requests",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // ================= FILTER LOGIC =================
    private void applyFilter() {

        filteredRequests.clear();

        if ("ALL".equals(currentFilter)) {
            filteredRequests.addAll(allRequests);
        } else {
            for (ServiceRequestModel m : allRequests) {
                if (currentFilter.equals(m.getStatus())) {
                    filteredRequests.add(m);
                }
            }
        }

        adapter.notifyDataSetChanged();
        showEmptyState(filteredRequests.isEmpty());
    }

    // ================= EMPTY STATE =================
    private void showEmptyState(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvServiceRequests.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
