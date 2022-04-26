package com.project.tape.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.R;
import com.project.tape.ItemClasses.Song;

import java.io.IOException;
import java.util.ArrayList;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ArtistsViewHolder> {

    private Context mContext;
    public static ArrayList<Song> mArtistsList;
    private OnArtistListener onArtistListener;


    public ArtistsAdapter(Context mContext, ArrayList<Song> mArtistsList, OnArtistListener onAlbumListener) {
        this.mContext = mContext;
        this.mArtistsList = mArtistsList;
        this.onArtistListener = onAlbumListener;
    }


    @NonNull
    @Override
    public ArtistsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.item_artist, parent, false);
        ArtistsViewHolder vHolder = new ArtistsViewHolder(v, onArtistListener);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistsViewHolder holder, int position) {
        holder.tv_artist_name.setText(mArtistsList.get(position).getArtist());
    }

    @Override
    public int getItemCount() {
        return mArtistsList.size();
    }


    public class ArtistsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_artist_name;

        OnArtistListener onArtistListener;

        public ArtistsViewHolder(@NonNull View itemView, OnArtistListener onArtistListener) {
            super(itemView);
            this.onArtistListener = onArtistListener;
            tv_artist_name = (TextView) itemView.findViewById(R.id.artist_name_artistsFragment);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)  {
            try {
                onArtistListener.OnArtistsClick(getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void updateArtistsList(ArrayList<Song> artistsArrayList) {
        mArtistsList = new ArrayList<>();
        mArtistsList.addAll(artistsArrayList);
        notifyDataSetChanged();
    }


    public interface OnArtistListener {
        void OnArtistsClick(int position) throws IOException;
    }
}

