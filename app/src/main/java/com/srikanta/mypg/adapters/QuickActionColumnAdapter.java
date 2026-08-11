package com.srikanta.mypg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import android.widget.TextView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.QuickActionModel;

import java.util.List;

public class QuickActionColumnAdapter
        extends RecyclerView.Adapter<QuickActionColumnAdapter.ViewHolder> {

    private final List<QuickActionModel> list;
    private final OnActionClick listener;
    private final OnActionLongClick longClickListener;

    private boolean isEditMode = false;

    // ================= EDIT MODE =================
    public void setEditMode(boolean enabled) {
        isEditMode = enabled;
        notifyDataSetChanged();
    }

    // ================= INTERFACES =================
    public interface OnActionClick {
        void onClick(int position);
    }

    public interface OnActionLongClick {
        void onLongClick(int position);
    }

    public QuickActionColumnAdapter(List<QuickActionModel> list,
                                    OnActionClick listener,
                                    OnActionLongClick longClickListener) {
        this.list = list;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_action_single, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        QuickActionModel model = list.get(position);

        holder.icon.setImageResource(model.getIcon());
        holder.title.setText(model.getTitle());

        // ================= NORMAL CLICK =================
        holder.card.setOnClickListener(v -> {
            if (!isEditMode) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onClick(pos);
                }

            }
        });

        holder.card.setOnLongClickListener(v -> {
            if (!model.isEmpty()) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    longClickListener.onLongClick(pos);
                }
            }
            return true;
        });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================
    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView card;
        ImageView icon;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.card);
            icon = itemView.findViewById(R.id.imgIcon);
            title = itemView.findViewById(R.id.tvTitle);
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

}
