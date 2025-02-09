package com.example.verter;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class PieFragment extends Fragment {

    private String userId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String name;
    double spent;

    public PieFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pie, container, false);
        PieChart pieChart = view.findViewById(R.id.chart);
        ArrayList<PieEntry> entries = new ArrayList<>();

        db.collection("Users")
                .document(userId)
                .collection("Categories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            name = document.getString("name");
                            spent = document.getDouble("spent");
                            entries.add(new PieEntry((float) spent, name));
                        }
                    }
                    PieDataSet pieDataSet = new PieDataSet(entries, "");
                    pieDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
                    pieDataSet.setValueTextColor(Color.BLACK);
                    pieDataSet.setValueTextSize(10f);
                    PieData pieData = new PieData(pieDataSet);
                    pieChart.setData(pieData);
                    pieChart.notifyDataSetChanged();
                    pieChart.getDescription().setEnabled(false);
                    pieChart.setRotationEnabled(false);
                    pieChart.animateY(1000);
                    pieChart.setEntryLabelTextSize(12f);
                    pieChart.setEntryLabelColor(Color.BLACK);
                    pieChart.invalidate();
                    Legend legend = pieChart.getLegend();
                    legend.setWordWrapEnabled(true);
                    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                    legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                    legend.setDrawInside(false);
                });
        return view;
    }
}