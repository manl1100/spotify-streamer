package com.example.manuelsanchez.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class MusicPlayerService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {


    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();

    public static final String ACTION_PLAY = "com.example.spotifystreamer.action.PLAY";
    public static final String ACTION_PREV = "com.example.spotifystreamer.action.PREV";
    public static final String ACTION_NEXT = "com.example.spotifystreamer.action.NEXT";
    public static final String ACTION_PAUSE = "com.example.spotifystreamer.action.PAUSE";
    public static final int MUSIC_PLAYER_SERVICE = 111;
    private MediaPlayer mMediaPlayer;
    private ArrayList<ArtistTopTrackItem> mTracks;
    private int mCurrentSong = 0;
    Callback mCallBack;

    NotificationManager mNotificationManager;
    private PendingIntent mPendingActivityIntent;
    private PendingIntent mPendingPauseIntent;
    private PendingIntent mPendingPlayIntent;
    private PendingIntent mPendingNextIntent;
    private PendingIntent mPendingPreviousIntent;


    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, ArtistSearchActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mPendingActivityIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(this, MusicPlayerService.class);
        previousIntent.setAction(ACTION_PREV);
        mPendingPreviousIntent = PendingIntent.getService(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(this, MusicPlayerService.class);
        playIntent.setAction(ACTION_PLAY);
        mPendingPlayIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, MusicPlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        mPendingNextIntent = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, MusicPlayerService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        mPendingPauseIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicPlayerBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {

            if (mTracks == null) {
                mTracks = intent.getParcelableArrayListExtra("TRACK");
                mCurrentSong = intent.getIntExtra("TRACK_INDEX", 0);
            }

            createMediaNotification(ACTION_PAUSE);
            initializeMediaPlayer();
            if (mCallBack != null) {
                mCallBack.onPlaybackStatusChange(ACTION_PLAY);
            }

        } else if (intent.getAction().equals(ACTION_NEXT)) {
            mCurrentSong = mCurrentSong == mTracks.size() - 1 ? mCurrentSong : ++mCurrentSong;
            createMediaNotification(ACTION_PAUSE);
            initializeMediaPlayer();
            if (mCallBack != null) {
                mCallBack.onTrackChanged(mCurrentSong);
            }

        } else if (intent.getAction().equals(ACTION_PREV)) {
            mCurrentSong = mCurrentSong == 0 ? mCurrentSong : --mCurrentSong;
            createMediaNotification(ACTION_PAUSE);
            initializeMediaPlayer();
            if (mCallBack != null) {
                mCallBack.onTrackChanged(mCurrentSong);
            }
        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            createMediaNotification(ACTION_PLAY);
            mMediaPlayer.pause();
            if (mCallBack != null) {
                mCallBack.onPlaybackStatusChange(ACTION_PAUSE);
            }
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

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initializeMediaPlayer();
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

    private void initializeMediaPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
        } else {
            mMediaPlayer = new MediaPlayer();
        }

        try {
            mMediaPlayer.setDataSource(mTracks.get(mCurrentSong).getPreviewUrl());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            WifiManager.WifiLock wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
            wifiLock.acquire();
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        } catch (Exception e) {
            Log.e(LOG_TAG, "Media Player error: " + e.getMessage());
        }
    }

    private void createMediaNotification(String status) {
        PendingIntent intent = status.equals(ACTION_PLAY) ? mPendingPlayIntent : mPendingPauseIntent;
        int resource = status.equals(ACTION_PLAY) ? R.drawable.ic_play_arrow_black_48dp : R.drawable.ic_pause_black_48dp;
        Notification notification = new Notification.Builder(this)
                .setStyle(new Notification.MediaStyle())
                .setContentTitle(mTracks.get(mCurrentSong).getTrack())
                .setTicker("Spotify Streamer")
                .setContentText(mTracks.get(mCurrentSong).getArtist())
                .setSmallIcon(R.drawable.ic_play_arrow_black_48dp)
                .setLargeIcon(loadBitMap(mTracks.get(mCurrentSong).getImageUrl()))
                .setContentIntent(mPendingActivityIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_skip_previous_black_48dp, "", mPendingPreviousIntent)
                .addAction(resource, "", intent)
                .addAction(R.drawable.ic_skip_next_black_48dp, "", mPendingNextIntent)
                .build();
        startForeground(MUSIC_PLAYER_SERVICE, notification);
        mNotificationManager.notify(MUSIC_PLAYER_SERVICE, notification);
    }

    public void setCallBack(Callback callBack) {
        mCallBack = callBack;
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

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        this.stopSelf();
    }

    public class MusicPlayerBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    interface Callback {
        void onTrackCompletion(int trackIndex);

        void onPlaybackStatusChange(String status);

        void onTrackChanged(int trackIndex);
    }

}
