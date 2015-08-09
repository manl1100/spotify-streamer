package com.example.manuelsanchez.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.manuelsanchez.spotifystreamer.ui.ArtistSearchActivity;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_CLOSE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_NEXT;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PAUSE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PLAY;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PREV;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_RESUME;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.MUSIC_PLAYER_SERVICE;


/**
 * Created by Manuel Sanchez on 8/2/15
 */
public class MediaNotificationManager {

    private static final String LOG_TAG = MediaNotificationManager.class.getSimpleName();

    private PlaybackController mPlaybackController;
    NotificationManager mNotificationManager;
    private PendingIntent mPendingActivityIntent;
    private PendingIntent mPendingPauseIntent;
    private PendingIntent mPendingPlayIntent;
    private PendingIntent mPendingNextIntent;
    private PendingIntent mPendingPreviousIntent;
    private PendingIntent mPendingCloseIntent;

    private Context mContext;

    public MediaNotificationManager(Context context) {
        mContext = context;
        mPlaybackController = PlaybackController.getInstance();

        Intent notificationIntent = new Intent(mContext, ArtistSearchActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingActivityIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(mContext, MusicPlayerService.class);
        previousIntent.setAction(ACTION_PREV);
        mPendingPreviousIntent = PendingIntent.getService(mContext, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(mContext, MusicPlayerService.class);
        playIntent.setAction(ACTION_RESUME);
        mPendingPlayIntent = PendingIntent.getService(mContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(mContext, MusicPlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        mPendingNextIntent = PendingIntent.getService(mContext, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(mContext, MusicPlayerService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        mPendingPauseIntent = PendingIntent.getService(mContext, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(mContext, MusicPlayerService.class);
        closeIntent.setAction(ACTION_CLOSE);
        mPendingCloseIntent = PendingIntent.getService(mContext, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
    }


    public Notification createMediaNotification(String status) {
        PendingIntent intent = status.equals(ACTION_PLAY) ? mPendingPlayIntent : mPendingPauseIntent;
        int resource = status.equals(ACTION_PLAY) ? R.drawable.ic_play_arrow_black_18dp : R.drawable.ic_pause_black_18dp;
        Notification notification = new Notification.Builder(mContext)
                .setStyle(new Notification.MediaStyle())
                .setContentTitle(mPlaybackController.getCurrentlyPlayingTrackName())
                .setTicker("Spotify Streamer")
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentText(mPlaybackController.getCurrentlyPlayingArtist())
                .setSmallIcon(R.drawable.ic_play_arrow_black_48dp)
                .setLargeIcon(loadBitMap(mPlaybackController.getCurrentlyPlayingImageUrl()))
                .setContentIntent(mPendingActivityIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_skip_previous_black_18dp, "", mPendingPreviousIntent)
                .addAction(resource, "", intent)
                .addAction(R.drawable.ic_skip_next_black_18dp, "", mPendingNextIntent)
                .addAction(R.drawable.ic_clear_black_18dp, "", mPendingCloseIntent)
                .build();
        mNotificationManager.notify(MUSIC_PLAYER_SERVICE, notification);
        return notification;
    }

    private Bitmap loadBitMap(final String imageUrl) {
        Bitmap bitmap = null;
        try {
            bitmap = new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... voids) {
                    try {
                        return Picasso.with(mContext)
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
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_audiotrack_black_48dp);
        }

        return bitmap;
    }
}
