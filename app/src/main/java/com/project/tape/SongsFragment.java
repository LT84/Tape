package com.project.tape;

import static com.project.tape.SongInfoTab.repeatBtnClicked;
import static com.project.tape.SongInfoTab.shuffleBtnClicked;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

public class SongsFragment extends Fragment implements SongAdapter.OnSongListener {

    static int position = 0;
    public static ArrayList<Song> songsList;
    static MediaPlayer mediaPlayer;
    static Uri uri;
    private RecyclerView myRecyclerView;
    ImageView album_cover_main;
    ImageButton mainPlayPauseBtn;
    TextView song_title_main, artist_name_main;
    private static final int VERTICAL_ITEM_SPACE = 3;


    //Creates RecyclerView filed with songs in SongsFragment
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) throws NullPointerException{
        View v;
        v = inflater.inflate(R.layout.songs_fragment, container, false);
        loadAudio();

        mainPlayPauseBtn = (ImageButton)getActivity().findViewById(R.id.pause_button);
        song_title_main = (TextView)getActivity().findViewById(R.id.song_title_main);
        artist_name_main = (TextView)getActivity().findViewById(R.id.artist_name_main);
        album_cover_main = (ImageView)getActivity().findViewById(R.id.album_cover_main);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.compositions_recyclerview);


        //add ItemDecoration
        myRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

        SongAdapter songAdapter = new SongAdapter(getContext(), songsList , this);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(songAdapter);
        return v;
    }

    //Searches for mp3 files on phone and puts information about them in columns
    private void loadAudio() throws NullPointerException {
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


    //Plays song after it clicked in RecyclerView
    @Override
    public void onSongClick(int position) {
        SongsFragment.position = position;
        playMusic();
        metaDataInSongsFragment(uri);
        song_title_main.setText(songsList.get(position).getTitle());
        artist_name_main.setText(songsList.get(position).getArtist());
        mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
    }

    //Switches song after previous ends
    public void switchSongInSongsFragment() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                position = getRandom(songsList.size() -1);
            }
            else if (!shuffleBtnClicked && repeatBtnClicked) {
                uri = Uri.parse(songsList.get(position).getData());
            }

            else if (!shuffleBtnClicked && !repeatBtnClicked) {
                position = (position + 1 % songsList.size());
                uri = Uri.parse(songsList.get(position).getData());
            }
            else if (shuffleBtnClicked && repeatBtnClicked) {
                position = getRandom(songsList.size() -1);
                repeatBtnClicked = false;
            }


            uri = Uri.parse(songsList.get(position).getData());
            mediaPlayer = MediaPlayer.create(getContext(), uri);
            song_title_main.setText(songsList.get(position).getTitle());
            artist_name_main.setText(songsList.get(position).getArtist());
            metaDataInSongsFragment(uri);
            mediaPlayer.start();
        } else  {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                position = getRandom(songsList.size() -1);
            }
            else if (!shuffleBtnClicked && repeatBtnClicked) {
                uri = Uri.parse(songsList.get(position).getData());
            }

            else if (!shuffleBtnClicked && !repeatBtnClicked) {
                position = (position + 1 % songsList.size());
                uri = Uri.parse(songsList.get(position).getData());
            }
            else if (shuffleBtnClicked && repeatBtnClicked) {
                position = getRandom(songsList.size() -1);
                repeatBtnClicked = false;
            }


            uri = Uri.parse(songsList.get(position).getData());
            mediaPlayer = MediaPlayer.create(getActivity(), uri);
            song_title_main.setText(songsList.get(position).getTitle());
            artist_name_main.setText(songsList.get(position).getArtist());
            metaDataInSongsFragment(uri);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
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
                    .load(R.drawable.default_cover)
                    .into(album_cover_main);
        }
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
                    switchSongInSongsFragment();
                    mediaPlayer = MediaPlayer.create(getActivity(), uri);
                    mediaPlayer.start();
                }
            });
        }


    }



}








