package com.srikanta.mypg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.NoticeModel;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.VH> {

    public interface OnNoticeClickListener {
        void onNoticeClick(NoticeModel notice);
    }

    private final List<NoticeModel> list;
    private final OnNoticeClickListener listener;

    public NoticeAdapter(List<NoticeModel> list, OnNoticeClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notice_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        NoticeModel n = list.get(position);

        h.tvTitle.setText(n.title);
        h.tvMessage.setText(n.message);
        h.tvType.setText(n.type);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoticeClick(n);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle, tvMessage, tvType;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}
