package com.example.memoria.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.memoria.model.Comment;

@Database(entities = {Comment.class},version = 1, exportSchema = false)
public abstract class CommentsDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "comment";
    private static CommentsDatabase sInstance;

    public static CommentsDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        CommentsDatabase.class, CommentsDatabase.DATABASE_NAME)
                        .allowMainThreadQueries()
                        .build();
            }
        }
        return sInstance;
    }

    public abstract CommentDao CommentDao();
}
