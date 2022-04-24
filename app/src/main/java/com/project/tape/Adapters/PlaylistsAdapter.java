package com.project.tape.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.R;
import com.project.tape.ItemClasses.Playlist;

import java.util.ArrayList;


public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

    private Context mContext;
    public static ArrayList<Playlist> mPlaylistsList = new ArrayList<>();


    public PlaylistsAdapter(Context mContext, ArrayList<Playlist> mPlaylistsList) {
        this.mContext = mContext;
        this.mPlaylistsList = mPlaylistsList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_playlist, parent, false);
        ViewHolder vHolder = new ViewHolder(v);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_playlist_title.setText(mPlaylistsList.get(position).getPlaylistName());
    }

    @Override
    public int getItemCount() {
        return mPlaylistsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_playlist_title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_playlist_title = itemView.findViewById(R.id.playlist_name);
        }

    }

    public void updatePlaylistList(ArrayList<Playlist> playlistArrayList) {
        mPlaylistsList = new ArrayList<>();
        mPlaylistsList.addAll(playlistArrayList);
        notifyDataSetChanged();
    }


}
