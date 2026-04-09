package com.example.echobox.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.database.DBHelper;
import com.example.echobox.models.Song;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

/**
 * PlaylistActivity displays the contents of a single playlist.
 * Users can add songs from their library, remove songs, and
 * tap to play the entire playlist from any position.
 */
public class PlaylistActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYLIST_ID = "playlistId";
    public static final String EXTRA_PLAYLIST_NAME = "playlistName";

    private Toolbar toolbar;
    private TextView tvSongCount, tvPlaylistTitle;
    private RecyclerView rvSongs;
    private LinearLayout emptyState;
    private ExtendedFloatingActionButton fabAddToPlaylist, fabPlayAll;

    private DBHelper dbHelper;
    private int playlistId;
    private String playlistName;
    private ArrayList<Song> playlistSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playlistId = getIntent().getIntExtra(EXTRA_PLAYLIST_ID, -1);
        playlistName = getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);

        if (playlistId == -1) {
            Toast.makeText(this, "Playlist not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        tvPlaylistTitle = findViewById(R.id.tvPlaylistTitle);
        tvSongCount = findViewById(R.id.tvSongCount);
        rvSongs = findViewById(R.id.rvSongs);
        emptyState = findViewById(R.id.emptyState);
        fabAddToPlaylist = findViewById(R.id.fabAddToPlaylist);
        fabPlayAll = findViewById(R.id.fabPlayAll);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        tvPlaylistTitle.setText(playlistName != null ? playlistName : "Playlist");

        dbHelper = DBHelper.getInstance(this);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        fabAddToPlaylist.setOnClickListener(v -> showAddSongDialog());
        fabPlayAll.setOnClickListener(v -> {
            if (playlistSongs != null && !playlistSongs.isEmpty()) {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_SONG_LIST, playlistSongs);
                intent.putExtra(PlayerActivity.EXTRA_SONG_INDEX, 0);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Add songs to this playlist first", Toast.LENGTH_SHORT).show();
            }
        });

        loadPlaylistSongs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaylistSongs();
    }

    private void loadPlaylistSongs() {
        playlistSongs = dbHelper.getPlaylistSongs(playlistId);
        tvSongCount.setText(playlistSongs.size() + (playlistSongs.size() == 1 ? " song" : " songs"));

        if (playlistSongs.isEmpty()) {
            rvSongs.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            fabPlayAll.setVisibility(View.GONE);
        } else {
            rvSongs.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            fabPlayAll.setVisibility(View.VISIBLE);

            // Use a simple adapter that shows remove option instead of global delete
            PlaylistSongAdapter adapter = new PlaylistSongAdapter();
            rvSongs.setAdapter(adapter);
        }
    }

    /**
     * Shows a dialog listing all library songs not already in this playlist,
     * allowing the user to pick one to add.
     */
    private void showAddSongDialog() {
        ArrayList<Song> allSongs = dbHelper.getAllSongs();

        // Filter out songs already in the playlist
        ArrayList<Song> available = new ArrayList<>();
        for (Song s : allSongs) {
            boolean alreadyIn = false;
            if (playlistSongs != null) {
                for (Song ps : playlistSongs) {
                    if (ps.getId() == s.getId()) {
                        alreadyIn = true;
                        break;
                    }
                }
            }
            if (!alreadyIn) available.add(s);
        }

        if (available.isEmpty()) {
            Toast.makeText(this, "All songs are already in this playlist", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] titles = new String[available.size()];
        for (int i = 0; i < available.size(); i++) {
            titles[i] = available.get(i).getTitle() + " — " + available.get(i).getArtist();
        }

        new AlertDialog.Builder(this)
                .setTitle("Add song to playlist")
                .setItems(titles, (dialog, which) -> {
                    Song selected = available.get(which);
                    dbHelper.addSongToPlaylist(playlistId, selected.getId());
                    Toast.makeText(this, "Added \"" + selected.getTitle() + "\"", Toast.LENGTH_SHORT).show();
                    loadPlaylistSongs();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Simple inner adapter for playlist songs with a remove-from-playlist action.
     */
    private class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.VH> {

        @androidx.annotation.NonNull
        @Override
        public VH onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_song, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull VH holder, int position) {
            Song song = playlistSongs.get(position);
            holder.tvTitle.setText(song.getTitle());
            holder.tvArtist.setText(song.getArtist());

            // Tap to play from this position in the playlist
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                Intent intent = new Intent(PlaylistActivity.this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_SONG_LIST, playlistSongs);
                intent.putExtra(PlayerActivity.EXTRA_SONG_INDEX, pos);
                startActivity(intent);
            });

            // Options: remove from playlist
            holder.btnOptions.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                android.widget.PopupMenu popup = new android.widget.PopupMenu(PlaylistActivity.this, v);
                popup.getMenu().add(0, 1, 0, "Remove from playlist");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        Song s = playlistSongs.get(pos);
                        dbHelper.removeSongFromPlaylist(playlistId, s.getId());
                        Toast.makeText(PlaylistActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                        loadPlaylistSongs();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        @Override
        public int getItemCount() {
            return playlistSongs.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvArtist;
            android.widget.ImageButton btnOptions;

            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvSongTitle);
                tvArtist = v.findViewById(R.id.tvSongArtist);
                btnOptions = v.findViewById(R.id.btnSongOptions);
            }
        }
    }
}
