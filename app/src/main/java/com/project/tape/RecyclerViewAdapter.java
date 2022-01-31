package com.project.tape;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    Context mContext;
    private ArrayList<Song> mSongsList;
    private OnSongListener mOnSongListener;

    public RecyclerViewAdapter(Context mContext, ArrayList<Song> mSongsList, OnSongListener monSongListener) {
        this.mContext = mContext;
        this.mSongsList = mSongsList;
        this.mOnSongListener = monSongListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.song_item, parent, false);
        ViewHolder vHolder = new ViewHolder(v, mOnSongListener);
        return vHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_title.setText(mSongsList.get(position).getTitle());
        holder.tv_artist.setText(mSongsList.get(position).getArtist());
        holder.tv_album.setText(mSongsList.get(position).getAlbum());


    }


    @Override
    public int getItemCount() {
        return mSongsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
         TextView tv_title;
         TextView tv_artist;
         TextView tv_album;
         OnSongListener onSongListener;

        public ViewHolder(@NonNull View itemView, OnSongListener onSongListener) {
            super(itemView);
            this.onSongListener = onSongListener;
            tv_title = (TextView) itemView.findViewById(R.id.song_title);
            tv_artist = (TextView) itemView.findViewById(R.id.artist_title);
            tv_album = (TextView) itemView.findViewById(R.id.album_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)  {
            try {
                onSongListener.onSongClick(getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public interface OnSongListener {
        void onSongClick(int position) throws IOException;
    }


}

