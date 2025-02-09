package com.example.verter;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    int rows, columns, total;
    String userId, nameNewCategory, notifText;
    LocalDate now = LocalDate.now();
    int year, month, which, day = now.getDayOfMonth();
    double spent, expense, limitNewCategory;
    List<Category> categories = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SQLiteDatabase dbs;
    GridLayout gridLayout;

    public HomeFragment() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            which = getArguments().getInt("which");
            // 0 for me, 1 for friend
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        now = LocalDate.now();
        year = now.getYear();
        month = now.getMonthValue();
        columns = 2;
        dbs = requireContext().openOrCreateDatabase("verterdb", MODE_PRIVATE, null);
        ViewGroup root = (ViewGroup) view.findViewById(R.id.content_main);
        gridLayout = new GridLayout(getContext());
        db.collection("Users")
                .document(userId)
                .collection("Categories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            double limit = document.getDouble("limit");
                            double spent;
                            // Restarts every month
                            if (day == 1) {
                                /*
                                Cursor c2 = dbs.rawQuery("SELECT amount FROM Monthly WHERE mno = ? AND year = ?", new String[]{String.valueOf(month-1), String.valueOf(year)});
                                Cursor c3 = dbs.rawQuery("SELECT amount FROM Monthly WHERE mno = ? AND year = ?", new String[]{String.valueOf(month-2), String.valueOf(year)});
                                if (c2.getCount()!=0 && c3.getCount()!=0)  {
                                    int indexC2 = c2.getColumnIndex("amount");
                                    int indexC3 = c3.getColumnIndex("amount");
                                    if (indexC2 >= 0 && indexC3 >= 0) {
                                        c2.moveToFirst();
                                        c3.moveToFirst();
                                        double am1 = c2.getInt(indexC2);
                                        double am2 = c3.getInt(indexC3);
                                        if (am1 > am2) {
                                            notifText = "Your expenses were up " + ((am1 - am2)/am1)*100 + "% last month, for an average of " + am1 + "$";
                                        } else if (am1<=am2) {
                                            notifText = "Your expenses were down " + ((am2 - am1)/am2)*100 + "% last month, for an average of " + am1 + "$";
                                        }
                                    }
                                    NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationChannel channel = new NotificationChannel("def", "Monthly Report", NotificationManager.IMPORTANCE_DEFAULT);
                                        notificationManager.createNotificationChannel(channel);
                                    }
                                    Notification notification = new Notification.Builder(getContext(), "def")
                                            .setContentTitle("Monthly Report")
                                            .setContentText(notifText)
                                            .setSmallIcon(R.drawable.statistics)
                                            .build();
                                    notificationManager.notify(1, notification);
                                }
                                c2.close();
                                c3.close();
                                 */
                                // Spent resets to 0 only if the user has not entered an expense yet for the 1st of the month
                                Cursor c1 = dbs.rawQuery("SELECT * FROM Monthly WHERE user = ? AND month =? AND year =?", new String[]{userId, String.valueOf(month), String.valueOf(year)});
                                if (c1.getCount() == 0) {
                                    String docId = document.getId();
                                    db.collection("Users")
                                            .document(userId)
                                            .collection("Categories")
                                            .document(docId)
                                            .update("spent", 0.0);
                                }
                                c1.close();
                            }
                            spent = document.getDouble("spent");
                            Category category = new Category(name, limit, spent);
                            categories.add(category);
                        }
                        total = categories.size();
                        rows = (int) Math.ceil((double) total / columns);
                        displayCategories(categories, root);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        return view;
    }
    private void displayCategories(List<Category> categories, ViewGroup root) {
        ScrollView scrollView = new ScrollView(getContext());
        GridLayout gridLayout = new GridLayout(getContext());
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.height = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams.setMargins(10, 10, 10, 10);
        gridLayout.setLayoutParams(layoutParams);
        gridLayout.setColumnCount(columns);

        int r = 0, c = 0;

        for (Category category: categories) {
            if (c == columns) {
                c = 0;
                r++;
            }

            LinearLayout cardLayout = new LinearLayout(getContext());
            cardLayout.setOrientation(LinearLayout.VERTICAL);
            cardLayout.setGravity(Gravity.CENTER_HORIZONTAL);

            ImageView imageView = new ImageView(getContext());
            int imgId = this.getResources().getIdentifier(
                    category.getName().toLowerCase(), "drawable", getContext().getPackageName());
            if (imgId != 0) imageView.setImageResource(imgId);
            else imageView.setImageResource(R.drawable.mine);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            imageParams.topMargin = (int) (18 * getResources().getDisplayMetrics().density);
            imageView.setLayoutParams(imageParams);

            // Name of Category
            TextView tv1 = new TextView(getContext());
            tv1.setText(category.getName());
            tv1.setTextColor(Color.BLACK);
            LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tv1Params.topMargin = (int) (3 * getResources().getDisplayMetrics().density);
            tv1.setLayoutParams(tv1Params);

            // Total Expenses
            TextView tv2 = new TextView(getContext());
            tv2.setText(String.valueOf(category.getSpent()));
            tv2.setTextColor(Color.GRAY);
            LinearLayout.LayoutParams tv2Params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tv2Params.topMargin = (int) (3 * getResources().getDisplayMetrics().density);
            tv2.setLayoutParams(tv2Params);

            CardView cardView = createChild();
            cardLayout.addView(imageView);
            cardLayout.addView(tv1);
            cardLayout.addView(tv2);

            if (which == 0) {
                // Button for adding an expense
                MaterialButton button = new MaterialButton(getContext());
                button.setText("+");
                button.setTextSize(20);
                button.setTextColor(Color.WHITE);
                button.setCornerRadius((int) (20 * getResources().getDisplayMetrics().density)); // Makes it circular
                button.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.dark_lilac));
                button.setGravity(Gravity.CENTER);
                button.setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        (int) (40 * getResources().getDisplayMetrics().density),
                        (int) (40 * getResources().getDisplayMetrics().density)
                );
                buttonParams.gravity = Gravity.END | Gravity.BOTTOM;
                buttonParams.setMargins(0, 0,
                        (int) (10 * getResources().getDisplayMetrics().density),
                        (int) (10 * getResources().getDisplayMetrics().density));
                button.setLayoutParams(buttonParams);
                button.setTag(category.getName());
                button.setOnClickListener(view -> {
                    Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.box);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.dialog_bg));
                    dialog.setCancelable(false);
                    TextView categoryTv, txt;
                    categoryTv = dialog.findViewById(R.id.category);
                    categoryTv.setText(String.valueOf(category.getName()));
                    txt = dialog.findViewById(R.id.txt);
                    txt.setText(R.string.addExp);
                    dialog.show();
                    Button confirm = dialog.findViewById(R.id.confirm);
                    confirm.setText(R.string.add);
                    Button cancel = dialog.findViewById(R.id.cancel);
                    confirm.setOnClickListener(view1 -> {
                        EditText amount = dialog.findViewById(R.id.amount);
                        String input = amount.getText().toString().trim();
                        if (!input.isEmpty()) {
                            try {
                                expense = Double.parseDouble(input);
                            } catch (NumberFormatException e) {
                                expense = 0.0;
                            }
                        } else {
                            expense = 0.0;
                        }
                        DocumentReference categoryRef = db.collection("Users")
                                .document(userId)
                                .collection("Categories")
                                .document(String.valueOf(button.getTag()));
                        Source source = Source.CACHE;
                        categoryRef.get(source).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                spent = document.getDouble("spent") != null ? document.getDouble("spent") : 0.0;
                                categoryRef.update("spent", spent + expense)
                                        .addOnSuccessListener(unused -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
                                tv2.setText(String.valueOf(spent + expense));
                                now = null;
                                Cursor cursor = dbs.rawQuery("SELECT * FROM Monthly WHERE User = ? AND year = ? AND month = ?",
                                        new String[]{userId, String.valueOf(year), });

                                if (cursor.moveToFirst()) {
                                    dbs.execSQL("UPDATE Monthly SET amount = amount + ? WHERE User = ? AND year = ? AND month = ?",
                                            new Object[]{expense, userId, year, month});
                                } else {
                                    dbs.execSQL("INSERT INTO Monthly (user, year, month, amount) VALUES (?, ?, ?, ?)",
                                            new Object[]{userId, year,month, expense});
                                }
                                cursor.close();
                            }
                        });
                        dialog.dismiss();
                    });
                    cancel.setOnClickListener(view2 -> dialog.dismiss());
                });
                cardLayout.addView(button);
            }
            cardView.addView(cardLayout);
            cardView.setTag(category.getName());

            if (which == 0) {
                cardView.setOnClickListener(view -> {
                    ((MainActivity) getActivity()).analyze(category.getName(), userId);
                });
            }
            GridLayout.Spec rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            GridLayout.Spec colSpan = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);

            GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
                    rowSpan, colSpan
            );
            gridLayout.addView(cardView, gridParam);
            c++;
        }

        if (which == 0) {
            LinearLayout adderLayout = new LinearLayout(getContext());
            adderLayout.setOrientation(LinearLayout.VERTICAL);
            adderLayout.setGravity(Gravity.CENTER);

            ImageView img = new ImageView(getContext());
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            imageParams.topMargin = (int) (18 * getResources().getDisplayMetrics().density);
            img.setLayoutParams(imageParams);
            img.setImageResource(R.drawable.add);

            TextView txt = new TextView(getContext());
            txt.setText(R.string.nova);
            txt.setTextColor(Color.GRAY);
            txt.setGravity(Gravity.CENTER_HORIZONTAL);

            CardView adderCard = createChild();
            adderLayout.addView(img);
            adderLayout.addView(txt);
            adderCard.addView(adderLayout);

            GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
            );
            adderCard.setOnClickListener(view -> {
                Dialog dialog2 = new Dialog(getContext());
                dialog2.setContentView(R.layout.new_category_dialog);

                if (dialog2.getWindow() != null) {
                    dialog2.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog2.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.dialog_bg));
                }

                dialog2.setCancelable(false);

                Button cancel = dialog2.findViewById(R.id.cancel);
                Button confirm = dialog2.findViewById(R.id.confirm);

                EditText nameTv = dialog2.findViewById(R.id.name);
                EditText limitTv = dialog2.findViewById(R.id.limit);

                dialog2.show();
                cancel.setOnClickListener(view3 -> dialog2.dismiss());
                confirm.setOnClickListener(view4 -> {
                    nameNewCategory = nameTv.getText().toString().trim();
                    String input = limitTv.getText().toString().trim();
                    try {
                        limitNewCategory = input.isEmpty() ? 0.0 : Double.parseDouble(input);
                    } catch (NumberFormatException e) {
                        limitNewCategory = 0.0;
                    }
                    ((MainActivity) getActivity()).addCategory(nameNewCategory, limitNewCategory, userId);
                    dialog2.dismiss();
                });
            });
            gridLayout.addView(adderCard, gridParam);
        }
        scrollView.addView(gridLayout);
        root.addView(scrollView);

    }
    private CardView createChild() {
        CardView cardView = new CardView(this.getContext());
        cardView.setCardElevation(8);
        cardView.setRadius(8);
        cardView.setUseCompatPadding(true);
        ViewGroup.LayoutParams cvLayoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardView.setLayoutParams(cvLayoutParams);
        return cardView;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() != null) {
            return getActivity().onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbs != null) {
            dbs.close();
        }
    }
}