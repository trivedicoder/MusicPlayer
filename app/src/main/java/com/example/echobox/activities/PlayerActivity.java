package com.example.echobox.activities;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echobox.R;
import com.example.echobox.models.Song;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity {

    private TextView tvTitle, tvArtist, tvCurrentTime, tvTotalTime;
    private ImageButton btnPlayPause, btnNext, btnPrevious;
    private MaterialButton btnShuffle, btnRepeat;
    private SeekBar seekBar;

    private static MediaPlayer player;

    private ArrayList<Song> list;
    private int pos;

    private FirebaseFirestore db;

    private boolean shuffle = false;
    private boolean repeat = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                try {
                    if (player.isPlaying()) {
                        int currentPosition = player.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        tvCurrentTime.setText(formatTime(currentPosition));
                    }
                } catch (IllegalStateException ignored) {
                }
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_player);

        tvTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        seekBar = findViewById(R.id.seekBar);

        db = FirebaseFirestore.getInstance();

        list = (ArrayList<Song>) getIntent().getSerializableExtra("songList");
        pos = getIntent().getIntExtra("currentPosition", 0);

        if (list == null || list.isEmpty()) {
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (pos < 0 || pos >= list.size()) {
            pos = 0;
        }

        btnShuffle.setText("Shuffle");
        btnRepeat.setText("Repeat");
        tvCurrentTime.setText("0:00");
        tvTotalTime.setText("0:00");

        play();

        btnPlayPause.setOnClickListener(v -> {
            if (player == null) return;

            try {
                if (player.isPlaying()) {
                    player.pause();
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    player.start();
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                }
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Playback error", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            pos = shuffle ? new Random().nextInt(list.size()) : (pos + 1) % list.size();
            play();
        });

        btnPrevious.setOnClickListener(v -> {
            if (shuffle) {
                pos = new Random().nextInt(list.size());
            } else {
                pos--;
                if (pos < 0) pos = list.size() - 1;
            }
            play();
        });

        btnShuffle.setOnClickListener(v -> {
            shuffle = !shuffle;
            if (shuffle) repeat = false;
            btnShuffle.setText(shuffle ? "Shuffle ON" : "Shuffle");
            btnRepeat.setText("Repeat");
        });

        btnRepeat.setOnClickListener(v -> {
            repeat = !repeat;
            if (repeat) shuffle = false;
            btnRepeat.setText(repeat ? "Repeat ON" : "Repeat");
            btnShuffle.setText("Shuffle");
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null) {
                    try {
                        player.seekTo(seekBar.getProgress());
                    } catch (IllegalStateException ignored) {
                    }
                }
            }
        });
    }

    private void play() {
        Song s = list.get(pos);

        tvTitle.setText(s.getTitle());
        tvArtist.setText(s.getArtist());
        tvCurrentTime.setText("0:00");

        releasePlayer();

        player = new MediaPlayer();

        try {
            player.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            player.setDataSource(s.getUrl());
            player.prepare();
            player.start();

            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);

            int duration = player.getDuration();
            seekBar.setMax(duration);
            seekBar.setProgress(0);
            tvTotalTime.setText(formatTime(duration));

            db.collection("songs")
                    .document(s.getId())
                    .update("playCount", FieldValue.increment(1));

        } catch (IOException | IllegalStateException e) {
            Toast.makeText(this, "Unable to play song", Toast.LENGTH_SHORT).show();
            return;
        }

        player.setOnCompletionListener(mp -> {
            if (repeat) {
                play();
            } else {
                pos = shuffle ? new Random().nextInt(list.size()) : (pos + 1) % list.size();
                play();
            }
        });

        handler.removeCallbacks(updateSeekBarRunnable);
        handler.post(updateSeekBarRunnable);
    }

    private void releasePlayer() {
        handler.removeCallbacks(updateSeekBarRunnable);

        if (player != null) {
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
            } catch (IllegalStateException ignored) {
            }

            try {
                player.reset();
            } catch (IllegalStateException ignored) {
            }

            player.release();
            player = null;
        }
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}