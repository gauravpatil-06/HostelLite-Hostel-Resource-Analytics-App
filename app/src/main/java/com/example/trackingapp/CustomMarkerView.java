package com.example.trackingapp;
import com.example.trackingapp.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.List;

public class CustomMarkerView extends MarkerView {

    private TextView textView;
    private List<String> dates;

    // Constructor that accepts the 'dates' list
    public CustomMarkerView(Context context, int layoutResource, List<String> dates) {
        super(context, layoutResource);
        this.dates = dates;
        textView = findViewById(R.id.marker_text);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String date = getDateForIndex((int) e.getX());
        String tooltip = "Date: " + date + "\n" + "Water: " + e.getY() + "L";
        textView.setText(tooltip);
    }

    private String getDateForIndex(int index) {
        // Retrieve the date from the list using the index
        return dates.get(index);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
