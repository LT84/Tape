package com.project.tape.Fragments;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.project.tape.Activities.AboutFragmentItem.fromAlbumInfo;
import static com.project.tape.Activities.AboutFragmentItem.fromArtistInfo;
import static com.project.tape.Activities.AboutFragmentItem.positionInInfoAboutItem;
import static com.project.tape.Activities.AboutPlaylist.fromPlaylist;
import static com.project.tape.Activities.AboutPlaylist.positionInAboutPlaylist;
import static com.project.tape.Activities.AboutPlaylist.previousSongsInPlaylist;
import static com.project.tape.Activities.MainActivity.SORT_PREF;
import static com.project.tape.Activities.MainActivity.artistNameStr;
import static com.project.tape.Activities.MainActivity.fromSearch;
import static com.project.tape.Activities.MainActivity.songNameStr;
import static com.project.tape.Activities.MainActivity.songsFromSearch;
import static com.project.tape.Activities.SongInfoTab.repeatBtnClicked;
import static com.project.tape.Activities.SongInfoTab.shuffleBtnClicked;
import static com.project.tape.Fragments.SongsFragment.artistList;
import static com.project.tape.Fragments.SongsFragment.staticCurrentSongsInAlbum;
import static com.project.tape.Fragments.SongsFragment.staticPreviousArtistSongs;
import static com.project.tape.Fragments.SongsFragment.staticPreviousSongsInAlbum;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.project.tape.Interfaces.Playable;
import com.project.tape.ItemClasses.Song;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.CreateNotification;
import com.project.tape.SecondaryClasses.HeadsetActionButtonReceiver;
import com.project.tape.Services.OnClearFromRecentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;


public abstract class FragmentGeneral extends Fragment implements Playable, HeadsetActionButtonReceiver.Delegate {

    ImageView album_cover_main;
    TextView song_title_main, artist_name_main;
    ImageButton mainPlayPauseBtn;

    NotificationManager notificationManager;

    public static Uri uri;
    public static MediaPlayer mediaPlayer = new MediaPlayer();

    public static int position = 0;

    public static ArrayList<Song> songsList = new ArrayList<>();

    public static byte[] art;

    public static boolean isPlaying = false;

    public static boolean coverLoaded;

    public static int audioFocusRequest = 0;
    public static AudioFocusRequest focusRequest;

    public static AudioManager audioManager;
    public static AudioAttributes playbackAttributes;


