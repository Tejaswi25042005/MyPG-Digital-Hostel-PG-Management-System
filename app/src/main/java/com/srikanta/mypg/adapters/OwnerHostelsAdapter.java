package com.srikanta.mypg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.OwnerHostelModel;

import java.util.List;

public class OwnerHostelsAdapter
        extends RecyclerView.Adapter<OwnerHostelsAdapter.HostelViewHolder> {

    // ================= INTERFACE =================
    public interface HostelClickListener {
        void onHostelClick(OwnerHostelModel hostel);
        void onSetDefaultClick(OwnerHostelModel hostel);
    }

    private final List<OwnerHostelModel> list;
    private final HostelClickListener listener;

    public OwnerHostelsAdapter(
            List<OwnerHostelModel> list,
            HostelClickListener listener
    ) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HostelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        return new HostelViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_owner_hostel, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull HostelViewHolder h,
            int position
    ) {

        OwnerHostelModel model = list.get(position);

        // ================= BASIC INFO =================
        h.tvName.setText(model.getName());
        h.tvAddress.setText(model.getAddress());

        // ================= STATUS =================
        if ("active".equalsIgnoreCase(model.getStatus())) {
            h.tvStatus.setText("ACTIVE");
            h.tvStatus.setTextColor(
                    h.itemView.getContext().getColor(R.color.darkgreen)
            );
            h.tvStatus.setBackgroundResource(R.drawable.bg_status_active);
        } else {
            h.tvStatus.setText("INACTIVE");
            h.tvStatus.setTextColor(
                    h.itemView.getContext().getColor(R.color.red)
            );
            h.tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
        }

        // ================= DEFAULT HOSTEL =================
        if (model.isDefault()) {
            h.tvDefaultHostel.setText("DEFAULT");
            h.tvDefaultHostel.setTextColor(
                    h.itemView.getContext().getColor(R.color.white)
            );
            h.tvDefaultHostel.setBackgroundResource(
                    R.drawable.bg_default_filled
            );
        } else {
            h.tvDefaultHostel.setText("Set as Default");
            h.tvDefaultHostel.setTextColor(
                    h.itemView.getContext().getColor(R.color.primary)
            );
            h.tvDefaultHostel.setBackgroundResource(
                    R.drawable.bg_default_outline
            );
        }

        // ================= STATS =================
        h.tvRoomsCount.setText(String.valueOf(model.getRoomsCount()));
        h.tvBedsCount.setText(String.valueOf(model.getBedsCount()));
        h.tvOccupiedCount.setText(String.valueOf(model.getOccupiedCount()));

        // ================= CLICKS =================
        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHostelClick(model);
            }
        });

        h.tvDefaultHostel.setOnClickListener(v -> {
            if (listener != null && !model.isDefault()) {
                listener.onSetDefaultClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================
    static class HostelViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvAddress, tvStatus;
        TextView tvRoomsCount, tvBedsCount, tvOccupiedCount;
        TextView tvDefaultHostel;

        HostelViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvHostelName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            tvRoomsCount = itemView.findViewById(R.id.tvRoomsCount);
            tvBedsCount = itemView.findViewById(R.id.tvBedsCount);
            tvOccupiedCount = itemView.findViewById(R.id.tvOccupiedCount);

            tvDefaultHostel = itemView.findViewById(R.id.tvDefaultHostel);
        }
    }
}
