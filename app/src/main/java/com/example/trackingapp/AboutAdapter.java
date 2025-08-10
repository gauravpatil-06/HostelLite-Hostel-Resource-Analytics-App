package com.example.trackingapp;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.AboutViewHolder> {

    private List<AboutItem> items;

    public AboutAdapter(List<AboutItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public AboutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expandable_info, parent, false);
        return new AboutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AboutViewHolder holder, int position) {
        AboutItem item = items.get(position);
        holder.bind(item);

        holder.headerLayout.setOnClickListener(v -> {
            boolean isExpanded = item.isExpanded();
            item.setExpanded(!isExpanded);
            notifyItemChanged(position); // Animate the change
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder Class
    static class AboutViewHolder extends RecyclerView.ViewHolder {
        LinearLayout headerLayout;
        ImageView ivIcon, ivArrow;
        TextView tvTitle, tvContent;

        public AboutViewHolder(@NonNull View itemView) {
            super(itemView);
            headerLayout = itemView.findViewById(R.id.header_layout);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
        }

        void bind(AboutItem item) {
            ivIcon.setImageResource(item.getIconResId());
            tvTitle.setText(item.getTitle());
            tvContent.setText(item.getContent());

            // Set visibility and arrow rotation based on the expanded state
            tvContent.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
            ivArrow.setRotation(item.isExpanded() ? 180f : 0f);
        }
    }
}