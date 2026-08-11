package com.srikanta.mypg.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.VacateRequestModel;
import com.srikanta.mypg.vacate.VacateRequestDetailActivity;

import java.util.List;

public class VacateRequestAdapter
        extends RecyclerView.Adapter<VacateRequestAdapter.VacateViewHolder> {

    private final Context context;
    private final List<VacateRequestModel> list;

    private final String hostelId;

    public VacateRequestAdapter(Context context,
                                List<VacateRequestModel> list,
                                String hostelId) {
        this.context = context;
        this.list = list;
        this.hostelId = hostelId;
    }


    @NonNull
    @Override
    public VacateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_vacate_request, parent, false);
        return new VacateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VacateViewHolder holder, int position) {

        VacateRequestModel model = list.get(position);

        // Name
        holder.tvTenantName.setText(model.getName());

        // Avatar
        if (!model.getName().isEmpty()) {
            holder.tvAvatar.setText(
                    model.getName().substring(0, 1).toUpperCase()
            );
        } else {
            holder.tvAvatar.setText("?");
        }

        // Paid till
        holder.tvRoomInfo.setText("Paid till: " + model.getPaidTill());

        // Vacate date
        holder.tvVacateDate.setText(
                "Vacate Date: " + model.getVacateDate()
        );

        // Amount info
        if (model.getTotalDue() > 0) {
            holder.tvReason.setText("Due Amount: ₹" + model.getTotalDue());
        } else if (model.getRefundAmount() > 0) {
            holder.tvReason.setText("Refund: ₹" + model.getRefundAmount());
        } else {
            holder.tvReason.setText("No dues / no refund");
        }

        // Status
        String status = model.getStatus();
        holder.tvStatus.setText(status);

        if ("PENDING".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_orange_stroke);
        } else if ("APPROVED".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_green_stroke);
        }

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, VacateRequestDetailActivity.class);
            intent.putExtra("hostelId", hostelId);   // send this
            intent.putExtra("requestId", model.getRequestId());
            context.startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ---------------- VIEW HOLDER ----------------

    static class VacateViewHolder extends RecyclerView.ViewHolder {

        TextView tvAvatar, tvTenantName, tvRoomInfo,
                tvStatus, tvVacateDate, tvReason;

        VacateViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvRoomInfo = itemView.findViewById(R.id.tvRoomInfo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvVacateDate = itemView.findViewById(R.id.tvVacateDate);
            tvReason = itemView.findViewById(R.id.tvReason);
        }
    }
}
