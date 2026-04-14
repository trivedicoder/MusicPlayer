package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

public class SongListActivity extends AppCompatActivity {

    private RecyclerView rvSongs;
    private TextView tvSongCount;
    private ExtendedFloatingActionButton fabAdd;

    private final ArrayList<Song> songList = new ArrayList<>();
    private SongAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        rvSongs = findViewById(R.id.rvSongs);
        tvSongCount = findViewById(R.id.tvSongCount);
        fabAdd = findViewById(R.id.fabAddSongList);

        adapter = new SongAdapter(songList, position -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("songList", songList);
            intent.putExtra("currentPosition", position);
            startActivity(intent);
        });

        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(adapter);

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddSongActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSongs();
    }

    private void loadSongs() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            songList.clear();
            adapter.notifyDataSetChanged();
            tvSongCount.setText("0 Songs");
            return;
        }

        db.collection("songs")
                .whereEqualTo("ownerId", currentUser.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    songList.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : query) {
                        Song song = doc.toObject(Song.class);
                        if (song != null) {
                            songList.add(song);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    tvSongCount.setText(songList.size() + " Songs");
                })
                .addOnFailureListener(e -> tvSongCount.setText("Load failed"));
    }
}