package com.srikanta.mypg.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.RoomModel;

import java.util.List;

public class AvailableRoomAdapter
        extends RecyclerView.Adapter<AvailableRoomAdapter.RoomViewHolder> {

    public interface OnRoomSelectedListener {
        void onRoomSelected(RoomModel room);
    }

    private final List<RoomModel> list;
    private final OnRoomSelectedListener listener;

    private int selectedPosition = -1;

    public AvailableRoomAdapter(
            List<RoomModel> list,
            OnRoomSelectedListener listener
    ) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RoomViewHolder holder,
            @SuppressLint("RecyclerView") int position
    ) {

        RoomModel room = list.get(position);

        // -------- DATA --------
        holder.tvRoomNo.setText("Room " + room.getRoomNo());

        int freeBeds =
                room.getTotalBeds() - room.getOccupiedBeds();

        holder.tvBedInfo.setText(
                room.getTotalBeds() +
                        " Beds · " +
                        freeBeds +
                        " Available"
        );

        // -------- SELECTION UI --------
        boolean selected = position == selectedPosition;


        holder.layoutRoot.setBackgroundResource(
                selected
                        ? R.drawable.bg_room_selected
                        : R.drawable.bg_room_normal
        );

        // -------- CLICK --------
        holder.itemView.setOnClickListener(v -> {

            int oldPos = selectedPosition;
            selectedPosition = position;

            if (oldPos >= 0) notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onRoomSelected(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================
    static class RoomViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutRoot;
        TextView tvRoomNo, tvBedInfo;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutRoot = itemView.findViewById(R.id.layoutRoot);
            tvRoomNo = itemView.findViewById(R.id.tvRoomNo);
            tvBedInfo = itemView.findViewById(R.id.tvBedInfo);

        }
    }
}
