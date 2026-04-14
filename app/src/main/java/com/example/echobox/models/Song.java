package com.example.echobox.models;

import java.io.Serializable;

public class Song implements Serializable {
    private String id;
    private String ownerId;
    private String title;
    private String artist;
    private String url;
    private long playCount;

    public Song() {}

    public Song(String id, String ownerId, String title, String artist, String url, long playCount) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.playCount = playCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(long playCount) {
        this.playCount = playCount;
    }
}