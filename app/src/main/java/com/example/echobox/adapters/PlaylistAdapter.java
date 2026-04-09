package com.example.echobox.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.activities.PlaylistActivity;
import com.example.echobox.models.Playlist;

import java.util.ArrayList;

/**
 * PlaylistAdapter displays a horizontal list of playlist cards.
 * Tapping a playlist starts playback of all its songs in order.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final Context context;
    private final ArrayList<Playlist> playlists;
    private OnPlaylistActionListener actionListener;

    public interface OnPlaylistActionListener {
        void onDeletePlaylist(Playlist playlist, int position);
        void onRenamePlaylist(Playlist playlist, int position);
    }

    public PlaylistAdapter(Context context, ArrayList<Playlist> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    public void setOnPlaylistActionListener(OnPlaylistActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);

        holder.tvPlaylistName.setText(playlist.getName());
        int count = playlist.getSongCount();
        holder.tvSongCount.setText(count + (count == 1 ? " song" : " songs"));

        // Tap to open the playlist detail view
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlaylistActivity.class);
            intent.putExtra(PlaylistActivity.EXTRA_PLAYLIST_ID, playlist.getId());
            intent.putExtra(PlaylistActivity.EXTRA_PLAYLIST_NAME, playlist.getName());
            context.startActivity(intent);
        });

        // Long press or options for delete/rename
        holder.btnPlaylistOptions.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenu().add(0, 1, 0, "Rename");
            popup.getMenu().add(0, 2, 1, "Delete");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1 && actionListener != null) {
                    actionListener.onRenamePlaylist(playlists.get(adapterPos), adapterPos);
                    return true;
                } else if (item.getItemId() == 2 && actionListener != null) {
                    actionListener.onDeletePlaylist(playlists.get(adapterPos), adapterPos);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaylistName, tvSongCount;
        ImageButton btnPlaylistOptions;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);
            tvSongCount = itemView.findViewById(R.id.tvSongCount);
            btnPlaylistOptions = itemView.findViewById(R.id.btnPlaylistOptions);
        }
    }
}
