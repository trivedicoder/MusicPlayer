//package com.example.echobox.database;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import com.example.echobox.models.User;
//
//import com.example.echobox.models.Song;
//
//import java.util.ArrayList;
//
///**
// * DBHelper handles all local database operations for the EchoBox app.
// * It manages the creation of tables (users, songs) and provides methods
// * for CRUD operations, user authentication, and play count tracking.
// */
//public class DBHelper extends SQLiteOpenHelper {
//
//    private static final String DB_NAME = "EchoBox.db";
//    private static final int DB_VERSION = 1;
//
//    public DBHelper(Context context) {
//        super(context, DB_NAME, null, DB_VERSION);
//    }
//
//    /**
//     * Creates the SQLite database tables for users and songs.
//     */
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        // Table to store user credentials for the local login system
//        db.execSQL("CREATE TABLE users (" +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                "email TEXT UNIQUE," +
//                "password TEXT)");
//
//        // Table to store song metadata and playback statistics
//        db.execSQL("CREATE TABLE songs (" +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                "title TEXT," +
//                "artist TEXT," +
//                "uri TEXT," +
//                "playCount INTEGER DEFAULT 0)");
//    }
//
//    /**
//     * Upgrades the database by dropping existing tables and recreating them.
//     */
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS users");
//        db.execSQL("DROP TABLE IF EXISTS songs");
//        onCreate(db);
//    }
//
//    /**
//     * Registers a new user in the database.
//     * @return true if registration was successful, false otherwise (e.g., email already exists).
//     */
//    public boolean registerUser(String email, String password) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("email", email);
//        values.put("password", password);
//        long result = db.insert("users", null, values);
//        return result != -1;
//    }
//
//    /**
//     * Verifies user credentials for login.
//     */
//    public boolean loginUser(String email, String password) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(
//                "SELECT * FROM users WHERE email=? AND password=?",
//                new String[]{email, password}
//        );
//        boolean found = cursor.getCount() > 0;
//        cursor.close();
//        return found;
//    }
//
//    /**
//     * Adds a new song to the library.
//     * @return the row ID of the newly inserted song, or -1 if an error occurred.
//     */
//    public long addSong(String title, String artist, String uri) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("title", title);
//        values.put("artist", artist);
//        values.put("uri", uri);
//        return db.insert("songs", null, values);
//    }
//
//    /**
//     * Retrieves all songs from the database, ordered by newest added.
//     */
//    public ArrayList<Song> getAllSongs() {
//        ArrayList<Song> songs = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM songs ORDER BY id DESC", null);
//
//        if (cursor.moveToFirst()) {
//            do {
//                Song song = new Song();
//                song.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
//                song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
//                song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow("artist")));
//                song.setUri(cursor.getString(cursor.getColumnIndexOrThrow("uri")));
//                song.setPlayCount(cursor.getInt(cursor.getColumnIndexOrThrow("playCount")));
//                songs.add(song);
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        return songs;
//    }
//
//    /**
//     * Retrieves the top played songs, ordered by playCount (highest first).
//     */
//    public ArrayList<Song> getTopPlayedSongs() {
//        ArrayList<Song> songs = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM songs ORDER BY playCount DESC, id DESC", null);
//
//        if (cursor.moveToFirst()) {
//            do {
//                Song song = new Song();
//                song.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
//                song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
//                song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow("artist")));
//                song.setUri(cursor.getString(cursor.getColumnIndexOrThrow("uri")));
//                song.setPlayCount(cursor.getInt(cursor.getColumnIndexOrThrow("playCount")));
//                songs.add(song);
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        return songs;
//    }
//
//    /**
//     * Increments the play count of a specific song by its ID.
//     */
//    public void incrementPlayCount(int songId) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("UPDATE songs SET playCount = playCount + 1 WHERE id = ?", new Object[]{songId});
//    }
//
//    /**
//     * Retrieves user details based on credentials.
//     * Extracts a username from the email if no explicit username field is present.
//     */
//    public User getUserByEmailAndPassword(String email, String password) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(
//                "SELECT * FROM users WHERE email=? AND password=?",
//                new String[]{email, password}
//        );
//
//        User user = null;
//
//        if (cursor.moveToFirst()) {
//            user = new User();
//            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
//            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
//
//            String emailValue = cursor.getString(cursor.getColumnIndexOrThrow("email"));
//            // Derive username from email (text before @)
//            String username = emailValue.contains("@")
//                    ? emailValue.substring(0, emailValue.indexOf("@"))
//                    : emailValue;
//
//            user.setUsername(username);
//        }
//
//        cursor.close();
//        return user;
//    }
//}