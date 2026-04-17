package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.adapters.PlaylistAdapter;
import com.example.echobox.models.Playlist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class PlaylistListActivity extends AppCompatActivity {

    private TextView tvPlaylistCount;
    private RecyclerView rvPlaylists;
    private Button btnCreatePlaylist;

    private final ArrayList<Playlist> playlists = new ArrayList<>();
    private PlaylistAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        tvPlaylistCount = findViewById(R.id.tvPlaylistCount);
        rvPlaylists = findViewById(R.id.rvPlaylists);
        btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist);

        adapter = new PlaylistAdapter(playlists, new PlaylistAdapter.ClickListener() {
            @Override
            public void onPlaylistClick(int position) {
                Playlist playlist = playlists.get(position);
                Intent intent = new Intent(PlaylistListActivity.this, PlaylistSongsActivity.class);
                intent.putExtra("playlistId", playlist.getId());
                intent.putExtra("playlistName", playlist.getName());
                startActivity(intent);
            }

            @Override
            public void onDeletePlaylistClick(int position) {
                deletePlaylist(playlists.get(position));
            }
        });

        rvPlaylists.setLayoutManager(new LinearLayoutManager(this));
        rvPlaylists.setAdapter(adapter);

        btnCreatePlaylist.setOnClickListener(v ->
                startActivity(new Intent(PlaylistListActivity.this, CreatePlaylistActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaylists();
    }

    private void loadPlaylists() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            playlists.clear();
            adapter.notifyDataSetChanged();
            tvPlaylistCount.setText("0 Playlists");
            return;
        }

        db.collection("playlists")
                .whereEqualTo("ownerId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    playlists.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : query) {
                        Playlist playlist = doc.toObject(Playlist.class);
                        if (playlist != null) {
                            playlists.add(playlist);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvPlaylistCount.setText(playlists.size() + " Playlists");
                });
    }

    private void deletePlaylist(Playlist playlist) {
        db.collection("playlists")
                .document(playlist.getId())
                .delete()
                .addOnSuccessListener(unused -> loadPlaylists());
    }
}