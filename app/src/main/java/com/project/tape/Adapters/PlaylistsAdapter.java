package com.project.tape.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.R;
import com.project.tape.SecondaryClasses.Playlist;

import java.io.IOException;
import java.util.ArrayList;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

    private Context mContext;

    public static ArrayList<Playlist> mPlaylistsList = new ArrayList<>();

    private OnPlaylistListener mOnPlaylistListener;


    public PlaylistsAdapter(Context mContext, ArrayList<Playlist> mPlaylistsList, OnPlaylistListener mOnPlaylistListener) {
        this.mContext = mContext;
        this.mPlaylistsList = mPlaylistsList;
        this.mOnPlaylistListener = mOnPlaylistListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.playlist_item, parent, false);
        ViewHolder vHolder = new ViewHolder(v, mOnPlaylistListener);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_playlist_title;
        OnPlaylistListener onPlaylistListener;


        public ViewHolder(@NonNull View itemView, OnPlaylistListener onPlaylistListener) {
            super(itemView);
            this.onPlaylistListener = onPlaylistListener;
            tv_playlist_title = (TextView) itemView.findViewById(R.id.playlist_name);
            
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            try {
                onPlaylistListener.onPlaylistClick(getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public void updatePlaylistList(ArrayList<Playlist> playlistArrayList) {
        mPlaylistsList = new ArrayList<>();
        mPlaylistsList.addAll(playlistArrayList);
        notifyDataSetChanged();
    }


    public interface OnPlaylistListener {
        void onPlaylistClick(int position) throws IOException;
    }


}
