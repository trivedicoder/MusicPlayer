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

/**
 * AddSongActivity allows users to add a new song to their local library.
 * It provides a form to enter song details (title, artist) and pick an audio file from the device.
 */
public class AddSongActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText etSongTitle, etSongArtist;
    private MaterialButton btnChooseAudio, btnSaveSong;
    private TextView tvSelectedFile;

    private Uri selectedAudioUri;
    private DBHelper dbHelper;

    /**
     * Launcher for the system file picker. Handles the result of picking an audio file,
     * requests persistable URI permissions so the app can access the file later,
     * and updates the UI with the selected filename.
     */
    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedAudioUri = result.getData().getData();

                    if (selectedAudioUri != null) {
                        // Request persistable permission to access the file across reboots
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

        // Initialize UI components and Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSongTitle = findViewById(R.id.etSongTitle);
        etSongArtist = findViewById(R.id.etSongArtist);
        btnChooseAudio = findViewById(R.id.btnChooseAudio);
        btnSaveSong = findViewById(R.id.btnSaveSong);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);

        // Set up click listeners
        btnChooseAudio.setOnClickListener(v -> openAudioPicker());
        btnSaveSong.setOnClickListener(v -> saveSong());
    }

    /**
     * Launches the system intent to pick an audio file.
     */
    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        filePickerLauncher.launch(intent);
    }

    /**
     * Validates user input and saves the song information (title, artist, and file URI)
     * into the local database using DBHelper.
     */
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

    /**
     * Helper method to extract the display name of a file from its Uri.
     */
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
