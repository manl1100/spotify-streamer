package com.example.manuelsanchez.spotifystreamer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MusicPlayerService extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {


    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();

    public static final String ACTION_PLAY = "com.example.spotifystreamer.action.PLAY";
    public static final String ACTION_PREV = "com.example.spotifystreamer.action.PREV";
    public static final String ACTION_NEXT = "com.example.spotifystreamer.action.NEXT";
    public static final String ACTION_STOP = "com.example.spotifystreamer.action.STOP";
    public static final int MUSIC_PLAYER_SERVICE = 111;
    private MediaPlayer mMediaPlayer;
    private ArrayList<ArtistTopTrackItem> mTracks;
    private int mCurrentSong;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicPlayerBinder();
    }

    public void initMediaPlayer() {
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {

            ArrayList<ArtistTopTrackItem> trackList = intent.getParcelableArrayListExtra("TRACK");

            Intent notificationIntent = new Intent(this, ArtistSearchActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Intent previousIntent = new Intent(this, MusicPlayerService.class);
            previousIntent.setAction(ACTION_PREV);
            PendingIntent pendingPreviousIntent = PendingIntent.getActivity(this, 0, previousIntent, 0);


            Intent nextIntent = new Intent(this, MusicPlayerService.class);
            previousIntent.setAction(ACTION_NEXT);
            PendingIntent pendingNextIntent = PendingIntent.getActivity(this, 0, nextIntent, 0);


            Intent stopIntent = new Intent(this, MusicPlayerService.class);
            previousIntent.setAction(ACTION_STOP);
            PendingIntent pendingStopIntent = PendingIntent.getActivity(this, 0, stopIntent, 0);

            Notification notification = new Notification.Builder(this)
                    .setStyle(new Notification.MediaStyle())
                    .setContentTitle(trackList.get(0).getTrack())
                    .setTicker("Spotify Streamer")
                    .setContentText(trackList.get(0).getArtist())
                    .setSmallIcon(R.drawable.ic_play_arrow_black_48dp)
                    .setLargeIcon(loadBitMap(trackList.get(0).getImageUrl()))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(R.drawable.ic_skip_previous_black_48dp, "", pendingPreviousIntent)
                    .addAction(R.drawable.ic_play_arrow_black_48dp, "", pendingStopIntent)
                    .addAction(R.drawable.ic_skip_next_black_48dp, "", pendingNextIntent).build();
            startForeground(MUSIC_PLAYER_SERVICE, notification);


            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(trackList.get(0).getPreviewUrl());
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);

                mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                WifiManager.WifiLock wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

                wifiLock.acquire();
                mMediaPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (Exception e) {
                Log.e(LOG_TAG, "Media Player error");
            }

        } else if (intent.getAction().equals(ACTION_NEXT)) {
            Toast.makeText(getApplicationContext(), "Next", Toast.LENGTH_LONG).show();

        } else if (intent.getAction().equals(ACTION_PREV)) {
            Toast.makeText(getApplicationContext(), "Previous", Toast.LENGTH_LONG).show();

        } else if (intent.getAction().equals(ACTION_STOP)) {
            Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_LONG).show();

        }

        return Service.START_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
    }

    public void setTracks(ArrayList<ArtistTopTrackItem> mTracks) {
        this.mTracks = mTracks;
    }

    public void setCurrentSong(int mCurrentSong) {
        this.mCurrentSong = mCurrentSong;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private Bitmap loadBitMap(final String imageUrl) {
        Bitmap bitmap = null;
        try {
            bitmap = new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... voids) {
                    try {
                        return Picasso.with(getApplicationContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_audiotrack_black_48dp)
                                .error(R.drawable.ic_audiotrack_black_48dp)
                                .resize(150, 150)
                                .centerCrop()
                                .get();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                        return null;
                    }
                }
            }.execute().get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.ic_audiotrack_black_48dp);
        }

        return bitmap;

    }

    public class MusicPlayerBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

}
