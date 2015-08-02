package com.example.manuelsanchez.spotifystreamer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;

import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_IDLE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PLAY;

/**
 * Created by Manuel Sanchez on 8/2/15
 */
public class PlaybackController implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
        /*AudioManager.OnAudioFocusChangeListener*/ {

    private static final String LOG_TAG = PlaybackController.class.getSimpleName();

    private static PlaybackController playbackController;
    private MediaPlayer mMediaPlayer;

    private ArrayList<ArtistTopTrackItem> tracks;
    private int currentIndex;

    private static Context mContext;
    private OnCompletionCallback onCompletionCallback;


    public interface OnCompletionCallback {
        void onCompletion(String status);
    }

    private PlaybackController() {
    }

    public static PlaybackController create(Context context) {
        mContext = context;
        return getInstance();
    }

    public static PlaybackController getInstance() {
        if (playbackController == null) {
            playbackController = new PlaybackController();
        }
        return playbackController;
    }

    public void play(ArrayList<ArtistTopTrackItem> topTrackItems, int trackIndex) {
        if (tracks == null || !isTrackCurrentlyPlaying(tracks, trackIndex)) {
            tracks = topTrackItems;
            currentIndex = trackIndex;
            initializeMediaPlayer();
        } else if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void next() {
        currentIndex = currentIndex == tracks.size() - 1 ? currentIndex : ++currentIndex;
        initializeMediaPlayer();
    }

    public void previous() {
        currentIndex = currentIndex == 0 ? currentIndex : --currentIndex;
        initializeMediaPlayer();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void resume() {
        mMediaPlayer.start();
    }

    public void stop() {
        mMediaPlayer.reset();
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
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
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
            onCompletionCallback.onCompletion(ACTION_IDLE);
        } else {
            ++currentIndex;
            initializeMediaPlayer();
            onCompletionCallback.onCompletion(ACTION_PLAY);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
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
}
