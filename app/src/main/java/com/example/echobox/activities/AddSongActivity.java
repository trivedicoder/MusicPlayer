package com.example.echobox.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.echobox.R;
import com.example.echobox.models.Song;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddSongActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etArtist;
    private MaterialButton btnSelect, btnUpload;

    private Uri audioUri;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private final ActivityResultLauncher<Intent> pickAudioLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    audioUri = result.getData().getData();
                    if (audioUri != null) {
                        Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        etTitle = findViewById(R.id.etTitle);
        etArtist = findViewById(R.id.etArtist);
        btnSelect = findViewById(R.id.btnSelectFile);
        btnUpload = findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            pickAudioLauncher.launch(intent);
        });

        btnUpload.setOnClickListener(v -> uploadSong());
    }

    private void uploadSong() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String artist = etArtist.getText() != null ? etArtist.getText().toString().trim() : "";

        if (title.isEmpty() || artist.isEmpty() || audioUri == null) {
            Toast.makeText(this, "Fill all fields and select a file", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpload.setEnabled(false);

        String id = UUID.randomUUID().toString();
        String ownerId = currentUser.getUid();

        StorageReference ref = storage.getReference()
                .child("songs")
                .child(ownerId)
                .child(id + ".mp3");

        ref.putFile(audioUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            Song song = new Song(
                                    id,
                                    ownerId,
                                    title,
                                    artist,
                                    downloadUri.toString(),
                                    0
                            );

                            db.collection("songs")
                                    .document(id)
                                    .set(song)
                                    .addOnSuccessListener(unused -> {
                                        btnUpload.setEnabled(true);
                                        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnUpload.setEnabled(true);
                                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }).addOnFailureListener(e -> {
                            btnUpload.setEnabled(true);
                            Toast.makeText(this, "URL failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        })
                )
                .addOnFailureListener(e -> {
                    btnUpload.setEnabled(true);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}