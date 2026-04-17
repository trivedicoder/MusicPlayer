package com.example.echobox.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable {
    private String id;
    private String ownerId;
    private String name;
    private ArrayList<String> songIds;

    public Playlist() {}

    public Playlist(String id, String ownerId, String name, ArrayList<String> songIds) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.songIds = songIds;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ArrayList<String> getSongIds() { return songIds; }
    public void setSongIds(ArrayList<String> songIds) { this.songIds = songIds; }
}