package com.example.memoria.loadMemory;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.example.memoria.model.Comment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private List<Comment> commentsList;
    private Context context;
    private DatabaseReference mRef;

    public CommentsAdapter(Context context, List<Comment> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.comments_card,parent,false);
        mRef = FirebaseDatabase.getInstance().getReference();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.ViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.commentText.setText(comment.getComment());

        mRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> retrieveMap = (Map<String, String>) dataSnapshot.child(comment.getUsername()).getValue();
                holder.usernameCom.setText(String.valueOf(retrieveMap.get("Name")));
                Glide.with(context).load(Uri.parse(retrieveMap.get("Image"))).into(holder.userDpCom);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context,databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList != null ? commentsList.size() :0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView userDpCom;
        TextView usernameCom, commentText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userDpCom = itemView.findViewById(R.id.userDpCom);
            usernameCom = itemView.findViewById(R.id.usernameCom);
            commentText = itemView.findViewById(R.id.commentText);
        }
    }
}
