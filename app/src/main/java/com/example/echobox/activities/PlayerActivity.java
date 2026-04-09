package com.example.echobox.activities;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echobox.R;
import com.example.echobox.models.Song;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity {

    private TextView tvSongTitle, tvArtist;
    private ImageButton btnPlayPause, btnNext, btnPrevious;
    private MaterialButton btnShuffle, btnRepeat;

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songList;
    private int currentPosition = 0;

    private boolean isShuffleOn = false;
    private boolean isRepeatOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);

        Object extra = getIntent().getSerializableExtra("songList");
        if (extra instanceof ArrayList<?>) {
            try {
                songList = (ArrayList<Song>) extra;
            } catch (Exception e) {
                songList = new ArrayList<>();
            }
        } else {
            songList = new ArrayList<>();
        }

        currentPosition = getIntent().getIntExtra("currentPosition", 0);

        if (songList == null) {
            songList = new ArrayList<>();
        }

        if (songList.isEmpty()) {
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentPosition < 0 || currentPosition >= songList.size()) {
            currentPosition = 0;
        }

        updateModeButtons();
        playSong(currentPosition);

        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer == null) return;

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (songList.isEmpty()) return;

            if (isShuffleOn) {
                currentPosition = getRandomSongIndex();
            } else {
                currentPosition++;
                if (currentPosition >= songList.size()) {
                    currentPosition = 0;
                }
            }

            playSong(currentPosition);
        });

        btnPrevious.setOnClickListener(v -> {
            if (songList.isEmpty()) return;

            if (isShuffleOn) {
                currentPosition = getRandomSongIndex();
            } else {
                currentPosition--;
                if (currentPosition < 0) {
                    currentPosition = songList.size() - 1;
                }
            }

            playSong(currentPosition);
        });

        btnShuffle.setOnClickListener(v -> {
            isShuffleOn = !isShuffleOn;
            if (isShuffleOn) {
                isRepeatOn = false;
            }
            updateModeButtons();
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeatOn = !isRepeatOn;
            if (isRepeatOn) {
                isShuffleOn = false;
            }
            updateModeButtons();
        });
    }

    private void updateModeButtons() {
        btnShuffle.setText(isShuffleOn ? "Shuffle ON" : "Shuffle");
        btnRepeat.setText(isRepeatOn ? "Repeat ON" : "Repeat");
    }

    private int getRandomSongIndex() {
        if (songList == null || songList.isEmpty()) {
            return 0;
        }

        if (songList.size() == 1) {
            return 0;
        }

        Random random = new Random();
        int randomIndex;
        do {
            randomIndex = random.nextInt(songList.size());
        } while (randomIndex == currentPosition);

        return randomIndex;
    }

    private void playSong(int position) {
        if (songList == null || songList.isEmpty()) {
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (position < 0 || position >= songList.size()) {
            position = 0;
        }

        currentPosition = position;
        Song song = songList.get(position);

        tvSongTitle.setText(song.getTitle() != null ? song.getTitle() : "Unknown Title");
        tvArtist.setText(song.getArtist() != null ? song.getArtist() : "Unknown Artist");

        releasePlayer();

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            String uriString = song.getUri();
            if (uriString == null || uriString.trim().isEmpty()) {
                Toast.makeText(this, "Song file is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            mediaPlayer.setDataSource(this, Uri.parse(uriString));
            mediaPlayer.prepare();
            mediaPlayer.start();

            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);

            mediaPlayer.setOnCompletionListener(mp -> {
                if (isRepeatOn) {
                    playSong(currentPosition);
                } else if (isShuffleOn) {
                    currentPosition = getRandomSongIndex();
                    playSong(currentPosition);
                } else {
                    currentPosition++;
                    if (currentPosition >= songList.size()) {
                        currentPosition = 0;
                    }
                    playSong(currentPosition);
                }
            });

        } catch (IOException | IllegalArgumentException | SecurityException e) {
            Toast.makeText(this, "Unable to play song", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}