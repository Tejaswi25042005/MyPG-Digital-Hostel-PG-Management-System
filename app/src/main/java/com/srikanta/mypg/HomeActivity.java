    package com.srikanta.mypg;
    
    import android.app.AlertDialog;
    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;
    
    import androidx.activity.EdgeToEdge;
    import androidx.annotation.NonNull;
    import androidx.cardview.widget.CardView;
    import androidx.recyclerview.widget.GridLayoutManager;
    import androidx.recyclerview.widget.ItemTouchHelper;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;
    import com.srikanta.mypg.actions.RecentActionsActivity;
    import com.srikanta.mypg.adapters.HostelPickerAdapter;
    import com.srikanta.mypg.adapters.QuickActionColumnAdapter;
    import com.srikanta.mypg.adapters.RecentActionAdapter;
    import com.srikanta.mypg.expenses.ExpenditureActivity;
    import com.srikanta.mypg.helpers.BaseOwnerActivity;
    import com.srikanta.mypg.helpers.PricingDialogHelper;
    import com.srikanta.mypg.helpers.SubscriptionChecker;
    import com.srikanta.mypg.helpers.SubscriptionPurchaseHelper;
    import com.srikanta.mypg.hostels.FacilitiesActivity;
    import com.srikanta.mypg.menu.MenuActivity;
    import com.srikanta.mypg.models.QuickActionModel;
    import com.srikanta.mypg.models.RecentActionModel;
    import com.srikanta.mypg.profile.ProfileActivity;
    import com.srikanta.mypg.revenue.RevenueActivity;
    import com.srikanta.mypg.rooms.RoomsActivity;
    import com.srikanta.mypg.servicerequests.ServiceRequestActivity;
    import com.srikanta.mypg.tenants.AddTenantActivity;
    import com.srikanta.mypg.tenants.RequestActivity;
    import com.srikanta.mypg.tenants.TenantActivity;
    import com.srikanta.mypg.notices.NoticeActivity;
    import com.srikanta.mypg.vacate.VacateRequestActivity;
    
    
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Date;
    import java.util.List;
    import java.util.Locale;
    
    public class HomeActivity extends BaseOwnerActivity {
    
        private TextView tvPgName, tvLocation;
        private TextView tvTenantsCount, tvRoomsCount, tvMonthAmount;
        private ImageView profile, ivChangeHostel;
    
        private CardView rooms, tenants, revenue, expenditure;
    
        private DatabaseReference rootRef;
        private String ownerUid;
        private String hostelId; // CURRENT HOSTEL
    
        // Hostel switcher data
        private final List<String> hostelIds = new ArrayList<>();
        private final List<String> hostelNames = new ArrayList<>();
    
        RecyclerView rvRecentActions;
        RecentActionAdapter actionAdapter;
        List<RecentActionModel> actionList = new ArrayList<>();
    
        private TextView allactions;
    
        private DatabaseReference actionsRef;
        private ValueEventListener actionsListener;
    
        RecyclerView rvQuickActions;
        QuickActionColumnAdapter quickAdapter;
        List<QuickActionModel> quickList = new ArrayList<>();

        private TextView tvExpenseAmount;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_home);
    
            initViews();
    
            rootRef = FirebaseDatabase.getInstance().getReference();
            ownerUid = FirebaseAuth.getInstance().getUid();
    
            loadDefaultHostel();
    
            rooms.setOnClickListener(v -> openWithHostel(RoomsActivity.class));
            tenants.setOnClickListener(v -> openWithHostel(TenantActivity.class));
            revenue.setOnClickListener(v -> openWithHostel(RevenueActivity.class));
            profile.setOnClickListener(v -> openWithHostel(ProfileActivity.class));
            expenditure.setOnClickListener(v -> openWithHostel(ExpenditureActivity.class));
    
            ivChangeHostel.setOnClickListener(v -> showHostelPicker());
            tvPgName.setOnClickListener(v -> showHostelPicker());
    
    
            allactions.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, RecentActionsActivity.class);
                intent.putExtra("hostelId", hostelId);
                startActivity(intent);
            });
    
    
        }
    
        private void saveQuickActionsToDb() {
    
            if (hostelId == null) return;
    
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Hostels")
                    .child(hostelId)
                    .child("settings")
                    .child("quickActions");
    
            List<String> ids = new ArrayList<>();
    
            for (QuickActionModel m : quickList) {
                if (!m.isEmpty()) ids.add(m.getId());
            }
    
            ref.setValue(ids);
        }
    
        // ================= QUICK ACTIONS =================
    
        private void loadQuickActionsFromDb() {
    
            if (hostelId == null) return;
    
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Hostels")
                    .child(hostelId)
                    .child("settings")
                    .child("quickActions");
    
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
    
                    quickList.clear();
    
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String id = snap.getValue(String.class);
                        QuickActionModel model = getModelFromId(id);
                        if (model != null) quickList.add(model);
                    }
    
    // if nothing saved → show only add
                    if (quickList.isEmpty()) {
                        quickList.add(new QuickActionModel(
                                "", "+ Add Action", R.drawable.ic_add, true
                        ));
                    }
    // if saved → ensure add at end
                    else if (!quickList.get(quickList.size() - 1).isEmpty()) {
                        quickList.add(new QuickActionModel(
                                "", "+ Add Action", R.drawable.ic_add, true
                        ));
                    }


                    quickAdapter = new QuickActionColumnAdapter(
                            quickList,

                            // click
                            position -> {
                                QuickActionModel model = quickList.get(position);

                                if (model.isEmpty()) openActionPicker(position);
                                else openFeature(model.getId());
                            },

                            // ⭐ LONG PRESS → DIALOG
                            position -> showDeleteDialog(position)
                    );
    
    
                    rvQuickActions.setAdapter(quickAdapter);
                    attachDragHelper();
    
                    rvQuickActions.setOnTouchListener((v, event) -> {
                        quickAdapter.setEditMode(false);
                        return false;
                    });

    
    
                    quickAdapter.setEditMode(false);
                }
    
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }

        private void showDeleteDialog(int position) {

            new AlertDialog.Builder(this)
                    .setTitle("Remove Action")
                    .setMessage("Do you want to remove this shortcut?")
                    .setPositiveButton("Remove", (d, w) -> {

                        quickList.remove(position);

                        // always keep Add button last
                        if (quickList.isEmpty() ||
                                !quickList.get(quickList.size() - 1).isEmpty()) {

                            quickList.add(new QuickActionModel(
                                    "", "+ Add Action", R.drawable.ic_add, true
                            ));
                        }

                        quickAdapter.notifyDataSetChanged();
                        saveQuickActionsToDb();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void openFeature(String id) {
    
            if (hostelId == null) {
                Toast.makeText(this, "Select hostel first", Toast.LENGTH_SHORT).show();
                return;
            }
    
            switch (id) {
    
                case "ADD_TENANT":
                    openWithHostel(AddTenantActivity.class);
                    break;
    
                case "NOTICE":
                    openWithHostel(NoticeActivity.class);
                    break;
    
                case "FOOD":
                    openWithHostel(MenuActivity.class);
                    break;
    
                case "SERVICE":
                    openWithHostel(ServiceRequestActivity.class);
                    break;
    
                case "VACATE":
                    openWithHostel(VacateRequestActivity.class);
                    break;
    
                case "PRICES":
                    new PricingDialogHelper(this, hostelId).openPricing();
                    break;
    
                case "FACILITIES":
                    openWithHostel(FacilitiesActivity.class);
                    break;

                case "TENANT_REQUEST":
                    openWithHostel(RequestActivity.class);
                    break;

            }
        }
    
    
        private void openActionPicker(int position) {
    
            String[] options = {
                    "Add Tenant",
                    "Create Notice",
                    "Food Menu",
                    "Service Requests",
                    "Vacate Requests",
                    "Prices",
                    "Facilities",
                    "Tenant Requests"
            };
    
            new AlertDialog.Builder(this)
                    .setTitle("Select Action")
                    .setItems(options, (d, which) -> {
    
                        QuickActionModel model = null;
    
                        switch (which) {
                            case 0:
                                model = new QuickActionModel("ADD_TENANT", "Add Tenant", R.drawable.ic_add, false);
                                break;
                            case 1:
                                model = new QuickActionModel("NOTICE", "Create Notice", R.drawable.ic_notice, false);
                                break;
                            case 2:
                                model = new QuickActionModel("FOOD", "Food Menu", R.drawable.ic_food, false);
                                break;
                            case 3:
                                model = new QuickActionModel("SERVICE", "Service Requests", R.drawable.ic_service, false);
                                break;
                            case 4:
                                model = new QuickActionModel("VACATE", "Vacate Requests", R.drawable.ic_vacate, false);
                                break;
                            case 5:
                                model = new QuickActionModel("PRICES", "Prices", R.drawable.ic_money, false);
                                break;
                            case 6:
                                model = new QuickActionModel("FACILITIES", "Facilities", R.drawable.wifi, false);
                                break;

                            case 7:
                                model = new QuickActionModel("TENANT_REQUEST", "Tenant Requests", R.drawable.ic_notification, false);
                                break;
                        }
    
                        // prevent duplicate
                        for (QuickActionModel m : quickList) {
                            if (!m.isEmpty() && m.getId().equals(model.getId())) {
                                Toast.makeText(this, "Already added", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
    
                        // replace current Add with real item
                        quickList.set(position, model);
    
    // ensure last item is Add button
                        if (!quickList.get(quickList.size() - 1).isEmpty()) {
                            quickList.add(new QuickActionModel(
                                    "", "+ Add Action", R.drawable.ic_add, true
                            ));
                        }

                        quickAdapter.setEditMode(false);
                        saveQuickActionsToDb();

                    })
                    .show();
        }
    
        private QuickActionModel getModelFromId(String id) {
    
            switch (id) {
                case "ADD_TENANT":
                    return new QuickActionModel(id, "Add Tenant", R.drawable.ic_add, false);
    
                case "NOTICE":
                    return new QuickActionModel(id, "Create Notice", R.drawable.ic_notice, false);
    
                case "FOOD":
                    return new QuickActionModel(id, "Food Menu", R.drawable.ic_food, false);
    
                case "SERVICE":
                    return new QuickActionModel(id, "Service Requests", R.drawable.ic_service, false);
    
                case "VACATE":
                    return new QuickActionModel(id, "Vacate Requests", R.drawable.ic_vacate, false);
    
                case "PRICES":
                    return new QuickActionModel(id, "Prices", R.drawable.ic_money, false);
    
                case "FACILITIES":
                    return new QuickActionModel(id, "Facilities", R.drawable.wifi, false);
            }
    
            return null;
        }
    
    
        // ================= INIT =================
        private void initViews() {
    
            tvPgName = findViewById(R.id.tvPgName);
            tvLocation = findViewById(R.id.tvLocation);
    
            tvTenantsCount = findViewById(R.id.tvTenantsCount);
            tvRoomsCount = findViewById(R.id.tvRoomsCount);
            tvMonthAmount = findViewById(R.id.tvMonthAmount);
            tvExpenseAmount = findViewById(R.id.tvMonthAmountexp);
    
            rooms = findViewById(R.id.cardRooms);
            tenants = findViewById(R.id.cardTenants);
            revenue = findViewById(R.id.cardRevenue);
            expenditure = findViewById(R.id.cardExpenditure);
    
            profile = findViewById(R.id.ivProfile);
            ivChangeHostel = findViewById(R.id.ivChangeHostel);
    
    
            rvRecentActions = findViewById(R.id.rvRecentActions);
            rvRecentActions.setLayoutManager(new LinearLayoutManager(this));
            actionAdapter = new RecentActionAdapter(actionList);
            rvRecentActions.setAdapter(actionAdapter);
    
            allactions = findViewById(R.id.tvViewAllActions);
    
            rvQuickActions = findViewById(R.id.rvQuickActions);
    
            GridLayoutManager manager =
                    new GridLayoutManager(
                            this,
                            1, // rows
                            RecyclerView.HORIZONTAL,
                            false
                    );
    
            rvQuickActions.setLayoutManager(manager);
    
        }

        private void loadCurrentMonthExpenses() {

            if (hostelId == null) return;

            String monthKey = new SimpleDateFormat(
                    "yyyy-MM", Locale.getDefault()
            ).format(new Date());

            rootRef.child("Hostels")
                    .child(hostelId)
                    .child("expenses")
                    .child(monthKey)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            long total = 0;

                            for (DataSnapshot snap : snapshot.getChildren()) {
                                Long amt = snap.child("amount").getValue(Long.class);
                                if (amt != null) total += amt;
                            }

                            tvExpenseAmount.setText("₹" + total);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
        }


        // ================= LOAD DEFAULT HOSTEL =================
        private void loadDefaultHostel() {
    
            rootRef.child("Owners")
                    .child(ownerUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
    
                            hostelId = snapshot
                                    .child("defaultHostelId")
                                    .getValue(String.class);
    
                            loadOwnerHostels();
                        }
    
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loadOwnerHostels();
                        }
                    });
        }
    
        private void attachDragHelper() {

            ItemTouchHelper.SimpleCallback callback =
                    new ItemTouchHelper.SimpleCallback(
                            ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                            0) {

                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView,
                                              @NonNull RecyclerView.ViewHolder viewHolder,
                                              @NonNull RecyclerView.ViewHolder target) {

                            int from = viewHolder.getAdapterPosition();
                            int to = target.getAdapterPosition();

                            // block moving add button
                            if (quickList.get(from).isEmpty() || quickList.get(to).isEmpty()) {
                                return false;
                            }

                            Collections.swap(quickList, from, to);
                            quickAdapter.notifyItemMoved(from, to);
                            saveQuickActionsToDb();

                            return true;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
                    };

            new ItemTouchHelper(callback).attachToRecyclerView(rvQuickActions);
        }


    
        // ================= LOAD OWNER HOSTELS =================
        private void loadOwnerHostels() {
    
            rootRef.child("Owners")
                    .child(ownerUid)
                    .child("hostels")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
    
                            hostelIds.clear();
                            hostelNames.clear();
    
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                hostelIds.add(snap.getKey());
                                hostelNames.add(
                                        snap.child("hostelName")
                                                .getValue(String.class)
                                );
                            }
    
                            if (hostelId == null && !hostelIds.isEmpty()) {
                                hostelId = hostelIds.get(0);
                                saveDefaultHostel(hostelId);
                            }
    
                            loadHostelInfo();
    
                            loadDashboardStats();
                        }
    
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(HomeActivity.this,
                                    "Failed to load hostels",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    
        // ================= HOSTEL INFO =================
        private void loadHostelInfo() {
    
            if (hostelId == null) return;

            SubscriptionChecker.check(hostelId, (isActive, plan, endDate) -> {

                if (!isActive) {
                    showSubscriptionExpiredDialog();   // only inform
                }

                // ALWAYS allow app usage
            });



            rootRef.child("Hostels")
                    .child(hostelId)
                    .child("info")
                    .get()
                    .addOnSuccessListener(snapshot -> {
    
                        if (!snapshot.exists()) return;
    
                        tvPgName.setText(snapshot.child("pgName").getValue(String.class));
    
                        String address = snapshot
                                .child("address")
                                .getValue(String.class);
    
                        tvLocation.setText(shortenAddress(address));
                    });
    
            loadRecentActions();
    
            loadQuickActionsFromDb();
        }

        private void showSubscriptionExpiredDialog() {

            new AlertDialog.Builder(this)
                    .setTitle("Subscription Expired")
                    .setMessage("Please renew your plan to continue using features.")
                    .setCancelable(false)
                    .setPositiveButton("Renew", (d, w) -> {

                        if (hostelId == null) {
                            Toast.makeText(this,
                                    "Hostel not selected",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new SubscriptionPurchaseHelper(this, hostelId)
                                .startPurchase();
                    })
                    .show();
        }



        // ================= HOSTEL PICKER =================
        private void showHostelPicker() {
    
            if (hostelNames.isEmpty()) return;
    
            AlertDialog dialog = new AlertDialog.Builder(this).create();
    
            View view = getLayoutInflater()
                    .inflate(R.layout.dialog_hostel_picker, null);
    
            RecyclerView rv = view.findViewById(R.id.rvHostels);
            rv.setLayoutManager(new LinearLayoutManager(this));
    
            HostelPickerAdapter adapter =
                    new HostelPickerAdapter(hostelNames, position -> {
    
                        hostelId = hostelIds.get(position);
                        saveDefaultHostel(hostelId);
    
                        loadHostelInfo();
                        loadDashboardStats();
    
                        dialog.dismiss();
                    });
    
            rv.setAdapter(adapter);
    
            dialog.setView(view);
            dialog.show();
    
            // Optional: rounded corners
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(
                        R.drawable.bg_dialog_round
                );
            }
        }
    
    
        private void saveDefaultHostel(String hostelId) {
            rootRef.child("Owners")
                    .child(ownerUid)
                    .child("defaultHostelId")
                    .setValue(hostelId);
        }
    
        // ================= DASHBOARD =================
        private void loadDashboardStats() {
    
            if (hostelId == null) return;
    
            rootRef.child("Hostels")
                    .child(hostelId)
                    .child("tenants")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            tvTenantsCount.setText(
                                    String.valueOf(snapshot.getChildrenCount()));
                        }
    
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
    
            rootRef.child("Hostels")
                    .child(hostelId)
                    .child("rooms")
                    .addValueEventListener(new ValueEventListener() {
    
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
    
                            int totalRooms = 0;
                            int occupiedRooms = 0;
    
                            for (DataSnapshot floor : snapshot.getChildren()) {
                                for (DataSnapshot room : floor.getChildren()) {
                                    totalRooms++;
                                    Integer occ = room.child("occupiedBeds")
                                            .getValue(Integer.class);
                                    if (occ != null && occ > 0) occupiedRooms++;
                                }
                            }
    
                            tvRoomsCount.setText(
                                    occupiedRooms + "/" + totalRooms);
                        }
    
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
    
            loadCurrentMonthRevenue();

            loadCurrentMonthExpenses();

        }
    
        // ================= NAV =================
        private void openWithHostel(Class<?> cls) {
            if (hostelId == null) {
                Toast.makeText(this,
                        "Hostel not selected",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, cls)
                    .putExtra("hostelId", hostelId));
        }
    
        private String shortenAddress(String address) {
            if (address == null) return "";
            return address.length() > 40
                    ? address.substring(0, 40) + "..."
                    : address;
        }
        private void loadRecentActions() {
    
            if (hostelId == null) return;
    
            actionsRef = FirebaseDatabase.getInstance()
                    .getReference("Hostels")
                    .child(hostelId)
                    .child("actions");
    
            // Remove old listener if hostel switches
            if (actionsListener != null) {
                actionsRef.removeEventListener(actionsListener);
            }
    
            actionsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
    
                    actionList.clear();
    
                    // Loop months (2026-02, 2026-03, ...)
                    for (DataSnapshot monthSnap : snapshot.getChildren()) {
    
                        for (DataSnapshot actionSnap : monthSnap.getChildren()) {
    
                            RecentActionModel model =
                                    actionSnap.getValue(RecentActionModel.class);
    
                            if (model != null) {
                                actionList.add(model);
                            }
                        }
                    }
    
                    // Sort newest first
                    Collections.sort(actionList,
                            (a, b) -> Long.compare(
                                    b.getTimestampMillis(),
                                    a.getTimestampMillis()
                            ));
    
                    // Keep only last 5
                    if (actionList.size() > 5) {
                        actionList.subList(5, actionList.size()).clear();
                    }
    
                    actionAdapter.notifyDataSetChanged();
                }
    
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            };
    
            actionsRef.addValueEventListener(actionsListener);
        }
    
        private void loadCurrentMonthRevenue() {
    
            if (hostelId == null) return;
    
            String monthKey = new SimpleDateFormat(
                    "yyyy-MM", Locale.getDefault()
            ).format(new Date());
    
            rootRef.child("Hostels")
                    .child(hostelId)
                    .child("tenants")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
    
                            long total = 0;
    
                            for (DataSnapshot tenantSnap : snapshot.getChildren()) {
    
                                DataSnapshot paymentSnap =
                                        tenantSnap.child("payments").child(monthKey);
    
                                Long paid = paymentSnap.child("rentPaid")
                                        .getValue(Long.class);
    
                                if (paid != null) {
                                    total += paid;
                                }
                            }
    
                            tvMonthAmount.setText("₹" + total);
                        }
    
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
        }
    
    
        @Override
        protected void onDestroy() {
            super.onDestroy();
    
            if (actionsRef != null && actionsListener != null) {
                actionsRef.removeEventListener(actionsListener);
            }
        }
    
    
    }
