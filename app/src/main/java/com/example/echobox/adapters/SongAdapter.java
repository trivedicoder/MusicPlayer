package com.example.echobox.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.activities.PlayerActivity;
import com.example.echobox.models.Song;

import java.util.ArrayList;

/**
 * SongAdapter is a RecyclerView adapter used to display a list of Song objects.
 * It handles the creation of individual song items and binds song data to the UI.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final Context context;
    private final ArrayList<Song> songs;

    /**
     * Constructor for SongAdapter.
     * @param context The activity or fragment context.
     * @param songs The list of songs to be displayed.
     */
    public SongAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    /**
     * Inflates the item_song layout when a new ViewHolder is created.
     */
    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    /**
     * Binds song data (title, artist) to the views in the ViewHolder.
     * Also sets up click listeners for the song item and the options button.
     */
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.tvSongTitle.setText(song.getTitle());
        holder.tvSongArtist.setText(song.getArtist());

        // When a song is clicked, navigate to PlayerActivity and pass the song details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("songId", song.getId());
            intent.putExtra("title", song.getTitle());
            intent.putExtra("artist", song.getArtist());
            intent.putExtra("uri", song.getUri());
            context.startActivity(intent);
        });

        // Placeholder for additional song options (e.g., delete, share)
        holder.btnSongOptions.setOnClickListener(v ->
                Toast.makeText(context, "More options later", Toast.LENGTH_SHORT).show());
    }

    /**
     * Returns the total number of songs in the list.
     */
    @Override
    public int getItemCount() {
        return songs.size();
    }

    /**
     * ViewHolder class that holds references to the views for each song item.
     */
    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvSongTitle, tvSongArtist;
        ImageButton btnSongOptions;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
            btnSongOptions = itemView.findViewById(R.id.btnSongOptions);
        }
    }
}