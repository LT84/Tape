package com.project.tape.Activities;

import static com.project.tape.Activities.AboutFragmentItem.aboutFragmentItemAdapter;
import static com.project.tape.Activities.AboutFragmentItem.aboutFragmentItemOpened;
import static com.project.tape.Activities.AboutFragmentItem.fromAlbumInfo;
import static com.project.tape.Activities.AboutFragmentItem.fromArtistInfo;
import static com.project.tape.Activities.AboutFragmentItem.positionInInfoAboutItem;
import static com.project.tape.Activities.AboutPlaylist.aboutPlaylistAdapter;
import static com.project.tape.Activities.AboutPlaylist.fromPlaylist;
import static com.project.tape.Activities.AboutPlaylist.positionInAboutPlaylist;
import static com.project.tape.Activities.AboutPlaylist.previousSongsInPlaylist;
import static com.project.tape.Activities.MainActivity.artistNameStr;
import static com.project.tape.Activities.MainActivity.fromSearch;
import static com.project.tape.Activities.MainActivity.songNameStr;
import static com.project.tape.Activities.MainActivity.songSearchWasOpened;
import static com.project.tape.Activities.MainActivity.songsFromSearch;
import static com.project.tape.Activities.SortChoice.switchBetweenSorts;
import static com.project.tape.Fragments.AlbumsFragment.albumsFragmentOpened;
import static com.project.tape.Fragments.ArtistsFragment.artistsFragmentOpened;
import static com.project.tape.Fragments.FragmentGeneral.art;
import static com.project.tape.Fragments.FragmentGeneral.audioFocusRequest;
import static com.project.tape.Fragments.FragmentGeneral.audioManager;
import static com.project.tape.Fragments.FragmentGeneral.coverLoaded;
import static com.project.tape.Fragments.FragmentGeneral.focusRequest;
import static com.project.tape.Fragments.FragmentGeneral.isPlaying;
import static com.project.tape.Fragments.PlaylistsFragment.playlistsFragmentOpened;
import static com.project.tape.Fragments.SongsFragment.mediaPlayer;
import static com.project.tape.Fragments.SongsFragment.position;
import static com.project.tape.Fragments.SongsFragment.songsAdapter;
import static com.project.tape.Fragments.SongsFragment.songsFragmentOpened;
import static com.project.tape.Fragments.SongsFragment.songsList;
import static com.project.tape.Fragments.SongsFragment.staticCurrentSongsInAlbum;
import static com.project.tape.Fragments.SongsFragment.staticPreviousArtistSongs;
import static com.project.tape.Fragments.SongsFragment.staticPreviousSongsInAlbum;
import static com.project.tape.Fragments.SongsFragment.uri;

import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.tape.Interfaces.Playable;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.CreateNotification;
import com.project.tape.SecondaryClasses.HeadsetActionButtonReceiver;
import com.project.tape.Services.OnClearFromRecentService;

import java.util.Random;

