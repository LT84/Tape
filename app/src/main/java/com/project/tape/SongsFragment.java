package com.project.tape;

import static com.project.tape.AlbumInfo.fromAlbumInfo;
import static com.project.tape.AlbumInfo.positionInOpenedAlbum;
import static com.project.tape.MainActivity.artistNameStr;
import static com.project.tape.MainActivity.songNameStr;
import static com.project.tape.SongInfoTab.repeatBtnClicked;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SongsFragment extends FragmentGeneral implements SongAdapter.OnSongListener, MediaPlayer.OnCompletionListener {

    private RecyclerView myRecyclerView;
    private static final int VERTICAL_ITEM_SPACE = 3;

    static List<Song> albumList;
    static ArrayList<Song> copyOfSongsInAlbum = new ArrayList<>();

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


        repeatBtnClicked = getActivity().getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE)
                .getBoolean("repeatBtnClicked", true);

        position = getActivity().getSharedPreferences("position", Context.MODE_PRIVATE)
                .getInt("position", position);

        positionInOpenedAlbum = getActivity().getSharedPreferences("positionInOpenedAlbum", Context.MODE_PRIVATE)
                .getInt("positionInOpenedAlbum", positionInOpenedAlbum);

        songNameStr = getActivity().getSharedPreferences("songNameStr", Context.MODE_PRIVATE)
                .getString("songNameStr", " ");

        artistNameStr = getActivity().getSharedPreferences("artistNameStr", Context.MODE_PRIVATE)
                .getString("artistNameStr", " ");

        fromAlbumInfo = getActivity().getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE)
                .getBoolean("fromAlbumInfo", false);



        if (fromAlbumInfo) {
            uri = Uri.parse(getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE)
                    .getString("uri",  songsList.get(position).getData()));
        } else {
            uri = Uri.parse(songsList.get(position).getData());
        }


        metaDataInFragment(uri);

        song_title_main.setText(songNameStr);
        artist_name_main.setText(artistNameStr);


        mediaPlayer = MediaPlayer.create(getContext(), uri);
        mediaPlayer.setOnCompletionListener(SongsFragment.this);

        //Sets adapter to list and applies settings to recyclerView
        SongAdapter songAdapter = new SongAdapter(getContext(), songsList , this);
        myRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        myRecyclerView.setAdapter(songAdapter);

        //Throwing out duplicates from list
        //Sorting albums
        Collections.sort(albumList, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getAlbum().toLowerCase().compareTo(rhs.getAlbum().toLowerCase());
            }
        });


        //Creates iterator and throws out duplicates
        Iterator<Song> iterator = albumList.iterator();
        String album = "";
        while (iterator.hasNext()) {
            Song track = iterator.next();
            String currentAlbum = track.getAlbum().toLowerCase();
            if (currentAlbum.equals(album)) {
                iterator.remove();

            } else {
                album = currentAlbum;
            }
        }

        String albumName = getActivity().getSharedPreferences("albumName", Context.MODE_PRIVATE)
                .getString("albumName", " ");


        int j = 0;
        for (int i = 0; i < songsList.size(); i++) {
            if (albumName.equals(songsList.get(i).getAlbum())) {
                copyOfSongsInAlbum.add(j, songsList.get(i));
                j++;
            }

        }

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

        AlbumInfo.fromAlbumInfo = false;

        songNameStr = songsList.get(position).getTitle();
        artistNameStr = songsList.get(position).getArtist();

        playMusic();

        metaDataInFragment(uri);

        getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        getActivity().getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        getActivity().getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        getActivity().getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();

        mediaPlayer.setOnCompletionListener(SongsFragment.this);
        song_title_main.setText(songsList.get(position).getTitle());
        artist_name_main.setText(songsList.get(position).getArtist());
        mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
    }


    /*Switches next composition, sets album cover in main, sets
       title and artist when SongsFragment is opened*/
    @Override
    public void onResume() {
        if (!coverLoaded) {
            if (uri != null) {
                metaDataInFragment(uri);
                coverLoaded = true;
            }
        }

        if (mediaPlayer != null) {

            song_title_main.setText(songNameStr);
            artist_name_main.setText(artistNameStr);

            if (mediaPlayer.isPlaying()) {
                mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
            } else  {
                mainPlayPauseBtn.setImageResource(R.drawable.play_song);
            }
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        if (repeatBtnClicked) {
            getActivity().getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                    .putBoolean("repeatBtnClicked", true).commit();
        } else {
            getActivity().getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                    .putBoolean("repeatBtnClicked", false).commit();
        }

        getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        getActivity().getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        getActivity().getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        getActivity().getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();

        super.onStop();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        position = getActivity().getSharedPreferences("position", Context.MODE_PRIVATE)
                .getInt("position", position);
        if (repeatBtnClicked) {
            switchSongInFragment();
            metaDataInFragment(uri);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
        } else {
            switchSongInFragment();
            metaDataInFragment(uri);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
        }
        getActivity().getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
    }
}





