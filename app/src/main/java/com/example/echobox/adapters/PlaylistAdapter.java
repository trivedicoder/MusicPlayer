package com.example.echobox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.models.Playlist;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public interface ClickListener {
        void onPlaylistClick(int position);
        void onDeletePlaylistClick(int position);
    }

    private final ArrayList<Playlist> playlists;
    private final ClickListener listener;

    public PlaylistAdapter(ArrayList<Playlist> playlists, ClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaylistName, tvPlaylistSongCount;
        ImageButton btnDeletePlaylist;

        ViewHolder(View itemView) {
            super(itemView);
            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);
            tvPlaylistSongCount = itemView.findViewById(R.id.tvPlaylistSongCount);
            btnDeletePlaylist = itemView.findViewById(R.id.btnDeletePlaylist);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist p = playlists.get(position);
        holder.tvPlaylistName.setText(p.getName());
        int count = p.getSongIds() == null ? 0 : p.getSongIds().size();
        holder.tvPlaylistSongCount.setText(count + " songs");

        holder.itemView.setOnClickListener(v -> listener.onPlaylistClick(position));
        holder.btnDeletePlaylist.setOnClickListener(v -> listener.onDeletePlaylistClick(position));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }
}