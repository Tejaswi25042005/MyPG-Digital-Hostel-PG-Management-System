package com.srikanta.mypg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srikanta.mypg.R;
import com.srikanta.mypg.models.TenantPaymentModel;

import java.util.List;

public class TenantPaymentAdapter
        extends RecyclerView.Adapter<TenantPaymentAdapter.VH> {

    private final List<TenantPaymentModel> list;

    public TenantPaymentAdapter(List<TenantPaymentModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_payment_history, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        TenantPaymentModel m = list.get(position);

        // ---------- TITLE ----------
        h.tvType.setText("Payment");

        // ---------- AMOUNT ----------
        StringBuilder amountText = new StringBuilder();

        if (m.hasRent()) {
            amountText.append("Rent: ₹").append(m.getRentPaid());
        }

        if (m.hasDeposit()) {
            if (amountText.length() > 0) amountText.append("  •  ");
            amountText.append("Deposit: ₹").append(m.getDepositPaid());
        }

        h.tvAmount.setText(amountText.toString());

        // ---------- META ----------
        h.tvMeta.setText(
                m.getMonthText() + " • " + m.getPaidOnDisplay()
        );

        // ---------- COLOR ----------
        if (m.hasDeposit() && !m.hasRent()) {
            h.tvAmount.setTextColor(0xFF009688); // teal
        } else {
            h.tvAmount.setTextColor(0xFF3F51B5); // primary
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvType, tvAmount, tvMeta;

        VH(View v) {
            super(v);
            tvType = v.findViewById(R.id.tvType);
            tvAmount = v.findViewById(R.id.tvAmount);
            tvMeta = v.findViewById(R.id.tvMeta);
        }
    }
}
