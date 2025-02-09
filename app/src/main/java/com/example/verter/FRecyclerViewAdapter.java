package com.example.verter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FRecyclerViewAdapter extends RecyclerView.Adapter<FRecyclerViewAdapter.MyViewHolder> {
    private final FRecyclerViewInterface recyclerViewInterface;
    Context context;
    List<Request> requests;
    int which; // 0 for friends, 1 for requests, 2 for sharing
    public FRecyclerViewAdapter(Context context, List<Request> requests, int which, FRecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.requests = requests;
        this.which = which;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public FRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.friend_row, parent, false);
        return new FRecyclerViewAdapter.MyViewHolder(view, which, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull FRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.tv.setText(requests.get(position).getFriendUsername());
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv, txt;
        ImageView accept, deleteRequest, deleteFriend;
        public MyViewHolder(@NonNull View itemView, int which, FRecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            tv = itemView.findViewById(R.id.name);
            accept = itemView.findViewById(R.id.accept);
            deleteFriend = itemView.findViewById(R.id.deleteFriend);
            deleteRequest = itemView.findViewById(R.id.deleteRequest);
            txt = itemView.findViewById(R.id.txt);
            if (which == 2) {
                deleteFriend.setVisibility(VISIBLE); // Stop sharing
                deleteRequest.setVisibility(INVISIBLE);
                accept.setVisibility(INVISIBLE);
                txt.setVisibility(INVISIBLE);
                deleteFriend.setOnClickListener(v -> {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onDeleteFriendClick(pos);
                        }
                    }
                });
            } else if (which == 1) { // Requests
                accept.setVisibility(VISIBLE);
                deleteRequest.setVisibility(VISIBLE);
                deleteFriend.setVisibility(INVISIBLE);
                txt.setVisibility(INVISIBLE);
                accept.setOnClickListener(v -> {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onAcceptClick(pos);
                        }
                    }
                });
                deleteRequest.setOnClickListener(v -> {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onDeleteRequestClick(pos);
                        }
                    }
                });
            } else if (which ==0) { // Friends
                accept.setVisibility(INVISIBLE);
                deleteRequest.setVisibility(INVISIBLE);
                deleteFriend.setVisibility(INVISIBLE);
                itemView.setOnClickListener(view -> {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                });
            }
        }
    }
}
