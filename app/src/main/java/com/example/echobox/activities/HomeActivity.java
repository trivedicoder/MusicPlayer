package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.echobox.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MaterialCardView cardMySongs, cardTopPlayed;
    private MaterialButton btnAddSongHome, btnLogout;
    private FloatingActionButton fabPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cardMySongs = findViewById(R.id.cardMySongs);
        cardTopPlayed = findViewById(R.id.cardTopPlayed);
        btnAddSongHome = findViewById(R.id.btnAddSongHome);
        btnLogout = findViewById(R.id.btnLogout);
        fabPlayer = findViewById(R.id.fabPlayer);

        btnAddSongHome.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, AddSongActivity.class)));

        cardMySongs.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SongListActivity.class)));

        cardTopPlayed.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, TopPlayedActivity.class)));

        fabPlayer.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SongListActivity.class)));

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }
}