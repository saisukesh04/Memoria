package com.example.memoria.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.memoria.model.Comment;

import java.util.List;

public class ViewModel extends AndroidViewModel {

    private List<Comment> comments;

    public ViewModel(@NonNull Application application) {
        super(application);
        CommentsDatabase commentsDatabase = CommentsDatabase.getInstance(this.getApplication());
        comments = commentsDatabase.CommentDao().loadAllComments();
    }

    public List<Comment> getComment() {
        return comments;
    }
}
