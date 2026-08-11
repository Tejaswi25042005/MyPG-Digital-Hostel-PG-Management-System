package com.srikanta.mypg.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;

import java.util.HashMap;
import java.util.Map;

public class PricingDialogHelper {

    private final Activity activity;
    private final String hostelId;

    public PricingDialogHelper(Activity activity, String hostelId) {
        this.activity = activity;
        this.hostelId = hostelId;
    }

    /* ================= OPEN PRICING ================= */

    public void openPricing() {

        View view = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_pricing, null);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(false)
                .create();

        EditText etSharing = view.findViewById(R.id.etSharing);
        EditText etSharingPrice = view.findViewById(R.id.etSharingPrice);
        EditText etDeposit = view.findViewById(R.id.etDeposit);
        LinearLayout layoutSharingList =
                view.findViewById(R.id.layoutSharingList);
        Button btnAddSharing = view.findViewById(R.id.btnAddSharing);

        // Holds all sharing prices
        Map<String, Integer> sharingPrices = new HashMap<>();

        DatabaseReference pricingRef = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("pricing");

        /* ================= LOAD EXISTING ================= */
        pricingRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        Integer deposit =
                                snapshot.child("deposit")
                                        .getValue(Integer.class);

                        if (deposit != null) {
                            etDeposit.setText(
                                    String.valueOf(deposit));
                        }

                        DataSnapshot sharingSnap =
                                snapshot.child("sharingPrices");

                        for (DataSnapshot s :
                                sharingSnap.getChildren()) {

                            String sharing = s.getKey();
                            Integer price =
                                    s.getValue(Integer.class);

                            if (sharing == null || price == null)
                                continue;

                            sharingPrices.put(sharing, price);
                            addSharingRow(
                                    sharing,
                                    price,
                                    layoutSharingList,
                                    sharingPrices
                            );
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {
                    }
                });

        /* ================= ADD SHARING ================= */
        btnAddSharing.setOnClickListener(v -> {

            String sharing =
                    etSharing.getText().toString().trim();
            String priceStr =
                    etSharingPrice.getText().toString().trim();

            if (sharing.isEmpty() || priceStr.isEmpty()) {
                toast("Enter sharing and price");
                return;
            }

            int price = Integer.parseInt(priceStr);
            sharingPrices.put(sharing, price);

            addSharingRow(
                    sharing,
                    price,
                    layoutSharingList,
                    sharingPrices
            );

            etSharing.setText("");
            etSharingPrice.setText("");
        });

        /* ================= CANCEL ================= */
        view.findViewById(R.id.btnCancel)
                .setOnClickListener(v -> dialog.dismiss());

        /* ================= SAVE ================= */
        view.findViewById(R.id.btnSave)
                .setOnClickListener(v -> {

                    String depositStr =
                            etDeposit.getText().toString().trim();

                    if (depositStr.isEmpty()) {
                        toast("Enter deposit amount");
                        return;
                    }

                    if (sharingPrices.isEmpty()) {
                        toast("Add at least one sharing price");
                        return;
                    }

                    savePricing(sharingPrices, depositStr);
                    dialog.dismiss();
                });

        dialog.show();
    }

    /* ================= SAVE ================= */

    private void savePricing(
            Map<String, Integer> sharingPrices,
            String depositStr) {

        DatabaseReference pricingRef =
                FirebaseDatabase.getInstance()
                        .getReference("Hostels")
                        .child(hostelId)
                        .child("pricing");

        int deposit = Integer.parseInt(depositStr);
        int deduction = 1000; // TODO: make dynamic if needed

        if (deduction > deposit) {
            toast("Deduction cannot be greater than deposit");
            return;
        }

        int refund = deposit - deduction;

        Map<String, Object> pricing = new HashMap<>();
        pricing.put("deposit", deposit);
        pricing.put("deduction", deduction);
        pricing.put("refund", refund);
        pricing.put("sharingPrices", sharingPrices);

        pricingRef.setValue(pricing)
                .addOnSuccessListener(v ->
                        toast("Pricing updated")
                )
                .addOnFailureListener(e ->
                        toast("Failed to update pricing")
                );
    }


    /* ================= UTIL ================= */

    private void toast(String msg) {
        Toast.makeText(
                activity,
                msg,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void addSharingRow(
            String sharing,
            int price,
            LinearLayout parent,
            Map<String, Integer> sharingPrices
    ) {

        View row = LayoutInflater.from(activity)
                .inflate(R.layout.item_sharing_price, parent, false);

        TextView tvSharing = row.findViewById(R.id.tvSharing);
        TextView tvPrice = row.findViewById(R.id.tvPrice);
        TextView btnEdit = row.findViewById(R.id.btnEdit);
        TextView btnDelete = row.findViewById(R.id.btnDelete);

        tvSharing.setText(sharing + " Sharing");
        tvPrice.setText("₹" + price);

        /* ================= EDIT ================= */
        btnEdit.setOnClickListener(v -> {

            View editView = LayoutInflater.from(activity)
                    .inflate(R.layout.dialog_edit_price, null);

            EditText etPrice = editView.findViewById(R.id.etEditPrice);
            etPrice.setText(String.valueOf(sharingPrices.get(sharing)));

            new AlertDialog.Builder(activity)
                    .setTitle("Update Price")
                    .setView(editView)
                    .setPositiveButton("Update", (d, w) -> {

                        String str = etPrice.getText().toString().trim();

                        if (str.isEmpty()) {
                            toast("Enter price");
                            return;
                        }

                        int newPrice = Integer.parseInt(str);

                        // update map
                        sharingPrices.put(sharing, newPrice);

                        // update UI
                        tvPrice.setText("₹" + newPrice);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        /* ================= DELETE ================= */
        btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(activity)
                    .setTitle("Delete")
                    .setMessage("Remove this sharing?")
                    .setPositiveButton("Yes", (d, w) -> {
                        sharingPrices.remove(sharing);
                        parent.removeView(row);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        parent.addView(row);
    }



}
