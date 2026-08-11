package com.srikanta.mypg.expenses;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.srikanta.mypg.adapters.ExpenseAdapter;
import com.srikanta.mypg.models.ExpenseModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenditureActivity extends AppCompatActivity {

    private String hostelId;

    private ImageView ivPreviousMonth, ivNextMonth, ivOpenMonthPicker;
    private TextView tvSelectedMonth;
    private RecyclerView rvExpenses;
    private FloatingActionButton fabAddExpense;

    private final Calendar calendar = Calendar.getInstance();

    private DatabaseReference expensesRef;
    private ExpenseAdapter adapter;
    private final List<ExpenseModel> expenseList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expenditure);

        hostelId = getIntent().getStringExtra("hostelId");
        if (hostelId == null || hostelId.isEmpty()) {
            Toast.makeText(this, "Invalid hostel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initRecycler();
        updateMonthUI();
        loadExpenses();

        clickListeners();
    }

    private void initViews() {
        ivPreviousMonth = findViewById(R.id.ivPreviousMonth);
        ivNextMonth = findViewById(R.id.ivNextMonth);
        ivOpenMonthPicker = findViewById(R.id.ivOpenMonthPicker);
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
        rvExpenses = findViewById(R.id.rvExpenses);
        fabAddExpense = findViewById(R.id.fabAddExpense);
    }

    private void initRecycler() {
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(expenseList);
        rvExpenses.setAdapter(adapter);
    }


    private void clickListeners() {

        ivPreviousMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateMonthUI();
            loadExpenses();
        });

        ivNextMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateMonthUI();
            loadExpenses();
        });

        fabAddExpense.setOnClickListener(v -> openAddExpenseDialog());

        // calendar picker later
        ivOpenMonthPicker.setOnClickListener(v -> openMonthPicker());

    }

    private void openMonthPicker() {

        android.app.AlertDialog dialog =
                new android.app.AlertDialog.Builder(this).create();

        android.view.View view = getLayoutInflater()
                .inflate(R.layout.dialog_month_year_picker, null);

        android.widget.Spinner spMonth = view.findViewById(R.id.spMonth);
        android.widget.Spinner spYear = view.findViewById(R.id.spYear);
        android.widget.TextView btnOk = view.findViewById(R.id.btnOk);
        android.widget.TextView btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // ================= MONTHS =================
        String[] months = new java.text.DateFormatSymbols().getMonths();

        java.util.List<String> monthList = new java.util.ArrayList<>();
        for (int i = 0; i < 12; i++) monthList.add(months[i]);

        android.widget.ArrayAdapter<String> monthAdapter =
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        monthList);

        spMonth.setAdapter(monthAdapter);

        // ================= YEARS (2025 + 8) =================
        java.util.List<Integer> years = new java.util.ArrayList<>();
        for (int y = 2025; y <= 2025 + 8; y++) {
            years.add(y);
        }

        android.widget.ArrayAdapter<Integer> yearAdapter =
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        years);

        spYear.setAdapter(yearAdapter);

        // ================= SET CURRENT =================
        spMonth.setSelection(calendar.get(Calendar.MONTH));
        int index = years.indexOf(calendar.get(Calendar.YEAR));
        if (index < 0) index = 0;
        spYear.setSelection(index);


        // ================= OK =================
        btnOk.setOnClickListener(v -> {

            int selectedMonth = spMonth.getSelectedItemPosition();
            int selectedYear = (int) spYear.getSelectedItem();

            calendar.set(Calendar.YEAR, selectedYear);
            calendar.set(Calendar.MONTH, selectedMonth);

            updateMonthUI();
            loadExpenses();

            dialog.dismiss();
        });

        dialog.setView(view);
        dialog.show();
    }



    private void updateMonthUI() {
        String display = new SimpleDateFormat(
                "MMMM yyyy", Locale.getDefault()).format(calendar.getTime());
        tvSelectedMonth.setText(display);
    }

    private String getMonthKey() {
        return new SimpleDateFormat(
                "yyyy-MM", Locale.getDefault()).format(calendar.getTime());
    }

    private void loadExpenses() {

        String monthKey = getMonthKey();

        expensesRef = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("expenses")
                .child(monthKey);

        expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                expenseList.clear();

                if (!snapshot.exists()) {
                    findViewById(R.id.emptyView).setVisibility(android.view.View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    return;
                }

                findViewById(R.id.emptyView).setVisibility(android.view.View.GONE);

                for (DataSnapshot snap : snapshot.getChildren()) {
                    ExpenseModel model = snap.getValue(ExpenseModel.class);

                    if (model != null) {
                        model.setId(snap.getKey());
                        expenseList.add(model);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void openAddExpenseDialog() {

        android.app.AlertDialog dialog =
                new android.app.AlertDialog.Builder(this).create();

        android.view.View view = getLayoutInflater()
                .inflate(R.layout.dialog_add_expense, null);

        android.widget.EditText etTitle = view.findViewById(R.id.etTitle);
        android.widget.EditText etAmount = view.findViewById(R.id.etAmount);
        android.widget.Spinner spCategory = view.findViewById(R.id.spCategory);
        android.widget.Spinner spSubCategory = view.findViewById(R.id.spSubCategory);
        android.widget.TextView btnSave = view.findViewById(R.id.btnSave);
        android.widget.TextView btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // ================= CATEGORIES =================
        String[] categories = {
                "-- Select Category --",
                "Food",
                "Salary",
                "Maintenance",
                "Utilities",
                "Cleaning",
                "Purchase",
                "Other"
        };


        android.widget.ArrayAdapter<String> catAdapter =
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        categories);

        spCategory.setAdapter(catAdapter);

        // ================= SUB CATEGORY MAP =================
        java.util.Map<String, String[]> subMap = new java.util.HashMap<>();

        subMap.put("Food", new String[]{
                "Groceries", "Milk", "Vegetables", "Gas", "Water"
        });

        subMap.put("Salary", new String[]{
                "Warden", "Security", "Cook", "Cleaning", "Maintenance Staff"
        });

        subMap.put("Maintenance", new String[]{
                "Electrical", "Plumbing", "Carpentry", "Painting", "General"
        });

        subMap.put("Utilities", new String[]{
                "Electricity", "Water", "Internet", "Diesel", "Garbage"
        });

        subMap.put("Cleaning", new String[]{
                "Laundry", "Toiletries", "Materials"
        });

        subMap.put("Purchase", new String[]{
                "Furniture", "Bedding", "Appliances", "Equipment"
        });

        subMap.put("Other", new String[]{
                "Transport", "Emergency", "Misc"
        });

        // default subcategory
        updateSubSpinner(spSubCategory, new String[]{"-- Select Subcategory --"});


        // change on category select
        spCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {

                if (position == 0) {
                    updateSubSpinner(spSubCategory, new String[]{"-- Select Subcategory --"});
                    return;
                }

                String cat = categories[position];
                String[] data = subMap.get(cat);

                if (data == null) data = new String[]{"Other"};

                // add SELECT at top
                String[] finalData = new String[data.length + 1];
                finalData[0] = "-- Select Subcategory --";
                System.arraycopy(data, 0, finalData, 1, data.length);

                updateSubSpinner(spSubCategory, finalData);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });


        // ================= SAVE =================
        btnSave.setOnClickListener(v -> {

            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            int catPos = spCategory.getSelectedItemPosition();
            int subPos = spSubCategory.getSelectedItemPosition();

            if (title.isEmpty()) {
                Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (catPos == 0) {
                Toast.makeText(this, "Select category", Toast.LENGTH_SHORT).show();
                return;
            }

            if (subPos == 0) {
                Toast.makeText(this, "Select subcategory", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            long amount = Long.parseLong(amountStr);

            String category = spCategory.getSelectedItem().toString();
            String subCategory = spSubCategory.getSelectedItem().toString();

            saveExpenseToDb(title, category, subCategory, amount);
            dialog.dismiss();
        });


        dialog.setView(view);
        dialog.show();
    }

    private void updateSubSpinner(android.widget.Spinner spinner, String[] data) {

        android.widget.ArrayAdapter<String> adapter =
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        data);

        spinner.setAdapter(adapter);
    }

    private void saveExpenseToDb(String title,
                                 String category,
                                 String subCategory,
                                 long amount) {

        String monthKey = getMonthKey();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("expenses")
                .child(monthKey)
                .push();

        ref.child("title").setValue(title);
        ref.child("amount").setValue(amount);
        ref.child("category").setValue(category);
        ref.child("subcategory").setValue(subCategory);
        ref.child("createdAt").setValue(System.currentTimeMillis());

        Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();

        loadExpenses();
    }


}
