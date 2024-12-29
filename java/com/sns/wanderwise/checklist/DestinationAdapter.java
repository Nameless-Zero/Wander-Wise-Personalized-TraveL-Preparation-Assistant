package com.sns.wanderwise.checklist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import com.sns.wanderwise.R;

public class DestinationAdapter extends ArrayAdapter<DestinationItem> {

    private Context context;
    private List<DestinationItem> destinations;

    public DestinationAdapter(Context context, List<DestinationItem> destinations) {
        super(context, 0, destinations);
        this.context = context;
        this.destinations = destinations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_destination, parent, false);
        }

        // Get the current destination item
        DestinationItem destination = destinations.get(position);

        // Find the TextViews for destination name and date
        TextView destinationText = convertView.findViewById(R.id.list_item_text);
        TextView dateCreatedText = convertView.findViewById(R.id.list_item_date_created);

        // Set the destination name and date
        destinationText.setText(destination.getName());
        dateCreatedText.setText("Destination type: "+destination.getTravelType()+"\nMode: "+destination.getTravelMode());

        return convertView;
    }
}
