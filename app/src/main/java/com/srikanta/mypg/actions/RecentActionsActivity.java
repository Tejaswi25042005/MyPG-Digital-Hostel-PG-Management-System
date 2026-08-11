package com.srikanta.mypg.actions;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.RecentActionAdapter;
import com.srikanta.mypg.models.RecentActionModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentActionsActivity extends AppCompatActivity {

    // -------- UI --------
    private ImageView ivBack, ivFilter;
    private MaterialButton btnAll, btnTenant, btnPayment;
    private RecyclerView rvActions;
    private LinearLayout layoutEmpty;

    // -------- DATA --------
    private final List<RecentActionModel> allActions = new ArrayList<>();
    private final List<RecentActionModel> filteredActions = new ArrayList<>();
    private RecentActionAdapter adapter;

    // -------- FIREBASE --------
    private String hostelId;
    private DatabaseReference rootRef;

    // -------- STATE --------
    private String selectedCategory = "ALL";

    private String selectedTimeFilter = "THIS_MONTH";
    private String selectedActionType = null;
    private String selectedSort = "NEWEST";

    private DatabaseReference actionsRef;
    private ValueEventListener actionsListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recent_actions);

        hostelId = getIntent().getStringExtra("hostelId");
        rootRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRecyclerView();
        setupClicks();
        loadAllActionsRealtime();

    }

    // ================= INIT =================
    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivFilter = findViewById(R.id.ivFilter);

        btnAll = findViewById(R.id.btnAll);
        btnTenant = findViewById(R.id.btnTenant);
        btnPayment = findViewById(R.id.btnPayment);

        rvActions = findViewById(R.id.rvActions);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        rvActions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecentActionAdapter(filteredActions);
        rvActions.setAdapter(adapter);
    }

    private void setupClicks() {

        ivBack.setOnClickListener(v -> finish());

        ivFilter.setOnClickListener(v -> showFilterBottomSheet());

        btnAll.setOnClickListener(v -> {
            selectedCategory = "ALL";
            updatePrimaryFilterUI();
            applyFilters();
        });

        btnTenant.setOnClickListener(v -> {
            selectedCategory = "TENANT";
            updatePrimaryFilterUI();
            applyFilters();
        });

        btnPayment.setOnClickListener(v -> {
            selectedCategory = "PAYMENT";
            updatePrimaryFilterUI();
            applyFilters();
        });
    }

    private void showFilterBottomSheet() {

        BottomSheetDialog dialog =
                new BottomSheetDialog(this);

        View view = getLayoutInflater()
                .inflate(R.layout.bottom_sheet_action_filters, null);

        dialog.setContentView(view);

        // ---- Time ----
        ChipGroup chipGroupTime = view.findViewById(R.id.chipGroupTime);
        Chip chipToday = view.findViewById(R.id.chipToday);
        Chip chipThisWeek = view.findViewById(R.id.chipThisWeek);
        Chip chipThisMonth = view.findViewById(R.id.chipThisMonth);
        Chip chipLastMonth = view.findViewById(R.id.chipLastMonth);

        // ---- Action Type ----
        ChipGroup chipGroupAction = view.findViewById(R.id.chipGroupActionType);

        // ---- Sort ----
        ChipGroup chipGroupSort = view.findViewById(R.id.chipGroupSort);

        Button btnClear = view.findViewById(R.id.btnClear);
        Button btnApply = view.findViewById(R.id.btnApply);

        // -------- DEFAULT SELECTIONS --------
        switch (selectedTimeFilter) {
            case "TODAY": chipToday.setChecked(true); break;
            case "WEEK": chipThisWeek.setChecked(true); break;
            case "LAST_MONTH": chipLastMonth.setChecked(true); break;
            default: chipThisMonth.setChecked(true);
        }


        // -------- CLEAR --------
        btnClear.setOnClickListener(v -> {

            selectedCategory = "ALL";
            selectedTimeFilter = "THIS_MONTH";
            selectedActionType = null;
            selectedSort = "NEWEST";

            updatePrimaryFilterUI();
            applyFilters();
            dialog.dismiss();
        });


        // -------- APPLY --------
        btnApply.setOnClickListener(v -> {

            // Time filter (example)
            if (chipToday.isChecked()) {
                selectedTimeFilter = "TODAY";
            } else if (chipThisWeek.isChecked()) {
                selectedTimeFilter = "WEEK";
            } else if (chipLastMonth.isChecked()) {
                selectedTimeFilter = "LAST_MONTH";
            } else {
                selectedTimeFilter = "THIS_MONTH";
            }

            // Action type
            if (chipGroupAction.getCheckedChipId() != View.NO_ID) {
                Chip chip = view.findViewById(
                        chipGroupAction.getCheckedChipId());
                selectedActionType = chip.getText().toString();
            } else {
                selectedActionType = null;
            }

            // Sort
            if (chipGroupSort.getCheckedChipId() != View.NO_ID) {
                Chip chip = view.findViewById(
                        chipGroupSort.getCheckedChipId());
                selectedSort = chip.getText().toString();
            }

            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadAllActionsRealtime() {

        if (hostelId == null) return;

        actionsRef = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("actions");

        // Remove old listener if any (important when reopening activity)
        if (actionsListener != null) {
            actionsRef.removeEventListener(actionsListener);
        }

        actionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                allActions.clear();

                // Loop months (yyyy-MM)
                for (DataSnapshot monthSnap : snapshot.getChildren()) {

                    for (DataSnapshot actionSnap : monthSnap.getChildren()) {

                        RecentActionModel model =
                                actionSnap.getValue(RecentActionModel.class);

                        if (model != null) {
                            allActions.add(model);
                        }
                    }
                }

                // Sort newest first
                Collections.sort(
                        allActions,
                        (a, b) -> Long.compare(
                                b.getTimestampMillis(),
                                a.getTimestampMillis()
                        )
                );

                applyFilters(); // 🔥 filters + empty state
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        actionsRef.addValueEventListener(actionsListener);
    }


    // ================= FILTER LOGIC =================
    private void applyFilters() {

        filteredActions.clear();
        long now = System.currentTimeMillis();

        for (RecentActionModel action : allActions) {

            // -------- CATEGORY --------
            if (!"ALL".equals(selectedCategory)
                    && !selectedCategory.equals(action.getCategory())) {
                continue;
            }

            // -------- ACTION TYPE --------
            if (selectedActionType != null) {
                if (!action.getActionType()
                        .replace("_", " ")
                        .equalsIgnoreCase(selectedActionType)) {
                    continue;
                }
            }

            // -------- TIME FILTER --------
            long diff = now - action.getTimestampMillis();


            if ("TODAY".equals(selectedTimeFilter) && diff > 86400000L)
                continue;

            if ("WEEK".equals(selectedTimeFilter) && diff > 7 * 86400000L)
                continue;

            if ("LAST_MONTH".equals(selectedTimeFilter)) {
                String lastMonth = new SimpleDateFormat(
                        "yyyy-MM", Locale.getDefault()
                ).format(new Date(now - 30L * 86400000L));

                String actionMonth = new SimpleDateFormat(
                        "yyyy-MM", Locale.getDefault()
                ).format(new Date(action.getTimestampMillis()));


                if (!lastMonth.equals(actionMonth))
                    continue;
            }

            filteredActions.add(action);
        }

        // -------- SORT --------
        if ("Oldest First".equals(selectedSort)) {
            Collections.reverse(filteredActions);
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }


    // ================= UI HELPERS =================
    private void updatePrimaryFilterUI() {

        resetButton(btnAll);
        resetButton(btnTenant);
        resetButton(btnPayment);

        if ("ALL".equals(selectedCategory)) {
            selectButton(btnAll);
        } else if ("TENANT".equals(selectedCategory)) {
            selectButton(btnTenant);
        } else {
            selectButton(btnPayment);
        }
    }

    private void selectButton(MaterialButton btn) {
        btn.setBackgroundTintList(
                getColorStateList(R.color.primary));
        btn.setTextColor(getColor(android.R.color.white));
    }

    private void resetButton(MaterialButton btn) {
        btn.setBackgroundTintList(
                getColorStateList(android.R.color.darker_gray));
        btn.setTextColor(getColor(android.R.color.black));
    }

    private void updateEmptyState() {
        if (filteredActions.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvActions.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvActions.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (actionsRef != null && actionsListener != null) {
            actionsRef.removeEventListener(actionsListener);
        }
    }

}
