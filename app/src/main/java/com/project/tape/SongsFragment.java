package com.project.tape;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SongsFragment extends FragmentGeneral implements SongAdapter.OnSongListener {

    private RecyclerView myRecyclerView;
    private static final int VERTICAL_ITEM_SPACE = 3;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) throws NullPointerException{
        View v;
        v = inflater.inflate(R.layout.songs_fragment, container, false);
        //Loading audio list
        loadAudio();

        //Init views
        myRecyclerView = (RecyclerView) v.findViewById(R.id.compositions_recyclerview);
        song_title_main = (TextView) getActivity().findViewById(R.id.song_title_main);
        artist_name_main = (TextView) getActivity().findViewById(R.id.artist_name_main);

        album_cover_main = (ImageView) getActivity().findViewById(R.id.album_cover_main);
        mainPlayPauseBtn = (ImageButton) getActivity().findViewById(R.id.pause_button);
        song_title_main.setText(songsList.get(position).getTitle());
        artist_name_main.setText(songsList.get(position).getArtist());

        //Saving last played song
        position = getActivity().getSharedPreferences("preferences_name", Context.MODE_PRIVATE).getInt("progress", 0);
        uri = Uri.parse(songsList.get(position).getData());
        mediaPlayer = MediaPlayer.create(getContext(), uri);

        //Sets adapter to list and applies settings to recyclerView
        SongAdapter songAdapter = new SongAdapter(getContext(), songsList , this);
        myRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        myRecyclerView.setAdapter(songAdapter);
        return v;
    }


    //Creates mediaPlayer
    private void playMusic() {
        if (songsList != null) {
            uri = Uri.parse(songsList.get(position).getData());
        } if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(getContext(), uri);
        mediaPlayer.start();
    }


    //Plays song after it clicked in RecyclerView
    @Override
    public void onSongClick(int position) {
        this.position = position;

        getActivity().getSharedPreferences("preferences_name", Context.MODE_PRIVATE).edit().putInt("progress", this.position).commit();

        playMusic();
        metaDataInFragment(uri);

        song_title_main.setText(songsList.get(position).getTitle());
        artist_name_main.setText(songsList.get(position).getArtist());
        mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
    }


    /*Switches next composition, sets album cover in main, sets
       title and artist when SongsFragment is opened*/
    @Override
    public void onResume() {
        super.onResume();

        if (uri != null) {
            metaDataInFragment(uri);
        }

        if (mediaPlayer != null) {

            song_title_main.setText(songsList.get(position).getTitle());
            artist_name_main.setText(songsList.get(position).getArtist());

            if (mediaPlayer.isPlaying()) {
                mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
            } else  {
                mainPlayPauseBtn.setImageResource(R.drawable.play_song);
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    switchSongInFragment();
                    getActivity().getSharedPreferences("preferences_name", Context.MODE_PRIVATE).edit().putInt("progress", position).commit();
                    mediaPlayer = MediaPlayer.create(getActivity(), uri);
                    mediaPlayer.start();
                }
            });
        }
    }


}








