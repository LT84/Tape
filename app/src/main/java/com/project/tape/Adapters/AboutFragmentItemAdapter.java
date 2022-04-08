package com.project.tape.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.R;
import com.project.tape.SecondaryClasses.Song;

import java.io.IOException;
import java.util.ArrayList;

public class AboutFragmentItemAdapter extends RecyclerView.Adapter<AboutFragmentItemAdapter.ItemInfoViewHolder> {

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
        v = LayoutInflater.from(mContext).inflate(R.layout.song_item, parent, false);
        ItemInfoViewHolder vHolder = new ItemInfoViewHolder(v, onItemInfoListener);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemInfoViewHolder holder, int position) {
        holder.tv_title.setText(songsInAlbumList.get(position).getTitle());
        holder.tv_artist.setText(songsInAlbumList.get(position).getArtist());
        holder.tv_album.setText(songsInAlbumList.get(position).getAlbum());
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

        public ItemInfoViewHolder(@NonNull View itemView, OnItemListener onAlbumInfoListener) {
            super(itemView);
            this.onAlbumInfoListener = onAlbumInfoListener;
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
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Changing color of item in recyclerView for a short time after it is clicked
            tv_title.setTextColor(Color.parseColor("#66d9e0"));
            tv_artist.setTextColor(Color.parseColor("#66d9e0"));
            tv_album.setTextColor(Color.parseColor("#66d9e0"));
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tv_title.setTextColor(Color.parseColor("#ffffff"));
                    tv_artist.setTextColor(Color.parseColor("#ffffff"));
                    tv_album.setTextColor(Color.parseColor("#ffffff"));
                }
            }, 300);

        }

    }

    public interface OnItemListener {
        void onItemClick(int position) throws IOException;
    }


}
