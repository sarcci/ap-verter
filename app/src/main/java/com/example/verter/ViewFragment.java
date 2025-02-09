package com.example.verter;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewFragment extends Fragment {
    public String name, userId;
    TextView spentTv, limitTv, nameTv, percentTv, changeTv;
    ProgressBar pBar;
    double limitNew, percent, limit, spent;
    Category category = new Category();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString("name");
            userId = getArguments().getString("userId");
            Log.d("Arguments got!", name);
        } else{
            Log.d("ViewFragment Error", "Error");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view, container, false);
        spentTv = view.findViewById(R.id.spent);
        limitTv = view.findViewById(R.id.limit);
        changeTv = view.findViewById(R.id.change);
        nameTv = view.findViewById(R.id.category);
        percentTv = view.findViewById(R.id.percent);
        pBar = view.findViewById(R.id.progressBar);
        pBar.setMax(100);

        db.collection("Users")
                .document(userId)
                .collection("Categories")
                .document(name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            category.setName(document.getString("name"));
                            limit = document.getDouble("limit");
                            spent = document.getDouble("spent");
                            category.setLimit(limit);
                            category.setSpent(spent);

                            nameTv.setText(category.getName());
                            limitTv.setText(String.valueOf(category.getLimit()));
                            spentTv.setText(String.valueOf(category.getSpent()));

                            percent = (spent/limit)*100;
                            percentTv.setText(percent + " %");
                            pBar.setProgress((int)Math.ceil(percent));
                        }else {
                            Log.d(TAG, "Error getting document: ", task.getException());
                        }
                    }
                });


        changeTv.setOnClickListener(view1 -> {
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.box);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.dialog_bg));
            dialog.setCancelable(false);
            TextView categoryTv, txt;
            categoryTv = dialog.findViewById(R.id.category);
            categoryTv.setText(String.valueOf(category.getName()));
            txt = dialog.findViewById(R.id.txt);
            txt.setText(R.string.change);
            dialog.show();
            Button confirm = dialog.findViewById(R.id.confirm);
            confirm.setText(R.string.ch);
            Button cancel = dialog.findViewById(R.id.cancel);
            confirm.setOnClickListener(view2 -> {
                EditText amount = dialog.findViewById(R.id.amount);
                String input = amount.getText().toString().trim();
                if (!input.isEmpty()) {
                    try {
                        limitNew = Double.parseDouble(input);
                    } catch (NumberFormatException e) {
                        limitNew = 0.0;
                    }
                } else {
                    limitNew = 0.0;
                }
                category.setLimit(limitNew);
                DocumentReference categoryRef = db.collection("Users")
                        .document(userId)
                        .collection("Categories")
                        .document(category.getName());

                categoryRef.update("limit", limitNew)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error updating document", e);
                        });
                dialog.dismiss();
            });
            cancel.setOnClickListener(view2 -> dialog.dismiss());
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