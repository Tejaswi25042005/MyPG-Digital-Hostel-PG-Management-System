package com.srikanta.mypg.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.VH> {

    public interface OnDayClickListener {
        void onDayClick(String day);
    }

    private final List<String> daysOrdered;
    private final String today;
    private int selectedPosition = -1;
    private final OnDayClickListener listener;

    public DayAdapter(String today, OnDayClickListener listener) {
        this.today = today;
        this.listener = listener;
        this.daysOrdered = buildCenteredDays(today);
        this.selectedPosition = daysOrdered.indexOf(today);
    }

    // 🔥 CORE LOGIC: today in the middle
    private List<String> buildCenteredDays(String today) {

        List<String> base = Arrays.asList(
                "monday", "tuesday", "wednesday",
                "thursday", "friday", "saturday", "sunday"
        );

        int todayIndex = base.indexOf(today);
        List<String> result = new ArrayList<>();

        for (int i = 3; i >= 1; i--) {
            result.add(base.get((todayIndex - i + 7) % 7));
        }

        result.add(today);

        for (int i = 1; i <= 3; i++) {
            result.add(base.get((todayIndex + i) % 7));
        }

        return result;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        Context c = h.itemView.getContext();
        int white = ContextCompat.getColor(c, R.color.white);
        int black = ContextCompat.getColor(c, R.color.black);

        String day = daysOrdered.get(position);
        h.tvDay.setText(day.substring(0, 3).toUpperCase());

        // ===== RESET =====
        h.tvDay.setBackgroundResource(R.drawable.bg_day_normal);
        h.tvDay.setTextColor(black);
        h.itemView.setScaleX(1f);
        h.itemView.setScaleY(1f);

        // ===== TODAY (only if NOT selected) =====
        if (day.equals(today) && position != selectedPosition) {
            h.tvDay.setBackgroundResource(R.drawable.bg_day_today);
            h.tvDay.setTextColor(white);
        }

        // ===== SELECTED (wins over everything) =====
        if (position == selectedPosition) {
            h.tvDay.setBackgroundResource(R.drawable.bg_day_selected);
            h.tvDay.setTextColor(black);

            h.itemView.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .start();
        }

        // ===== CLICK =====
        h.itemView.setOnClickListener(v -> {

            int oldPosition = selectedPosition;
            selectedPosition = h.getAdapterPosition();

            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
            notifyItemChanged(selectedPosition);

            listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() {
        return daysOrdered.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDay;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
        }
    }
}
