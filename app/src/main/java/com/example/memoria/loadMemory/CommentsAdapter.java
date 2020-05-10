package com.example.memoria.loadMemory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.model.Comment;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private List<Comment> commentsList;
    private Context context;

    public CommentsAdapter(Context context, List<Comment> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.comments_card,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.ViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.commentText.setText(comment.getComment());
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
