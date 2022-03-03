package com.project.tape;

import static com.project.tape.AlbumInfo.fromAlbumInfo;
import static com.project.tape.AlbumInfo.positionInOpenedAlbum;
import static com.project.tape.MainActivity.artistNameStr;
import static com.project.tape.MainActivity.songNameStr;
import static com.project.tape.SongInfoTab.repeatBtnClicked;
import static com.project.tape.SongInfoTab.shuffleBtnClicked;
import static com.project.tape.SongsFragment.albumList;
import static com.project.tape.SongsFragment.copyOfSongsInAlbum;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

public abstract class FragmentGeneral extends Fragment {

    ImageView album_cover_main;
    TextView song_title_main, artist_name_main;
    ImageButton mainPlayPauseBtn;

    public static int position = 0;
    static Uri uri;
    static MediaPlayer mediaPlayer = new MediaPlayer();

    public static ArrayList<Song> songsList;

    static byte[] art;

    static boolean coverLoaded;


    //Searches for mp3 files on phone and puts information about them in columns
    protected void loadAudio() throws NullPointerException {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            songsList = new ArrayList<>();
            albumList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                // Save to audioList
                songsList.add(new Song(data, title, album, artist, duration));
                albumList.add(new Song(data, title, album, artist, duration));
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
    public void switchSongInFragment() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = getRandom(copyOfSongsInAlbum.size() - 1);
                }
            } else if (!shuffleBtnClicked && repeatBtnClicked) {
                uri = Uri.parse(songsList.get(position).getData());
                if (fromAlbumInfo) {
                    uri = Uri.parse(copyOfSongsInAlbum.get(positionInOpenedAlbum).getData());
                }
            } else if (!shuffleBtnClicked && !repeatBtnClicked) {
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = (positionInOpenedAlbum + 1 % copyOfSongsInAlbum.size());
                } else {
                    position = position + 1 == songsList.size() ? (0) : (position + 1);
                }
            } else if (shuffleBtnClicked && repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = getRandom(copyOfSongsInAlbum.size() - 1);
                }
                repeatBtnClicked = false;
            }

            if (fromAlbumInfo) {
                songNameStr = copyOfSongsInAlbum.get(positionInOpenedAlbum).getTitle();
                artistNameStr = copyOfSongsInAlbum.get(positionInOpenedAlbum).getArtist();
                uri = Uri.parse(copyOfSongsInAlbum.get(positionInOpenedAlbum).getData());
            } else {
                uri = Uri.parse(songsList.get(position).getData());
                songNameStr = songsList.get(position).getTitle();
                artistNameStr = songsList.get(position).getArtist();
            }

            mediaPlayer = MediaPlayer.create(getContext(), uri);

            metaDataInFragment(uri);

            song_title_main.setText(songNameStr);
            artist_name_main.setText(artistNameStr);

            getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                    .putString("progress", uri.toString()).commit();
            getActivity().getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                    .putString("songNameStr", songNameStr).commit();
            getActivity().getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                    .putString("artistNameStr", artistNameStr).commit();

            mediaPlayer.start();

        } else {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = getRandom(copyOfSongsInAlbum.size() - 1);
                }
            } else if (!shuffleBtnClicked && repeatBtnClicked) {
                uri = Uri.parse(songsList.get(position).getData());
                if (fromAlbumInfo) {
                    uri = Uri.parse(copyOfSongsInAlbum.get(positionInOpenedAlbum).getData());
                }
            } else if (!shuffleBtnClicked && !repeatBtnClicked) {
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = (positionInOpenedAlbum + 1 % copyOfSongsInAlbum.size());
                } else {
                    position = position + 1 == songsList.size() ? (0) : (position + 1);
                }
            } else if (shuffleBtnClicked && repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = getRandom(copyOfSongsInAlbum.size() - 1);
                }
                repeatBtnClicked = false;
            }

            if (fromAlbumInfo) {
                songNameStr = copyOfSongsInAlbum.get(positionInOpenedAlbum).getTitle();
                artistNameStr = copyOfSongsInAlbum.get(positionInOpenedAlbum).getArtist();
                uri = Uri.parse(copyOfSongsInAlbum.get(positionInOpenedAlbum).getData());
            } else {
                uri = Uri.parse(songsList.get(position).getData());
                songNameStr = songsList.get(position).getTitle();
                artistNameStr = songsList.get(position).getArtist();
            }

            mediaPlayer = MediaPlayer.create(getContext(), uri);

            metaDataInFragment(uri);

            song_title_main.setText(songNameStr);
            artist_name_main.setText(artistNameStr);

            getActivity().getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                    .putString("progress", uri.toString()).commit();
            getActivity().getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                    .putString("songNameStr", songNameStr).commit();
            getActivity().getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                    .putString("artistNameStr", artistNameStr).commit();

            coverLoaded = true;
        }
    }


}





