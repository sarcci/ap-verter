package com.example.verter;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsFragment extends Fragment implements FRecyclerViewInterface {
    String userId, friendUsername, receiverId;
    Button add, requestsButton, sharingButton, friendsButton;
    // 0 for friends, 1 for requests, 2 for sharing
    RecyclerView recyclerView;
    List<Request> friends = new ArrayList<>();
    List<Request> requests = new ArrayList<>();
    List<Request> sharing = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FRecyclerViewAdapter adapterS, adapterR, adapterF;

    public FriendsFragment() {}

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
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerView = view.findViewById(R.id.rv);
        showFriends();
        db.collection("Requests")
                .whereEqualTo("sender", userId)
                .whereEqualTo("status", 1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String recvId = document.getString("receiver");
                        String reqId = document.getId();
                        db.collection("Users")
                                .document(recvId)
                                .get()
                                .addOnSuccessListener(userDocument -> {
                                    friendUsername = userDocument.getString("username");
                                    friends.add(new Request(reqId, friendUsername, recvId));
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching requests.", Toast.LENGTH_LONG).show()
                );
        db.collection("Requests")
                .whereEqualTo("receiver", userId)
                .whereEqualTo("status", 0)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String senderId = document.getString("sender");
                        String reqId = document.getId();
                        db.collection("Users")
                                .document(senderId)
                                .get()
                                .addOnSuccessListener(userDocument -> {
                                    String senderUsername = userDocument.getString("username");
                                    requests.add(new Request(reqId, senderUsername, senderId));
                                });
                    }
                });
        db.collection("Requests")
                .whereEqualTo("receiver", userId)
                .whereEqualTo("status", 1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String senderId = document.getString("sender");
                        String reqId = document.getId();
                        db.collection("Users")
                                .document(senderId)
                                .get()
                                .addOnSuccessListener(userDocument -> {
                                    String senderUsername = userDocument.getString("username");
                                    sharing.add(new Request(reqId, senderUsername, senderId));
                                });
                    }
                });
        requestsButton = view.findViewById(R.id.requests);
        requestsButton.setOnClickListener(view3 ->{
            requestsButton.getBackground().setTint(getResources().getColor(R.color.dark_lilac));
            sharingButton.getBackground().setTint(getResources().getColor(R.color.lilac));
            friendsButton.getBackground().setTint(getResources().getColor(R.color.lilac));
            adapterR = new FRecyclerViewAdapter(getContext(), requests, 1, this);
            recyclerView.setAdapter(adapterR);
            recyclerView.getAdapter().notifyDataSetChanged();
        });
        // All the users the current user shares expenses with
        sharingButton = view.findViewById(R.id.sharing);
        sharingButton.setOnClickListener(view5 ->{
            sharingButton.getBackground().setTint(getResources().getColor(R.color.dark_lilac));
            requestsButton.getBackground().setTint(getResources().getColor(R.color.lilac));
            friendsButton.getBackground().setTint(getResources().getColor(R.color.lilac));
            adapterS = new FRecyclerViewAdapter(getContext(), sharing, 2, this);
            recyclerView.setAdapter(adapterS);
            recyclerView.getAdapter().notifyDataSetChanged();
        });
        add = view.findViewById(R.id.add);
        add.setOnClickListener(view1 -> {
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.user_box);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.dialog_bg));
            dialog.setCancelable(false);
            EditText nameTv;
            nameTv = dialog.findViewById(R.id.username);
            dialog.show();
            Button confirm = dialog.findViewById(R.id.confirm);
            confirm.setText(R.string.request);
            Button cancel = dialog.findViewById(R.id.cancel);
            cancel.setOnClickListener(view2 -> {
                dialog.dismiss();
            });
            confirm.setOnClickListener(view2 -> {
                friendUsername = nameTv.getText().toString().trim();
                db.collection("Users")
                        .whereEqualTo("username", friendUsername)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            Log.d("FirestoreQuery", "Documents found: " + queryDocumentSnapshots.size());
                            if (queryDocumentSnapshots.isEmpty()) {
                                Toast.makeText(getContext(), "User does not exist.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            receiverId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            Map<String, Object> request = new HashMap<>();
                            request.put("sender", userId);
                            request.put("receiver", receiverId);
                            request.put("status", 0);
                            db.collection("Requests").add(request);
                            Toast.makeText(getContext(), "Request sent!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        });
            });
        });
        friendsButton = view.findViewById(R.id.friends);
        friendsButton.setOnClickListener(view6 -> {
            friendsButton.getBackground().setTint(getResources().getColor(R.color.dark_lilac));
            requestsButton.getBackground().setTint(getResources().getColor(R.color.lilac));
            sharingButton.getBackground().setTint(getResources().getColor(R.color.lilac));
            showFriends();
        });
        return view;
    }
    private void showFriends() {
        adapterF = new FRecyclerViewAdapter(getContext(), friends, 0, this);
        recyclerView.setAdapter(adapterF);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    @Override
    public void onItemClick(int position) {
        String friendId = friends.get(position).getFriendId();
        ((MainActivity) getActivity()).showFriendsDetails(friendId);
    }
    @Override
    public void onAcceptClick(int position) {
        Request request = requests.get(position);
        String reqId = request.getReqId();
        db.collection("Requests")
                .document(reqId)
                .update("status", 1)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Request accepted", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erorr accepting request", Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public void onDeleteFriendClick(int position) {
        Request request = sharing.get(position);
        String reqId = request.getReqId();
        db.collection("Requests")
                .document(reqId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Friend deleted", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erorr deleting friend", Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public void onDeleteRequestClick(int position) {
        Request request = requests.get(position);
        String reqId = request.getReqId();
        db.collection("Requests")
                .document(reqId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Request deleted", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erorr deleting request", Toast.LENGTH_SHORT).show();
                });
    }
}