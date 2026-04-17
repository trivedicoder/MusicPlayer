package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.echobox.R;
import com.example.echobox.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MaterialCardView cardMySongs, cardTopPlayed;
    private MaterialButton btnAddSongHome, btnLogout;
    private FloatingActionButton fabPlayer;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        cardMySongs = findViewById(R.id.cardMySongs);
        cardTopPlayed = findViewById(R.id.cardTopPlayed);
        btnAddSongHome = findViewById(R.id.btnAddSongHome);
        btnLogout = findViewById(R.id.btnLogout);
        fabPlayer = findViewById(R.id.fabPlayer);
        MaterialButton btnPlaylists = findViewById(R.id.btnPlaylists);

        sessionManager = new SessionManager(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            String email = sessionManager.getUserEmail();
            String name = "User";

            if (email != null && !email.isEmpty()) {
                int atIndex = email.indexOf("@");
                if (atIndex > 0) {
                    name = email.substring(0, atIndex);
                } else {
                    name = email;
                }
            }

            getSupportActionBar().setTitle("Hello, " + name + "!");
        }
        btnPlaylists.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, PlaylistListActivity.class)));

        cardMySongs.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SongListActivity.class)));

        cardTopPlayed.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, TopPlayedActivity.class)));

        btnAddSongHome.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, AddSongActivity.class)));

        fabPlayer.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SongListActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            sessionManager.logoutUser();

            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}