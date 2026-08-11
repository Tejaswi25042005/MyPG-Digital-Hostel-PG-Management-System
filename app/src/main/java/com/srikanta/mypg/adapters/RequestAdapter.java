package com.srikanta.mypg.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.models.RequestModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private final Context context;
    private final List<RequestModel> list;
    private final String hostelId;
    private final OnRequestClickListener listener;

    public RequestAdapter(
            Context context,
            List<RequestModel> list,
            String hostelId,
            OnRequestClickListener listener
    ) {
        this.context = context;
        this.list = list;
        this.hostelId = hostelId;
        this.listener = listener;
    }


    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RequestViewHolder holder,
            int position
    ) {

        RequestModel model = list.get(position);

        // -------- TENANT INFO --------
        holder.tvTenantName.setText("Loading...");
        holder.tvTenantMobile.setText("");

        loadTenantDetails(
                model.getTenantId(),
                holder.tvTenantName,
                holder.tvTenantMobile
        );

        // -------- SHARING & RENT --------
        holder.tvSharing.setText(model.getSharing() + " Sharing");
        holder.tvRent.setText("· ₹" + model.getRent());

        // -------- DATE --------
        String requestedAt = model.getRequestedAt();

        if (requestedAt != null && !requestedAt.isEmpty()) {
            try {
                Date date = new SimpleDateFormat(
                        "ddMMyyyyHHmm",
                        Locale.getDefault()
                ).parse(requestedAt);

                String formatted = new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                ).format(date);

                holder.tvRequestedDate.setText("Requested on " + formatted);

            } catch (Exception e) {
                holder.tvRequestedDate.setText("");
            }
        } else {
            holder.tvRequestedDate.setText("");
        }


        // -------- STATUS --------
        holder.tvStatus.setText(model.getStatus());
        setStatusBackground(holder.tvStatus, model.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRequestClick(model);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================
    static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView tvTenantName, tvTenantMobile;
        TextView tvRequestedDate, tvStatus, tvSharing, tvRent;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvTenantMobile = itemView.findViewById(R.id.tvTenantMobile);
            tvRequestedDate = itemView.findViewById(R.id.tvRequestedDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSharing = itemView.findViewById(R.id.tvSharing);
            tvRent = itemView.findViewById(R.id.tvRent);
        }
    }

    // ================= LOAD TENANT =================
    private void loadTenantDetails(
            String tenantId,
            TextView tvName,
            TextView tvMobile
    ) {

        DatabaseReference tenantRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(tenantId)
                .child("Profile");

        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    tvName.setText("Tenant");
                    tvMobile.setText("");
                    return;
                }

                String name = snapshot.child("name").getValue(String.class);
                String mobile = snapshot.child("mobile").getValue(String.class);

                tvName.setText(name != null ? name : "Tenant");
                tvMobile.setText(
                        mobile != null ? "📞 " + mobile : ""
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvName.setText("Tenant");
                tvMobile.setText("");
            }
        });
    }

    // ================= STATUS UI =================
    private void setStatusBackground(TextView tv, String status) {

        if ("APPROVED".equals(status)) {
            tv.setBackgroundResource(R.drawable.bg_status_approved);
        } else if ("REJECTED".equals(status)) {
            tv.setBackgroundResource(R.drawable.bg_status_rejected);
        } else {
            tv.setBackgroundResource(R.drawable.bg_status_pending);
        }
    }

    public interface OnRequestClickListener {
        void onRequestClick(RequestModel model);
    }


}
