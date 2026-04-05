package com.example.echobox.models;

/**
 * Model class representing a Song in the application.
 * It stores metadata such as title, artist, file URI, and playback statistics.
 */
public class Song {
    private int id;
    private String title;
    private String artist;
    private String uri;
    private int playCount;

    /**
     * Default constructor for Song.
     */
    public Song() {
    }

    /**
     * Parameterized constructor for Song.
     */
    public Song(int id, String title, String artist, String uri, int playCount) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.uri = uri;
        this.playCount = playCount;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }
}