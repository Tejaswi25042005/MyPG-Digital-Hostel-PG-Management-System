package com.srikanta.mypg.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.ServiceRequestModel;
import com.srikanta.mypg.servicerequests.ServiceRequestDetailActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ServiceRequestAdapter
        extends RecyclerView.Adapter<ServiceRequestAdapter.RequestViewHolder> {

    private final Context context;
    private final List<ServiceRequestModel> list;
    private final String hostelId;


    public interface OnRequestClickListener {
        void onRequestClick(ServiceRequestModel model);
    }

    private final OnRequestClickListener listener;

    public ServiceRequestAdapter(
            Context context,
            String hostelId,
            List<ServiceRequestModel> list,
            OnRequestClickListener listener
    ) {
        this.context = context;
        this.hostelId = hostelId;
        this.list = list;
        this.listener = listener;
    }


    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_owner_service_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RequestViewHolder holder, int position) {

        ServiceRequestModel m = list.get(position);

        holder.tvTenantName.setText(m.getTenantName());

        String name = m.getTenantName();
        holder.tvAvatar.setText(
                name != null && !name.isEmpty()
                        ? name.substring(0, 1).toUpperCase()
                        : "?"
        );

        // -------- FLOOR FROM ROOM NO --------

        holder.tvIssueTitle.setText(m.getTitle());
        holder.tvStatus.setText(m.getStatus());

        // -------- DATE --------
        String createdAt = m.getCreatedAt();
        if (createdAt != null) {
            try {
                Date d = new SimpleDateFormat(
                        "ddMMyyyyHHmm",
                        Locale.getDefault()
                ).parse(createdAt);

                holder.tvDate.setText(
                        new SimpleDateFormat(
                                "dd MMM yyyy",
                                Locale.getDefault()
                        ).format(d)
                );
            } catch (Exception e) {
                holder.tvDate.setText("");
            }
        } else {
            holder.tvDate.setText("");
        }

        // -------- STATUS COLOR --------
        switch (m.getStatus()) {
            case "OPEN":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336"));
                break;

            case "IN_PROGRESS":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
                break;

            case "RESOLVED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;

            default:
                holder.tvStatus.setBackgroundColor(Color.GRAY);
        }

        holder.itemView.setOnClickListener(v -> {

            Intent i = new Intent(context, ServiceRequestDetailActivity.class);
            i.putExtra("HOSTEL_ID", hostelId);
            i.putExtra("REQUEST_ID", m.getRequestId());
            context.startActivity(i);
        });

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    // ================= FLOOR LOGIC =================
    private int getFloorFromRoom(String roomNo) {
        if (roomNo == null || roomNo.isEmpty()) return 0;

        try {
            return Integer.parseInt(roomNo.substring(0, 1));
        } catch (Exception e) {
            return 0;
        }
    }

    // ================= VIEW HOLDER =================
    static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView tvTenantName,
                tvIssueTitle,
                tvDate, tvStatus, tvAvatar;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvIssueTitle = itemView.findViewById(R.id.tvIssueTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
        }
    }
}
