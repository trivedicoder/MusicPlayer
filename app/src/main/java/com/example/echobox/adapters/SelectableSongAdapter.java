package com.example.echobox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.models.Song;

import java.util.ArrayList;
import java.util.HashSet;

public class SelectableSongAdapter extends RecyclerView.Adapter<SelectableSongAdapter.ViewHolder> {

    private final ArrayList<Song> songs;
    private final HashSet<String> selectedSongIds = new HashSet<>();

    public SelectableSongAdapter(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public ArrayList<String> getSelectedSongIds() {
        return new ArrayList<>(selectedSongIds);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;
        CheckBox checkSong;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            checkSong = itemView.findViewById(R.id.checkSong);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_selectable, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());

        holder.checkSong.setOnCheckedChangeListener(null);
        holder.checkSong.setChecked(selectedSongIds.contains(song.getId()));

        holder.checkSong.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedSongIds.add(song.getId());
            } else {
                selectedSongIds.remove(song.getId());
            }
        });

        holder.itemView.setOnClickListener(v ->
                holder.checkSong.setChecked(!holder.checkSong.isChecked()));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}