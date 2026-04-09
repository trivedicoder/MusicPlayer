package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

/**
 * SongListActivity displays the user's entire music library with
 * search/filter capabilities and an empty state when no songs exist.
 */
public class SongListActivity extends AppCompatActivity implements SongAdapter.OnSongDeletedListener {

    private Toolbar toolbar;
    private TextView tvSongCount;
    private RecyclerView rvSongs;
    private ExtendedFloatingActionButton fabAddSongList;
    private EditText etSearch;
    private LinearLayout emptyState;
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
        etSearch = findViewById(R.id.etSearch);
        emptyState = findViewById(R.id.emptyState);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = DBHelper.getInstance(this);

        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        refreshSongList();

        fabAddSongList.setOnClickListener(v ->
                startActivity(new Intent(SongListActivity.this, AddSongActivity.class)));

        // Search filter
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterSongs(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSongList();
    }

    private void refreshSongList() {
        songs = dbHelper.getAllSongs();
        updateUI(songs);
    }

    private void filterSongs(String query) {
        if (query.isEmpty()) {
            updateUI(songs);
        } else {
            ArrayList<Song> filtered = dbHelper.searchSongs(query);
            updateUI(filtered);
        }
    }

    private void updateUI(ArrayList<Song> displayList) {
        tvSongCount.setText(displayList.size() + (displayList.size() == 1 ? " Song" : " Songs"));

        if (displayList.isEmpty()) {
            rvSongs.setVisibility(View.GONE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        } else {
            rvSongs.setVisibility(View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(View.GONE);

            SongAdapter adapter = new SongAdapter(this, displayList);
            adapter.setOnSongDeletedListener(this);
            rvSongs.setAdapter(adapter);
        }
    }

    @Override
    public void onSongDeleted() {
        // Re-fetch from DB to keep counts and list in sync
        songs = dbHelper.getAllSongs();
        tvSongCount.setText(songs.size() + (songs.size() == 1 ? " Song" : " Songs"));
        if (songs.isEmpty()) {
            rvSongs.setVisibility(View.GONE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        }
    }
}
