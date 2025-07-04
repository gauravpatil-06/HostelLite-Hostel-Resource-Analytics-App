package com.example.trackingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class DailyUsageAdapter extends RecyclerView.Adapter<DailyUsageAdapter.UsageViewHolder> {

    private List<Map<String, Object>> usageList;

    public DailyUsageAdapter(List<Map<String, Object>> usageList) {
        this.usageList = usageList;
    }

    @NonNull
    @Override
    public UsageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_usage, parent, false);
        return new UsageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsageViewHolder holder, int position) {
        Map<String, Object> usage = usageList.get(position);
        holder.bind(usage);
    }

    @Override
    public int getItemCount() {
        return usageList.size();
    }

    static class UsageViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvDate, tvWaterUsage, tvElecUsage;

        public UsageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmailReport);
            tvDate = itemView.findViewById(R.id.tvDateReport);
            tvWaterUsage = itemView.findViewById(R.id.tvWaterUsageReport);
            tvElecUsage = itemView.findViewById(R.id.tvElectricityUsageReport);
        }

        public void bind(Map<String, Object> usage) {
            tvUserEmail.setText(usage.get("email") != null ? (String) usage.get("email") : "N/A");
            tvDate.setText((String) usage.get("date"));
            tvWaterUsage.setText("💧 Water: " + usage.get("waterUsed") + " L");
            tvElecUsage.setText("⚡️ Elec: " + usage.get("electricityUsed") + " kWh");
        }
    }
}