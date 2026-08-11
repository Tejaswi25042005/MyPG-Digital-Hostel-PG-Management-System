package com.srikanta.mypg.menu;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.DayAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    // ================= UI =================
    private RecyclerView rvDays;
    private TextView tvBreakfast, tvLunch, tvDinner;
    private FloatingActionButton fabEditMenu;

    // ================= FIREBASE =================
    private DatabaseReference menuRef;

    // ================= STATE =================
    private String hostelId;
    private String selectedDay;
    private String todayDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        hostelId = getIntent().getStringExtra("hostelId");

        if (hostelId == null) {
            Toast.makeText(this, "Hostel not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        menuRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Hostels")
                .child(hostelId)
                .child("foodMenu")
                .child("weekly");

        todayDay = getTodayKey();
        selectedDay = todayDay;

        initViews();
        setupDayRecycler();
        loadMenuForDay(selectedDay);
        setupClicks();
    }

    // ================= INIT =================
    private void initViews() {
        rvDays = findViewById(R.id.rvDays);
        tvBreakfast = findViewById(R.id.tvBreakfast);
        tvLunch = findViewById(R.id.tvLunch);
        tvDinner = findViewById(R.id.tvDinner);
        fabEditMenu = findViewById(R.id.fabEditMenu);
    }

    // ================= DAY SELECTOR =================
    private void setupDayRecycler() {

        rvDays.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        DayAdapter adapter = new DayAdapter(
                todayDay,
                day -> {
                    selectedDay = day;
                    loadMenuForDay(day);
                }
        );

        rvDays.setAdapter(adapter);
    }

    // ================= LOAD MENU =================
    private void loadMenuForDay(String day) {

        menuRef.child(day)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snap) {

                        tvBreakfast.setText(
                                snap.child("breakfast").getValue(String.class) == null
                                        ? "Not set"
                                        : snap.child("breakfast").getValue(String.class)
                        );

                        tvLunch.setText(
                                snap.child("lunch").getValue(String.class) == null
                                        ? "Not set"
                                        : snap.child("lunch").getValue(String.class)
                        );

                        tvDinner.setText(
                                snap.child("dinner").getValue(String.class) == null
                                        ? "Not set"
                                        : snap.child("dinner").getValue(String.class)
                        );
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(MenuActivity.this,
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= CLICKS =================
    private void setupClicks() {

        fabEditMenu.setOnClickListener(v -> {
            showEditMenuDialog(selectedDay);
        });
    }

    // ================= EDIT DIALOG =================
    private void showEditMenuDialog(String day) {

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_edit_menu, null);

        EditText etBreakfast = view.findViewById(R.id.etBreakfast);
        EditText etLunch = view.findViewById(R.id.etLunch);
        EditText etDinner = view.findViewById(R.id.etDinner);

        setTextOrHint(etBreakfast, tvBreakfast.getText().toString(), "Breakfast");
        setTextOrHint(etLunch, tvLunch.getText().toString(), "Lunch");
        setTextOrHint(etDinner, tvDinner.getText().toString(), "Dinner");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Today's Menu")
                .setView(view)
                .setPositiveButton("SAVE", null)
                .setNegativeButton("CANCEL", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String breakfast = etBreakfast.getText().toString().trim();
                        String lunch = etLunch.getText().toString().trim();
                        String dinner = etDinner.getText().toString().trim();

                        if (breakfast.isEmpty() || lunch.isEmpty() || dinner.isEmpty()) {
                            Toast.makeText(this,
                                    "All meals are required",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        saveMenu(day, breakfast, lunch, dinner);
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void setTextOrHint(EditText et, String value, String hint) {
        if (value == null || value.equalsIgnoreCase("Not set")) {
            et.setText("");
            et.setHint(hint);
        } else {
            et.setText(value);
        }
    }

    // ================= SAVE MENU =================
    private void saveMenu(
            String day,
            String breakfast,
            String lunch,
            String dinner
    ) {

        Map<String, Object> menu = new HashMap<>();
        menu.put("breakfast", breakfast);
        menu.put("lunch", lunch);
        menu.put("dinner", dinner);
        menu.put("updatedAt", getCurrentDateTimeKey());

        menuRef.child(day)
                .updateChildren(menu)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Menu updated",
                            Toast.LENGTH_SHORT).show();
                    loadMenuForDay(day);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private String getCurrentDateTimeKey() {
        SimpleDateFormat sdf =
                new SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ================= HELPERS =================
    private String getTodayKey() {

        Calendar cal = Calendar.getInstance();
        String day = cal.getDisplayName(
                Calendar.DAY_OF_WEEK,
                Calendar.LONG,
                Locale.US
        );

        return day.toLowerCase();
    }
}
