package com.project.tape.Fragments;

import static com.project.tape.Activities.AboutFragmentItem.fromAlbumInfo;
import static com.project.tape.Activities.AboutFragmentItem.fromArtistInfo;
import static com.project.tape.Activities.AboutFragmentItem.positionInInfoAboutItem;
import static com.project.tape.Activities.AboutPlaylist.aboutPlaylistOpened;
import static com.project.tape.Activities.AboutPlaylist.fromPlaylist;
import static com.project.tape.Activities.MainActivity.artistNameStr;
import static com.project.tape.Activities.MainActivity.songNameStr;
import static com.project.tape.Activities.MainActivity.songSearchWasOpened;
import static com.project.tape.Activities.MainActivity.songsFromSearch;
import static com.project.tape.Activities.SongInfoTab.repeatBtnClicked;
import static com.project.tape.Activities.SongInfoTab.songInfoTabOpened;
import static com.project.tape.Activities.SortChoice.sortChoiceChanged;
import static com.project.tape.Adapters.SongsAdapter.mSongsList;
import static com.project.tape.Adapters.SongsAdapter.myRecyclerView;
import static com.project.tape.Fragments.AlbumsFragment.albumsFragmentOpened;
import static com.project.tape.Fragments.ArtistsFragment.artistsFragmentOpened;
import static com.project.tape.Activities.AboutFragmentItem.aboutFragmentItemOpened;
import static com.project.tape.Fragments.PlaylistsFragment.playlistsFragmentOpened;

import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.Adapters.SongsAdapter;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.CreateNotification;
import com.project.tape.SecondaryClasses.HeadsetActionButtonReceiver;
import com.project.tape.SecondaryClasses.Song;
import com.project.tape.SecondaryClasses.VerticalSpaceItemDecoration;

import java.util.ArrayList;


public class SongsFragment extends FragmentGeneral implements SongsAdapter.OnSongListener, MediaPlayer.OnCompletionListener {


    private static final int VERTICAL_ITEM_SPACE = 3;

    public static ArrayList<Song> artistList = new ArrayList<>();
    public static ArrayList<Song> staticCurrentSongsInAlbum = new ArrayList<>();
    public static ArrayList<Song> staticPreviousSongsInAlbum = new ArrayList<>();
    public static ArrayList<Song> staticCurrentArtistSongs = new ArrayList<>();
    public static ArrayList<Song> staticPreviousArtistSongs = new ArrayList<>();

    public static String albumName;
    public static String previousAlbumName;
    public static String artistName;
    public static String previousArtistName;

    public static SongsAdapter songsAdapter;

    public static Bitmap notificationBackground;

    private boolean fromBackground = false;

