package com.example.echobox.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.echobox.R;
import com.example.echobox.database.DBHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddSongActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText etSongTitle, etSongArtist;
    private MaterialButton btnChooseAudio, btnSaveSong;
    private TextView tvSelectedFile;

    private Uri selectedAudioUri;
    private DBHelper dbHelper;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedAudioUri = result.getData().getData();

                    if (selectedAudioUri != null) {
                        final int takeFlags = result.getData().getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        try {
                            getContentResolver().takePersistableUriPermission(selectedAudioUri, takeFlags);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        tvSelectedFile.setText(getFileName(selectedAudioUri));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        dbHelper = new DBHelper(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSongTitle = findViewById(R.id.etSongTitle);
        etSongArtist = findViewById(R.id.etSongArtist);
        btnChooseAudio = findViewById(R.id.btnChooseAudio);
        btnSaveSong = findViewById(R.id.btnSaveSong);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);

        btnChooseAudio.setOnClickListener(v -> openAudioPicker());

        btnSaveSong.setOnClickListener(v -> saveSong());
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        filePickerLauncher.launch(intent);
    }

    private void saveSong() {
        String title = etSongTitle.getText() != null ? etSongTitle.getText().toString().trim() : "";
        String artist = etSongArtist.getText() != null ? etSongArtist.getText().toString().trim() : "";

        if (title.isEmpty()) {
            etSongTitle.setError("Enter song title");
            return;
        }

        if (artist.isEmpty()) {
            etSongArtist.setError("Enter artist name");
            return;
        }

        if (selectedAudioUri == null) {
            Toast.makeText(this, "Please choose an audio file", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.addSong(title, artist, selectedAudioUri.toString());

        if (result != -1) {
            Toast.makeText(this, "Song saved to library", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving song", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = "Selected audio";
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex);
                }
            }
        }
        return result;
    }
}