package com.example.manuelsanchez.spotifystreamer;

import android.app.Fragment;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;



public class MusicPlayerActivityFragment extends Fragment {

    private static final String LOG_TAG = MusicPlayerActivityFragment.class.getSimpleName();

    private MediaPlayer mMediaPlayer;
    private ArtistTopTrackItem mTrack;

    public MusicPlayerActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         mTrack = getActivity().getIntent().getParcelableExtra(ArtistTopTracksFragment.TRACK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicPlayerView = inflater.inflate(R.layout.fragment_music_player, container, false);


        TextView artistTextView = (TextView) musicPlayerView.findViewById(R.id.artist_title);
        artistTextView.setText(mTrack.getArtist());

        TextView albumTextView = (TextView) musicPlayerView.findViewById(R.id.artist_album);
        albumTextView.setText(mTrack.getAlbum());

        ImageView albumCover = (ImageView) musicPlayerView.findViewById(R.id.artist_album_cover);
        Picasso.with(getActivity())
                .load(mTrack.getImageUrl())
                .placeholder(R.drawable.ic_audiotrack_black_48dp)
                .error(R.drawable.ic_audiotrack_black_48dp)
                .into(albumCover);

        TextView trackTextView = (TextView) musicPlayerView.findViewById(R.id.artist_track);
        trackTextView.setText(mTrack.getTrack());

        TextView elapsed = (TextView) musicPlayerView.findViewById(R.id.time_elapse);
        elapsed.setText("0:00");

        TextView remaining = (TextView) musicPlayerView.findViewById(R.id.time_remaining);
        remaining.setText("0:30");

        SeekBar seekBar = (SeekBar) musicPlayerView.findViewById(R.id.track_duration_bar);

        Button rewind = (Button) musicPlayerView.findViewById(R.id.rewind);

        Button pause = (Button) musicPlayerView.findViewById(R.id.pause);
        pause.setOnClickListener(onPlayClickListener());

        Button forward = (Button) musicPlayerView.findViewById(R.id.forward);


        return musicPlayerView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private View.OnClickListener onPlayClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer == null) {
                    try {
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setDataSource(mTrack.getPreviewUrl());
                        mMediaPlayer.setOnPreparedListener(onPreparedListener());
                        mMediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "MediaPlayer error: " + e.getMessage());
                    }
                } else if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                }

            }
        };
    }

    private MediaPlayer.OnPreparedListener onPreparedListener() {
        return new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        };
    }

}
