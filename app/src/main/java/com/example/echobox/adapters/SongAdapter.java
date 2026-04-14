package com.example.echobox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.echobox.R;
import com.example.echobox.models.Song;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    public interface ClickListener {
        void onClick(int position);
    }

    private final ArrayList<Song> list;
    private final ClickListener listener;

    public SongAdapter(ArrayList<Song> list, ClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            artist = v.findViewById(R.id.tvArtist);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int i) {
        Song s = list.get(i);
        h.title.setText(s.getTitle());
        h.artist.setText(s.getArtist());
        h.itemView.setOnClickListener(v -> listener.onClick(i));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}