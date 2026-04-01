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

import java.io.IOException;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private ImageButton btnBack, btnPrevious, btnPlayPause, btnNext;
    private TextView tvPlayerTitle, tvPlayerArtist, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean countUpdated = false;

    private final Handler handler = new Handler();
    private Runnable updateSeekBar;

    private int songId = -1;
    private String uriString;

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

        songId = getIntent().getIntExtra("songId", -1);
        String title = getIntent().getStringExtra("title");
        String artist = getIntent().getStringExtra("artist");
        uriString = getIntent().getStringExtra("uri");

        tvPlayerTitle.setText(title != null ? title : "Unknown Title");
        tvPlayerArtist.setText(artist != null ? artist : "Unknown Artist");

        btnBack.setOnClickListener(v -> finish());

        btnPrevious.setOnClickListener(v ->
                Toast.makeText(this, "Previous later", Toast.LENGTH_SHORT).show());

        btnNext.setOnClickListener(v ->
                Toast.makeText(this, "Next later", Toast.LENGTH_SHORT).show());

        setupMediaPlayer();

        btnPlayPause.setOnClickListener(v -> togglePlayback());

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

    private void setupMediaPlayer() {
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
            tvCurrentTime.setText(formatTime(0));

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                seekBar.setProgress(0);
                tvCurrentTime.setText(formatTime(0));
                stopSeekBarUpdates();
            });

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

            if (!countUpdated && songId != -1) {
                DBHelper dbHelper = new DBHelper(this);
                dbHelper.incrementPlayCount(songId);
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
                if (mediaPlayer != null) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSeekBarUpdates();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}