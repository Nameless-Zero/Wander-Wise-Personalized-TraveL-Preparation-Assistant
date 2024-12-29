package com.sns.wanderwise.LocationRecycle;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sns.wanderwise.MoreWebview;
import com.sns.wanderwise.R;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private final Context context;
    private final List<LocationItem> locationItemList;

    public LocationAdapter(Context context, List<LocationItem> locationItemList) {
        this.context = context;
        this.locationItemList = locationItemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LocationItem item = locationItemList.get(position);
        holder.bind(item);

        Glide.with(context)
                .load(item.getImage())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imageView);

        holder.locationCard.setOnClickListener(
                v -> {
                    Intent intent = new Intent(context, MoreWebview.class);
                    intent.putExtra("url", item.getViewMore());
                    intent.putExtra("image", item.getImage());
                    intent.putExtra("location", item.getLocation());
                    intent.putExtra("locationDes", item.getDescription());
                    intent.putExtra("locationMode", item.getTransportMode());
                    context.startActivity(intent);
                });

        holder.itemView.setAnimation(
                AnimationUtils.loadAnimation(context, R.anim.item_animation_fall));
    }

    @Override
    public int getItemCount() {
        return locationItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView locationTextView;
        private final TextView locationDes;
        private final CardView locationCard;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            locationTextView = itemView.findViewById(R.id.textViewTitle);
            locationDes = itemView.findViewById(R.id.textViewBody);
            locationCard = itemView.findViewById(R.id.location_item);
        }

        public void bind(LocationItem item) {
            locationTextView.setText(item.getLocation());
            locationDes.setText(item.getDescription());
        }
    }
}
