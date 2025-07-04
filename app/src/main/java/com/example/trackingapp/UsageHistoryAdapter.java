package com.example.trackingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class UsageHistoryAdapter extends RecyclerView.Adapter<UsageHistoryAdapter.HistoryViewHolder> {

    // ⭐ MODIFIED: Use List<Map<String, Object>> for dummy data
    private final List<Map<String, Object>> historyList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        // ⭐ MODIFIED: Interface uses Map<String, Object>
        void onItemClick(Map<String, Object> document);
    }

    // ⭐ MODIFIED: Constructor accepts Map list
    public UsageHistoryAdapter(List<Map<String, Object>> historyList, OnItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Assuming R.layout.item_usage_history exists
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usage_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Map<String, Object> doc = historyList.get(position);
        holder.bind(doc, listener);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryDate, tvHistoryWater, tvHistoryElec;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
            tvHistoryWater = itemView.findViewById(R.id.tvHistoryWater);
            tvHistoryElec = itemView.findViewById(R.id.tvHistoryElec);
        }

        // ⭐ MODIFIED: bind method accepts Map
        public void bind(final Map<String, Object> doc, final OnItemClickListener listener) {

            // Data retrieval uses Map.get() and safe casting
            String date = (String) doc.get("date");
            Number water = (Number) doc.get("waterUsed");
            Number elec = (Number) doc.get("electricityUsed");

            tvHistoryDate.setText(date);

            // Safe display with default values
            tvHistoryWater.setText("💧 " + (water != null ? water.longValue() : 0L) + " L");
            tvHistoryElec.setText("⚡️ " + (elec != null ? elec.doubleValue() : 0.0) + " kWh");

            itemView.setOnClickListener(v -> listener.onItemClick(doc));
        }
    }
}