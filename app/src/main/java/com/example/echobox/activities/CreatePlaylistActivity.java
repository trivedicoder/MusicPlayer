package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.adapters.SelectableSongAdapter;
import com.example.echobox.models.Playlist;
import com.example.echobox.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.UUID;

public class CreatePlaylistActivity extends AppCompatActivity {

    private EditText etPlaylistName;
    private RecyclerView rvSongsForPlaylist;
    private Button btnSavePlaylist;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final ArrayList<Song> songs = new ArrayList<>();
    private SelectableSongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        etPlaylistName = findViewById(R.id.etPlaylistName);
        rvSongsForPlaylist = findViewById(R.id.rvSongsForPlaylist);
        btnSavePlaylist = findViewById(R.id.btnSavePlaylist);

        adapter = new SelectableSongAdapter(songs);
        rvSongsForPlaylist.setLayoutManager(new LinearLayoutManager(this));
        rvSongsForPlaylist.setAdapter(adapter);

        loadMySongs();

        btnSavePlaylist.setOnClickListener(v -> savePlaylist());
    }

    private void loadMySongs() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("songs")
                .whereEqualTo("ownerId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    songs.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : query) {
                        Song song = doc.toObject(Song.class);
                        if (song != null) {
                            songs.add(song);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load songs failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void savePlaylist() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etPlaylistName.getText().toString().trim();
        ArrayList<String> selectedSongIds = adapter.getSelectedSongIds();

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter playlist name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSongIds == null || selectedSongIds.isEmpty()) {
            Toast.makeText(this, "Select at least one song", Toast.LENGTH_SHORT).show();
            return;
        }

        String playlistId = UUID.randomUUID().toString();
        Playlist playlist = new Playlist(playlistId, user.getUid(), name, selectedSongIds);

        db.collection("playlists")
                .document(playlistId)
                .set(playlist)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Playlist saved", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CreatePlaylistActivity.this, PlaylistSongsActivity.class);
                    intent.putExtra("playlistId", playlistId);
                    intent.putExtra("playlistName", name);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}