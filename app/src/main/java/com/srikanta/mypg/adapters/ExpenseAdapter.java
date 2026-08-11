package com.srikanta.mypg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.ExpenseModel;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {

    private final List<ExpenseModel> list;

    public ExpenseAdapter(List<ExpenseModel> list) {
        this.list = list;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount;

        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvAmount = v.findViewById(R.id.tvAmount);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_expense, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ExpenseModel m = list.get(pos);

        h.tvTitle.setText(m.getTitle());
        h.tvCategory.setText(m.getCategory() + " • " + m.getSubcategory());
        h.tvAmount.setText("₹" + m.getAmount());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
