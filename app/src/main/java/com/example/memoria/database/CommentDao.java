package com.example.memoria.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.memoria.model.Comment;

import java.util.List;

@Dao
public interface CommentDao {

    @Query("SELECT * FROM comment")
    List<Comment> loadAllComments();

    @Insert
    void insertComments(Comment comment);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateComments(Comment comment);

    @Query("DELETE FROM comment")
    void deleteAll();
}
