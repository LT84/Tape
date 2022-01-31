package com.project.tape;

import static com.project.tape.MainActivity.album_cover_main;
import static com.project.tape.MainActivity.artist_name_main;
import static com.project.tape.MainActivity.song_title_main;
import static com.project.tape.SongInfoTab.songSwitched;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SongsFragment extends Fragment implements RecyclerViewAdapter.OnSongListener {

    private RecyclerView myRecyclerView;
    static ArrayList<Song> songsList;
    static int position = 0;
    static MediaPlayer mediaPlayer;
    static Uri uri;


    //Searches for mp3 files on phone and puts information about them in columns
    private void loadAudio() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            songsList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                // Save to audioList
                songsList.add(new Song(data, title, album, artist, duration));
            }
        }
        cursor.close();
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

    //Sets album cover in main
    private void metaDataInSongsFragment(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art = retriever.getEmbeddedPicture();

        if (art != null) {
            Glide.with(SongsFragment.this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_main);
        } else {
            Glide.with(SongsFragment.this)
                    .asBitmap()
                    .load(R.drawable.bebra)
                    .into(album_cover_main);
        }
    }

    //Plays song after it clicked in RecyclerView
    @Override
    public void onSongClick(int position) {
        SongsFragment.position = position;
        playMusic();
        songSwitched = true;
        metaDataInSongsFragment(uri);
        song_title_main.setText(songsList.get(position).getTitle());
        artist_name_main.setText(songsList.get(position).getArtist());
    }

    //Switches song after previous ends
    public void switchSongInSongsFragment() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position + 1) % songsList.size());
            uri = Uri.parse(songsList.get(position).getData());
            mediaPlayer = MediaPlayer.create(getActivity(), uri);
            song_title_main.setText(songsList.get(position).getTitle());
            artist_name_main.setText(songsList.get(position).getArtist());
            metaDataInSongsFragment(uri);
            mediaPlayer.start();
        } else  {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position + 1) % songsList.size());
            uri = Uri.parse(songsList.get(position).getData());
            mediaPlayer = MediaPlayer.create(getActivity(), uri);
            song_title_main.setText(songsList.get(position).getTitle());
            artist_name_main.setText(songsList.get(position).getArtist());
            metaDataInSongsFragment(uri);
        }
    }

    //Creates RecyclerView filed with songs in SongsFragment
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v;
        loadAudio();
        v = inflater.inflate(R.layout.compositions_fragment, container, false);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.compositions_recyclerview);
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(getContext(), songsList , this);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(recyclerViewAdapter);
        return v;
    }

    /*Switches next composition, sets album cover in main, sets
       title and artist when SongsFragment is opened*/
    @Override
    public void onResume() {
        super.onResume();
        if (uri != null) {
            metaDataInSongsFragment(uri);
        }
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    switchSongInSongsFragment();
                    songSwitched = true;
                    mediaPlayer = MediaPlayer.create(getActivity(), uri);
                    mediaPlayer.start();
                }
            });
        }
    }


}








