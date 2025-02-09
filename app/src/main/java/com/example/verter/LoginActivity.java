package com.example.verter;

import static android.view.View.INVISIBLE;

import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button up, in;
    FirebaseAuth mAuth;
    SQLiteDatabase dbs;
    String userId, username;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        in = findViewById(R.id.login);
        up = findViewById(R.id.reg);

        dbs = openOrCreateDatabase("verterdb", MODE_PRIVATE, null);
        dbs.execSQL("CREATE TABLE IF NOT EXISTS User (ID text PRIMARY KEY);");
        dbs.execSQL("CREATE TABLE IF NOT EXISTS Monthly (ID INTEGER PRIMARY KEY AUTOINCREMENT, user text, month int, year varchar(50), amount double, FOREIGN KEY (user) REFERENCES User(ID))");

        up.setOnClickListener(view -> {
            up.setEnabled(false);
            in.setEnabled(false);
            String emailText, passwordText;
            emailText = email.getText().toString();
            passwordText = password.getText().toString();
            if (TextUtils.isEmpty(emailText)) {
                Toast.makeText(LoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(passwordText)) {
                Toast.makeText(LoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Account created",
                                    Toast.LENGTH_SHORT).show();
                            userId = mAuth.getCurrentUser().getUid();
                            dbs.execSQL("INSERT INTO User(ID) VALUES (?)", new Object[]{userId});
                            Dialog dialog = new Dialog(this);
                            dialog.setContentView(R.layout.user_box);
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
                            dialog.setCancelable(false);
                            Button confirm, cancel;
                            confirm = dialog.findViewById(R.id.confirm);
                            cancel = dialog.findViewById(R.id.cancel);
                            cancel.setVisibility(INVISIBLE);
                            EditText usernameTv = dialog.findViewById(R.id.username);
                            dialog.show();
                            confirm.setOnClickListener(view1 -> {
                                username = usernameTv.getText().toString().trim();
                                if (!username.isEmpty()) {
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", username);
                                    db.collection("Users").document(userId)
                                            .set(user, SetOptions.merge())
                                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Username saved!", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                                dialog.dismiss();
                            });
                            addCategoriesForUser(userId);
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(LoginActivity.this, "Email already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            up.setEnabled(true);
            in.setEnabled(true);
        });

        in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                up.setEnabled(false);
                in.setEnabled(false);
                String emailText, passwordText;
                emailText = email.getText().toString();
                passwordText = password.getText().toString();

                if (TextUtils.isEmpty(emailText)) {
                    Toast.makeText(LoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(passwordText)) {
                    Toast.makeText(LoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                up.setEnabled(true);
                in.setEnabled(true);
            }

        });
    }
    private void addCategoriesForUser(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference categoriesRef = db.collection("Users").document(userId).collection("Categories");
        List<Map<String, Object>> categories = new ArrayList<>();
        categories.add(createCategory("Bills", 10000.0, 0.0));
        categories.add(createCategory("Groceries", 10000.0, 0.0));
        categories.add(createCategory("Gas", 10000.0, 0.0));
        categories.add(createCategory("Events", 10000.0, 0.0));
        categories.add(createCategory("Shopping", 10000.0, 0.0));
        categories.add(createCategory("Cosmetics", 10000.0, 0.0));
        categories.add(createCategory("Restaurants", 10000.0, 0.0));
        categories.add(createCategory("Health", 10000.0, 0.0));
        categories.add(createCategory("Other", 10000.0, 0.0));

        for (Map<String, Object> category : categories) {
            String categoryName = (String) category.get("name");
            categoriesRef.document(categoryName).set(category)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Category added: " + categoryName))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding category", e));
        }
    }
    private Map<String, Object> createCategory(String name, double limit, double spent) {
        Map<String, Object> category = new HashMap<>();
        category.put("name", name);
        category.put("limit", limit);
        category.put("spent", spent);
        return category;
    }
}