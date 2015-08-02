package com.example.manuelsanchez.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_CLOSE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_IDLE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_NEXT;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PAUSE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PLAY;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PREV;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_RESUME;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.MUSIC_PLAYER_SERVICE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_INDEX;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;


public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();

    private MediaPlayer mMediaPlayer;
    private ArrayList<ArtistTopTrackItem> mTracks;
    private int mCurrentSong;

    Callback mCallBack;
    ArrayList<StatusChangeListener> statusChangeListener;

    NotificationManager mNotificationManager;
    private PendingIntent mPendingActivityIntent;
    private PendingIntent mPendingPauseIntent;
    private PendingIntent mPendingResumeIntent;
    private PendingIntent mPendingPlayIntent;
    private PendingIntent mPendingNextIntent;
    private PendingIntent mPendingPreviousIntent;
    private PendingIntent mPendingCloseIntent;


    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, ArtistSearchActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

        Intent resumeIntent = new Intent(this, MusicPlayerService.class);
        resumeIntent.setAction(ACTION_RESUME);
        mPendingResumeIntent = PendingIntent.getService(this, 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(this, MusicPlayerService.class);
        closeIntent.setAction(ACTION_CLOSE);
        mPendingCloseIntent = PendingIntent.getService(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicPlayerBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_PLAY:
                play(intent);
                break;
            case ACTION_NEXT:
                nextTrack();
                break;
            case ACTION_PREV:
                previousTrack();
                break;
            case ACTION_PAUSE:
                pausePlayer();
                break;
            case ACTION_RESUME:
                resumePlayer();
                break;
            case ACTION_CLOSE:
                stopPlayer();
                break;
            default:
                stopPlayer();
        }
        return Service.START_STICKY;
    }

    private void play(Intent intent) {
        ArrayList<ArtistTopTrackItem> tracks = intent.getParcelableArrayListExtra(TRACK_ITEMS);
        int trackIndex = intent.getIntExtra(TRACK_INDEX, 0);

        if (mTracks == null || !isTrackCurrentlyPlaying(tracks, trackIndex)) {
            mTracks = tracks;
            mCurrentSong = trackIndex;

            createMediaNotification(ACTION_PAUSE);
            initializeMediaPlayer();

            fireStatusChangeEvent(ACTION_PLAY);

        }
    }

    private boolean isTrackCurrentlyPlaying(ArrayList<ArtistTopTrackItem> tracks, int index) {
        ArtistTopTrackItem currentTrack = mTracks.get(mCurrentSong);
        ArtistTopTrackItem track = tracks.get(index);
        return track.getArtist().equals(currentTrack.getArtist()) &&
                track.getTrack().equals(currentTrack.getTrack()) &&
                track.getAlbum().equals(currentTrack.getAlbum());
    }

    private void nextTrack() {
        mCurrentSong = mCurrentSong == mTracks.size() - 1 ? mCurrentSong : ++mCurrentSong;
        createMediaNotification(ACTION_PAUSE);
        initializeMediaPlayer();
        fireTrackChangeEvent(mCurrentSong);
    }

    private void previousTrack() {
        mCurrentSong = mCurrentSong == 0 ? mCurrentSong : --mCurrentSong;
        createMediaNotification(ACTION_PAUSE);
        initializeMediaPlayer();
        fireTrackChangeEvent(mCurrentSong);
    }

    private void pausePlayer() {
        createMediaNotification(ACTION_PLAY);
        mMediaPlayer.pause();
        fireStatusChangeEvent(ACTION_PAUSE);

    }

    private void resumePlayer() {
        createMediaNotification(ACTION_PAUSE);
        mMediaPlayer.start();
        fireStatusChangeEvent(ACTION_PLAY);
    }

    private void stopPlayer() {
        stopForeground(true);
        mMediaPlayer.reset();
        mMediaPlayer = null;
        fireStatusChangeEvent(ACTION_PAUSE);
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
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
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            WifiManager.WifiLock wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
            wifiLock.acquire();
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Media Player error: " + e.getMessage());
        }
    }

    private void createMediaNotification(String status) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationEnabled = preferences.getBoolean(SettingsFragment.NOTIFICATION_PREF, true);
        if (notificationEnabled) {
            PendingIntent intent = status.equals(ACTION_PLAY) ? mPendingPlayIntent : mPendingPauseIntent;
            int resource = status.equals(ACTION_PLAY) ? R.drawable.ic_play_arrow_black_18dp : R.drawable.ic_pause_black_18dp;
            Notification notification = new Notification.Builder(this)
                    .setStyle(new Notification.MediaStyle())
                    .setContentTitle(mTracks.get(mCurrentSong).getTrack())
                    .setTicker("Spotify Streamer")
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setContentText(mTracks.get(mCurrentSong).getArtist())
                    .setSmallIcon(R.drawable.ic_play_arrow_black_48dp)
                    .setLargeIcon(loadBitMap(mTracks.get(mCurrentSong).getImageUrl()))
                    .setContentIntent(mPendingActivityIntent)
                    .setOngoing(true)
                    .addAction(R.drawable.ic_skip_previous_black_18dp, "", mPendingPreviousIntent)
                    .addAction(resource, "", intent)
                    .addAction(R.drawable.ic_skip_next_black_18dp, "", mPendingNextIntent)
                    .addAction(R.drawable.ic_clear_black_18dp, "", mPendingCloseIntent)
                    .build();
            startForeground(MUSIC_PLAYER_SERVICE, notification);
            mNotificationManager.notify(MUSIC_PLAYER_SERVICE, notification);
        }

    }

    public void setCallBack(Callback callBack) {
        mCallBack = callBack;
    }

    private void fireTrackChangeEvent(int index) {
        if (mCallBack != null) {
            mCallBack.onTrackChanged(index);
        }
    }

    public void setStatusChangeListener(StatusChangeListener callBack) {
        if (statusChangeListener == null) {
            statusChangeListener = new ArrayList<>();
        }
        statusChangeListener.add(callBack);
    }

    private void fireStatusChangeEvent(String status) {
        for (StatusChangeListener listener : statusChangeListener) {
            listener.onPlaybackStatusChange(status);
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

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mCurrentSong == mTracks.size() - 1) {
            this.stopSelf();
            fireStatusChangeEvent(ACTION_IDLE);
        } else {
            ++mCurrentSong;
            initializeMediaPlayer();
            mCallBack.onTrackChanged(mCurrentSong);
        }
    }

    public class MusicPlayerBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    interface Callback {

        void onTrackChanged(int trackIndex);
    }

    interface StatusChangeListener {
        void onPlaybackStatusChange(String status);
    }


    public ArrayList<ArtistTopTrackItem> getTracks() {
        return mTracks;
    }

    public int getCurrentIndex() {
        return mCurrentSong;
    }

    public ArtistTopTrackItem getCurrentlyPlayingTrack() {
        return mTracks != null ? mTracks.get(mCurrentSong) : null;
    }

}
