package com.srikanta.mypg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.RecentActionModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecentActionAdapter
        extends RecyclerView.Adapter<RecentActionAdapter.VH> {

    private final List<RecentActionModel> list;

    public RecentActionAdapter(List<RecentActionModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_action, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int p) {

        RecentActionModel m = list.get(p);

        h.tvTitle.setText(m.getTitle());
        h.tvDesc.setText(m.getDescription());
        h.tvTime.setText(timeAgo(m.getTimestampMillis()));

        // Icon by type
        if ("PAYMENT".equals(m.getCategory())) {
            h.icon.setImageResource(R.drawable.ic_money);
        } else {
            h.icon.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView tvTitle, tvDesc, tvTime;

        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.imgActionIcon);
            tvTitle = v.findViewById(R.id.tvActionTitle);
            tvDesc = v.findViewById(R.id.tvActionDesc);
            tvTime = v.findViewById(R.id.tvActionTime);
        }
    }

    private String timeAgo(long time) {
        long diff = System.currentTimeMillis() - time;
        long mins = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (mins < 60) return mins + " min ago";
        long hrs = TimeUnit.MILLISECONDS.toHours(diff);
        if (hrs < 24) return hrs + " hrs ago";
        return TimeUnit.MILLISECONDS.toDays(diff) + " days ago";
    }
}
