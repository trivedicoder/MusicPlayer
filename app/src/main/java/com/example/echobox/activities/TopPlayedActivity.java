package com.example.echobox.activities;

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
 * TopPlayedActivity displays a list of songs filtered by their play count.
 * It reuses the song list layout to show the most frequently played tracks
 * in descending order.
 */
public class TopPlayedActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvSongCount;
    private RecyclerView rvSongs;
    private ExtendedFloatingActionButton fabAddSongList;
    private DBHelper dbHelper;
    private ArrayList<Song> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Reusing the song list layout for consistency
        setContentView(R.layout.activity_song_list);

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        tvSongCount = findViewById(R.id.tvSongCount);
        rvSongs = findViewById(R.id.rvSongs);
        fabAddSongList = findViewById(R.id.fabAddSongList);

        // Set up the Toolbar with a back navigation button
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Clear default title to use custom layout styling if needed
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        dbHelper = new DBHelper(this);
        
        // Fetch only the top played songs from the database
        songs = dbHelper.getTopPlayedSongs();

        // Update the UI with the number of top songs found
        tvSongCount.setText(songs.size() + (songs.size() == 1 ? " Song" : " Songs"));

        // Configure the RecyclerView with a vertical list layout and the SongAdapter
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(new SongAdapter(this, songs));

        // Hide the "Add Song" FAB as this is a read-only list of top tracks
        fabAddSongList.hide();
    }
}