package com.sns.wanderwise.checklist;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import com.sns.wanderwise.R;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private final List<ChecklistItem> items;
    private final SparseBooleanArray selectedItems;

    public RecommendationAdapter(List<ChecklistItem> items) {
        this.items = items;
        selectedItems = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommended_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChecklistItem item = items.get(position);
        holder.title.setText(item.getName());
        holder.checkBox.setOnCheckedChangeListener(null); // Unbind previous listener
        holder.checkBox.setChecked(selectedItems.get(position, false));

        // Update selection state on checkbox click
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedItems.put(position, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Method to select or deselect all items
    public void selectAll(boolean isSelected) {
        for (int i = 0; i < items.size(); i++) {
            selectedItems.put(i, isSelected);
        }
        notifyDataSetChanged(); // Refresh the RecyclerView
    }

    public SparseBooleanArray getSelectedItems() {
        return selectedItems;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            title = itemView.findViewById(R.id.title);
        }
    }
}
