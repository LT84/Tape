package com.project.tape;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private Context mContext;
    private List<Song> albumList;
    private OnAlbumListener onAlbumListener;
    private Uri uri;
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();


    public AlbumAdapter(Context mContext, List<Song> albumList, OnAlbumListener onAlbumListener) {
        this.mContext = mContext;
        this.albumList = albumList;
        this.onAlbumListener = onAlbumListener;
    }


    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        AlbumViewHolder vHolder = new AlbumViewHolder(v, onAlbumListener);
        return vHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.tv_album_title.setText(albumList.get(position).getAlbum());
        uri = Uri.parse(albumList.get(position).getData());
        retriever.setDataSource(uri.toString());
        byte[] art = retriever.getEmbeddedPicture();

        if (art != null) {
            Glide.with(mContext)
                    .load(art)
                    .override(100, 100)
                    .placeholder(R.drawable.default_cover)
                    .into(holder.album_cover_albumFragment);
        } else {
            Glide.with(mContext)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .override(100, 100)
                    .into(holder.album_cover_albumFragment);
        }
    }


    @Override
    public int getItemCount() {
        return albumList.size();
    }


    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_album_title;
        ImageView album_cover_albumFragment;

        OnAlbumListener onAlbumListener;

        public AlbumViewHolder(@NonNull View itemView, OnAlbumListener onAlbumListener) {
            super(itemView);
            this.onAlbumListener = onAlbumListener;
            tv_album_title = (TextView) itemView.findViewById(R.id.album_title_albumFragment);
            album_cover_albumFragment = (ImageView)itemView.findViewById(R.id.album_cover_albumFragment);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)  {
            try {
                onAlbumListener.onAlbumClick(getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public interface OnAlbumListener {
        void onAlbumClick(int position) throws IOException;
    }


}

