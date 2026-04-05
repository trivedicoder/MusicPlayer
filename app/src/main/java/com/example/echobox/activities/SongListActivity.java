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
import com.example.echobox.database.DBHelper;
import com.example.echobox.models.Song;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

/**
 * SongListActivity displays the user's entire music library.
 * It fetches all saved songs from the database and displays them in a scrollable list.
 */
public class SongListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvSongCount;
    private RecyclerView rvSongs;
    private ExtendedFloatingActionButton fabAddSongList;
    private DBHelper dbHelper;
    private ArrayList<Song> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        tvSongCount = findViewById(R.id.tvSongCount);
        rvSongs = findViewById(R.id.rvSongs);
        fabAddSongList = findViewById(R.id.fabAddSongList);

        // Set up the Toolbar with a back button
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DBHelper(this);
        
        // Initial data load and UI setup
        refreshSongList();

        // Navigate to AddSongActivity when the FAB is clicked
        fabAddSongList.setOnClickListener(v ->
                startActivity(new Intent(SongListActivity.this, AddSongActivity.class)));
    }

    /**
     * Refresh the list whenever the user returns to this activity.
     * This ensures the list is up-to-date if a new song was just added.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshSongList();
    }

    /**
     * Fetches the latest song list from the database, updates the count display,
     * and re-attaches the adapter to the RecyclerView.
     */
    private void refreshSongList() {
        songs = dbHelper.getAllSongs();
        tvSongCount.setText(songs.size() + (songs.size() == 1 ? " Song" : " Songs"));

        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(new SongAdapter(this, songs));
    }
}