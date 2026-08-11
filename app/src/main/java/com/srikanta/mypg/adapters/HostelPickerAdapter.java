package com.srikanta.mypg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;

import java.util.List;

public class HostelPickerAdapter
        extends RecyclerView.Adapter<HostelPickerAdapter.VH> {

    public interface OnHostelClick {
        void onClick(int position);
    }

    private final List<String> hostelNames;
    private final OnHostelClick listener;

    public HostelPickerAdapter(List<String> hostelNames,
                               OnHostelClick listener) {
        this.hostelNames = hostelNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hostel_picker, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.tvName.setText(hostelNames.get(position));
        h.itemView.setOnClickListener(v -> listener.onClick(position));
    }

    @Override
    public int getItemCount() {
        return hostelNames.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHostelName);
        }
    }
}

