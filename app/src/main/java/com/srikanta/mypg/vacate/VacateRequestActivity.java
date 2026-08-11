package com.srikanta.mypg.vacate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.srikanta.mypg.adapters.VacateRequestAdapter;
import com.srikanta.mypg.models.VacateRequestModel;

import java.util.ArrayList;
import java.util.List;

public class VacateRequestActivity extends AppCompatActivity {

    private String hostelId;

    private RecyclerView rvVacateRequests;
    private LinearLayout layoutEmpty;
    private ImageView btnBack;

    private VacateRequestAdapter adapter;
    private final List<VacateRequestModel> vacateList = new ArrayList<>();

    private DatabaseReference vacateRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vacate_request);

        hostelId = getIntent().getStringExtra("hostelId");

        if (hostelId == null || hostelId.isEmpty()) {
            Toast.makeText(this, "Invalid hostel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadVacateRequests();
    }

    private void initViews() {
        rvVacateRequests = findViewById(R.id.rvVacateRequests);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new VacateRequestAdapter(this, vacateList, hostelId);
        rvVacateRequests.setLayoutManager(new LinearLayoutManager(this));
        rvVacateRequests.setAdapter(adapter);
    }

    private void loadVacateRequests() {

        vacateRef = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("vacateRequests");

        vacateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                vacateList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot requestSnap : snapshot.getChildren()) {

                        VacateRequestModel model =
                                requestSnap.getValue(VacateRequestModel.class);

                        if (model != null) {
                            model.setRequestId(requestSnap.getKey());
                            vacateList.add(model);
                        }
                    }
                }

                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(
                        VacateRequestActivity.this,
                        "Failed to load vacate requests",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void updateUI() {
        if (vacateList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvVacateRequests.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvVacateRequests.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }
}
