package com.example.echobox.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.adapters.PlaylistAdapter;
import com.example.echobox.database.DBHelper;
import com.example.echobox.models.Playlist;
import com.example.echobox.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * HomeActivity is the main dashboard after login.
 * Shows navigation cards, a horizontal playlist list, quick actions,
 * and a FAB that opens the full song list for playback.
 */
public class HomeActivity extends AppCompatActivity implements PlaylistAdapter.OnPlaylistActionListener {

    private Toolbar toolbar;
    private MaterialCardView cardMySongs, cardTopPlayed;
    private MaterialButton btnAddSongHome, btnCreatePlaylist, btnLogout;
    private FloatingActionButton fabPlayer;
    private RecyclerView rvPlaylists;
    private SessionManager sessionManager;
    private DBHelper dbHelper;

    private ArrayList<Playlist> playlists;
    private PlaylistAdapter playlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        dbHelper = DBHelper.getInstance(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Hello, " + sessionManager.getUsername() + "!");

        cardMySongs = findViewById(R.id.cardMySongs);
        cardTopPlayed = findViewById(R.id.cardTopPlayed);
        btnAddSongHome = findViewById(R.id.btnAddSongHome);
        btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist);
        btnLogout = findViewById(R.id.btnLogout);
        fabPlayer = findViewById(R.id.fabPlayer);
        rvPlaylists = findViewById(R.id.rvPlaylists);

        // Navigation
        cardMySongs.setOnClickListener(v ->
                startActivity(new Intent(this, SongListActivity.class)));

        cardTopPlayed.setOnClickListener(v ->
                startActivity(new Intent(this, TopPlayedActivity.class)));

        btnAddSongHome.setOnClickListener(v ->
                startActivity(new Intent(this, AddSongActivity.class)));

        btnCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog());

        // FAB opens song list (where user can pick a song to play)
        fabPlayer.setOnClickListener(v ->
                startActivity(new Intent(this, SongListActivity.class)));

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Playlist horizontal list
        rvPlaylists.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        loadPlaylists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaylists();
    }

    private void loadPlaylists() {
        playlists = dbHelper.getAllPlaylists();
        playlistAdapter = new PlaylistAdapter(this, playlists);
        playlistAdapter.setOnPlaylistActionListener(this);
        rvPlaylists.setAdapter(playlistAdapter);
    }

    private void showCreatePlaylistDialog() {
        EditText input = new EditText(this);
        input.setHint("Playlist name");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(48, 16, 48, 0);
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("New Playlist")
                .setView(container)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long id = dbHelper.createPlaylist(name);
                    if (id != -1) {
                        Toast.makeText(this, "Playlist created", Toast.LENGTH_SHORT).show();
                        loadPlaylists();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeletePlaylist(Playlist playlist, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Playlist")
                .setMessage("Delete \"" + playlist.getName() + "\"? Songs won't be removed from your library.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deletePlaylist(playlist.getId());
                    playlists.remove(position);
                    playlistAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Playlist deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRenamePlaylist(Playlist playlist, int position) {
        EditText input = new EditText(this);
        input.setText(playlist.getName());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setSelectAllOnFocus(true);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(48, 16, 48, 0);
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Rename Playlist")
                .setView(container)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbHelper.renamePlaylist(playlist.getId(), newName);
                    playlist.setName(newName);
                    playlistAdapter.notifyItemChanged(position);
                    Toast.makeText(this, "Playlist renamed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
