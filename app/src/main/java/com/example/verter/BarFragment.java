package com.example.verter;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.getColor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class BarFragment extends Fragment {
    String userId;
    SQLiteDatabase dbs;

    public BarFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
        dbs = requireContext().openOrCreateDatabase("verterdb", MODE_PRIVATE, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bar, container, false);
        BarChart chart = view.findViewById(R.id.chart);
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> years = new ArrayList<>();

        Cursor yearCursor = dbs.rawQuery("SELECT DISTINCT year FROM Monthly WHERE user = ?", new String[]{userId});
        if (yearCursor.moveToFirst()) {
            do {
                years.add(yearCursor.getInt(0));
            } while (yearCursor.moveToNext());
        }
        yearCursor.close();

        Collections.sort(years, Collections.reverseOrder());

        int index = 0;
        for (int year : years) {
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            HashMap<Integer, Float> monthValues = new HashMap<>();

            Cursor c1 = dbs.rawQuery("SELECT month, amount FROM Monthly WHERE user = ? AND year = ?",
                    new String[]{userId, String.valueOf(year)});
            if (c1.moveToFirst()) {
                do {
                    int ci = c1.getColumnIndex("month");
                    int ci2 = c1.getColumnIndex("amount");
                    int monthIndex = c1.getInt(ci) - 1;
                    float amount = (float) c1.getDouble(ci2);
                    monthValues.put(monthIndex, amount);
                } while (c1.moveToNext());
            }
            c1.close();

            for (int i = 0; i < 12; i++) {
                float value = monthValues.getOrDefault(i, 0f);
                entries.add(new BarEntry(index, value));
                labels.add(months[i]);
                index++;
            }
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-90);

        BarDataSet barDataSet = new BarDataSet(entries, "Amount spent ($)");
        barDataSet.setColor(getColor(getContext(), R.color.blue));
        barDataSet.setValueTextColor(Color.BLACK);
        BarData barData = new BarData(barDataSet);

        chart.setFitBars(true);
        chart.setData(barData);
        chart.setVisibleXRangeMaximum(12);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.animateY(2000);
        chart.invalidate();

        return view;

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbs != null) {
            dbs.close();
        }
    }
}