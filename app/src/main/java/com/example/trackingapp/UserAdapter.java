package com.example.trackingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<Map<String, Object>> userList;
    private OnItemClickListener itemClickListener;
    private OnUserDeleteListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(Map<String, Object> user);
    }

    public interface OnUserDeleteListener {
        void onDeleteClick(Map<String, Object> user);
    }

    public UserAdapter(List<Map<String, Object>> userList, OnItemClickListener itemClickListener, OnUserDeleteListener deleteListener) {
        this.userList = userList;
        this.itemClickListener = itemClickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, Object> user = userList.get(position);
        holder.bind(user, itemClickListener, deleteListener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail;
        ImageButton btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }

        public void bind(final Map<String, Object> user, final OnItemClickListener itemListener, final OnUserDeleteListener deleteListener) {
            tvUserEmail.setText((String) user.get("email"));

            itemView.setOnClickListener(v -> itemListener.onItemClick(user));
            btnDeleteUser.setOnClickListener(v -> deleteListener.onDeleteClick(user));
        }
    }
}