public class SongInfoTab extends AppCompatActivity implements MediaPlayer.OnCompletionListener, Playable,
        HeadsetActionButtonReceiver.Delegate {

    TextView song_title, artist_name, time_duration_passed, time_duration_total, song_title_main, artist_name_main;
    ImageView album_cover, shuffleBtn, previousBtn, nextBtn, repeatBtn;
    ImageButton backBtn;
    FloatingActionButton playPauseBtn;

    private SeekBar seekBar;
    private final Handler handler = new Handler();

    public static boolean repeatBtnClicked, shuffleBtnClicked, songInfoTabOpened, secondBroadcastUnregistered;

    NotificationManager notificationManager;

    private boolean fromBackground = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info_tab);
        getSupportActionBar().hide();
        //Booleans
        aboutFragmentItemOpened = false;
        songsFragmentOpened = false;
        albumsFragmentOpened = false;
        artistsFragmentOpened = false;
        songInfoTabOpened = true;
        playlistsFragmentOpened = false;

        positionInInfoAboutItem = this.getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE)
                .getInt("positionInInfoAboutItem", positionInInfoAboutItem);
        shuffleBtnClicked = this.getSharedPreferences("shuffleBtnClicked", Context.MODE_PRIVATE)
                .getBoolean("shuffleBtnClicked", false);

        initViews();

        getIntentMethod();

        if (shuffleBtnClicked) {
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_songs_on);
        } else {
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_songs_off);
        }
        if (repeatBtnClicked) {
            repeatBtn.setImageResource(R.drawable.ic_repeat_song_on);
        } else {
            repeatBtn.setImageResource(R.drawable.ic_repeat_song_off);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo((progress * 1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SongInfoTab.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    time_duration_passed.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
        mediaPlayer.setOnCompletionListener(this);
    }

    //Sets pause button image
    private void getIntentMethod() {
        if (songsList != null) {
            if (mediaPlayer.isPlaying()) {
                playPauseBtn.setImageResource(R.drawable.ic_pause_song);
            } else {
                playPauseBtn.setImageResource(R.drawable.ic_play_song);
            }
            metaDataInInfoTab(uri);
        }
    }

    //Sets song title and artist name
    private void sentIntent() {
        Intent data = new Intent();
        if (fromAlbumInfo) {
            songNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getArtist();
        } else if (songSearchWasOpened) {
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
        } else {
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
        }
        setResult(RESULT_OK, data);
    }

    //Sets album cover when SongInfoTab is fully opened, set duration
    private void metaDataInInfoTab(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal;
        //Set duration here !!!
        if (fromAlbumInfo) {
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            durationTotal = Integer.parseInt(staticPreviousSongsInAlbum
                    .get(positionInInfoAboutItem)
                    .getDuration()) / 1000;
            time_duration_total.setText(formattedTime(durationTotal));
        } else if (songSearchWasOpened) {
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            durationTotal = Integer.parseInt(songsFromSearch
                    .get(position).getDuration()) / 1000;
            time_duration_total.setText(formattedTime(durationTotal));
        } else if (fromArtistInfo) {
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            durationTotal = Integer.parseInt(staticPreviousArtistSongs
                    .get(positionInInfoAboutItem)
                    .getDuration()) / 1000;
            time_duration_total.setText(formattedTime(durationTotal));
        } else if (fromPlaylist) {
            Log.i("fromPlaylist", Boolean.toString(fromPlaylist));
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            durationTotal = Integer.parseInt(previousSongsInPlaylist
                    .get(positionInAboutPlaylist)
                    .getDuration()) / 1000;
            time_duration_total.setText(formattedTime(durationTotal));
        } else {
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            if (switchBetweenSorts) {
                durationTotal = getSharedPreferences("durationTotal", Context.MODE_PRIVATE)
                        .getInt("durationTotal", 0);
            } else {
                durationTotal = Integer.parseInt(songsList.get(position).getDuration()) / 1000;
            }
            time_duration_total.setText(formattedTime(durationTotal));
        }
        //Sets art in SongInfoTab
        art = retriever.getEmbeddedPicture();
        if (art != null) {
            Glide.with(SongInfoTab.this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover);
        } else {
            Glide.with(SongInfoTab.this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover);
        }
    }

    //Formats time to seconds and minutes to pass it to seekBar
    private String formattedTime(int mCurrentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }

    //Creates new Thread for play
    private void playThreadBtn() {
        Thread playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    //Sets play button image and sets progress of seekBar
    public void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            onTrackPause();

            seekBar.setMax(mediaPlayer.getDuration() / 1000);

            SongInfoTab.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        } else {
            onTrackPlay();

            seekBar.setMax(mediaPlayer.getDuration() / 1000);

            SongInfoTab.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }

    //Creates new Thread for next song
    private void nextThreadBtn() {
        Thread nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (repeatBtnClicked) {
                            repeatBtnClicked = false;
                            onTrackNext();
                            sentIntent();
                            repeatBtnClicked = true;
                        } else {
                            onTrackNext();
                            sentIntent();
                        }
                    }
                });
            }
        };
        nextThread.start();
    }


    /*When next button is clicked, sets position of songsList to +1. Also sets song title,
     artist name, album cover and seekBar position when SongInfoTab is fully opened*/
    public void nextBtnClicked() {
        coverLoaded = false;
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
            } else if (fromPlaylist) {
                positionInAboutPlaylist = getRandom(previousSongsInPlaylist.size() - 1);
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

        //Sets song and artist strings
        if (fromAlbumInfo) {
            uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getArtist();
            if (aboutFragmentItemAdapter != null) {
                aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
            }
        } else if (fromSearch) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            uri = Uri.parse(staticPreviousArtistSongs.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
            if (aboutFragmentItemAdapter != null) {
                aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
            }
        } else if (fromPlaylist) {
            uri = Uri.parse(previousSongsInPlaylist.get(positionInAboutPlaylist).getData());
            songNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getTitle();
            artistNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getArtist();
            if (aboutPlaylistAdapter != null) {
                aboutPlaylistAdapter.updateColorAfterSongSwitch(positionInAboutPlaylist);
            }
        } else {
            uri = Uri.parse(songsList.get(position).getData());
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
            if (songsAdapter != null) {
                songsAdapter.updateColorAfterSongSwitch(position);
            }
        }

        //Creates mediaPlayer, sets song and artist name, sets seekBar
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);

        mediaPlayer.start();

        metaDataInInfoTab(uri);

        song_title.setText(songNameStr);
        artist_name.setText(artistNameStr);

        seekBar.setMax(mediaPlayer.getDuration() / 1000);

        SongInfoTab.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });

        mediaPlayer.setOnCompletionListener(this);

        //SharedPreferences
        this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", fromAlbumInfo).commit();
        this.getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
        this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        this.getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
    }

    //Creates new Thread for previous song
    private void previousThreadBtn() {
        Thread previousThread = new Thread() {
            @Override
            public void run() {
                super.run();
                previousBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (repeatBtnClicked) {
                            repeatBtnClicked = false;
                            onTrackPrevious();
                            sentIntent();
                            repeatBtnClicked = true;
                        } else {
                            onTrackPrevious();
                            sentIntent();
                        }
                    }
                });
            }
        };
        previousThread.start();
    }

    /*When previous button is clicked, sets position of songsList to -1. Also sets song title,
    artist name, album cover and seekBar position when SongInfoTab is fully opened*/
    private void previousBtnClicked() {
        coverLoaded = false;

        if (mediaPlayer.isPlaying()) {
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
                } else if (fromPlaylist) {
                    positionInAboutPlaylist = getRandom(staticPreviousArtistSongs.size() - 1);
                } else {
                    position = getRandom(songsList.size() - 1);
                }
            } else if (!shuffleBtnClicked && repeatBtnClicked) {
                if (fromAlbumInfo) {
                    uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
                } else if (songSearchWasOpened) {
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
                } else if (songSearchWasOpened) {
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
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                if (fromAlbumInfo) {
                    positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
                } else if (songSearchWasOpened) {
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
                } else if (songSearchWasOpened) {
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
                } else if (songSearchWasOpened) {
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
        }

        //Sets song and artist strings
        if (fromAlbumInfo) {
            uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getArtist();
            if (aboutFragmentItemAdapter != null) {
                aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
            }
        } else if (fromSearch) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            uri = Uri.parse(staticPreviousArtistSongs.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
            if (aboutFragmentItemAdapter != null) {
                aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
            }
        } else if (fromPlaylist) {
            uri = Uri.parse(previousSongsInPlaylist.get(positionInAboutPlaylist).getData());
            songNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getTitle();
            artistNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getArtist();
            if (aboutPlaylistAdapter != null) {
                aboutPlaylistAdapter.updateColorAfterSongSwitch(positionInAboutPlaylist);
            }
        } else {
            uri = Uri.parse(songsList.get(position).getData());
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
            if (songsAdapter != null) {
                songsAdapter.updateColorAfterSongSwitch(position);
            }
        }

        //Creates mediaPlayer, sets song and artist name, sets seekBar
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        metaDataInInfoTab(uri);

        song_title.setText(songNameStr);
        artist_name.setText(artistNameStr);

        SongInfoTab.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });

        mediaPlayer.setOnCompletionListener(this);
        playPauseBtn.setBackgroundResource(R.drawable.ic_play_song);
        mediaPlayer.start();

        //SharedPreferences
        this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", fromAlbumInfo).commit();
        this.getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
        this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        this.getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
    }

    //When song is finished, switches to next song
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (repeatBtnClicked) {
            onTrackNext();
            mediaPlayer.setOnCompletionListener(this);
        } else {
            onTrackNext();
            mediaPlayer.setOnCompletionListener(this);
            SongInfoTab.this.getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                    .putInt("position", position).commit();
        }

        mediaPlayer.start();

        //SharedPreferences
        this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", fromAlbumInfo).commit();
        this.getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
        this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        this.getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
    }

    @Override
    protected void onResume() {
        if (fromBackground) {
            this.unregisterReceiver(broadcastReceiverSongsInfoTab);
            fromBackground = false;
        }

        createChannel();
        trackAudioSource();

        //Register headphones buttons
        HeadsetActionButtonReceiver.delegate = this;
        HeadsetActionButtonReceiver.register(this);

        nextThreadBtn();
        playThreadBtn();
        previousThreadBtn();
        song_title.setText(songNameStr);
        artist_name.setText(artistNameStr);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (repeatBtnClicked) {
            this.getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                    .putBoolean("repeatBtnClicked", true).commit();
        } else {
            this.getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                    .putBoolean("repeatBtnClicked", false).commit();
        }

        //Checking is screen locked
        KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            //if locked
        } else {
            this.unregisterReceiver(broadcastReceiverSongsInfoTab);
        }

        this.getSharedPreferences("fromArtistInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromArtistInfo", fromArtistInfo).commit();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (songInfoTabOpened) {
            createChannel();
            fromBackground = true;
        }

        this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", fromAlbumInfo).commit();
        this.getSharedPreferences("songSearchWasOpened", Context.MODE_PRIVATE).edit()
                .putBoolean("songSearchWasOpened", songSearchWasOpened).commit();
        this.getSharedPreferences("fromArtistInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromArtistInfo", fromArtistInfo).commit();
        this.getSharedPreferences("fromPlaylist", Context.MODE_PRIVATE).edit()
                .putBoolean("fromPlaylist", fromPlaylist).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        song_title = findViewById(R.id.infoTab_song_title);
        artist_name = findViewById(R.id.infoTab_artist_title);
        time_duration_passed = findViewById(R.id.infoTab_duration_passed);
        time_duration_total = findViewById(R.id.infoTab_duration_left);
        album_cover = findViewById(R.id.infoTab_album_cover);
        shuffleBtn = findViewById(R.id.infoTab_shuffle_button);
        previousBtn = findViewById(R.id.infoTab_previous_button);
        nextBtn = findViewById(R.id.infoTab_next_button);
        repeatBtn = findViewById(R.id.infoTab_repeat_button);
        playPauseBtn = findViewById(R.id.infoTab_pause_button);
        seekBar = findViewById(R.id.infoTab_seekBar);
        backBtn = findViewById(R.id.backBtn_songInfo);

        artist_name_main = findViewById(R.id.artist_name_main);
        song_title_main = findViewById(R.id.song_title_main);

        shuffleBtn.setOnClickListener(btnListener);
        repeatBtn.setOnClickListener(btnListener);
        backBtn.setOnClickListener(btnListener);
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.infoTab_shuffle_button:
                    if (shuffleBtnClicked) {
                        shuffleBtnClicked = false;
                        shuffleBtn.setImageResource(R.drawable.ic_shuffle_songs_off);
                        SongInfoTab.this.getSharedPreferences("shuffleBtnClicked", Context.MODE_PRIVATE).edit()
                                .putBoolean("shuffleBtnClicked", shuffleBtnClicked).commit();
                        SongInfoTab.this.getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                                .putBoolean("repeatBtnClicked", repeatBtnClicked).commit();
                    } else {
                        shuffleBtnClicked = true;
                        repeatBtnClicked = false;
                        shuffleBtn.setImageResource(R.drawable.ic_shuffle_songs_on);
                        repeatBtn.setImageResource(R.drawable.ic_repeat_song_off);
                        SongInfoTab.this.getSharedPreferences("shuffleBtnClicked", Context.MODE_PRIVATE).edit()
                                .putBoolean("shuffleBtnClicked", shuffleBtnClicked).commit();
                        SongInfoTab.this.getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                                .putBoolean("repeatBtnClicked", repeatBtnClicked).commit();
                    }
                    break;
                case R.id.infoTab_repeat_button:
                    if (repeatBtnClicked) {
                        repeatBtnClicked = false;
                        repeatBtn.setImageResource(R.drawable.ic_repeat_song_off);
                        SongInfoTab.this.getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                                .putBoolean("repeatBtnClicked", repeatBtnClicked).commit();
                        SongInfoTab.this.getSharedPreferences("shuffleBtnClicked", Context.MODE_PRIVATE).edit()
                                .putBoolean("shuffleBtnClicked", shuffleBtnClicked).commit();
                    } else {
                        repeatBtnClicked = true;
                        shuffleBtnClicked = false;
                        repeatBtn.setImageResource(R.drawable.ic_repeat_song_on);
                        shuffleBtn.setImageResource(R.drawable.ic_shuffle_songs_off);
                        SongInfoTab.this.getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                                .putBoolean("repeatBtnClicked", repeatBtnClicked).commit();
                        SongInfoTab.this.getSharedPreferences("shuffleBtnClicked", Context.MODE_PRIVATE).edit()
                                .putBoolean("shuffleBtnClicked", shuffleBtnClicked).commit();
                    }
                    break;
                case R.id.backBtn_songInfo:
                    finish();
                    overridePendingTransition(0, R.anim.hold);
                    break;
            }
        }
    };

    //Calls when audio source changed
    BroadcastReceiver audioSourceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                onTrackPause();
            }
        }
    };

    //To register audioSourceChangedReceiver
    public void trackAudioSource() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(audioSourceChangedReceiver, intentFilter);
    }

    BroadcastReceiver broadcastReceiverSongsInfoTab = new BroadcastReceiver() {
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

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Tape", NotificationManager.IMPORTANCE_HIGH);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            this.registerReceiver(broadcastReceiverSongsInfoTab, new IntentFilter("SONGS_SONGS"));
            this.startService(new Intent(this, OnClearFromRecentService.class));
        }
    }

    @Override
    public void onTrackPrevious() {
        isPlaying = true;
        previousBtnClicked();
        if (fromSearch) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(this, previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_pause_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
            songsAdapter.updateColorAfterSongSwitch(position);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        playPauseBtn.setImageResource(R.drawable.ic_pause_song);
    }

    @Override
    public void onTrackNext() {
        isPlaying = true;
        nextBtnClicked();
        if (fromSearch) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(this, previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_pause_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
            songsAdapter.updateColorAfterSongSwitch(position);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        playPauseBtn.setImageResource(R.drawable.ic_pause_song);
    }

    @Override
    public void onTrackPlay() {
        isPlaying = true;
        mediaPlayer.start();
        if (fromSearch) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(this, previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_pause_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        playPauseBtn.setImageResource(R.drawable.ic_pause_song);
    }

    @Override
    public void onTrackPause() {
        isPlaying = false;
        mediaPlayer.pause();
        if (fromSearch) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.ic_play_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_play_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_play_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(this, previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_play_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_play_song, position, songsList.size() - 1);
        }
        playPauseBtn.setImageResource(R.drawable.ic_play_song);
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