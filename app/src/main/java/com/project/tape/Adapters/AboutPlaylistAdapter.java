package com.project.tape.Adapters;

import static com.project.tape.Fragments.SongsFragment.albumName;
import static com.project.tape.Fragments.SongsFragment.artistName;
import static com.project.tape.Fragments.SongsFragment.previousAlbumName;
import static com.project.tape.Fragments.SongsFragment.previousArtistName;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.R;
import com.project.tape.SecondaryClasses.Song;

import java.io.IOException;
import java.util.ArrayList;

public class AboutPlaylistAdapter extends RecyclerView.Adapter<AboutPlaylistAdapter.PlaylistViewHolder> {


    private int item_index;
    private Context mContext;
    private ArrayList<Song> songsInAlbumList;
    private OnPlaylistListener onPlaylistListener;


    public AboutPlaylistAdapter(Context mContext, ArrayList<Song> songsInAlbumList, OnPlaylistListener onPlaylistListener) {
        this.mContext = mContext;
        this.songsInAlbumList = songsInAlbumList;
        this.onPlaylistListener = onPlaylistListener;
    }


    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.song_item, parent, false);
        PlaylistViewHolder vHolder = new PlaylistViewHolder(v, onPlaylistListener);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.tv_title.setText(songsInAlbumList.get(position).getTitle());
        holder.tv_artist.setText(songsInAlbumList.get(position).getArtist());
        holder.tv_album.setText(songsInAlbumList.get(position).getAlbum());

        if (item_index == position &&  previousAlbumName.equals(albumName)) {
            holder.tv_title.setTextColor(Color.parseColor("#ff03dac5"));
            holder.tv_artist.setTextColor(Color.parseColor("#ff03dac5"));
            holder.tv_album.setTextColor(Color.parseColor("#ff03dac5"));
        } else if (item_index == position && previousArtistName.equals(artistName)){
            holder.tv_title.setTextColor(Color.parseColor("#ff03dac5"));
            holder.tv_artist.setTextColor(Color.parseColor("#ff03dac5"));
            holder.tv_album.setTextColor(Color.parseColor("#ff03dac5"));
        } else {
            holder.tv_title.setTextColor(Color.parseColor("#ffffff"));
            holder.tv_artist.setTextColor(Color.parseColor("#b3ffffff"));
            holder.tv_album.setTextColor(Color.parseColor("#b3ffffff"));
        }
    }

    @Override
    public int getItemCount() {
        return songsInAlbumList.size();
    }


    public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_title;
        TextView tv_artist;
        TextView tv_album;
        OnPlaylistListener onPlaylistListener;
        LinearLayout item_linearlayout;

        public PlaylistViewHolder(@NonNull View itemView, OnPlaylistListener onPlaylistListener) {
            super(itemView);
            this.onPlaylistListener = onPlaylistListener;
            item_linearlayout = (LinearLayout) itemView.findViewById(R.id.song_item);

            tv_title = (TextView) itemView.findViewById(R.id.song_title);
            tv_artist = (TextView) itemView.findViewById(R.id.artist_title);
            tv_album = (TextView) itemView.findViewById(R.id.album_title);
            tv_title.setTextColor(Color.parseColor("#ffffff"));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                onPlaylistListener.onPlaylistClick(getAdapterPosition());
                item_index = getAdapterPosition();
                notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateColorAfterSongSwitch(int position) {
        item_index = position;
        notifyDataSetChanged();
    }

    public interface OnPlaylistListener {
        void onPlaylistClick(int position) throws IOException;
    }


}
