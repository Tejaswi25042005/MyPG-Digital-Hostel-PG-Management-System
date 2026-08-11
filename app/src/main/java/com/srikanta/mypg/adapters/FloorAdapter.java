package com.srikanta.mypg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.FloorModel;

import java.util.List;

public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorViewHolder> {

    private final List<FloorModel> floorList;
    private final OnFloorClickListener listener;
    private int selectedPosition = 0;

    public interface OnFloorClickListener {
        void onFloorSelected(FloorModel floor);
    }

    public FloorAdapter(List<FloorModel> floorList, OnFloorClickListener listener) {
        this.floorList = floorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_floor, parent, false);
        return new FloorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorViewHolder holder, int position) {

        FloorModel model = floorList.get(position);
        holder.tvFloorName.setText(model.floorName);

        holder.itemView.setAlpha(position == selectedPosition ? 1f : 0.5f);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onFloorSelected(model);
        });
    }

    @Override
    public int getItemCount() {
        return floorList.size();
    }

    static class FloorViewHolder extends RecyclerView.ViewHolder {

        TextView tvFloorName;

        FloorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFloorName = itemView.findViewById(R.id.tvFloorName);
        }
    }
}
