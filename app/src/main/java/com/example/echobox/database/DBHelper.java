package com.example.echobox.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.echobox.models.Playlist;
import com.example.echobox.models.Song;
import com.example.echobox.models.User;

import java.util.ArrayList;

/**
 * DBHelper handles all local database operations for the EchoBox app.
 * Manages users, songs, playlists, and playlist-song associations.
 * Uses a singleton pattern to avoid multiple instances.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "EchoBox.db";
    private static final int DB_VERSION = 2;

    private static DBHelper instance;

    /**
     * Singleton access to avoid creating multiple DBHelper instances.
     */
    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

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

        db.execSQL("CREATE TABLE playlists (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL)");

        db.execSQL("CREATE TABLE playlist_songs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "playlistId INTEGER NOT NULL," +
                "songId INTEGER NOT NULL," +
                "position INTEGER NOT NULL," +
                "FOREIGN KEY (playlistId) REFERENCES playlists(id) ON DELETE CASCADE," +
                "FOREIGN KEY (songId) REFERENCES songs(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS playlists (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL)");

            db.execSQL("CREATE TABLE IF NOT EXISTS playlist_songs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "playlistId INTEGER NOT NULL," +
                    "songId INTEGER NOT NULL," +
                    "position INTEGER NOT NULL," +
                    "FOREIGN KEY (playlistId) REFERENCES playlists(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (songId) REFERENCES songs(id) ON DELETE CASCADE)");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ─── User Operations ────────────────────────────────────────────

    /**
     * Registers a new user in the database.
     * @return true if registration was successful, false otherwise.
     */
    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    /**
     * Verifies user credentials for login.
     */
    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email=? AND password=?",
                new String[]{email, password});
        boolean found = cursor.getCount() > 0;
        cursor.close();
        return found;
    }

    /**
     * Retrieves user details based on credentials.
     * Extracts a username from the email if no explicit username field is present.
     */
    public User getUserByEmailAndPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email=? AND password=?",
                new String[]{email, password});

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));

            String emailValue = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String username = emailValue.contains("@")
                    ? emailValue.substring(0, emailValue.indexOf("@"))
                    : emailValue;
            user.setUsername(username);
        }
        cursor.close();
        return user;
    }

    // ─── Song Operations ────────────────────────────────────────────

    /**
     * Adds a new song to the library.
     * @return the row ID of the newly inserted song, or -1 if an error occurred.
     */
    public long addSong(String title, String artist, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("artist", artist);
        values.put("uri", uri);
        return db.insert("songs", null, values);
    }

    /**
     * Retrieves all songs from the database, ordered by newest added.
     */
    public ArrayList<Song> getAllSongs() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM songs ORDER BY id DESC", null);
        ArrayList<Song> songs = cursorToSongList(cursor);
        cursor.close();
        return songs;
    }

    /**
     * Search songs by title or artist (case-insensitive partial match).
     */
    public ArrayList<Song> searchSongs(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String likeQuery = "%" + query + "%";
        Cursor cursor = db.rawQuery(
                "SELECT * FROM songs WHERE title LIKE ? OR artist LIKE ? ORDER BY id DESC",
                new String[]{likeQuery, likeQuery});
        ArrayList<Song> songs = cursorToSongList(cursor);
        cursor.close();
        return songs;
    }

    /**
     * Returns songs that have been played at least once, ordered by play count descending.
     * Limited to top 20.
     */
    public ArrayList<Song> getTopPlayedSongs() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC, id DESC LIMIT 20",
                null);
        ArrayList<Song> songs = cursorToSongList(cursor);
        cursor.close();
        return songs;
    }

    /**
     * Increments the play count of a specific song by its ID.
     */
    public void incrementPlayCount(int songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE songs SET playCount = playCount + 1 WHERE id = ?",
                new Object[]{songId});
    }

    /**
     * Deletes a song by ID. Also removes it from any playlists via ON DELETE CASCADE.
     */
    public boolean deleteSong(int songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("songs", "id = ?", new String[]{String.valueOf(songId)});
        return rows > 0;
    }

    /**
     * Retrieves a single song by its ID.
     */
    public Song getSongById(int songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM songs WHERE id = ?",
                new String[]{String.valueOf(songId)});
        Song song = null;
        if (cursor.moveToFirst()) {
            song = cursorToSong(cursor);
        }
        cursor.close();
        return song;
    }

    // ─── Playlist Operations ────────────────────────────────────────

    /**
     * Creates a new empty playlist and returns its ID, or -1 on failure.
     */
    public long createPlaylist(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        return db.insert("playlists", null, values);
    }

    /**
     * Returns all playlists with their songs loaded.
     */
    public ArrayList<Playlist> getAllPlaylists() {
        ArrayList<Playlist> playlists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM playlists ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Playlist playlist = new Playlist();
                playlist.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                playlist.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                playlists.add(playlist);
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (Playlist p : playlists) {
            p.setSongs(getPlaylistSongs(p.getId()));
        }
        return playlists;
    }

    /**
     * Returns all songs in a playlist, ordered by position.
     */
    public ArrayList<Song> getPlaylistSongs(int playlistId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT s.* FROM songs s " +
                        "INNER JOIN playlist_songs ps ON s.id = ps.songId " +
                        "WHERE ps.playlistId = ? ORDER BY ps.position ASC",
                new String[]{String.valueOf(playlistId)});
        ArrayList<Song> songs = cursorToSongList(cursor);
        cursor.close();
        return songs;
    }

    /**
     * Adds a song to a playlist at the end.
     */
    public long addSongToPlaylist(int playlistId, int songId) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(MAX(position), -1) FROM playlist_songs WHERE playlistId = ?",
                new String[]{String.valueOf(playlistId)});
        int nextPosition = 0;
        if (cursor.moveToFirst()) {
            nextPosition = cursor.getInt(0) + 1;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("playlistId", playlistId);
        values.put("songId", songId);
        values.put("position", nextPosition);
        return db.insert("playlist_songs", null, values);
    }

    /**
     * Removes a song from a playlist.
     */
    public boolean removeSongFromPlaylist(int playlistId, int songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("playlist_songs",
                "playlistId = ? AND songId = ?",
                new String[]{String.valueOf(playlistId), String.valueOf(songId)});
        return rows > 0;
    }

    /**
     * Deletes an entire playlist and its associations.
     */
    public boolean deletePlaylist(int playlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("playlists", "id = ?",
                new String[]{String.valueOf(playlistId)});
        return rows > 0;
    }

    /**
     * Renames a playlist.
     */
    public boolean renamePlaylist(int playlistId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        int rows = db.update("playlists", values, "id = ?",
                new String[]{String.valueOf(playlistId)});
        return rows > 0;
    }

    // ─── Helpers ────────────────────────────────────────────────────

    private ArrayList<Song> cursorToSongList(Cursor cursor) {
        ArrayList<Song> songs = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                songs.add(cursorToSong(cursor));
            } while (cursor.moveToNext());
        }
        return songs;
    }

    private Song cursorToSong(Cursor cursor) {
        Song song = new Song();
        song.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow("artist")));
        song.setUri(cursor.getString(cursor.getColumnIndexOrThrow("uri")));
        song.setPlayCount(cursor.getInt(cursor.getColumnIndexOrThrow("playCount")));
        return song;
    }
}