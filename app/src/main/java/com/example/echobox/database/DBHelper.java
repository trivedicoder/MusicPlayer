package com.example.echobox.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.echobox.models.Song;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "EchoBox.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE," +
                "password TEXT)");

        db.execSQL("CREATE TABLE songs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "artist TEXT," +
                "uri TEXT," +
                "playCount INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS songs");
        onCreate(db);
    }

    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email=? AND password=?",
                new String[]{email, password}
        );
        boolean found = cursor.getCount() > 0;
        cursor.close();
        return found;
    }

    public long addSong(String title, String artist, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("artist", artist);
        values.put("uri", uri);
        return db.insert("songs", null, values);
    }

    public ArrayList<Song> getAllSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM songs ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Song song = new Song();
                song.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow("artist")));
                song.setUri(cursor.getString(cursor.getColumnIndexOrThrow("uri")));
                song.setPlayCount(cursor.getInt(cursor.getColumnIndexOrThrow("playCount")));
                songs.add(song);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return songs;
    }

    public ArrayList<Song> getTopPlayedSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM songs ORDER BY playCount DESC, id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Song song = new Song();
                song.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow("artist")));
                song.setUri(cursor.getString(cursor.getColumnIndexOrThrow("uri")));
                song.setPlayCount(cursor.getInt(cursor.getColumnIndexOrThrow("playCount")));
                songs.add(song);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return songs;
    }

    public void incrementPlayCount(int songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE songs SET playCount = playCount + 1 WHERE id = ?", new Object[]{songId});
    }
}