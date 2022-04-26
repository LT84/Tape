package com.project.tape.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.ItemClasses.Song;
import com.project.tape.R;

import java.util.ArrayList;


public class AboutPlaylistAdapter extends RecyclerView.Adapter<AboutPlaylistAdapter.PlaylistViewHolder> {

    private int item_index;
    private Context mContext;
    private ArrayList<Song> songsInAlbumList;

    public AboutPlaylistAdapter(Context mContext, ArrayList<Song> songsInAlbumList) {
        this.mContext = mContext;
        this.songsInAlbumList = songsInAlbumList;
    }


    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.item_song, parent, false);
        PlaylistViewHolder vHolder = new PlaylistViewHolder(v);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.tv_title.setText(songsInAlbumList.get(position).getTitle());
        holder.tv_artist.setText(songsInAlbumList.get(position).getArtist());
        holder.tv_album.setText(songsInAlbumList.get(position).getAlbum());

        if (item_index == position){
            holder.tv_title.setTextColor(Color.parseColor("#ff03dac5"));
            holder.tv_artist.setTextColor(Color.parseColor("#ff03dac5"));
            holder.tv_album.setTextColor(Color.parseColor("#ff03dac5"));
            holder.tv_dash.setTextColor(Color.parseColor("#ff03dac5"));
        } else {
            holder.tv_title.setTextColor(Color.parseColor("#ffffff"));
            holder.tv_artist.setTextColor(Color.parseColor("#b3ffffff"));
            holder.tv_album.setTextColor(Color.parseColor("#b3ffffff"));
            holder.tv_dash.setTextColor(Color.parseColor("#b3ffffff"));
        }
    }

    @Override
    public int getItemCount() {
        return songsInAlbumList.size();
    }


    public class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_artist;
        TextView tv_album;
        TextView tv_dash;
        LinearLayout item_linearlayout;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            item_linearlayout = (LinearLayout) itemView.findViewById(R.id.song_item);

            tv_title = (TextView) itemView.findViewById(R.id.song_title);
            tv_artist = (TextView) itemView.findViewById(R.id.artist_title);
            tv_dash = (TextView) itemView.findViewById(R.id.dash);
            tv_album = (TextView) itemView.findViewById(R.id.album_title);
            tv_title.setTextColor(Color.parseColor("#ffffff"));
        }
    }

    public void updatePlaylistList(ArrayList<Song> updateSongsInAlbumList) {
        songsInAlbumList = new ArrayList<>();
        songsInAlbumList.addAll(updateSongsInAlbumList);
        notifyDataSetChanged();
    }

    public void updateColorAfterSongSwitch(int position) {
        item_index = position;
        notifyDataSetChanged();
    }
}
