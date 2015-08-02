package com.example.manuelsanchez.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;


public class ArtistSearchItem implements Parcelable {

    private String imageUrl;
    private String artistId;
    private String artistName;


    public ArtistSearchItem(String imageUrl, String artistId, String artistName) {
        this.imageUrl = imageUrl;
        this.artistId = artistId;
        this.artistName = artistName;
    }

    private ArtistSearchItem(Parcel in) {
        imageUrl = in.readString();
        artistId = in.readString();
        artistName = in.readString();
    }

    public static final Creator<ArtistSearchItem> CREATOR = new Creator<ArtistSearchItem>() {
        @Override
        public ArtistSearchItem createFromParcel(Parcel in) {
            return new ArtistSearchItem(in);
        }

        @Override
        public ArtistSearchItem[] newArray(int size) {
            return new ArtistSearchItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(artistId);
        dest.writeString(artistName);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }
}
