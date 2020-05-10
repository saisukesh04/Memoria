package com.example.memoria.model;

import android.text.format.DateFormat;

public class Memory extends MemoryId{

    private String Description, Link, TimeStamp, Type, Username;

    public Memory(){
    }

    public Memory(String description, String link, String timeStamp, String type, String username) {
        Description = description;
        Link = link;
        TimeStamp = timeStamp;
        Type = type;
        Username = username;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getLink() {
        return Link;
    }

    public void setLink(String link) {
        Link = link;
    }

    public String getTimeStamp() {
        return DateFormat.format("MM/dd/yyyy", Long.parseLong(TimeStamp)).toString();
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
}
