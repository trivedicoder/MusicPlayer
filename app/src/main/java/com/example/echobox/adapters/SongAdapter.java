package com.example.echobox.adapters;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.activities.PlayerActivity;
import com.example.echobox.database.DBHelper;
import com.example.echobox.models.Song;

import java.util.ArrayList;

/**
 * SongAdapter displays a list of Song objects in a RecyclerView.
 * Supports tapping to play (with full list for prev/next), and a
 * popup menu for delete.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final Context context;
    private final ArrayList<Song> songs;
    private OnSongDeletedListener deleteListener;

    /**
     * Callback interface to notify the hosting Activity/Fragment
     * when a song has been deleted so it can refresh its UI.
     */
    public interface OnSongDeletedListener {
        void onSongDeleted();
    }

    public SongAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    public void setOnSongDeletedListener(OnSongDeletedListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.tvSongTitle.setText(song.getTitle());
        holder.tvSongArtist.setText(song.getArtist());

        // Launch player with the full song list and the tapped index
        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_SONG_LIST, songs);
            intent.putExtra(PlayerActivity.EXTRA_SONG_INDEX, adapterPos);
            context.startActivity(intent);
        });

        // Popup menu for song options
        holder.btnSongOptions.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenu().add(0, 1, 0, "Delete");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    showDeleteConfirmation(adapterPos);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void showDeleteConfirmation(int position) {
        Song song = songs.get(position);
        new AlertDialog.Builder(context)
                .setTitle("Delete Song")
                .setMessage("Remove \"" + song.getTitle() + "\" from your library?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DBHelper db = DBHelper.getInstance(context);
                    boolean deleted = db.deleteSong(song.getId());
                    if (deleted) {
                        songs.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, songs.size());
                        Toast.makeText(context, "Song deleted", Toast.LENGTH_SHORT).show();
                        if (deleteListener != null) {
                            deleteListener.onSongDeleted();
                        }
                    } else {
                        Toast.makeText(context, "Could not delete song", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

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
