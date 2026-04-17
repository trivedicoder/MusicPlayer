package com.example.echobox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.models.Song;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    public interface ClickListener {
        void onSongClick(int position);
        void onDeleteClick(int position);
    }

    private final ArrayList<Song> list;
    private final ClickListener listener;
    private final boolean showDelete;

    public SongAdapter(ArrayList<Song> list, ClickListener listener, boolean showDelete) {
        this.list = list;
        this.listener = listener;
        this.showDelete = showDelete;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            artist = itemView.findViewById(R.id.tvArtist);
            btnDelete = itemView.findViewById(R.id.btnDeleteSong);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_with_delete, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song s = list.get(position);
        holder.title.setText(s.getTitle());
        holder.artist.setText(s.getArtist());

        holder.itemView.setOnClickListener(v -> listener.onSongClick(position));

        if (showDelete) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(position));
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}