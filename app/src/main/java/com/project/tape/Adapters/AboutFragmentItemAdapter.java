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
import com.project.tape.ItemClasses.Song;

import java.io.IOException;
import java.util.ArrayList;

public class AboutFragmentItemAdapter extends RecyclerView.Adapter<AboutFragmentItemAdapter.ItemInfoViewHolder> {


    private int item_index;
    private Context mContext;
    private ArrayList<Song> songsInAlbumList;
    private OnItemListener onItemInfoListener;


    public AboutFragmentItemAdapter(Context mContext, ArrayList<Song> songsInAlbumList, OnItemListener onItemInfoListener) {
        this.mContext = mContext;
        this.songsInAlbumList = songsInAlbumList;
        this.onItemInfoListener = onItemInfoListener;
    }


    @NonNull
    @Override
    public ItemInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.item_song, parent, false);
        ItemInfoViewHolder vHolder = new ItemInfoViewHolder(v, onItemInfoListener);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemInfoViewHolder holder, int position) {
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


    public class ItemInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_title;
        TextView tv_artist;
        TextView tv_album;
        OnItemListener onAlbumInfoListener;
        LinearLayout item_linearlayout;

        public ItemInfoViewHolder(@NonNull View itemView, OnItemListener onAlbumInfoListener) {
            super(itemView);
            this.onAlbumInfoListener = onAlbumInfoListener;
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
                onAlbumInfoListener.onItemClick(getAdapterPosition());
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

    public interface OnItemListener {
        void onItemClick(int position) throws IOException;
    }


}
