package com.example.manuelsanchez.spotifystreamer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;

import java.util.ArrayList;

/**
 * Created by Manuel Sanchez on 8/2/15
 */
public class PlaybackController implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = PlaybackController.class.getSimpleName();

    private static PlaybackController playbackController;
    private MediaPlayer mMediaPlayer;

    private ArrayList<ArtistTopTrackItem> tracks;
    private int currentIndex;

    private Context mContext;
    private ArrayList<Callback> callBacks;

    private PlaybackState playbackState = PlaybackState.IDLE;
    private boolean isBuffering;

    public interface Callback {
        void onTrackChanged(int trackIndex);

        void onPlaybackStatusChange(PlaybackState state);
    }

    private PlaybackController() {
    }

    public static PlaybackController getInstance() {
        if (playbackController == null) {
            playbackController = new PlaybackController();
        }
        return playbackController;
    }

    public void play(ArrayList<ArtistTopTrackItem> topTrackItems, int trackIndex) {
        if (tracks == null || !isTrackCurrentlyPlaying(topTrackItems, trackIndex) || playbackState.equals(PlaybackState.IDLE)) {
            tracks = topTrackItems;
            currentIndex = trackIndex;
            initializeMediaPlayer();
        } else if (playbackState.equals(PlaybackState.PAUSED)) {
            mMediaPlayer.start();
        }
        fireStatusChangeEvent(PlaybackState.PLAY);
        playbackState = PlaybackState.PLAY;
    }

    public void next() {
        currentIndex = currentIndex == tracks.size() - 1 ? currentIndex : ++currentIndex;
        initializeMediaPlayer();
        fireTrackChangeEvent(getCurrentIndex());
    }

    public void previous() {
        currentIndex = currentIndex == 0 ? currentIndex : --currentIndex;
        initializeMediaPlayer();
        fireTrackChangeEvent(getCurrentIndex());
    }

    public void pause() {
        playbackState = PlaybackState.PAUSED;
        if (!isBuffering) {
            mMediaPlayer.pause();
        }
        fireStatusChangeEvent(PlaybackState.PAUSED);

    }

    public void stop() {
        playbackState = PlaybackState.IDLE;
        mMediaPlayer.reset();
        fireStatusChangeEvent(PlaybackState.PAUSED);

    }

    public void resume() {
        if (!isBuffering) {
            mMediaPlayer.start();
        }
        playbackState = PlaybackState.PLAY;
        fireStatusChangeEvent(PlaybackState.PLAY);
    }

    private void initializeMediaPlayer() {
        isBuffering = true;
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        } else {
            mMediaPlayer = new MediaPlayer();
        }

        try {
            mMediaPlayer.setDataSource(tracks.get(currentIndex).getPreviewUrl());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
            WifiManager.WifiLock wifiLock = ((WifiManager) mContext.getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
            wifiLock.acquire();
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Media Player error: " + e.getMessage());
        }
    }

    private boolean isTrackCurrentlyPlaying(ArrayList<ArtistTopTrackItem> topTrackItems, int index) {
        ArtistTopTrackItem currentTrack = tracks.get(currentIndex);
        ArtistTopTrackItem track = topTrackItems.get(index);
        return track.getArtist().equals(currentTrack.getArtist()) &&
                track.getTrack().equals(currentTrack.getTrack()) &&
                track.getAlbum().equals(currentTrack.getAlbum());
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (currentIndex == tracks.size() - 1) {
            fireTrackChangeEvent(getCurrentIndex());
            fireStatusChangeEvent(PlaybackState.IDLE);
            playbackState = PlaybackState.IDLE;
        } else {
            ++currentIndex;
            initializeMediaPlayer();
            fireStatusChangeEvent(PlaybackState.PLAY);
            fireTrackChangeEvent(getCurrentIndex());
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        if (what == MediaPlayer.MEDIA_ERROR_TIMED_OUT || what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Toast.makeText(mContext, "Check your internet connection", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, "Error has occurred", Toast.LENGTH_LONG).show();
        }
        mediaPlayer.reset();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isBuffering = false;
        if (playbackState.equals(PlaybackState.PLAY)) {
            mMediaPlayer.start();
        } else if (playbackState.equals(PlaybackState.PAUSED)) {
            mediaPlayer.start();
            mediaPlayer.pause();
        }
    }

    public ArrayList<ArtistTopTrackItem> getTracks() {
        return tracks;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public ArtistTopTrackItem getCurrentlyPlayingTrack() {
        return tracks != null ? tracks.get(currentIndex) : null;
    }

    public PlaybackState getPlaybackState() {
        return playbackState;
    }

    public String getCurrentlyPlayingTrackName() {
        return tracks.get(currentIndex).getTrack();
    }

    public String getCurrentlyPlayingArtist() {
        return tracks.get(currentIndex).getArtist();
    }

    public String getCurrentlyPlayingImageUrl() {
        return tracks.get(currentIndex).getImageUrl();
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public int getDuration() {
        return mMediaPlayer.isPlaying() ? mMediaPlayer.getDuration() : 0;
    }

    public void seekTo(int seconds) {
        if (mMediaPlayer.isPlaying() || mMediaPlayer.isLooping() || playbackState.equals(PlaybackState.PAUSED)) {
            mMediaPlayer.seekTo(seconds);
        }
    }

    private void fireTrackChangeEvent(int index) {
        for (Callback callback : callBacks) {
            callback.onTrackChanged(index);
        }
    }

    private void fireStatusChangeEvent(PlaybackState status) {
        for (Callback callback : callBacks) {
            callback.onPlaybackStatusChange(status);
        }
    }

    public void registerCallback(Callback callBack) {
        if (callBacks == null) {
            callBacks = new ArrayList<>();
        }
        callBacks.add(callBack);
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void unregisterCallback(Callback callback) {
        callBacks.remove(callback);
    }

}
