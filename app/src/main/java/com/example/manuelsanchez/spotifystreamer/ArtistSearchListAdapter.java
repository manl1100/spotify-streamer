package com.example.manuelsanchez.spotifystreamer;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ArtistSearchListAdapter extends ArrayAdapter<ArtistSearchItem> {

    ArrayList<ArtistSearchItem> mArtistSearchItems;


    public ArtistSearchListAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<ArtistSearchItem>());
    }

    private ArtistSearchListAdapter(Context context, int resource, ArrayList<ArtistSearchItem> items) {
        super(context, resource, items);
        mArtistSearchItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.artist_search_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ArtistSearchItem artist = getItem(position);

        Picasso.with(getContext())
                .load(artist.getImageUrl())
                .resize(150, 150)
                .placeholder(R.drawable.ic_audiotrack_black_48dp)
                .error(R.drawable.ic_audiotrack_black_48dp)
                .centerCrop()
                .into(viewHolder.artistImage);

        viewHolder.artistName.setText(artist.getArtistName());

        return convertView;
    }

    public ArrayList<? extends Parcelable> getItems() {
        return mArtistSearchItems;
    }

    private class ViewHolder {
        public final TextView artistName;
        public final ImageView artistImage;

        public ViewHolder(View view) {
            artistName = (TextView) view.findViewById(R.id.artist_name);
            artistImage = (ImageView) view.findViewById(R.id.artist_thumbnail);
        }
    }
}
