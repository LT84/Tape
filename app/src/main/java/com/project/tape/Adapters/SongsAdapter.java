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


public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private Context mContext;
    public static ArrayList<Song> mSongsList;
    private OnSongListener mOnSongListener;

    public static RecyclerView myRecyclerView;


    ArrayList<TextView> elementsList = new ArrayList<TextView>();


    public SongsAdapter(Context mContext, ArrayList<Song> mSongsList, OnSongListener mOnSongListener) {
        this.mContext = mContext;
        this.mSongsList = mSongsList;
        this.mOnSongListener = mOnSongListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.song_item, parent, false);
        ViewHolder vHolder = new ViewHolder(v, mOnSongListener);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_title.setText(mSongsList.get(position).getTitle());
        holder.tv_artist.setText(mSongsList.get(position).getArtist());
        holder.tv_album.setText(mSongsList.get(position).getAlbum());


        if (!elementsList.contains(holder.tv_title)) {
            elementsList.add(holder.tv_title);
        }

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
            tv_title.setTextColor(Color.parseColor("#ffffff"));
            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View v)  {
            try {
                onSongListener.onSongClick(getAdapterPosition());
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


    public void updateSongList(ArrayList<Song> songsArrayList) {
        mSongsList = new ArrayList<>();
        mSongsList.addAll(songsArrayList);
        notifyDataSetChanged();
    }

    public interface OnSongListener {
        void onSongClick(int position) throws IOException;
    }


}