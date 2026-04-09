package com.example.echobox.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.echobox.R;
import com.example.echobox.database.DBHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

/**
 * AddSongActivity lets users add one or multiple songs to their library.
 * Handles runtime storage permission, multi-file selection, and auto-derives
 * titles from filenames when multiple songs are picked at once.
 */
public class AddSongActivity extends AppCompatActivity {

    private static final int REQ_STORAGE_PERMISSION = 1001;

    private Toolbar toolbar;
    private TextInputEditText etSongTitle, etSongArtist;
    private MaterialButton btnChooseAudio, btnSaveSong;
    private TextView tvSelectedFile;

    private final ArrayList<Uri> selectedAudioUris = new ArrayList<>();
    private DBHelper dbHelper;

    /**
     * Handles the picker result — supports both single and multi-select.
     */
    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                selectedAudioUris.clear();
                Intent data = result.getData();

                // Multi-select: clipData is populated
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        if (uri != null) selectedAudioUris.add(uri);
                    }
                }
                // Single-select: fall back to getData()
                else if (data.getData() != null) {
                    selectedAudioUris.add(data.getData());
                }

                updateSelectedFilesLabel();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        dbHelper = DBHelper.getInstance(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSongTitle = findViewById(R.id.etSongTitle);
        etSongArtist = findViewById(R.id.etSongArtist);
        btnChooseAudio = findViewById(R.id.btnChooseAudio);
        btnSaveSong = findViewById(R.id.btnSaveSong);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);

        btnChooseAudio.setOnClickListener(v -> checkPermissionAndOpenPicker());
        btnSaveSong.setOnClickListener(v -> saveSongs());
    }

    /**
     * On API 23–32, reading content://media URIs returned by ACTION_GET_CONTENT
     * requires READ_EXTERNAL_STORAGE at runtime. Request it before opening the picker.
     * API 33+ uses READ_MEDIA_AUDIO and ACTION_OPEN_DOCUMENT flows that grant per-URI access.
     */
    private void checkPermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            int granted = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQ_STORAGE_PERMISSION);
                return;
            }
        }
        openAudioPicker();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAudioPicker();
            } else {
                Toast.makeText(this,
                        "Storage permission is required to pick audio files",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select audio"));
    }

    private void updateSelectedFilesLabel() {
        int count = selectedAudioUris.size();
        if (count == 0) {
            tvSelectedFile.setText("No file selected");
        } else if (count == 1) {
            tvSelectedFile.setText(getFileName(selectedAudioUris.get(0)));
        } else {
            tvSelectedFile.setText(count + " songs selected");
        }
    }

    /**
     * Saves selected songs to the database.
     * Single file: uses the title/artist from the form fields.
     * Multiple files: auto-derives title from filename, uses artist field (or "Unknown Artist").
     */
    private void saveSongs() {
        if (selectedAudioUris.isEmpty()) {
            Toast.makeText(this, "Please choose at least one audio file", Toast.LENGTH_SHORT).show();
            return;
        }

        String sharedArtist = etSongArtist.getText() != null
                ? etSongArtist.getText().toString().trim() : "";

        if (selectedAudioUris.size() == 1) {
            // Single-song flow — respect the user's title/artist input
            String title = etSongTitle.getText() != null
                    ? etSongTitle.getText().toString().trim() : "";

            if (title.isEmpty()) {
                // Fall back to filename if title is empty
                title = stripExtension(getFileName(selectedAudioUris.get(0)));
            }
            if (sharedArtist.isEmpty()) sharedArtist = "Unknown Artist";

            long result = dbHelper.addSong(title, sharedArtist, selectedAudioUris.get(0).toString());
            if (result != -1) {
                Toast.makeText(this, "Song saved to library", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error saving song", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Multi-song flow — auto-title from filenames
        if (sharedArtist.isEmpty()) sharedArtist = "Unknown Artist";

        int saved = 0;
        for (Uri uri : selectedAudioUris) {
            String title = stripExtension(getFileName(uri));
            long result = dbHelper.addSong(title, sharedArtist, uri.toString());
            if (result != -1) saved++;
        }

        Toast.makeText(this,
                "Added " + saved + " of " + selectedAudioUris.size() + " songs",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Removes the file extension from a filename (e.g. "song.mp3" → "song").
     */
    private String stripExtension(String filename) {
        if (filename == null) return "Unknown Title";
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    /**
     * Extracts the display name of a file from its Uri.
     */
    private String getFileName(Uri uri) {
        String result = "Unknown";
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}