package com.project.tape.Adapters;

import static com.project.tape.Activities.AboutPlaylist.currentSongsInPlaylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.R;
import com.project.tape.ItemClasses.Song;

import java.io.IOException;
import java.util.ArrayList;

public class AddSongsAdapter extends RecyclerView.Adapter<AddSongsAdapter.ViewHolder> {

    private Context mContext;

    public static ArrayList<Song> addSongsArray;

    private OnAddSongListener onAddSongListener;

    ArrayList<Song> checkedSongs = new ArrayList<>();

    public AddSongsAdapter(Context mContext, ArrayList<Song> addSongsArray, OnAddSongListener onAddSongListener) {
        this.mContext = mContext;
        this.addSongsArray = addSongsArray;
        this.onAddSongListener = onAddSongListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_add_song, parent, false);
        ViewHolder vHolder = new ViewHolder(v, onAddSongListener);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_title.setText(addSongsArray.get(position).getTitle());
        holder.tv_artist.setText(addSongsArray.get(position).getArtist());
        holder.tv_album.setText(addSongsArray.get(position).getAlbum());

        //in some cases, it will prevent unwanted situations
        holder.checkBox.setOnCheckedChangeListener(null);

        //if true, your checkbox will be selected, else unselected
        //holder.checkBox.setChecked(addSongsArray.get(position).getSelected());

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                if (isChecked) {
                    addSongsArray.get(position).setSelected(isChecked);
                    currentSongsInPlaylist.add(addSongsArray.get(position));
                } else {
                    addSongsArray.get(position).setSelected(isChecked);
                    currentSongsInPlaylist.remove(addSongsArray.get(position));
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return addSongsArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_title;
        TextView tv_artist;
        TextView tv_album;
        TextView tv_dash;
        CheckBox checkBox;
        OnAddSongListener onAddSongListener;

        public ViewHolder(@NonNull View itemView, OnAddSongListener onAddSongListener) {
            super(itemView);
            this.onAddSongListener = onAddSongListener;
            tv_title = itemView.findViewById(R.id.song_title_addSongs);
            tv_artist = itemView.findViewById(R.id.artist_title_addSongs);
            tv_dash = itemView.findViewById(R.id.dash_addSongs);
            tv_album = itemView.findViewById(R.id.album_title_addSongs);
            checkBox = itemView.findViewById(R.id.add_songs_checkBox);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            try {
                onAddSongListener.onAddSongClick(getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void updateAddSongsListList(ArrayList<Song> mAddSongsArray) {
        addSongsArray = new ArrayList<>();
        addSongsArray.addAll(mAddSongsArray);
        notifyDataSetChanged();
    }

    public void cleanCheckBoxes(boolean clearFlag) {

    }


    public interface OnAddSongListener {
        void onAddSongClick(int position) throws IOException;
    }


}
