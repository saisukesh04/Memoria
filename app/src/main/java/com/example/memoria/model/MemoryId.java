package com.example.memoria.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

public class MemoryId {

    @Exclude
    public String MemoryId;

    public <T extends MemoryId> T withId (@NonNull final String id){
        this.MemoryId = id;
        return (T) this;
    }
}