    public static boolean songsFragmentOpened;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) throws NullPointerException {
        View v;
        v = inflater.inflate(R.layout.songs_fragment, container, false);
        //Loading audio list and albumList
        loadAudio();
        //Booleans
        songsFragmentOpened = true;
        albumsFragmentOpened = false;
        artistsFragmentOpened = false;
        aboutFragmentItemOpened = false;

        artistList.addAll(songsList);
        //Init views
        myRecyclerView = (RecyclerView) v.findViewById(R.id.compositions_recyclerview);
        song_title_main = (TextView) getActivity().findViewById(R.id.song_title_main);
        artist_name_main = (TextView) getActivity().findViewById(R.id.artist_name_main);
        album_cover_main = (ImageView) getActivity().findViewById(R.id.album_cover_main);
        mainPlayPauseBtn = (ImageButton) getActivity().findViewById(R.id.pause_button);

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build();

        //sharedPreferences
        repeatBtnClicked = getActivity().getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE)
                .getBoolean("repeatBtnClicked", true);
        position = getActivity().getSharedPreferences("position", Context.MODE_PRIVATE)
                .getInt("position", position);
        positionInInfoAboutItem = getActivity().getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE)
                .getInt("positionInInfoAboutItem", positionInInfoAboutItem);
        songNameStr = getActivity().getSharedPreferences("songNameStr", Context.MODE_PRIVATE)
                .getString("songNameStr", " ");
        fromAlbumInfo = getActivity().getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE)
                .getBoolean("fromAlbumInfo", false);
        fromArtistInfo = getActivity().getSharedPreferences("fromArtistInfo", Context.MODE_PRIVATE)
                .getBoolean("fromArtistInfo", false);
        uri = Uri.parse(getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE)
                .getString("uri", songsList.get(0).getData()));
        //uri = Uri.parse(songsList.get(0).getData());

        //Fills up staticCurrentSongsInAlbum
        int a = 0;
        albumName = getActivity().getSharedPreferences("albumName", Context.MODE_PRIVATE)
                .getString("albumName", " ");
        for (int i = 0; i < songsList.size(); i++) {
            if (albumName.equals(songsList.get(i).getAlbum())) {
                staticCurrentSongsInAlbum.add(a, songsList.get(i));
                a++;
            }
        }
        //Fills up staticPreviousSongsInAlbum
        previousAlbumName = getActivity().getSharedPreferences("previousAlbumName", Context.MODE_PRIVATE)
                .getString("previousAlbumName", " ");
        int b = 0;
        for (int i = 0; i < songsList.size(); i++) {
            if (previousAlbumName.equals(songsList.get(i).getAlbum())) {
                staticPreviousSongsInAlbum.add(b, songsList.get(i));
                b++;
            }
        }

        //Fills up staticArtistSongs
        artistNameStr = getActivity().getSharedPreferences("artistNameStr", Context.MODE_PRIVATE)
                .getString("artistNameStr", " ");
        int c = 0;
        for (int i = 0; i < songsList.size(); i++) {
            if (artistNameStr.equals(songsList.get(i).getArtist())) {
                staticCurrentArtistSongs.add(c, songsList.get(i));
                c++;
            }
        }

        //Fills up staticPreviousArtistSongs
        int d = 0;
        previousArtistName = getActivity().getSharedPreferences("previousArtistName", Context.MODE_PRIVATE)
                .getString("previousArtistName", " ");
        for (int i = 0; i < songsList.size(); i++) {
            if (previousArtistName.equals(songsList.get(i).getArtist())) {
                staticPreviousArtistSongs.add(d, songsList.get(i));
                d++;
            }
        }

        metaDataInFragment(uri);

        song_title_main.setText(songNameStr);
        artist_name_main.setText(artistNameStr);

        mediaPlayer = MediaPlayer.create(getContext(), uri);
        mediaPlayer.setOnCompletionListener(SongsFragment.this);

        //Sets adapter to list and applies settings to recyclerView
        songsAdapter = new SongsAdapter(getContext(), songsList, this);
        myRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(songsAdapter);

        SongsFragment.songsAdapter.updateColorAfterSongSwitch(position);

        //Sorting artists array
        sortArtistsList();

        return v;
    }

    //Plays song after it clicked in RecyclerView
    @Override
    public void onSongClick(int position) {
        this.position = position;
        fromAlbumInfo = false;
        fromArtistInfo = false;
        fromPlaylist = false;

        if (songSearchWasOpened) {
            songsList = songsFromSearch;
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else {
            songsList = mSongsList;
            uri = Uri.parse(songsList.get(position).getData());
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
        }

        //Gets audioFocus, then creates mediaPlayer
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        if (audioFocusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getContext(), uri);
            onTrackPlay();
        }

        metaDataInFragment(uri);

        if (songSearchWasOpened) {
            CreateNotification.createNotification(getActivity(), songsFromSearch.get(position),
                    R.drawable.pause_song, position, songsFromSearch.size() - 1);
        } else {
            CreateNotification.createNotification(getActivity(), songsList.get(position),
                    R.drawable.pause_song, position, songsList.size() - 1);
        }

        getActivity().getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        getActivity().getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        getActivity().getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
        getActivity().getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", fromAlbumInfo).commit();
        getActivity().getSharedPreferences("durationTotal", Context.MODE_PRIVATE).edit()
                .putInt("durationTotal", Integer.parseInt(songsList.get(position).getDuration()) / 1000).apply();

        mediaPlayer.setOnCompletionListener(this);
        song_title_main.setText(songsList.get(position).getTitle());
        artist_name_main.setText(songsList.get(position).getArtist());
        mainPlayPauseBtn.setImageResource(R.drawable.pause_song);

        for (Song song : songsList) {
            if (song.getTitle().contains(songNameStr)) {
                uri = Uri.parse(song.getData());
                getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                        .putString("uri", uri.toString()).commit();
            }
        }

        getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        loadAudio();
    }


    /*Switches next composition, sets album cover in main, sets
           title and artist when SongsFragment is opened*/
    @Override
    public void onResume() {
        super.onResume();
        songsFragmentOpened = true;
        albumsFragmentOpened = false;
        artistsFragmentOpened = false;
        aboutFragmentItemOpened = false;
        playlistsFragmentOpened = false;
        aboutPlaylistOpened = false;

        if (fromBackground) {
            getActivity().unregisterReceiver(broadcastReceiver);
            Log.i("broadcast", "unreg_SONGSFRAGMENT");
            fromBackground = false;
        }

        createChannel();
        Log.i("broadcast", "reg_SONGSFRAGMENT");
        trackAudioSource();

        //Sets adapter after user sets sort preference
        if (sortChoiceChanged) {
            loadAudio();
            songsAdapter = new SongsAdapter(getContext(), songsList, this);
            myRecyclerView.setAdapter(songsAdapter);
            sortChoiceChanged = false;
        }

        //Register headphones buttons
        HeadsetActionButtonReceiver.delegate = this;
        HeadsetActionButtonReceiver.register(getActivity());

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
            } else {
                mainPlayPauseBtn.setImageResource(R.drawable.play_song);
            }
        }
        mediaPlayer.setOnCompletionListener(SongsFragment.this);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (songsFragmentOpened && !songInfoTabOpened) {
            createChannel();
            fromBackground = true;
            Log.i("broadcast", "reg_SONGSFRAGMENT");
        }

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
        getActivity().getSharedPreferences("fromArtistInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromArtistInfo", false).commit();
        getActivity().getSharedPreferences("previousArtistName", Context.MODE_PRIVATE).edit()
                .putString("previousArtistName", previousArtistName).commit();
        getContext().getSharedPreferences("fromArtistInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromArtistInfo", fromArtistInfo).commit();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onTrackNext();
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancel(1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Checking is screen locked
        KeyguardManager myKM = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            //if locked
        } else {
            getActivity().unregisterReceiver(broadcastReceiver);
            Log.i("broadcast", "unreg_SONGSFRAGMENT");
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    //Called when headphones button pressed
    @Override
    public void onMediaButtonSingleClick() {
        if (isPlaying) {
            onTrackPause();
        } else {
            onTrackPlay();
        }
    }

    @Override
    public void onMediaButtonDoubleClick() {
    }


}


