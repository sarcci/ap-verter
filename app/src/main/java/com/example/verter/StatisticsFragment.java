package com.example.verter;

import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class StatisticsFragment extends Fragment {

    String userId;
    CardView cv1, cv2;

    public StatisticsFragment() {
    }

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
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        cv1 = view.findViewById(R.id.pie);
        cv2 = view.findViewById(R.id.chart);
        cv1.setOnClickListener(view1 -> {
            ((MainActivity) getActivity()).showPie();
        });
        cv2.setOnClickListener(view2 -> {
            ((MainActivity) getActivity()).showBar();
        });
        return view;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() != null) {
            return getActivity().onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}