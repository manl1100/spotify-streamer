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

    private ArrayList<Callback> callBacks;

    public void onCreate() {
        super.onCreate();
        playbackController = PlaybackController.create(this);
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


    public interface Callback {
        void onTrackChanged(int trackIndex);

        void onPlaybackStatusChange(String status);
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
            default:
                stopPlayer();
        }
        return Service.START_STICKY;
    }

    private void play(Intent intent) {
        ArrayList<ArtistTopTrackItem> tracks = intent.getParcelableArrayListExtra(TRACK_ITEMS);
        int trackIndex = intent.getIntExtra(TRACK_INDEX, 0);

        if (tracks == null) {
            playbackController.resume();
        } else {
            playbackController.play(tracks, trackIndex);

        }
        fireStatusChangeEvent(ACTION_PLAY);
        createNotificationIfNeeded(ACTION_PAUSE);
    }

    private void nextTrack() {
        playbackController.next();
        createNotificationIfNeeded(ACTION_PAUSE);
        fireTrackChangeEvent(getCurrentIndex());
    }

    private void previousTrack() {
        playbackController.previous();
        createNotificationIfNeeded(ACTION_PAUSE);
        fireTrackChangeEvent(getCurrentIndex());
    }

    private void pausePlayer() {
        playbackController.pause();
        createNotificationIfNeeded(ACTION_PLAY);
        fireStatusChangeEvent(ACTION_PAUSE);
    }

    private void resumePlayer() {
        playbackController.resume();
        createNotificationIfNeeded(ACTION_PAUSE);
        fireStatusChangeEvent(ACTION_PLAY);
    }

    private void stopPlayer() {
        stopForeground(true);
        playbackController.stop();
        fireStatusChangeEvent(ACTION_PAUSE);
    }

    @Override
    public void onCompletion(String status) {
        if (status.equals(ACTION_IDLE)) {
            this.stopSelf();
            fireTrackChangeEvent(getCurrentIndex());
            fireStatusChangeEvent(ACTION_IDLE);
        } else if (status.equals(ACTION_PLAY)) {
            fireTrackChangeEvent(getCurrentIndex());
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

    private void fireTrackChangeEvent(int index) {
        for (Callback callback : callBacks) {
            callback.onTrackChanged(index);
        }
    }

    private void fireStatusChangeEvent(String status) {
        for (Callback callback : callBacks) {
            callback.onPlaybackStatusChange(status);
        }
    }
    public void setCallBack(Callback callBack) {
        if (callBacks == null) {
            callBacks = new ArrayList<>();
        }
        callBacks.add(callBack);
    }

    public ArrayList<ArtistTopTrackItem> getTracks() {
        return playbackController.getTracks();
    }

    public int getCurrentIndex() {
        return playbackController.getCurrentIndex();
    }

    public ArtistTopTrackItem getCurrentlyPlayingTrack() {
        return playbackController.getCurrentlyPlayingTrack();
    }
}
