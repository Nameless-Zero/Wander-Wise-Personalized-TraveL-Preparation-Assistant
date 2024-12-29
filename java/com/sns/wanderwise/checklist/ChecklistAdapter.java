package com.sns.wanderwise.checklist;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sns.wanderwise.R;

import java.util.List;

public class ChecklistAdapter extends BaseAdapter {
    private Context context;
    private List<ChecklistItem> items;
    private SparseBooleanArray selectedItems = new SparseBooleanArray(); // Tracks selected items

    public ChecklistAdapter(Context context, List<ChecklistItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView =
                    LayoutInflater.from(context)
                            .inflate(R.layout.list_item_checklist, parent, false);
            holder = new ViewHolder();
            holder.itemName = convertView.findViewById(R.id.checklist_item_name);
            holder.itemWeight = convertView.findViewById(R.id.checklist_item_weight);
            holder.itemQuantity =
                    convertView.findViewById(R.id.checklist_item_quantity); 
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChecklistItem item = items.get(position);
        holder.itemName.setText(item.getName());

        int weightInGrams = item.getWeight();
        String weightText =
                weightInGrams >= 1000
                        ? String.format("%.1f kg", weightInGrams / 1000.0)
                        : weightInGrams + " g";
        holder.itemWeight.setText(weightText);

        holder.itemQuantity.setText("X" + item.getQuantity());

        if (selectedItems.get(position, false)) {
            convertView.setBackgroundColor(
                    ContextCompat.getColor(context, android.R.color.darker_gray));
        } else {
            convertView.setBackgroundColor(
                    ContextCompat.getColor(context, android.R.color.transparent));
        }

        return convertView;
    }

    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedItems.size();
    }
   public void selectAll() {
    for (int i = 0; i < getCount(); i++) {
        selectedItems.put(i, true); 
    }
    notifyDataSetChanged(); 
}

    
    public int getFirstSelectedPosition() {
        for (int i = 0; i < selectedItems.size(); i++) {
            int key = selectedItems.keyAt(i);
            if (selectedItems.get(key)) {
                return key;
            }
        }
        return -1;
    }

    public void removeSelectedItems() {
        for (int i = selectedItems.size() - 1; i >= 0; i--) {
            int position = selectedItems.keyAt(i);
            if (selectedItems.get(position)) {
                items.remove(position);
            }
        }
        clearSelection();
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView itemName;
        TextView itemWeight;
        TextView itemQuantity;
    }
}
