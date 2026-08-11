package com.srikanta.mypg.adapters;

import android.content.Context;
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

public class RoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ROOM = 1;
    private static final int TYPE_ADD_ROOM = 2;

    private final List<Object> list;
    private final RoomClickListener listener;

    // ================= LISTENER =================
    public interface RoomClickListener {
        void onRoomClick(RoomModel room);
        void onAddRoomClick();
    }

    public RoomAdapter(List<Object> list, RoomClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    // ================= VIEW TYPE =================
    @Override
    public int getItemViewType(int position) {
        return list.get(position) instanceof RoomModel
                ? TYPE_ROOM
                : TYPE_ADD_ROOM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_ROOM) {
            return new RoomViewHolder(
                    inflater.inflate(R.layout.item_room, parent, false)
            );
        } else {
            return new AddRoomViewHolder(
                    inflater.inflate(R.layout.item_add_room, parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {

        if (holder instanceof RoomViewHolder) {

            RoomModel model = (RoomModel) list.get(position);
            RoomViewHolder h = (RoomViewHolder) holder;

            h.tvRoomNo.setText("Room " + model.getRoomNo());

            // -------- BED INDICATORS --------
            h.layoutBeds.removeAllViews();

            int totalBeds = model.getTotalBeds();
            int freeBeds = model.getFreeBeds();
            Context context = h.itemView.getContext();

            for (int i = 0; i < totalBeds; i++) {

                ImageView bed = new ImageView(context);

                bed.setImageResource(
                        i < freeBeds
                                ? R.drawable.ic_bed_green
                                : R.drawable.ic_bed_red
                );

                // 🔹 Slightly smaller size (24dp)
                int size = (int) (24 * context.getResources().getDisplayMetrics().density);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(size, size);

                // 🔹 Smaller spacing between beds
                params.setMarginEnd((int) (4 * context.getResources().getDisplayMetrics().density));

                bed.setLayoutParams(params);

                // ❌ removed background circle
                h.layoutBeds.addView(bed);
            }


            h.itemView.setOnClickListener(v ->
                    listener.onRoomClick(model)
            );

        } else {
            holder.itemView.setOnClickListener(v ->
                    listener.onAddRoomClick()
            );
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDERS =================
    static class RoomViewHolder extends RecyclerView.ViewHolder {

        TextView tvRoomNo;
        LinearLayout layoutBeds;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomNo = itemView.findViewById(R.id.tvRoomNo);
            layoutBeds = itemView.findViewById(R.id.layoutBeds);
        }
    }

    static class AddRoomViewHolder extends RecyclerView.ViewHolder {
        AddRoomViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
