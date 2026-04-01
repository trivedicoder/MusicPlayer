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

        dbHelper = new DBHelper(this);
        songs = dbHelper.getTopPlayedSongs();

        tvSongCount.setText(songs.size() + (songs.size() == 1 ? " Song" : " Songs"));

        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(new SongAdapter(this, songs));

        fabAddSongList.hide();
    }
}