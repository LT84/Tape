package com.project.tape;

import static com.project.tape.SongInfoTab.repeatBtnClicked;
import static com.project.tape.SongInfoTab.shuffleBtnClicked;
import static com.project.tape.SongsFragment.art;
import static com.project.tape.SongsFragment.mediaPlayer;
import static com.project.tape.SongsFragment.position;
import static com.project.tape.SongsFragment.songsList;
import static com.project.tape.SongsFragment.uri;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AlbumsFragment extends Fragment implements AlbumAdapter.OnAlbumListener {

    TextView album_title_albumFragments, song_title_main, artist_name_main;
    ImageView album_cover_albumFragment, album_cover_main;
    ImageButton mainPlayPauseBtn;
    private Parcelable listState;
    private RecyclerView myRecyclerView;
    LinearLayoutManager LLMAlbumFragment = new LinearLayoutManager(getContext());

    static List<Song> albumList;

    int positionIndex, topView;
    private static final int VERTICAL_ITEM_SPACE = 5;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.albums_fragment, container, false);
        //Loading audio list
        loadAudio();

        //Init views
        album_title_albumFragments = (TextView) v.findViewById(R.id.album_title_albumFragment);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.albums_recyclerview);
        album_cover_albumFragment = v.findViewById(R.id.album_cover_albumFragment);

        song_title_main = (TextView)getActivity().findViewById(R.id.song_title_main);
        artist_name_main = (TextView)getActivity().findViewById(R.id.artist_name_main);
        album_cover_main = (ImageView)getActivity().findViewById(R.id.album_cover_main);
        mainPlayPauseBtn = (ImageButton)getActivity().findViewById(R.id.pause_button);

        //Setting song title and artist name in infoTab
        song_title_main.setText(songsList.get(position).getTitle());
        artist_name_main.setText(songsList.get(position).getArtist());

        //Throwing out duplicates from list
        //Sorting to albums
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

        //Sets adapter to list and applies settings to recyclerView
        AlbumAdapter albumAdapter = new AlbumAdapter(getContext(),albumList, this);

        myRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        myRecyclerView.setLayoutManager(LLMAlbumFragment);
        myRecyclerView.setAdapter(albumAdapter);
        myRecyclerView.setItemViewCacheSize(300);
        myRecyclerView.setDrawingCacheEnabled(true);
        myRecyclerView.setHasFixedSize(true);

        //Opens place where recyclerView has stopped
        if (listState != null) {
            myRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
            listState = savedInstanceState.getParcelable("ListState");
        }

        return v;
    }


    //Loading audio files
    private void loadAudio() throws NullPointerException {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            albumList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                // Save to audioList
                albumList.add(new Song(data, title, album, artist, duration));
            }
        }
        cursor.close();
    }


    @Override
    public void onPause() {
        super.onPause();
        positionIndex = LLMAlbumFragment.findFirstVisibleItemPosition();
        View startView = myRecyclerView.getChildAt(0);
        topView = (startView == null) ? 0 : (startView.getTop() - myRecyclerView.getPaddingTop());
    }

    @Override
    public void onResume() {
        super.onResume();
        song_title_main.setText(songsList.get(position).getTitle());
        artist_name_main.setText(songsList.get(position).getArtist());
        if (mediaPlayer != null) {
            if (art != null) {
                Glide.with(this)
                        .asBitmap()
                        .load(art)
                        .into(album_cover_main);
            } else {
                Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.default_cover)
                        .into(album_cover_main);
            }

            if (mediaPlayer.isPlaying()) {
                mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
            } else {
                mainPlayPauseBtn.setImageResource(R.drawable.play_song);
            }
        } else {
            song_title_main.setText(" ");
            artist_name_main.setText(" ");
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                switchSongInAlbumsFragment();
                getActivity().getSharedPreferences("preferences_name", Context.MODE_PRIVATE).edit().putInt("progress", position).commit();
                mediaPlayer = MediaPlayer.create(getActivity(), uri);
                mediaPlayer.start();
            }
        });
    }

    //Saving place where recyclerView stopped
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ListState", myRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    //ClickListener in recyclerView
    @Override
    public void onAlbumClick(int position) throws IOException {
        Intent intent = new Intent(getContext(), AlbumInfo.class);
        intent.putExtra("albumName",  albumList.get(position).getAlbum());
        getContext().startActivity(intent);
    }

    //Loads cover
    private void metaDataInAlbumsFragment(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        art = retriever.getEmbeddedPicture();

        if (art != null) {
            Glide.with(getContext())
                    .asBitmap()
                    .load(art)
                    .into(album_cover_main);
        } else {
            Glide.with(getContext())
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_main);
        }
    }

    //Gets random number
    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    //Switches song
    public void switchSongInAlbumsFragment() {
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
            metaDataInAlbumsFragment(uri);
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
            mediaPlayer = MediaPlayer.create(getContext(), uri);
            song_title_main.setText(songsList.get(position).getTitle());
            artist_name_main.setText(songsList.get(position).getArtist());
            metaDataInAlbumsFragment(uri);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    }

    }



