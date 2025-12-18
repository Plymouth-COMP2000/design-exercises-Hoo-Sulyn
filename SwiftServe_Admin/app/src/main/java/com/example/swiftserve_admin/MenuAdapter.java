package com.example.swiftserve_admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.bumptech.glide.Glide;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<MenuItem> menuList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MenuItem item);
    }

    public MenuAdapter(List<MenuItem> menuList, OnItemClickListener listener) {
        this.menuList = menuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_card, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuList.get(position);

        // 1. Set the standard text data
        holder.itemName.setText(item.getName());
        holder.itemPrice.setText("RM " + String.format("%.2f", item.getPrice()));

        Glide.with(holder.itemView.getContext())
                .load(item.getDescription())
                .placeholder(R.drawable.image_unavailable)
                .error(R.drawable.image_unavailable)
                .centerCrop()
                .into(holder.itemImage);

        // 2. Apply Conditional Styling based on Status
        if (item.getStatus() != null && item.getStatus().equalsIgnoreCase("Sold Out")) {
            holder.itemView.setAlpha(0.4f);
            holder.itemPrice.setText("SOLD OUT");
            holder.itemPrice.setTextColor(android.graphics.Color.RED);

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Sorry, " + item.getName() + " is currently unavailable", Toast.LENGTH_SHORT).show();
            });
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.itemPrice.setTextColor(android.graphics.Color.parseColor("#575757"));
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemPrice;
        ImageView itemImage;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs must match your item_menu_card.xml exactly
            itemName = itemView.findViewById(R.id.item_name);
            itemPrice = itemView.findViewById(R.id.item_price);
            itemImage = itemView.findViewById(R.id.item_image);
        }
    }

    public void updateList(List<MenuItem> newList) {
        this.menuList = newList;
        notifyDataSetChanged();
    }
}