    //Searches for mp3 files on phone and puts information about them in columns
    protected void loadAudio() throws NullPointerException {
        SharedPreferences preferences = getActivity().getSharedPreferences(SORT_PREF, Context.MODE_PRIVATE);
        String sortArrayOrder = preferences.getString("sort", "sortByDate");

        String sortOrder = null;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        switch (sortArrayOrder) {
            case "sortByName":
                sortOrder = MediaStore.MediaColumns.DISPLAY_NAME;
                break;
            case "sortByDate":
                sortOrder = MediaStore.MediaColumns.DATE_ADDED;
                break;
        }

        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            songsList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                // Save to audioList
                songsList.add(new Song(data, title, album, artist, duration, false));
            }
        }
        cursor.close();
        if (sortArrayOrder.equals("sortByDate")) {
            Collections.reverse(songsList);
        }
    }

    //Gets random number
    public int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    //Sets album cover in main
    public void metaDataInFragment(Uri uri) {
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

    //Switches next composition
    public void switchNextSongInFragment() {
        mediaPlayer.stop();
        mediaPlayer.release();

        if (shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
            } else if (fromSearch) {
                position = getRandom(songsFromSearch.size() - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousArtistSongs.size() - 1);
            } else if (fromPlaylist) {
                positionInAboutPlaylist = getRandom(previousSongsInPlaylist.size() - 1);
            } else {
                position = getRandom(songsList.size() - 1);
            }
        } else if (!shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousSongsInAlbum.size()
                        ? (0) : (positionInInfoAboutItem + 1);
            } else if (fromSearch) {
                position = position + 1 == songsFromSearch.size() ? (0)
                        : (position + 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousArtistSongs.size()
                        ? (0) : (positionInInfoAboutItem + 1);
            } else if (fromPlaylist) {
                positionInAboutPlaylist = positionInAboutPlaylist + 1 == previousSongsInPlaylist.size()
                        ? (0) : (positionInAboutPlaylist + 1);
            } else {
                position = position + 1 == songsList.size() ? (0)
                        : (position + 1);
            }
        } else if (shuffleBtnClicked && repeatBtnClicked) {
            position = getRandom(songsList.size() - 1);
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(staticCurrentSongsInAlbum.size() - 1);
            }
            repeatBtnClicked = false;
        }

        coverLoaded = true;

        if (fromAlbumInfo) {
            uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getArtist();
        } else if (fromSearch) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            uri = Uri.parse(staticPreviousArtistSongs.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
        } else if (fromPlaylist) {
            uri = Uri.parse(previousSongsInPlaylist.get(positionInAboutPlaylist).getData());
            songNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getTitle();
            artistNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getArtist();
        } else {
            uri = Uri.parse(songsList.get(position).getData());
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
        }

        mediaPlayer = MediaPlayer.create(getContext(), uri);
        metaDataInFragment(uri);

        song_title_main.setText(songNameStr);
        artist_name_main.setText(artistNameStr);
        mediaPlayer.start();

        getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        getActivity().getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        getActivity().getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        getActivity().getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
        getActivity().getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
    }

    //Switches previous composition
    public void switchPreviousSongInFragment() {
        mediaPlayer.stop();
        mediaPlayer.release();

        //Checking is shuffle or repeat button clicked
        if (shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
            } else if (fromSearch) {
                position = getRandom(songsFromSearch.size() - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousArtistSongs.size() - 1);
            } else if (fromPlaylist) {
                positionInAboutPlaylist = getRandom(staticPreviousArtistSongs.size() - 1);
            } else {
                position = getRandom(songsList.size() - 1);
            }
        } else if (!shuffleBtnClicked && repeatBtnClicked) {
            if (fromAlbumInfo) {
                uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            } else if (fromSearch) {
                uri = Uri.parse(songsFromSearch.get(position).getData());
            } else if (fromPlaylist) {
                uri = Uri.parse(previousSongsInPlaylist.get(positionInAboutPlaylist).getData());
            } else {
                uri = Uri.parse(songsList.get(position).getData());
            }
        } else if (!shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem - 1 < 0 ? (staticPreviousSongsInAlbum.size() - 1)
                        : (positionInInfoAboutItem - 1);
            } else if (fromSearch) {
                position = position - 1 < 0 ? (songsFromSearch.size() - 1)
                        : (position - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem - 1 < 0 ? (staticPreviousArtistSongs.size() - 1)
                        : (positionInInfoAboutItem - 1);
            } else if (fromPlaylist) {
                positionInAboutPlaylist = positionInAboutPlaylist - 1 < 0 ? (previousSongsInPlaylist.size() - 1)
                        : (positionInAboutPlaylist - 1);
            } else {
                position = position - 1 < 0 ? (songsList.size() - 1)
                        : (position - 1);
            }
        } else if (shuffleBtnClicked && repeatBtnClicked) {
            position = getRandom(songsList.size() - 1);
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
            }
            repeatBtnClicked = false;
        }

        coverLoaded = true;

        if (fromAlbumInfo) {
            uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getArtist();
        } else if (fromSearch) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            uri = Uri.parse(staticPreviousArtistSongs.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
        } else if (fromPlaylist) {
            uri = Uri.parse(previousSongsInPlaylist.get(positionInAboutPlaylist).getData());
            songNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getTitle();
            artistNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getArtist();
        } else {
            uri = Uri.parse(songsList.get(position).getData());
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
        }

        mediaPlayer = MediaPlayer.create(getContext(), uri);
        metaDataInFragment(uri);

        song_title_main.setText(songNameStr);
        artist_name_main.setText(artistNameStr);
        mediaPlayer.start();

        getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        getActivity().getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        getActivity().getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        getActivity().getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
        getActivity().getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
    }

    protected void sortArtistsList() {
        //Throwing out duplicates from list
        //Sorting albums
        Collections.sort(artistList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getArtist().toLowerCase().compareTo(b.getArtist().toLowerCase());
            }
        });

        //Creates iterator and throws out duplicates
        Iterator<Song> iterator = artistList.iterator();
        String artist = "";
        while (iterator.hasNext()) {
            Song track = iterator.next();
            String currentArtist = track.getArtist().toLowerCase();
            if (currentArtist.equals(artist)) {
                iterator.remove();
            } else {
                artist = currentArtist;
            }
        }
    }


    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                onTrackPlay();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                onTrackPause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                onTrackPause();
            }
        }
    };

    BroadcastReceiver audioSourceChangedReceiver;

    public void trackAudioSource() {
        audioSourceChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                    onTrackPause();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        getActivity().registerReceiver(audioSourceChangedReceiver, intentFilter);
    }

    //Notification methods
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionName");
            switch (action) {
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (isPlaying) {
                        onTrackPause();
                    } else {
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
            }
        }
    };

    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Tape", NotificationManager.IMPORTANCE_HIGH);

            notificationManager = getSystemService(getContext(), NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("SONGS_SONGS"));
        getActivity().startService(new Intent(getContext(), OnClearFromRecentService.class));
    }

    @Override
    public void onTrackPrevious() {
        isPlaying = true;
        switchPreviousSongInFragment();
        if (fromSearch) {
            CreateNotification.createNotification(getContext(), songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(getContext(), staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(getContext(), staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(getContext(), previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_pause_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(getContext(), songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
            SongsFragment.songsAdapter.updateColorAfterSongSwitch(position);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        mainPlayPauseBtn.setImageResource(R.drawable.ic_pause_song);
    }

    @Override
    public void onTrackNext() {
        isPlaying = true;
        switchNextSongInFragment();
        if (fromSearch) {
            CreateNotification.createNotification(getContext(), songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
            SongsFragment.songsAdapter.updateColorAfterSongSwitch(position);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(getContext(), staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(getContext(), staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(getContext(), previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_pause_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(getContext(), songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
            SongsFragment.songsAdapter.updateColorAfterSongSwitch(position);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        mainPlayPauseBtn.setImageResource(R.drawable.ic_pause_song);
    }

    @Override
    public void onTrackPlay() {
        isPlaying = true;
        mediaPlayer.start();
        if (fromSearch) {
            CreateNotification.createNotification(getContext(), songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(getContext(), staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(getContext(), staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(getContext(), previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_pause_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(getContext(), songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        mainPlayPauseBtn.setImageResource(R.drawable.ic_pause_song);
    }

    @Override
    public void onTrackPause() {
        isPlaying = false;
        mediaPlayer.pause();
        if (fromSearch) {
            CreateNotification.createNotification(getContext(), songsFromSearch.get(position),
                    R.drawable.ic_play_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(getContext(), staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_play_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(getContext(), staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_play_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(getContext(), previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_play_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(getContext(), songsList.get(position),
                    R.drawable.ic_play_song, position, songsList.size() - 1);
        }
        mainPlayPauseBtn.setImageResource(R.drawable.ic_play_song);
    }


}





