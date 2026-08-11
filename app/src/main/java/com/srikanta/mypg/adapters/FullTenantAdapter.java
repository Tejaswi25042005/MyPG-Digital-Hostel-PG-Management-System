package com.srikanta.mypg.adapters;

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
import com.srikanta.mypg.models.TenantModel;

import java.util.List;

public class FullTenantAdapter
        extends RecyclerView.Adapter<FullTenantAdapter.TenantViewHolder> {

    private final List<TenantModel> tenantList;
    private final TenantClickListener listener;

    // ================= LISTENER =================
    public interface TenantClickListener {
        void onTenantClick(TenantModel tenant);
    }

    public FullTenantAdapter(
            List<TenantModel> tenantList,
            TenantClickListener listener
    ) {
        this.tenantList = tenantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant_full, parent, false);
        return new TenantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TenantViewHolder holder,
            int position
    ) {

        TenantModel tenant = tenantList.get(position);

        String name = tenant.getName();
        holder.tvAvatar.setText(
                name != null && !name.isEmpty()
                        ? name.substring(0, 1).toUpperCase()
                        : "?"
        );

        // ---------- PLACEHOLDERS ----------
        holder.tvName.setText("Loading...");
        holder.tvMobile.setText("");

        holder.tvRoom.setText(
                "Floor " + tenant.getFloorNo() +
                        " • Room " + tenant.getRoomNo()
        );

        // ---------- LOAD USER PROFILE ----------
        loadTenantProfile(tenant.getTenantId(), holder);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTenantClick(tenant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
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

        TextView tvName, tvMobile, tvRoom, tvAvatar;

        TenantViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvTenantName);
            tvMobile = itemView.findViewById(R.id.tvTenantMobile);
            tvRoom = itemView.findViewById(R.id.tvTenantRoom);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
        }
    }
}
