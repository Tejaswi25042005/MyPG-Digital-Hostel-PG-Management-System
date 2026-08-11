package com.srikanta.mypg.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.R;
import com.srikanta.mypg.tenants.TenantDetailActivity;

import java.util.List;

public class TenantAdapter
        extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {

    private final List<String> tenantIds;
    private final String hostelId; // ✅ ADD THIS

    public TenantAdapter(List<String> tenantIds, String hostelId) {
        this.tenantIds = tenantIds;
        this.hostelId = hostelId;
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TenantViewHolder holder,
            int position
    ) {

        String tenantId = tenantIds.get(position);

        holder.tvName.setText("Loading...");
        holder.tvMobile.setText("");

        loadTenantProfile(tenantId, holder);

        // ✅ CLICK → OPEN TENANT DETAIL
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    v.getContext(),
                    TenantDetailActivity.class
            );
            intent.putExtra("hostelId", hostelId);
            intent.putExtra("tenantId", tenantId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tenantIds.size();
    }

    // ================= LOAD TENANT PROFILE =================
    private void loadTenantProfile(
            String tenantId,
            TenantViewHolder holder
    ) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(tenantId)
                .child("Profile");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    holder.tvName.setText("Tenant");
                    holder.tvMobile.setText("");
                    return;
                }

                String name = snapshot.child("name").getValue(String.class);
                String mobile = snapshot.child("mobile").getValue(String.class);

                holder.tvName.setText(
                        name != null ? name : "Tenant"
                );
                holder.tvMobile.setText(
                        mobile != null ? "📞 " + mobile : ""
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.tvName.setText("Tenant");
                holder.tvMobile.setText("");
            }
        });
    }

    // ================= VIEW HOLDER =================
    static class TenantViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvMobile;

        TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTenantName);
            tvMobile = itemView.findViewById(R.id.tvTenantMobile);
        }
    }
}
