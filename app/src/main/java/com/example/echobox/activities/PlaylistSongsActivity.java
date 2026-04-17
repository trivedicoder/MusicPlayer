package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.adapters.SongAdapter;
import com.example.echobox.models.Playlist;
import com.example.echobox.models.Song;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class PlaylistSongsActivity extends AppCompatActivity {

    private TextView tvSongCount;
    private RecyclerView rvSongs;

    private final ArrayList<Song> songs = new ArrayList<>();
    private SongAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String playlistId;
    private String playlistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        tvSongCount = findViewById(R.id.tvSongCount);
        rvSongs = findViewById(R.id.rvSongs);

        playlistId = getIntent().getStringExtra("playlistId");
        playlistName = getIntent().getStringExtra("playlistName");

        if (playlistId == null || playlistId.isEmpty()) {
            Toast.makeText(this, "Playlist missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new SongAdapter(songs, new SongAdapter.ClickListener() {
            @Override
            public void onSongClick(int position) {
                Intent intent = new Intent(PlaylistSongsActivity.this, PlayerActivity.class);
                intent.putExtra("songList", songs);
                intent.putExtra("currentPosition", position);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                removeSongFromPlaylist(songs.get(position));
            }
        }, true);

        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(adapter);

        loadPlaylistSongs();
    }

    private void loadPlaylistSongs() {
        db.collection("playlists")
                .document(playlistId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Playlist playlist = documentSnapshot.toObject(Playlist.class);

                    if (playlist == null) {
                        tvSongCount.setText("Playlist not found");
                        return;
                    }

                    ArrayList<String> songIds = playlist.getSongIds();

                    if (songIds == null || songIds.isEmpty()) {
                        songs.clear();
                        adapter.notifyDataSetChanged();
                        tvSongCount.setText((playlistName != null ? playlistName : "Playlist") + " (0 Songs)");
                        return;
                    }

                    songs.clear();

                    for (String songId : songIds) {
                        db.collection("songs")
                                .document(songId)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    Song song = doc.toObject(Song.class);
                                    if (song != null) {
                                        songs.add(song);
                                        adapter.notifyDataSetChanged();
                                        tvSongCount.setText((playlistName != null ? playlistName : "Playlist") + " (" + songs.size() + " Songs)");
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        tvSongCount.setText("Load failed: " + e.getMessage()));
    }

    private void removeSongFromPlaylist(Song song) {
        db.collection("playlists")
                .document(playlistId)
                .get()
                .addOnSuccessListener(doc -> {
                    Playlist playlist = doc.toObject(Playlist.class);
                    if (playlist == null || playlist.getSongIds() == null) return;

                    ArrayList<String> ids = playlist.getSongIds();
                    ids.remove(song.getId());

                    db.collection("playlists")
                            .document(playlistId)
                            .update("songIds", ids)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Removed from playlist", Toast.LENGTH_SHORT).show();
                                loadPlaylistSongs();
                            });
                });
    }
}