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

import com.project.tape.Fragments.FragmentGeneral;
import com.project.tape.R;
import com.project.tape.ItemClasses.Song;

import java.io.IOException;
import java.util.ArrayList;


public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private int item_index;
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
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_song, parent, false);
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

        if (item_index == position) {
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
        return mSongsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_title;
        TextView tv_artist;
        TextView tv_album;
        TextView tv_dash;
        OnSongListener onSongListener;
        LinearLayout item_linearlayout;

        public ViewHolder(@NonNull View itemView, OnSongListener onSongListener) {
            super(itemView);
            this.onSongListener = onSongListener;
            item_linearlayout = (LinearLayout) itemView.findViewById(R.id.song_item);

            tv_title = (TextView) itemView.findViewById(R.id.song_title);
            tv_artist = (TextView) itemView.findViewById(R.id.artist_title);
            tv_dash = (TextView) itemView.findViewById(R.id.dash);
            tv_album = (TextView) itemView.findViewById(R.id.album_title);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            try {
                onSongListener.onSongClick(getAdapterPosition());
                item_index = getAdapterPosition();
                notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void updateSongList(ArrayList<Song> songsArrayList) {
        mSongsList = new ArrayList<>();
        mSongsList.addAll(songsArrayList);
        notifyDataSetChanged();
    }

    public void updateColorAfterSongSwitch(int position) {
        item_index = FragmentGeneral.position;
        notifyDataSetChanged();
    }

    public interface OnSongListener {
        void onSongClick(int position) throws IOException;
    }
}