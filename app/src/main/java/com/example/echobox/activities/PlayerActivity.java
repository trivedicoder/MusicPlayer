package com.example.echobox.activities;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echobox.R;
import com.example.echobox.database.DBHelper;
import com.example.echobox.models.Song;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

/**
 * PlayerActivity provides the music playback interface.
 * Supports play/pause, seek, and previous/next navigation across a song list.
 * The song list and current index are passed via Intent extras so the player
 * knows what comes before and after the current track.
 */
public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_SONG_LIST = "songList";
    public static final String EXTRA_SONG_INDEX = "songIndex";

    private ImageButton btnBack, btnPrevious, btnPlayPause, btnNext;
    private TextView tvPlayerTitle, tvPlayerArtist, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean countUpdated = false;

    private final Handler handler = new Handler();
    private Runnable updateSeekBar;

    private ArrayList<Song> songList;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnBack = findViewById(R.id.btnBack);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);

        tvPlayerTitle = findViewById(R.id.tvPlayerTitle);
        tvPlayerArtist = findViewById(R.id.tvPlayerArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);

        seekBar = findViewById(R.id.seekBar);

        // Retrieve the full song list and the tapped index
        Serializable extra = getIntent().getSerializableExtra(EXTRA_SONG_LIST);
        if (extra instanceof ArrayList<?>) {
            @SuppressWarnings("unchecked")
            ArrayList<Song> list = (ArrayList<Song>) extra;
            songList = list;
        }
        currentIndex = getIntent().getIntExtra(EXTRA_SONG_INDEX, 0);

        if (songList == null || songList.isEmpty()) {
            Toast.makeText(this, "No songs to play", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Clamp index
        if (currentIndex < 0) currentIndex = 0;
        if (currentIndex >= songList.size()) currentIndex = songList.size() - 1;

        // Load the initial song
        loadSong(songList.get(currentIndex));

        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> togglePlayback());

        btnPrevious.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                loadSong(songList.get(currentIndex));
            } else {
                Toast.makeText(this, "Already at the first track", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < songList.size() - 1) {
                currentIndex++;
                loadSong(songList.get(currentIndex));
            } else {
                Toast.makeText(this, "End of the list", Toast.LENGTH_SHORT).show();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * Releases any existing player, sets up the UI for the given song,
     * prepares MediaPlayer, and auto-plays.
     */
    private void loadSong(Song song) {
        // Reset state
        releasePlayer();
        countUpdated = false;
        isPlaying = false;

        tvPlayerTitle.setText(song.getTitle() != null ? song.getTitle() : "Unknown Title");
        tvPlayerArtist.setText(song.getArtist() != null ? song.getArtist() : "Unknown Artist");
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        seekBar.setProgress(0);
        tvCurrentTime.setText(formatTime(0));
        tvTotalTime.setText(formatTime(0));

        String uriString = song.getUri();
        if (uriString == null || uriString.isEmpty()) {
            Toast.makeText(this, "Song file missing", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(uriString));
            mediaPlayer.prepare();

            seekBar.setMax(mediaPlayer.getDuration());
            tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));

            mediaPlayer.setOnCompletionListener(mp -> {
                // Auto-advance to next song, or reset if at end
                if (currentIndex < songList.size() - 1) {
                    currentIndex++;
                    loadSong(songList.get(currentIndex));
                    togglePlayback(); // auto-play next
                } else {
                    isPlaying = false;
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                    seekBar.setProgress(0);
                    tvCurrentTime.setText(formatTime(0));
                    stopSeekBarUpdates();
                }
            });

            // Auto-play when loading a new song via prev/next
            togglePlayback();

        } catch (IOException e) {
            Toast.makeText(this, "Could not load audio", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void togglePlayback() {
        if (mediaPlayer == null) return;

        if (!isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);

            if (!countUpdated) {
                Song current = songList.get(currentIndex);
                DBHelper dbHelper = DBHelper.getInstance(this);
                dbHelper.incrementPlayCount(current.getId());
                countUpdated = true;
            }

            startSeekBarUpdates();
        } else {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            stopSeekBarUpdates();
        }
    }

    private void startSeekBarUpdates() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.post(updateSeekBar);
    }

    private void stopSeekBarUpdates() {
        if (updateSeekBar != null) {
            handler.removeCallbacks(updateSeekBar);
        }
    }

    private String formatTime(int milliseconds) {
        int totalSeconds = milliseconds / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private void releasePlayer() {
        stopSeekBarUpdates();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
