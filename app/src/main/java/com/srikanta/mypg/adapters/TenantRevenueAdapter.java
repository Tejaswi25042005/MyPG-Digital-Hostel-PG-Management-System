package com.srikanta.mypg.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.models.TenantRevenueModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TenantRevenueAdapter
        extends RecyclerView.Adapter<TenantRevenueAdapter.ViewHolder> {

    private final List<TenantRevenueModel> list;
    private final String hostelId;
    private final Runnable refreshCallback;

    private String monthKey;

    public TenantRevenueAdapter(
            List<TenantRevenueModel> list,
            String hostelId,
            Runnable refreshCallback
    ) {
        this.list = list;
        this.hostelId = hostelId;
        this.refreshCallback = refreshCallback;
    }

    public void setMonthKey(String monthKey) {
        this.monthKey = monthKey;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant_revenue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        TenantRevenueModel model = list.get(position);

        // ---------- NAME ----------
        holder.tvTenantName.setText(
                model.getName() == null ? "Tenant" : model.getName()
        );

        // ---------- TENANT TYPE ----------
        String type = model.getTenantType();
        holder.tvTenantType.setText(
                type == null ? "Tenant" : type
        );

        // ---------- ROOM INFO ----------
        holder.tvRoomInfo.setText(
                "Room " + model.getRoomNo() +
                        " • Floor " + model.getFloorNo()
        );

        // ---------- RENT ----------
        int rent = model.getRentAmount();
        holder.tvRentAmount.setText("₹" + rent);

        // ---------- STATUS ----------
        String status = model.getStatus();
        holder.tvPaymentStatus.setText(status);

        if ("PAID".equalsIgnoreCase(status)) {
            holder.tvPaymentStatus.setTextColor(
                    Color.parseColor("#4CAF50")
            );
        } else {
            holder.tvPaymentStatus.setTextColor(
                    Color.parseColor("#F44336")
            );
        }

        // ---------- PAYMENT SUMMARY ----------
        int rentPaid = model.getRentPaidAmount();
        int deposit = model.getDepositAmount();

        // 🟢 JOINING MONTH (NEW)
        if ("NEW".equalsIgnoreCase(type)) {

            holder.tvPaymentSummary.setText(
                    "Collected: ₹" + rentPaid +
                            " + ₹" + deposit + " (Deposit)"
            );
            holder.tvPaymentSummary.setTextColor(
                    Color.parseColor("#4CAF50")
            );

            // Optional UX improvement
            holder.tvTenantType.setText("NEW • Joining Month");
            holder.tvTenantType.setTextColor(Color.parseColor("#2196F3"));


        } else {
            // 🔵 REGULAR MONTH
            int pending = Math.max(rent - rentPaid, 0);

            if (pending > 0) {
                holder.tvPaymentSummary.setText(
                        "Pending: ₹" + pending
                );
                holder.tvPaymentSummary.setTextColor(
                        Color.parseColor("#F44336")
                );
            } else {
                holder.tvPaymentSummary.setText(
                        "Collected: ₹" + rentPaid
                );
                holder.tvPaymentSummary.setTextColor(
                        Color.parseColor("#4CAF50")
                );
            }
        }

        holder.itemView.setOnClickListener(v -> {

            int paid = model.getRentPaidAmount();
            int pending = Math.max(rent - paid, 0);

            if (pending <= 0) {
                android.widget.Toast.makeText(
                        v.getContext(),
                        "No dues for this tenant",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
                return;
            }

            showCollectDialog(v.getContext(), model);
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTenantName;
        TextView tvTenantType;
        TextView tvRoomInfo;
        TextView tvRentAmount;
        TextView tvPaymentStatus;
        TextView tvPaymentSummary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvTenantType = itemView.findViewById(R.id.tvTenantType);
            tvRoomInfo = itemView.findViewById(R.id.tvRoomInfo);
            tvRentAmount = itemView.findViewById(R.id.tvRentAmount);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvPaymentSummary =
                    itemView.findViewById(R.id.tvPaymentSummary);
        }
    }

    private void showCollectDialog(Context context, TenantRevenueModel model) {

        // ❌ If no due, don’t open dialog
        int rent = model.getRentAmount();
        int paid = model.getRentPaidAmount();
        int pending = Math.max(rent - paid, 0);

        if (pending <= 0) {
            Toast.makeText(context, "No due for this tenant", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_collect_rent, null);

        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvRoom = view.findViewById(R.id.tvRoom);
        TextView tvRent = view.findViewById(R.id.tvRent);
        TextView tvPaid = view.findViewById(R.id.tvPaid);
        TextView tvPending = view.findViewById(R.id.tvPending);

        EditText etAmount = view.findViewById(R.id.etAmount);

        RadioGroup rgPayment = view.findViewById(R.id.rgPaymentType);
        RadioButton rbFull = view.findViewById(R.id.rbFull);
        RadioButton rbPartial = view.findViewById(R.id.rbPartial);

        FrameLayout swipeLayout = view.findViewById(R.id.swipeLayout);
        View swipeThumb = view.findViewById(R.id.swipeThumb);


        // ================= SET DATA =================
        tvName.setText(model.getName());
        tvRoom.setText(
                "Room " + model.getRoomNo() + " • Floor " + model.getFloorNo()
        );
        tvRent.setText("Rent: ₹" + rent);
        tvPaid.setText("Paid: ₹" + paid);
        tvPending.setText("Pending: ₹" + pending);

        // Default → FULL payment
        rbFull.setChecked(true);
        etAmount.setVisibility(View.GONE);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();

        // ================= RADIO LOGIC =================
        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPartial) {
                etAmount.setVisibility(View.VISIBLE);
            } else {
                etAmount.setVisibility(View.GONE);
                etAmount.setText("");
            }
        });

        // ================= COLLECT ACTION =================
        swipeThumb.setOnTouchListener(new View.OnTouchListener() {

            float dX = 0;
            boolean completed = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        float newX = event.getRawX() + dX;
                        float max = swipeLayout.getWidth() - v.getWidth();

                        if (newX < 0) newX = 0;
                        if (newX > max) newX = max;

                        v.setX(newX);

                        // ✅ reached end → try collect
                        if (newX >= max * 0.95 && !completed) {
                            completed = true;

                            int amountToPay;

                            // FULL
                            if (rbFull.isChecked()) {
                                amountToPay = pending;
                            } else {
                                String input = etAmount.getText().toString().trim();

                                if (input.isEmpty()) {
                                    etAmount.setError("Enter amount");
                                    resetThumb(v);
                                    completed = false;
                                    return true;
                                }

                                try {
                                    amountToPay = Integer.parseInt(input);
                                } catch (Exception e) {
                                    etAmount.setError("Invalid number");
                                    resetThumb(v);
                                    completed = false;
                                    return true;
                                }

                                if (amountToPay <= 0 || amountToPay > pending) {
                                    etAmount.setError("Amount must be ≤ ₹" + pending);
                                    resetThumb(v);
                                    completed = false;
                                    return true;
                                }
                            }

                            // ✅ SUCCESS
                            collectRent(context, model, amountToPay);
                            dialog.dismiss();
                        }

                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!completed) {
                            resetThumb(v);
                        }
                        return true;
                }
                return false;
            }

            private void resetThumb(View v) {
                v.animate().x(0).setDuration(200).start();
            }
        });


        dialog.show();
    }
    private void collectRent(
            Context context,
            TenantRevenueModel model,
            int amount
    ) {

        DatabaseReference tenantRef =
                FirebaseDatabase.getInstance()
                        .getReference("Hostels")
                        .child(hostelId)
                        .child("tenants")
                        .child(model.getTenantId());

        DatabaseReference monthRef =
                tenantRef
                        .child("payments")
                        .child(monthKey);

        monthRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long rent = model.getRentAmount();

                long currentPaid = 0;
                long currentDue = rent;

                if (snapshot.exists()) {
                    Long paid = snapshot.child("rentPaid").getValue(Long.class);
                    Long due  = snapshot.child("rentDue").getValue(Long.class);

                    if (paid != null) currentPaid = paid;
                    if (due != null)  currentDue  = due;
                }

                long newPaid = currentPaid + amount;
                long newDue  = Math.max(rent - newPaid, 0);

                long nowMillis = System.currentTimeMillis();

                String paidOn = new SimpleDateFormat(
                        "ddMMyyyyHHmm",
                        Locale.getDefault()
                ).format(new Date(nowMillis));

                Map<String, Object> updates = new HashMap<>();
                updates.put("rent", rent);
                updates.put("rentPaid", newPaid);
                updates.put("rentDue", newDue);
                updates.put("paidOn", paidOn);

                monthRef.updateChildren(updates)
                        .addOnSuccessListener(unused -> {

                            // ✅ UPDATE RENT PAID TILL ONLY IF MONTH IS FULLY PAID
                            if (newDue == 0) {

                                try {
                                    // monthKey = yyyy-MM
                                    SimpleDateFormat ymFmt =
                                            new SimpleDateFormat("yyyy-MM", Locale.US);

                                    Date monthDate = ymFmt.parse(monthKey);

                                    Calendar paidTillCal = Calendar.getInstance();
                                    paidTillCal.setTime(monthDate);

                                    // 🔑 Move to end of that month
                                    paidTillCal.set(Calendar.DAY_OF_MONTH,
                                            paidTillCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                                    paidTillCal.set(Calendar.HOUR_OF_DAY, 23);
                                    paidTillCal.set(Calendar.MINUTE, 59);
                                    paidTillCal.set(Calendar.SECOND, 59);
                                    paidTillCal.set(Calendar.MILLISECOND, 999);

                                    tenantRef
                                            .child("rentPaidTill")
                                            .setValue(paidTillCal.getTimeInMillis());

                                } catch (Exception ignored) {}
                            }

                            logRentPaidAction(
                                    model,
                                    amount,
                                    paidOn,
                                    nowMillis
                            );

                            if (refreshCallback != null) {
                                refreshCallback.run();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        context,
                        "Failed to collect rent",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void logRentPaidAction(
            TenantRevenueModel model,
            int amount,
            String paidOn,
            long nowMillis
    ) {

        String monthYear = monthKey; // yyyy-MM

        DatabaseReference actionRef =
                FirebaseDatabase.getInstance()
                        .getReference("Hostels")
                        .child(hostelId)
                        .child("actions")
                        .child(monthYear)
                        .push();

        actionRef.child("actionType").setValue("RENT_PAID");
        actionRef.child("category").setValue("PAYMENT");
        actionRef.child("title").setValue("Rent Paid");

        actionRef.child("description").setValue(
                "₹" + amount + " rent collected from " + model.getName()
        );

        actionRef.child("tenantId").setValue(model.getTenantId());
        actionRef.child("tenantName").setValue(model.getName());
        actionRef.child("roomNumber").setValue(model.getRoomNo());
        actionRef.child("floorNo").setValue(model.getFloorNo());
        actionRef.child("amount").setValue(amount);

        actionRef.child("timestamp").setValue(paidOn);          // String
        actionRef.child("timestampMillis").setValue(nowMillis); // Long (for sorting)

        actionRef.child("triggeredBy").setValue("OWNER");
    }




}
