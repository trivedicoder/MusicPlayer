package com.example.echobox.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.adapters.SongAdapter;
import com.example.echobox.database.DBHelper;
import com.example.echobox.models.Song;

import java.util.ArrayList;

/**
 * TopPlayedActivity displays the most frequently played songs,
 * filtered to only those with at least one play, capped at 20.
 * Shows an empty state when nothing has been played yet.
 */
public class TopPlayedActivity extends AppCompatActivity implements SongAdapter.OnSongDeletedListener {

    private Toolbar toolbar;
    private TextView tvSongCount;
    private RecyclerView rvSongs;
    private LinearLayout emptyState;
    private DBHelper dbHelper;
    private ArrayList<Song> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_played);

        toolbar = findViewById(R.id.toolbar);
        tvSongCount = findViewById(R.id.tvSongCount);
        rvSongs = findViewById(R.id.rvSongs);
        emptyState = findViewById(R.id.emptyState);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        dbHelper = DBHelper.getInstance(this);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        loadTopPlayed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTopPlayed();
    }

    private void loadTopPlayed() {
        songs = dbHelper.getTopPlayedSongs();
        tvSongCount.setText(songs.size() + (songs.size() == 1 ? " Song" : " Songs"));

        if (songs.isEmpty()) {
            rvSongs.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvSongs.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            SongAdapter adapter = new SongAdapter(this, songs);
            adapter.setOnSongDeletedListener(this);
            rvSongs.setAdapter(adapter);
        }
    }

    @Override
    public void onSongDeleted() {
        loadTopPlayed();
    }
}
