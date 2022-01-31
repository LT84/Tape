package com.project.tape;

import static com.project.tape.MainActivity.artist_name_main;
import static com.project.tape.MainActivity.song_title_main;
import static com.project.tape.SongsFragment.mediaPlayer;
import static com.project.tape.SongsFragment.position;
import static com.project.tape.SongsFragment.songsList;
import static com.project.tape.SongsFragment.uri;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongInfoTab extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    TextView song_title, artist_name, time_duration_passed, time_duration_total;
    ImageView album_cover, shuffleBtn, previousBtn, nextBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    private SeekBar seekBar;
    private final Handler handler = new Handler();
    static boolean songSwitched;
    private boolean isActivityOpen = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info_tab);

        initViews();
        getIntentMethod();

        song_title.setText(songsList.get(position).getTitle());
        artist_name.setText(songsList.get(position).getArtist());

        mediaPlayer.setOnCompletionListener(this);

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
    }

    //Sets pause button image and max value of seekBar
    private void getIntentMethod() {
            if (songsList != null) {
                playPauseBtn.setImageResource(R.drawable.pause_song);
                seekBar.setMax(mediaPlayer.getDuration() / 1000);
                metaDataInInfoTab(uri);
            }
        }

    //Sets song title and artist name
    private void sentIntent() {
        Intent data = new Intent();
        song_title_main.setText(songsList.get(position).getTitle());
        artist_name_main.setText(songsList.get(position).getArtist());
        setResult(RESULT_OK, data);
    }

    //Sets album cover when SongInfoTab is fully opened
    private void metaDataInInfoTab(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(songsList.get(position).getDuration()) / 1000;
        time_duration_total.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();

        if (art != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover);
        } else if (isActivityOpen == true) {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.bebra)
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
        totalNew =  minutes + ":" + "0" + seconds;
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

    //Sets play button image and sets progress of seekBar
    public void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.play_song);
            mediaPlayer.pause();
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
                playPauseBtn.setImageResource(R.drawable.pause_song);
                mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration() / 1000);

                    SongInfoTab.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null)
                            {
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
                        nextBtnClicked();
                        sentIntent();
                        songSwitched = true;
                    }
                });
            }
        };
        nextThread.start();
    }

    /*When next button is clicked, sets position of songsList to +1. Also sets song title,
     artist name, album cover and seekBar position when SongInfoTab is fully opened*/
    public void nextBtnClicked() {
        if (mediaPlayer.isPlaying() && isActivityOpen == true) {
            mediaPlayer.stop();
            mediaPlayer.release();

            position = ((position + 1) % songsList.size());
            uri = Uri.parse(songsList.get(position).getData());

            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaDataInInfoTab(uri);

            song_title.setText(songsList.get(position).getTitle());
            artist_name.setText(songsList.get(position).getArtist());
            song_title_main.setText(songsList.get(position).getTitle());
            artist_name_main.setText(songsList.get(position).getArtist());

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
            playPauseBtn.setBackgroundResource(R.drawable.pause_song);
            mediaPlayer.start();
        } else if (isActivityOpen == true) {
                mediaPlayer.stop();
                mediaPlayer.release();

                position = ((position + 1) % songsList.size());
                uri = Uri.parse(songsList.get(position).getData());

                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                metaDataInInfoTab(uri);

                song_title.setText(songsList.get(position).getTitle());
                artist_name.setText(songsList.get(position).getArtist());
                song_title_main.setText(songsList.get(position).getTitle());
                artist_name_main.setText(songsList.get(position).getArtist());

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
                playPauseBtn.setBackgroundResource(R.drawable.play_song);
        }
    }

    //Creates new Thread for previous song
        private void previousThreadBtn () {
            Thread previousThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    previousBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                              previousBtnClicked();
                              sentIntent();
                            songSwitched = true;
                        }
                    });
                }
            };
            previousThread.start();
        }

    /*When previous button is clicked, sets position of songsList to -1. Also sets song title,
    artist name, album cover and seekBar position when SongInfoTab is fully opened*/
    private void previousBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();

            position = ((position - 1) < 0 ? (songsList.size() - 1) : (position - 1));
            uri = Uri.parse(songsList.get(position).getData());

            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaDataInInfoTab(uri);

            song_title.setText(songsList.get(position).getTitle());
            artist_name.setText(songsList.get(position).getArtist());

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
            playPauseBtn.setBackgroundResource(R.drawable.pause_song);
            mediaPlayer.start();
        } else {
                mediaPlayer.stop();
                mediaPlayer.release();

                position = ((position - 1) < 0 ? (songsList.size() - 1) : (position - 1));
                uri = Uri.parse(songsList.get(position).getData());

                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                metaDataInInfoTab(uri);

                song_title.setText(songsList.get(position).getTitle());
                artist_name.setText(songsList.get(position).getArtist());

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
                playPauseBtn.setBackgroundResource(R.drawable.play_song);
            }
    }

    //When song is finished, switches to next song
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isActivityOpen == true) {
            nextBtnClicked();
            songSwitched = true;
            if (mediaPlayer != null) {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(this);
            }
        }
    }

    @Override
    protected void onResume() {
        playThreadBtn();
        nextThreadBtn();
        previousThreadBtn();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isActivityOpen = false;
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
    }


}

