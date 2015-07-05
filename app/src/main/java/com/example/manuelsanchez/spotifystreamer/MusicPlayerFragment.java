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
    private int mCurrentTrackIndex;
    Context mContext;

    TextView artistTextView;
    TextView albumTextView;
    ImageView albumCover;
    TextView trackTextView;

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
        mCurrentTrackIndex = getActivity().getIntent().getIntExtra(ArtistTopTracksFragment.TRACK_INDEX, 0);
        if (mTracks == null) {
            mTracks = getArguments().getParcelableArrayList(ArtistTopTracksFragment.TRACK);
            mCurrentTrackIndex = getArguments().getInt(ArtistTopTracksFragment.TRACK_INDEX);
        }
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicPlayerView = inflater.inflate(R.layout.fragment_music_player, container, false);


        artistTextView = (TextView) musicPlayerView.findViewById(R.id.artist_title);
        albumTextView = (TextView) musicPlayerView.findViewById(R.id.artist_album);
        albumCover = (ImageView) musicPlayerView.findViewById(R.id.artist_album_cover);
        trackTextView = (TextView) musicPlayerView.findViewById(R.id.artist_track);
        updateTrack();

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

    private void updateTrack() {
        artistTextView.setText(mTracks.get(mCurrentTrackIndex).getArtist());
        albumTextView.setText(mTracks.get(mCurrentTrackIndex).getAlbum());
        Picasso.with(getActivity())
                .load(mTracks.get(mCurrentTrackIndex).getImageUrl())
                .placeholder(R.drawable.ic_audiotrack_black_48dp)
                .error(R.drawable.ic_audiotrack_black_48dp)
                .into(albumCover);
        trackTextView.setText(mTracks.get(mCurrentTrackIndex).getTrack());
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
                    intent.getIntExtra("TRACK_INDEX", mCurrentTrackIndex);
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
                mCurrentTrackIndex = mCurrentTrackIndex == mTracks.size() - 1 ? mCurrentTrackIndex : ++mCurrentTrackIndex;
                Intent intent = new Intent(mContext, MusicPlayerService.class);
                intent.setAction(MusicPlayerService.ACTION_NEXT);
                mContext.startService(intent);
                updateTrack();
            }
        };
    }

    private View.OnClickListener onPreviousTrackClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentTrackIndex = mCurrentTrackIndex == 0 ? mCurrentTrackIndex : --mCurrentTrackIndex;
                Intent intent = new Intent(mContext, MusicPlayerService.class);
                intent.setAction(MusicPlayerService.ACTION_PREV);
                mContext.startService(intent);
                updateTrack();
            }
        };
    }

}
