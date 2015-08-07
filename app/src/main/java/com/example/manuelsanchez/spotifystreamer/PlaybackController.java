package com.example.manuelsanchez.spotifystreamer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_IDLE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PAUSE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PLAY;

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

    private OnCompletionCallback onCompletionCallback;

    private ArrayList<Callback> callBacks;

    private PlaybackState playbackState = PlaybackState.IDLE;

    public interface Callback {
        void onTrackChanged(int trackIndex);

        void onPlaybackStatusChange(String status);
    }

    public interface OnCompletionCallback {
        void onCompletion(String status);
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
        if (tracks == null || !isTrackCurrentlyPlaying(tracks, trackIndex) || playbackState.equals(PlaybackState.IDLE)) {
            tracks = topTrackItems;
            currentIndex = trackIndex;
            initializeMediaPlayer();
        } else if (playbackState.equals(PlaybackState.PAUSED)) {
            mMediaPlayer.start();
        }
        fireStatusChangeEvent(ACTION_PLAY);

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
        mMediaPlayer.pause();
        fireStatusChangeEvent(ACTION_PAUSE);

    }

    public void stop() {
        playbackState = PlaybackState.IDLE;
        mMediaPlayer.reset();
        fireStatusChangeEvent(ACTION_PAUSE);

    }

//    @Override
//    public void onAudioFocusChange(int focusChange) {
//        switch (focusChange) {
//            case AudioManager.AUDIOFOCUS_GAIN:
//                if (mMediaPlayer == null) initializeMediaPlayer();
//                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
//                mMediaPlayer.setVolume(1.0f, 1.0f);
//                break;
//
//            case AudioManager.AUDIOFOCUS_LOSS:
//                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
//                mMediaPlayer.release();
//                mMediaPlayer = null;
//                break;
//
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
//                break;
//
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
//                break;
//        }
//    }

    public void setOnCompletionCallback(OnCompletionCallback onCompletionCallback) {
        this.onCompletionCallback = onCompletionCallback;
    }

    private void initializeMediaPlayer() {
        playbackState = PlaybackState.BUFFERING;
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
//            mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
//            WifiManager.WifiLock wifiLock = ((WifiManager) mContext.getSystemService(Context.WIFI_SERVICE))
//                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
//            wifiLock.acquire();
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
            onCompletionCallback.onCompletion(ACTION_IDLE);
            fireTrackChangeEvent(getCurrentIndex());
            fireStatusChangeEvent(ACTION_IDLE);
        } else {
            ++currentIndex;
            initializeMediaPlayer();
            onCompletionCallback.onCompletion(ACTION_PLAY);
            fireTrackChangeEvent(getCurrentIndex());
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playbackState = PlaybackState.PLAY;
        mMediaPlayer.start();
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

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void seekTo(int seconds) {
        mMediaPlayer.seekTo(seconds);
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

    public void registerCallback(Callback callBack) {
        if (callBacks == null) {
            callBacks = new ArrayList<>();
        }
        callBacks.add(callBack);
    }

    public void unregisterCallback(Callback callback) {
        callBacks.remove(callback);
    }

    public PlaybackState getPlaybackState() {
        return playbackState;
    }
}
