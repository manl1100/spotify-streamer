package com.example.manuelsanchez.spotifystreamer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MusicPlayerFragment extends DialogFragment {

    private static final String LOG_TAG = MusicPlayerFragment.class.getSimpleName();

    private ArrayList<ArtistTopTrackItem> mTracks;
    Context mContext;

    public MusicPlayerFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracks = getActivity().getIntent().getParcelableArrayListExtra(ArtistTopTracksFragment.TRACK);
        if (mTracks == null) {
            mTracks = getArguments().getParcelableArrayList(ArtistTopTracksFragment.TRACK);
        }
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicPlayerView = inflater.inflate(R.layout.fragment_music_player, container, false);


        TextView artistTextView = (TextView) musicPlayerView.findViewById(R.id.artist_title);
        artistTextView.setText(mTracks.get(0).getArtist());

        TextView albumTextView = (TextView) musicPlayerView.findViewById(R.id.artist_album);
        albumTextView.setText(mTracks.get(0).getAlbum());

        ImageView albumCover = (ImageView) musicPlayerView.findViewById(R.id.artist_album_cover);
        Picasso.with(getActivity())
                .load(mTracks.get(0).getImageUrl())
                .placeholder(R.drawable.ic_audiotrack_black_48dp)
                .error(R.drawable.ic_audiotrack_black_48dp)
                .into(albumCover);

        TextView trackTextView = (TextView) musicPlayerView.findViewById(R.id.artist_track);
        trackTextView.setText(mTracks.get(0).getTrack());

        TextView elapsed = (TextView) musicPlayerView.findViewById(R.id.time_elapse);
        elapsed.setText("0:00");

        TextView remaining = (TextView) musicPlayerView.findViewById(R.id.time_remaining);
        remaining.setText("0:30");

        SeekBar seekBar = (SeekBar) musicPlayerView.findViewById(R.id.track_duration_bar);

        Button rewind = (Button) musicPlayerView.findViewById(R.id.rewind);
        rewind.setOnClickListener(onPreviousTrackClickListener());

        Button pause = (Button) musicPlayerView.findViewById(R.id.pause);
        pause.setOnClickListener(onPlayPauseClickListener());

        Button forward = (Button) musicPlayerView.findViewById(R.id.forward);
        forward.setOnClickListener(onNextTrackClickListener());

        return musicPlayerView;
    }

    private View.OnClickListener onPlayPauseClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isPlay = ((ToggleButton) view).isChecked();
                if (isPlay) {
                    Intent intent = new Intent(mContext, MusicPlayerService.class);
                    intent.setAction(MusicPlayerService.ACTION_PLAY);
                    intent.putParcelableArrayListExtra("TRACK", mTracks);
                    mContext.startService(intent);
                } else {
                    Intent intent = new Intent(mContext, MusicPlayerService.class);
                    intent.setAction(MusicPlayerService.ACTION_PAUSE);
                    mContext.startService(intent);
                }
            }
        };
    }

    private View.OnClickListener onNextTrackClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MusicPlayerService.class);
                intent.setAction(MusicPlayerService.ACTION_NEXT);
                mContext.startService(intent);
            }
        };
    }

    private View.OnClickListener onPreviousTrackClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MusicPlayerService.class);
                intent.setAction(MusicPlayerService.ACTION_PREV);
                mContext.startService(intent);
            }
        };
    }

}
