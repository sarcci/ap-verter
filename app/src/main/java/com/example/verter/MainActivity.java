package com.example.verter;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button confirm, cancel;
    Dialog dialog;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String userId;
    SQLiteDatabase dbs;
    String notifText;
    LocalDate now = LocalDate.now();
    int year, month, which, day = now.getDayOfMonth();

//    SharedPreferences sharedPref = this.getSharedPreferences(
//            getString(R.string.preference_file_key), Context.MODE_PRIVATE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        dbs = openOrCreateDatabase("verterdb", MODE_PRIVATE, null);

        now = LocalDate.now();
        year = 2024;
        month = 12;
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            userId = user.getUid();
            Log.d("Tag", userId);
            showHome(0, userId);
        }

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        dialog.setCancelable(false);
        confirm = dialog.findViewById(R.id.confirm);
        cancel = dialog.findViewById(R.id.cancel);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void showHome(int which, String userId) {
        Fragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        bundle.putInt("which", which);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_search) {
            Toast.makeText(MainActivity.this, "Search",
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "Settings",
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_out) {
            dialog.show();
            return true;
        } else if (itemId == R.id.action_home) {
            showHome(0, userId);
        } else if (itemId == R.id.action_statistics) {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            Fragment fragment3 = new StatisticsFragment();
            fragment3.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl, fragment3)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else if (itemId == R.id.action_friend) {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            Fragment fragment4 = new FriendsFragment();
            fragment4.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl, fragment4)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else if(itemId == R.id.action_notif) {
            Cursor c2 = dbs.rawQuery("SELECT amount FROM Monthly WHERE month = ? AND year = ?", new String[]{String.valueOf(month - 1), String.valueOf(year)});
            Cursor c3 = dbs.rawQuery("SELECT amount FROM Monthly WHERE month = ? AND year = ?", new String[]{String.valueOf(month - 2), String.valueOf(year)});
            if (c2.getCount() != 0 && c3.getCount() != 0) {
                int indexC2 = c2.getColumnIndex("amount");
                int indexC3 = c3.getColumnIndex("amount");
                if (indexC2 >= 0 && indexC3 >= 0) {
                    c2.moveToFirst();
                    c3.moveToFirst();
                    double am1 = c2.getInt(indexC2);
                    double am2 = c3.getInt(indexC3);
                    if (am1 > am2) {
                        notifText = "Your expenses were up " + (int) (((am1 - am2) / am1) * 100) + "% last month, for an average of " + (int) (am1/30) + " $ per day";
                    } else if (am1 <= am2) {
                        notifText = "Your expenses were down " +(int) (((am2 - am1) / am2) * 100) + "% last month, for an average of " + (int) (am1/30) + " $ per day";
                    }
                }
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("def", "Monthly Report", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
                Notification notification = new Notification.Builder(this, "def")
                        .setContentTitle("Monthly Report")
                        .setStyle(new Notification.BigTextStyle().bigText(notifText))
                        .setSmallIcon(R.drawable.statistics)
                        .build();
                notificationManager.notify(1, notification);
            }
            c2.close();
            c3.close();
        }
            return super.onOptionsItemSelected(item);
    }

    public void analyze (String name, String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("userId", userId);

        Fragment fragment2 = new ViewFragment();
        fragment2.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl, fragment2)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    public void addCategory(String name, double limit, String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference categoriesRef = db.collection("Users").document(uid).collection("Categories");
        Map<String, Object> newCategory = createCategory(name, limit, 0.0);

        categoriesRef.document(name).set(newCategory)
                    .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "Category added: " + name);
                            showHome(0, userId);
                            })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding category", e));
    }
    private Map<String, Object> createCategory(String name, double limit, double spent) {
        Map<String, Object> category = new HashMap<>();
        category.put("name", name);
        category.put("limit", limit);
        category.put("spent", spent);
        return category;
    }
    public void showFriendsDetails(String friendId) {
        showHome(1, friendId);
    }

    public void showPie() {
        Fragment fragment = new PieFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
    public void showBar() {
        Fragment fragment = new BarFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

    }
}
