package com.example.manuelsanchez.spotifystreamer;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;
import com.example.manuelsanchez.spotifystreamer.ui.SettingsFragment;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_CLOSE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_RESUME;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_IDLE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_NEXT;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PAUSE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PLAY;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PREV;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.MUSIC_PLAYER_SERVICE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_INDEX;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;


public class MusicPlayerService extends Service implements PlaybackController.OnCompletionCallback {

    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();

    private MediaNotificationManager mediaNotificationManager;
    private PlaybackController playbackController;

    public void onCreate() {
        super.onCreate();
        playbackController = PlaybackController.getInstance();
        playbackController.setOnCompletionCallback(this);
        mediaNotificationManager = new MediaNotificationManager(this);
    }

    public class MusicPlayerBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
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
            case ACTION_CLOSE:
                stopPlayer();
                break;
            case ACTION_RESUME:
                resumePlayer();
                break;
            default:
                stopPlayer();
        }
        return Service.START_STICKY;
    }

    private void play(Intent intent) {
        ArrayList<ArtistTopTrackItem> tracks = intent.getParcelableArrayListExtra(TRACK_ITEMS);
        int trackIndex = intent.getIntExtra(TRACK_INDEX, 0);
        playbackController.play(tracks, trackIndex);
        createNotificationIfNeeded(ACTION_PAUSE);
    }

    private void nextTrack() {
        playbackController.next();
        if (playbackController.getPlaybackState().equals(PlaybackState.PLAY)) {
            createNotificationIfNeeded(ACTION_PAUSE);

        } else {
            createNotificationIfNeeded(ACTION_PLAY);

        }
    }

    private void previousTrack() {
        playbackController.previous();
        if (playbackController.getPlaybackState().equals(PlaybackState.PLAY)) {
            createNotificationIfNeeded(ACTION_PAUSE);

        } else {
            createNotificationIfNeeded(ACTION_PLAY);

        }
    }

    private void pausePlayer() {
        playbackController.pause();
        createNotificationIfNeeded(ACTION_PLAY);
    }

    private void stopPlayer() {
        stopForeground(true);
        playbackController.stop();
    }

    private void resumePlayer() {
        playbackController.resume();
        createNotificationIfNeeded(ACTION_PAUSE);
    }

    @Override
    public void onCompletion(String status) {
        if (status.equals(ACTION_IDLE)) {
            this.stopSelf();
        }
    }

    private void createNotificationIfNeeded(String status) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationEnabled = preferences.getBoolean(SettingsFragment.NOTIFICATION_PREF, true);
        if (notificationEnabled) {
            Notification notification = mediaNotificationManager.createMediaNotification(status);
            startForeground(MUSIC_PLAYER_SERVICE, notification);
        }
    }
}
