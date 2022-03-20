package com.project.tape;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.project.tape.AboutFragmentItem.fromAlbumInfo;
import static com.project.tape.AboutFragmentItem.fromArtistInfo;
import static com.project.tape.AboutFragmentItem.positionInInfoAboutItem;
import static com.project.tape.MainActivity.artistNameStr;
import static com.project.tape.MainActivity.songNameStr;
import static com.project.tape.MainActivity.songSearchWasOpened;
import static com.project.tape.MainActivity.songsFromSearch;
import static com.project.tape.SongInfoTab.repeatBtnClicked;
import static com.project.tape.SongInfoTab.shuffleBtnClicked;
import static com.project.tape.SongsFragment.albumList;
import static com.project.tape.SongsFragment.artistList;
import static com.project.tape.SongsFragment.staticCurrentSongsInAlbum;
import static com.project.tape.SongsFragment.staticPreviousArtistSongs;
import static com.project.tape.SongsFragment.staticPreviousSongsInAlbum;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

public abstract class FragmentGeneral extends Fragment implements Playable {

    ImageView album_cover_main;
    TextView song_title_main, artist_name_main;
    ImageButton mainPlayPauseBtn;

    public static int position = 0;
    static Uri uri;
    static MediaPlayer mediaPlayer = new MediaPlayer();

    public static ArrayList<Song> songsList = new ArrayList<>();

    static byte[] art;

    static boolean coverLoaded;

    boolean isPlaying = false;

    NotificationManager notificationManager;

    //Searches for mp3 files on phone and puts information about them in columns
    protected void loadAudio() throws NullPointerException {
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
            } else if (songSearchWasOpened) {
                position = getRandom(songsFromSearch.size() - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousArtistSongs.size() - 1);
            } else {
                position = getRandom(songsList.size() - 1);
            }
        } else if (!shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousSongsInAlbum.size()
                        ? (0) : (positionInInfoAboutItem + 1);
            } else if (songSearchWasOpened) {
                position = position + 1 == songsFromSearch.size() ? (0)
                        : (position + 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousArtistSongs.size()
                        ? (0) : (positionInInfoAboutItem + 1);
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
        } else if (songSearchWasOpened) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            uri = Uri.parse(staticPreviousArtistSongs.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
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
            } else if (songSearchWasOpened) {
                position = getRandom(songsFromSearch.size() - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousArtistSongs.size() - 1);
            } else {
                position = getRandom(songsList.size() - 1);
            }
        } else if (!shuffleBtnClicked && repeatBtnClicked) {
            if (fromAlbumInfo) {
                uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            } else if (songSearchWasOpened) {
                uri = Uri.parse(songsFromSearch.get(position).getData());
            } else {
                uri = Uri.parse(songsList.get(position).getData());
            }
        } else if (!shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem - 1 < 0 ? (staticPreviousSongsInAlbum.size() - 1)
                        : (positionInInfoAboutItem - 1);
            } else if (songSearchWasOpened) {
                position = position - 1 < 0 ? (songsFromSearch.size())
                        : (position - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem - 1 < 0 ? (staticPreviousArtistSongs.size() - 1)
                        : (positionInInfoAboutItem - 1);
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
        } else if (songSearchWasOpened) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            uri = Uri.parse(staticPreviousArtistSongs.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
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


    protected void sortAlbumsList() {
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


    //Notification methods
    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Tape", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(getContext(), NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

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

    @Override
    public void onTrackPrevious() {
        switchPreviousSongInFragment();
        CreateNotification.createNotification(getActivity(), songsList.get(position), R.drawable.pause_song,
                1, songsList.size() - 1);
    }

    @Override
    public void onTrackNext() {
        switchNextSongInFragment();
        CreateNotification.createNotification(getActivity(), songsList.get(position), R.drawable.pause_song,
                1, songsList.size() - 1);
    }

    @Override
    public void onTrackPlay() {
        CreateNotification.createNotification(getActivity(), songsList.get(position),
                R.drawable.pause_song, position, songsList.size() - 1);
        if (mediaPlayer.isPlaying()) {
            mainPlayPauseBtn.setImageResource(R.drawable.play_song);
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
            mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
        }
        isPlaying = true;
    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(getActivity(), songsList.get(position),
                R.drawable.pause_song, position, songsList.size() - 1);
        if (mediaPlayer.isPlaying()) {
            mainPlayPauseBtn.setImageResource(R.drawable.play_song);
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
            mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
        }
        isPlaying = false;
    }


}





