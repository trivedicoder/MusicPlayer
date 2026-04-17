package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.adapters.SongAdapter;
import com.example.echobox.models.Song;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

public class TopPlayedActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvSongCount;
    private RecyclerView rvSongs;
    private ExtendedFloatingActionButton fabAddSongList;

    private final ArrayList<Song> songs = new ArrayList<>();
    private SongAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        toolbar = findViewById(R.id.toolbar);
        tvSongCount = findViewById(R.id.tvSongCount);
        rvSongs = findViewById(R.id.rvSongs);
        fabAddSongList = findViewById(R.id.fabAddSongList);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SongAdapter(songs, new SongAdapter.ClickListener() {
            @Override
            public void onSongClick(int position) {
                Intent intent = new Intent(TopPlayedActivity.this, PlayerActivity.class);
                intent.putExtra("songList", songs);
                intent.putExtra("currentPosition", position);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                // no delete in top played
            }
        }, false);

        rvSongs.setAdapter(adapter);
        fabAddSongList.hide();

        loadTopPlayedSongs();
    }

    private void loadTopPlayedSongs() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            songs.clear();
            adapter.notifyDataSetChanged();
            tvSongCount.setText("0 Songs");
            return;
        }

        db.collection("songs")
                .whereEqualTo("ownerId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    songs.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Song song = doc.toObject(Song.class);
                        if (song != null) {
                            songs.add(song);
                        }
                    }

                    Collections.sort(songs, (s1, s2) -> Long.compare(s2.getPlayCount(), s1.getPlayCount()));

                    tvSongCount.setText(songs.size() + (songs.size() == 1 ? " Song" : " Songs"));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> tvSongCount.setText("Load failed"));
    }
}