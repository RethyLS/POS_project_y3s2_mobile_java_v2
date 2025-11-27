package com.example.pos_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_project.R;
import com.example.pos_project.model.Sale;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SalesHistoryAdapter extends RecyclerView.Adapter<SalesHistoryAdapter.SalesViewHolder> {

    private List<Sale> salesList;

    public SalesHistoryAdapter(List<Sale> salesList) {
        this.salesList = salesList;
    }

    @NonNull
    @Override
    public SalesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sale_history, parent, false);
        return new SalesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesViewHolder holder, int position) {
        Sale sale = salesList.get(position);
        holder.bind(sale);
    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    static class SalesViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTransactionId, tvDate, tvTotalAmount, tvPaymentMethod, tvStatus;

        public SalesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionId = itemView.findViewById(R.id.tv_transaction_id);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }

        public void bind(Sale sale) {
            // Use transaction ID from API, fallback to generated ID if null
            String transactionId = sale.getTransactionId();
            if (transactionId != null && !transactionId.isEmpty()) {
                tvTransactionId.setText(transactionId);
            } else {
                tvTransactionId.setText("TXN-" + sale.getId());
            }

            // Format date from API created_at field
            String createdAt = sale.getCreatedAt();
            if (createdAt != null && !createdAt.isEmpty()) {
                try {
                    // Try ISO 8601 format first (Laravel default)
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                    java.util.Date date = isoFormat.parse(createdAt);
                    tvDate.setText(outputFormat.format(date));
                } catch (Exception e) {
                    try {
                        // Fallback to standard format
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                        java.util.Date date = inputFormat.parse(createdAt);
                        tvDate.setText(outputFormat.format(date));
                    } catch (Exception e2) {
                        tvDate.setText(createdAt); // Fallback to original format
                    }
                }
            } else {
                tvDate.setText("N/A");
            }

            // Format total amount
            tvTotalAmount.setText(String.format("$%.2f", sale.getTotalAmount()));

            // Payment method
            tvPaymentMethod.setText(capitalizeFirst(sale.getPaymentMethod()));

            // Status from API
            String status = sale.getStatus();
            tvStatus.setText(capitalizeFirst(status != null ? status : "Completed"));
        }

        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
    }
}