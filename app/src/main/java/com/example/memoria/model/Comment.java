package com.example.memoria.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "comment")
public class Comment {

    @NonNull
    @PrimaryKey
    private String timestamp;
    private String username, comment;

    @Ignore
    public Comment(){
    }

    public Comment(String username, String comment, String timestamp) {
        this.username = username;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
